package libomv.imaging.j2k;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import jj2000.j2k.codestream.writer.CodestreamWriter;
import jj2000.j2k.codestream.writer.HeaderEncoder;
import jj2000.j2k.codestream.writer.PktEncoder;
import jj2000.j2k.encoder.EncoderSpecs;
import jj2000.j2k.entropy.encoder.EntropyCoder;
import jj2000.j2k.entropy.encoder.PostCompRateAllocator;
import jj2000.j2k.image.ImgDataConverter;
import jj2000.j2k.image.Tiler;
import jj2000.j2k.image.forwcomptransf.ForwCompTransf;
import jj2000.j2k.image.input.ImgReader;
import jj2000.j2k.quantization.quantizer.Quantizer;
import jj2000.j2k.roi.encoder.ROIScaler;
import jj2000.j2k.util.ParameterList;
import jj2000.j2k.wavelet.analysis.AnWTFilter;
import jj2000.j2k.wavelet.analysis.ForwardWT;

public class ImgEncoder {
	private static final Logger logger = Logger.getLogger(ImgEncoder.class);

	private static final String[][] pinfo = {
			{ "debug", null, "Print debugging messages when an error is encountered.", "off" },
			{ "disable_jp2_extension", "[on|off]",
					"JJ2000 automatically adds .jp2 extension when using 'file_format'"
							+ "option. This option disables it when on.",
					"off" },
			{ "file_format", "[on|off]", "Puts the JPEG 2000 codestream in a JP2 file format wrapper.", "off" },
			{ "pph_tile", "[on|off]", "Packs the packet headers in the tile headers.", "off" },
			{ "pph_main", "[on|off]", "Packs the packet headers in the main header.", "off" },
			{ "pfile", "<filename of arguments file>",
					"Loads the arguments from the specified file. Arguments that are "
							+ "specified on the command line override the ones from the file.\n"
							+ "The arguments file is a simple text file with one argument per "
							+ "line of the following form:\n  <argument name>=<argument value>\n"
							+ "If the argument is of boolean type (i.e. its presence turns a "
							+ "feature on), then the 'on' value turns it on, while the 'off' "
							+ "value turns it off. The argument name does not include the '-' "
							+ "or '+' character. Long lines can be broken into several lines "
							+ "by terminating them with '\'. Lines starting with '#' are "
							+ "considered as comments. This option is not recursive: any 'pfile' "
							+ "argument appearing in the file is ignored.",
					null },
			{ "tile_parts", "<packets per tile-part>",
					"This option specifies the maximum number of packets to have in "
							+ "one tile-part. 0 means include all packets in first tile-part of each tile",
					"0" },
			{ "tiles", "<nominal tile width> <nominal tile height>",
					"This option specifies the maximum tile dimensions to use. "
							+ "If both dimensions are 0 then no tiling is used.",
					"0 0" },
			{ "ref", "<x> <y>",
					"Sets the origin of the image in the canvas system. It sets the "
							+ "coordinate of the top-left corner of the image reference grid, "
							+ "with respect to the canvas origin",
					"0 0" },
			{ "tref", "<x> <y>",
					"Sets the origin of the tile partitioning on the reference grid, "
							+ "with respect to the canvas origin. The value of 'x' ('y') "
							+ "specified can not be larger than the 'x' one specified in the ref option.",
					"0 0" },
			{ "rate", "<output bitrate in bpp>",
					"This is the output bitrate of the codestream in bits per pixel."
							+ " When equal to -1, no image information (beside quantization "
							+ "effects) is discarded during compression.\n"
							+ "Note: In the case where '-file_format' option is used, the "
							+ "resulting file may have a larger bitrate.",
					"-1" },
			{ "lossless", "[on|off]",
					"Specifies a lossless compression for the encoder. This options"
							+ " is equivalent to use reversible quantization ('-Qtype reversible')"
							+ " and 5x3 wavelet filters pair ('-Ffilters w5x3'). Note that "
							+ "this option cannot be used with '-rate'. When this option is "
							+ "off, the quantization type and the filters pair is defined by "
							+ "'-Qtype' and '-Ffilters' respectively.",
					"off" },
			{ "i", "<image file> [,<image file> [,<image file> ... ]]",
					"Mandatory argument. This option specifies the name of the input "
							+ "image files. If several image files are provided, they have to be"
							+ " separated by commas in the command line. Supported formats are "
							+ "PGM (raw), PPM (raw) and PGX, "
							+ "which is a simple extension of the PGM file format for single "
							+ "component data supporting arbitrary bitdepths. If the extension "
							+ "is '.pgm', PGM-raw file format is assumed, if the extension is "
							+ "'.ppm', PPM-raw file format is assumed, otherwise PGX file "
							+ "format is assumed. PGM and PPM files are assumed to be 8 bits "
							+ "deep. A multi-component image can be specified by either "
							+ "specifying several PPM and/or PGX files, or by specifying one PPM file.",
					null },
			{ "o", "<file name>",
					"Mandatory argument. This option specifies the name of the output "
							+ "file to which the codestream will be written.",
					null },
			{ "verbose", null, "Prints information about the obtained bit stream.", "on" },
			{ "v", "[on|off]", "Prints version and copyright information.", "off" }, { "u", "[on|off]",
					"Prints usage information. If specified all other arguments (except 'v') are ignored", "off" }, };

