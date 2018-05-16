package libomv.imaging.j2k;

import java.io.EOFException;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

import colorspace.ColorSpace;
import colorspace.ColorSpaceException;
import icc.ICCProfileException;
import icc.ICCProfiler;
import jj2000.j2k.codestream.HeaderInfo;
import jj2000.j2k.codestream.reader.BitstreamReaderAgent;
import jj2000.j2k.codestream.reader.HeaderDecoder;
import jj2000.j2k.decoder.DecoderSpecs;
import jj2000.j2k.entropy.decoder.EntropyDecoder;
import jj2000.j2k.fileformat.reader.FileFormatReader;
import jj2000.j2k.image.BlkImgDataSrc;
import jj2000.j2k.image.ImgDataConverter;
import jj2000.j2k.image.invcomptransf.InvCompTransf;
import jj2000.j2k.io.RandomAccessIO;
import jj2000.j2k.quantization.dequantizer.Dequantizer;
import jj2000.j2k.roi.ROIDeScaler;
import jj2000.j2k.util.ParameterList;
import jj2000.j2k.wavelet.synthesis.InverseWT;

public class ImgDecoder {
	private static final Logger logger = Logger.getLogger(ImgDecoder.class);

	private static final String[][] pinfo = {
			{ "u", "[on|off]", "Prints usage information. If specified all other arguments (except 'v') are ignored",
					"off" },
			{ "v", "[on|off]", "Prints version and copyright information", "off" },
			{ "verbose", "[on|off]", "Prints information about the decoded codestream", "on" },
			{ "pfile", "<filename>",
					"Loads the arguments from the specified file. Arguments that are specified on the "
							+ "command line override the ones from the file.\nThe arguments file is a simple text file "
							+ "with one argument per line of the following form:\n  <argument name>=<argument value>\n"
							+ "If the argument is of boolean type (i.e. its presence turns a feature on), then the"
							+ "'on' value turns it on, while the 'off' value turns it off. The argument name does not "
							+ "include the '-' or '+' character. Long lines can be broken into several lines by "
							+ "terminating them with '\\'. Lines starting with '#' are considered as comments. This "
							+ "option is not recursive: any 'pfile' argument appearing in the file is ignored.",
					null },
			{ "res", "<resolution level index>",
					"The resolution level at which to reconstruct the image (0 means the "
							+ "lowest available resolution whereas the maximum resolution level corresponds to the "
							+ "original image resolution). If the given index is greater than the number of available "
							+ "resolution levels of the compressed image, the image is reconstructed at its highest "
							+ "resolution (among all tile-components). Note that this option affects only the inverse "
							+ "wavelet transform and not the number of bytes read by the codestream parser: this "
							+ "number of bytes depends only on options '-nbytes' or '-rate'.",
					null },
			{ "i", "<filename or url>",
					"The file containing the JPEG 2000 compressed data. This can be either a "
							+ "JPEG 2000 codestream or a JP2 file containing a JPEG 2000 codestream. In the latter "
							+ "case the first codestream in the file will be decoded. If an URL is specified (e.g., "
							+ "http://...) the data will be downloaded and cached in memory before decoding. This is "
							+ "intended for easy use in applets, but it is not a very efficient way of decoding "
							+ "network served data.",
					null },
			{ "o", "<filename>",
					"This is the name of the file to which the decompressed image "
							+ "is written. If no output filename is given, the image is displayed on the screen. "
							+ "Output file format is PGX by default. If the extension"
							+ " is '.pgm' then a PGM file is written as output, however this is "
							+ "only permitted if the component bitdepth does not exceed 8. If "
							+ "the extension is '.ppm' then a PPM file is written, however this "
							+ "is only permitted if there are 3 components and none of them has "
							+ "a bitdepth of more than 8. If there is more than 1 component, "
							+ "suffices '-1', '-2', '-3', ... are added to the file name, just "
							+ "before the extension, except for PPM files where all three "
							+ "components are written to the same file.",
					null },
			{ "rate", "<decoding rate in bpp>",
					"Specifies the decoding rate in bits per pixel (bpp) where the "
							+ "number of pixels is related to the image's original size (Note:"
							+ " this number is not affected by the '-res' option). If it is equal"
							+ "to -1, the whole codestream is decoded. "
							+ "The codestream is either parsed (default) or truncated depending "
							+ "the command line option '-parsing'. To specify the decoding "
							+ "rate in bytes, use '-nbytes' options instead.",
					"-1" },
			{ "nbytes", "<decoding rate in bytes>",
					"Specifies the decoding rate in bytes. The codestream is either "
							+ "parsed (default) or truncated depending the command line option '-parsing'. To specify "
							+ "the decoding rate in bits per pixel, use '-rate' options instead.",
					"-1" },
			{ "parsing", null,
					"Enable or not the parsing mode when decoding rate is specified "
							+ "('-nbytes' or '-rate' options). If it is false, the codestream "
							+ "is decoded as if it were truncated to the given rate. If it is "
							+ "true, the decoder creates, truncates and decodes a virtual layer"
							+ " progressive codestream with the same truncation points in each code-block.",
					"on" },
			{ "ncb_quit", "<max number of code blocks>",
					"Use the ncb and lbody quit conditions. If state information "
							+ "is found for more code blocks than is indicated with this option, the decoder "
							+ "will decode using only information found before that point. "
							+ "Using this otion implies that the 'rate' or 'nbyte' parameter "
							+ "is used to indicate the lbody parameter which is the number of "
							+ "packet body bytes the decoder will decode.",
					"-1" },
			{ "l_quit", "<max number of layers>", "Specifies the maximum number of layers to decode for any code-block",
					"-1" },
			{ "m_quit", "<max number of bit planes>",
					"Specifies the maximum number of bit planes to decode for any code-block", "-1" },
			{ "poc_quit", null,
					"Specifies the whether the decoder should only decode code-blocks "
							+ "included in the first progression order.",
					"off" },
			{ "one_tp", null, "Specifies whether the decoder should only decode the first tile part of each tile.",
					"off" },
			{ "comp_transf", null,
					"Specifies whether the component transform indicated in the codestream should be used.", "on" },
			{ "debug", null, "Print debugging messages when an error is encountered.", "off" },
			{ "cdstr_info", null,
					"Display information about the codestream. This information is: "
							+ "\n- Marker segments value in main and tile-part headers,"
							+ "\n- Tile-part length and position within the code-stream.",
					"off" },
			{ "nocolorspace", null, "Ignore any colorspace information in the image.", "off" }, { "colorspace_debug",
					null, "Print debugging messages when an error is encountered in the colorspace module.", "off" } };

	private ParameterList pl;
	private HeaderInfo hi;
	private ColorSpace csMap;

	public ImgDecoder(ParameterList parameterList) {
		this.pl = parameterList;
	}

	/**
	 * Returns the parameters that are used in this class. It returns a 2D String
	 * array. Each of the 1D arrays is for a different option, and they have 3
	 * elements. The first element is the option name, the second one is the
	 * synopsis and the third one is a long description of what the parameter is.
	 * The synopsis or description may be 'null', in which case it is assumed that
	 * there is no synopsis or description of the option, respectively.
	 *
	 * @return the options name, their synopsis and their explanation.
	 */
	private static String[][] getParameterInfo() {
		return pinfo;
	}

	/**
	 * Returns all the parameters used in the decoding chain. It calls parameter
	 * from each module and store them in one array (one row per parameter and 4
	 * columns).
	 *
	 * @return All decoding parameters
	 *
	 * @see #getParameterInfo
	 */
	public static String[][] getAllParameters() {
		Vector<String[]> vec = new Vector<String[]>();
		int i;

		String[][] str = BitstreamReaderAgent.getParameterInfo();
		if (str != null)
			for (i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = EntropyDecoder.getParameterInfo();
		if (str != null)
			for (i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = ROIDeScaler.getParameterInfo();
		if (str != null)
			for (i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = Dequantizer.getParameterInfo();
		if (str != null)
			for (i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = InvCompTransf.getParameterInfo();
		if (str != null)
			for (i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = HeaderDecoder.getParameterInfo();
		if (str != null)
			for (i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = ICCProfiler.getParameterInfo();
		if (str != null)
			for (i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = getParameterInfo();
		if (str != null)
			for (i = str.length - 1; i >= 0; i--)
				vec.addElement(str[i]);

		str = new String[vec.size()][4];
		for (i = str.length - 1; i >= 0; i--)
			str[i] = vec.elementAt(i);

		return str;
	}

	public BlkImgDataSrc decode(RandomAccessIO in, FileFormatReader ff, boolean verbose)
			throws IOException, ICCProfileException {
		HeaderDecoder hd;
		DecoderSpecs decSpec;
		int[] depth;
		BitstreamReaderAgent breader;
		EntropyDecoder entdec;
		ROIDeScaler roids;
		Dequantizer deq;
		InverseWT invWT;
		ImgDataConverter converter;
		InvCompTransf ictransf;
		BlkImgDataSrc channels;
		BlkImgDataSrc resampled;
		BlkImgDataSrc palettized;
		BlkImgDataSrc color;

		// +----------------------------+
		// | Instantiate decoding chain |
		// +----------------------------+

		// **** Header decoder ****
		// Instantiate header decoder and read main header
		hi = new HeaderInfo();
		try {
			hd = new HeaderDecoder(in, pl, hi);
		} catch (EOFException e) {
			logger.error("Codestream too short or bad header, " + "unable to decode.", e);
			return null;
		}

		int nCompCod = hd.getNumComps();
		int nTiles = hi.siz.getNumTiles();
		decSpec = hd.getDecoderSpecs();

		// Report information
		if (verbose) {
			String info = nCompCod + " component(s) in codestream, " + nTiles + " tile(s)\n";
			info += "Image dimension: ";
			for (int c = 0; c < nCompCod; c++) {
				info += hi.siz.getCompImgWidth(c) + "x" + hi.siz.getCompImgHeight(c) + " ";
			}

			if (nTiles != 1) {
				info += "\nNom. Tile dim. (in canvas): " + hi.siz.xtsiz + "x" + hi.siz.ytsiz;
			}
			logger.info(info);
		}
		if (pl.getBooleanParameter("cdstr_info")) {
			logger.info("Main header:\n" + hi.toStringMainHeader());
		}

		// Get demixed bitdepths
		depth = new int[nCompCod];
		for (int i = 0; i < nCompCod; i++) {
			depth[i] = hd.getOriginalBitDepth(i);
		}

		// **** Bit stream reader ****
		try {
			breader = BitstreamReaderAgent.createInstance(in, hd, pl, decSpec, pl.getBooleanParameter("cdstr_info"),
					hi);
		} catch (IOException e) {
			logger.error("Error while reading bit stream header or parsing packets", e);
			return null;
		} catch (IllegalArgumentException e) {
			logger.error("Cannot instantiate bit stream reader", e);
			return null;
		}

		// **** Entropy decoder ****
		try {
			entdec = hd.createEntropyDecoder(breader, pl);
		} catch (IllegalArgumentException e) {
			logger.error("Cannot instantiate entropy decoder", e);
			return null;
		}

		// **** ROI de-scaler ****
		try {
			roids = hd.createROIDeScaler(entdec, pl, decSpec);
		} catch (IllegalArgumentException e) {
			logger.error("Cannot instantiate roi de-scaler.", e);
			return null;
		}

		// **** Dequantizer ****
		try {
			deq = hd.createDequantizer(roids, depth, decSpec);
		} catch (IllegalArgumentException e) {
			logger.error("Cannot instantiate dequantizer", e);
			return null;
		}

		// **** Inverse wavelet transform ***
		try {
			// full page inverse wavelet transform
			invWT = InverseWT.createInstance(deq, decSpec);
		} catch (IllegalArgumentException e) {
			logger.error("Cannot instantiate inverse wavelet transform", e);
			return null;
		}

		int res = breader.getImgRes();
		invWT.setImgResLevel(res);

		// **** Data converter **** (after inverse transform module)
		converter = new ImgDataConverter(invWT, 0);

		// **** Inverse component transformation ****
		ictransf = new InvCompTransf(converter, decSpec, depth, pl);

		// **** Color space mapping ****
		if (ff.JP2FFUsed && "off".equals(pl.getParameter("nocolorspace"))) {
			try {
				csMap = new ColorSpace(in, hd, pl);
				channels = hd.createChannelDefinitionMapper(ictransf, csMap);
				resampled = hd.createResampler(channels, csMap);
				palettized = hd.createPalettizedColorSpaceMapper(resampled, csMap);
				color = hd.createColorSpaceMapper(palettized, csMap);

				if (csMap.debugging()) {
					logger.error("" + csMap);
					logger.error("" + channels);
					logger.error("" + resampled);
					logger.error("" + palettized);
					logger.error("" + color);
				}
			} catch (IllegalArgumentException e) {
				logger.error("Could not instantiate ICC profiler", e);
				return null;
			} catch (ColorSpaceException e) {
				logger.error("error processing jp2 colorspace information", e);
				return null;
			}
		} else { // Skip colorspace mapping
			return ictransf;
		}

		// This is the last image in the decoding chain and should be
		// assigned by the last transformation:
		return color != null ? color : ictransf;
	}

}