	/** The default parameter list (arguments) */
	private ParameterList pl;

	/** The parameter list (arguments) */
	private ParameterList defpl;

	public ImgEncoder(ParameterList pl) {
		this.pl = pl;
		this.defpl = pl.getDefaultParameterList();
	}

	/**
	 * Returns the parameters that are used in this class and implementing classes.
	 * It returns a 2D String array. Each of the 1D arrays is for a different
	 * option, and they have 4 elements. The first element is the option name, the
	 * second one is the synopsis, the third one is a long description of what the
	 * parameter is and the fourth is its default value. The synopsis or description
	 * may be 'null', in which case it is assumed that there is no synopsis or
	 * description of the option, respectively. Null may be returned if no options
	 * are supported.
	 * 
	 * @return the options name, their synopsis and their explanation, or null if no
	 *         options are supported.
	 */
	private static String[][] getParameterInfo() {
		return pinfo;
	}

	/**
	 * Returns all the parameters used in the encoding chain. It calls parameter
	 * from each module and store them in one array (one row per parameter and 4
	 * columns).
	 * 
	 * @return All encoding parameters
	 * 
	 * @see #getParameterInfo
	 */
	public static String[][] getAllParameters() {
		Vector<String[]> vec = new Vector<String[]>();

		String[][] str = getParameterInfo();
		if (str != null)
			for (int i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = ForwCompTransf.getParameterInfo();
		if (str != null)
			for (int i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = AnWTFilter.getParameterInfo();
		if (str != null)
			for (int i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = ForwardWT.getParameterInfo();
		if (str != null)
			for (int i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = Quantizer.getParameterInfo();
		if (str != null)
			for (int i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = ROIScaler.getParameterInfo();
		if (str != null)
			for (int i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = EntropyCoder.getParameterInfo();
		if (str != null)
			for (int i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = HeaderEncoder.getParameterInfo();
		if (str != null)
			for (int i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = PostCompRateAllocator.getParameterInfo();
		if (str != null)
			for (int i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = PktEncoder.getParameterInfo();
		if (str != null)
			for (int i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = new String[vec.size()][4];
		for (int i = str.length - 1; i >= 0; i--)
			str[i] = vec.elementAt(i);

		return str;
	}

	public int encode(ImgReader source, boolean[] imsigned, int components, boolean ppminput, OutputStream out,
			boolean useFileFormat, boolean verbose) throws IOException {
		boolean pphTile = false;
		boolean pphMain = false;
		boolean tempSop = false;
		boolean tempEph = false;
		float rate;
		int pktspertp;
		StreamTokenizer stok;
		StringTokenizer sgtok;
		int tw, th;
		int refx, refy;
		int trefx, trefy;
		Tiler imgtiler;
		EncoderSpecs encSpec;
		ForwCompTransf fctransf;
		ImgDataConverter converter;
		ForwardWT dwt;
		Quantizer quant;
		ROIScaler rois;
		EntropyCoder ecoder;
		CodestreamWriter bwriter;
		PostCompRateAllocator ralloc;
		HeaderEncoder headenc;

		if (pl.getParameter("tiles") == null) {
			logger.error("No tiles option specified");
			return -1;
		}

		if ("on".equals(pl.getParameter("pph_tile"))) {
			pphTile = true;
			if ("off".equals(pl.getParameter("Psop"))) {
				pl.put("Psop", "on");
				tempSop = true;
			}
			if ("off".equals(pl.getParameter("Peph"))) {
				pl.put("Peph", "on");
				tempEph = true;
			}
		}

		if ("on".equals(pl.getParameter("pph_main"))) {
			pphMain = true;

			if ("off".equals(pl.getParameter("Psop"))) {
				pl.put("Psop", "on");
				tempSop = true;
			}
			if ("off".equals(pl.getParameter("Peph"))) {
				pl.put("Peph", "on");
				tempEph = true;
			}
		}

		if (pphTile && pphMain) {
			logger.error("Can't have packed packet headers in both main and tile headers");
			return -1;
		}

		if (pl.getBooleanParameter("lossless") && pl.getParameter("rate") != null
				&& pl.getFloatParameter("rate") != defpl.getFloatParameter("rate"))
			throw new IllegalArgumentException(
					"Cannot use '-rate' and " + "'-lossless' option at " + " the same time.");

		if (pl.getParameter("rate") == null) {
			logger.error("Target bitrate not specified");
			return -1;
		}
		try {
			rate = pl.getFloatParameter("rate");
			if (rate == -1) {
				rate = Float.MAX_VALUE;
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid value in 'rate' option: " + pl.getParameter("rate"));
			return -1;
		}
		try {
			pktspertp = pl.getIntParameter("tile_parts");
			if (pktspertp != 0) {
				if ("off".equals(pl.getParameter("Psop"))) {
					pl.put("Psop", "on");
					tempSop = true;
				}
				if ("off".equals(pl.getParameter("Peph"))) {
					pl.put("Peph", "on");
					tempEph = true;
				}
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid value in 'tile_parts' option: " + pl.getParameter("tile_parts"));
			return -1;
		}

		// **** Tiler ****
		// get nominal tile dimensions
		stok = new StreamTokenizer(new StringReader(pl.getParameter("tiles")));
		stok.eolIsSignificant(false);

		stok.nextToken();
		if (stok.ttype != StreamTokenizer.TT_NUMBER) {
			logger.error("An error occurred while parsing the tiles option: " + pl.getParameter("tiles"));
			return -1;
		}
		tw = (int) stok.nval;
		stok.nextToken();
		if (stok.ttype != StreamTokenizer.TT_NUMBER) {
			logger.error("An error occurred while parsing the tiles option: " + pl.getParameter("tiles"));
			return -1;
		}
		th = (int) stok.nval;

		// Get image reference point
		sgtok = new StringTokenizer(pl.getParameter("ref"));
		try {
			refx = Integer.parseInt(sgtok.nextToken());
			refy = Integer.parseInt(sgtok.nextToken());
		} catch (NoSuchElementException e) {
			throw new IllegalArgumentException("Error while parsing 'ref' option", e);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid number type in 'ref' option", e);
		}
		if (refx < 0 || refy < 0) {
			throw new IllegalArgumentException("Invalid value in 'ref' option ");
		}

		// Get tiling reference point
		sgtok = new StringTokenizer(pl.getParameter("tref"));
		try {
			trefx = Integer.parseInt(sgtok.nextToken());
			trefy = Integer.parseInt(sgtok.nextToken());
		} catch (NoSuchElementException e) {
			throw new IllegalArgumentException("Error while parsing 'tref' option", e);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid number type in 'tref' option", e);
		}
		if (trefx < 0 || trefy < 0 || trefx > refx || trefy > refy) {
			throw new IllegalArgumentException("Invalid value in 'tref' option ");
		}

		// Instantiate tiler
		try {
			imgtiler = new Tiler(source, refx, refy, trefx, trefy, tw, th);
		} catch (IllegalArgumentException e) {
			logger.error("Could not tile image", e);
			return -1;
		}
		int ntiles = imgtiler.getNumTiles();

		// **** Encoder specifications ****
		encSpec = new EncoderSpecs(ntiles, components, source, pl);

		// **** Component transformation ****
		if (ppminput && pl.getParameter("Mct") != null && "off".equals(pl.getParameter("Mct"))) {
			logger.warn("Input image is RGB and no color transform has "
					+ "been specified. Compression performance and " + "image quality might be greatly degraded. Use "
					+ "the 'Mct' option to specify a color transform");
		}
		try {
			fctransf = new ForwCompTransf(imgtiler, encSpec);
		} catch (IllegalArgumentException e) {
			logger.error("Could not instantiate forward component transformation", e);
			return -1;
		}

		// **** ImgDataConverter ****
		converter = new ImgDataConverter(fctransf);

		// **** ForwardWT ****
		try {
			dwt = ForwardWT.createInstance(converter, pl, encSpec);
		} catch (IllegalArgumentException e) {
			logger.error("Could not instantiate wavelet transform", e);
			return -1;
		}

		// **** Quantizer ****
		try {
			quant = Quantizer.createInstance(dwt, encSpec);
		} catch (IllegalArgumentException e) {
			logger.error("Could not instantiate quantizer", e);
			return -1;
		}

		// **** ROIScaler ****
		try {
			rois = ROIScaler.createInstance(quant, pl, encSpec);
		} catch (IllegalArgumentException e) {
			logger.error("Could not instantiate ROI scaler", e);
			return -1;
		}

		// **** EntropyCoder ****
		try {
			ecoder = EntropyCoder.createInstance(rois, pl, encSpec.cblks, encSpec.pss, encSpec.bms, encSpec.mqrs,
					encSpec.rts, encSpec.css, encSpec.sss, encSpec.lcs, encSpec.tts);
		} catch (IllegalArgumentException e) {
			logger.error("Could not instantiate entropy coder", e);
			return -1;
		}

		// TODO:Insert something special here?!?
		OutputStream os;
		ByteArrayOutputStream baos = null;
		if (pktspertp > 0 || pphTile || pphMain || useFileFormat) {
			baos = new ByteArrayOutputStream();
			os = baos;
		} else {
			os = out;
		}

		// **** CodestreamWriter ****
		try {
			// Rely on rate allocator to limit amount of data
			bwriter = new BufferedCodestreamWriter(os, Integer.MAX_VALUE);
		} catch (Exception e) {
			out.close();
			if (baos != null)
				baos.close();
			logger.error("Could not open codestream output", e);
			return -1;
		}

		// **** Rate allocator ****
		try {
			ralloc = PostCompRateAllocator.createInstance(ecoder, pl, rate, bwriter, encSpec);
		} catch (IllegalArgumentException e) {
			out.close();
			if (baos != null)
				baos.close();
			logger.error("Could not instantiate rate allocator", e);
			return -1;
		}

		// **** HeaderEncoder ****
		headenc = new HeaderEncoder(source, imsigned, dwt, imgtiler, encSpec, rois, ralloc, pl);
		ralloc.setHeaderEncoder(headenc);

		// **** Write header to be able to estimate header overhead ****
		headenc.encodeMainHeader();

		// **** Initialize rate allocator, with proper header
		// overhead. This will also encode all the data ****
		ralloc.initialize();

		// **** Write header (final) ****
		headenc.reset();
		headenc.encodeMainHeader();

		// Insert header into the codestream
		bwriter.commitBitstreamHeader(headenc);

		// **** Now do the rate-allocation and write result ****
		ralloc.runAndWrite();

		// **** Done ****
		bwriter.close();

		// **** Calculate file length ****
		int fileLength = bwriter.getLength();

		if (pktspertp > 0 || pphTile || pphMain || useFileFormat) {
			BufferedRandomAccessIO io = new BufferedRandomAccessIO(baos.toByteArray(), Integer.MAX_VALUE);
			baos.close();

			// **** Tile-parts and packed packet headers ****
			if (pktspertp > 0 || pphTile || pphMain) {
				try {
					CodestreamManipulator cm = new CodestreamManipulator(io, ntiles, pktspertp, pphMain, pphTile,
							tempSop, tempEph);
					fileLength += cm.doCodestreamManipulation();
					if (pktspertp > 0) {
						logger.info("Created tile-parts containing at most " + pktspertp + " packets per tile.");
					}
					if (pphTile) {
						logger.info("Moved packet headers to tile headers");
					}
					if (pphMain) {
						logger.info("Moved packet headers to main header");
					}
				} catch (IOException e) {
					io.close();
					logger.error("Error while creating tileparts or packed packet  headers", e);
					return -1;
				}
			}

			// **** File Format ****
			if (useFileFormat) {
				try {
					int nc = source.getNumComps();
					int[] bpc = new int[nc];
					for (int comp = 0; comp < nc; comp++) {
						bpc[comp] = source.getNomRangeBits(comp);
					}

					FileFormatWriter ffw = new FileFormatWriter(out, source.getImgHeight(), source.getImgWidth(), nc,
							bpc, fileLength);
					fileLength += ffw.length();
				} catch (IOException e) {
					io.close();
					throw new IOException("Error while writing JP2 file format", e);
				}
			}
			io.writeTo(out);
			io.close();
		}

		return fileLength;
	}

}
