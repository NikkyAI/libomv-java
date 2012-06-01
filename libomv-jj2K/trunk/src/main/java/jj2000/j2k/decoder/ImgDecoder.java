package jj2000.j2k.decoder;

import icc.ICCProfileException;

import java.io.EOFException;
import java.io.IOException;
import java.util.Enumeration;

import jj2000.j2k.codestream.HeaderInfo;
import jj2000.j2k.codestream.HeaderInfo.COM;
import jj2000.j2k.codestream.reader.BitstreamReaderAgent;
import jj2000.j2k.codestream.reader.HeaderDecoder;
import jj2000.j2k.entropy.decoder.EntropyDecoder;
import jj2000.j2k.fileformat.reader.FileFormatReader;
import jj2000.j2k.image.BlkImgDataSrc;
import jj2000.j2k.image.ImgDataConverter;
import jj2000.j2k.image.invcomptransf.InvCompTransf;
import jj2000.j2k.io.RandomAccessIO;
import jj2000.j2k.quantization.dequantizer.Dequantizer;
import jj2000.j2k.roi.ROIDeScaler;
import jj2000.j2k.util.FacilityManager;
import jj2000.j2k.util.MsgLogger;
import jj2000.j2k.util.ParameterList;
import jj2000.j2k.wavelet.synthesis.InverseWT;
import colorspace.ColorSpace;
import colorspace.ColorSpaceException;

public class ImgDecoder
{
	/** The exit code of the run method */
	private int exitCode;

	/**
	 * Returns the exit code of the class. This is only initialized after the
	 * constructor and when the run method returns.
	 * 
	 * @return The exit code of the constructor and the run() method.
	 */
	public int getExitCode()
	{
		return exitCode;
	}

	/** The parameter information for this class */
	private final static String[][] pinfo = {
			{ "u", "[on|off]",
					"Prints usage information. If specified all other arguments (except 'v') are ignored", "off" },
			{ "v", "[on|off]", "Prints version and copyright information", "off" },
			{ "verbose", "[on|off]", "Prints information about the decoded codestream", "on" },
			{ "pfile", "<filename>", "Loads the arguments from the specified file. Arguments that are specified on the "
							+ "command line override the ones from the file.\nThe arguments file is a simple text file "
							+ "with one argument per line of the following form:\n  <argument name>=<argument value>\n"
							+ "If the argument is of boolean type (i.e. its presence turns a feature on), then the"
							+ "'on' value turns it on, while the 'off' value turns it off. The argument name does not "
							+ "include the '-' or '+' character. Long lines can be broken into several lines by "
							+ "terminating them with '\\'. Lines starting with '#' are considered as comments. This "
							+ "option is not recursive: any 'pfile' argument appearing in the file is ignored.", null },
			{ "res", "<resolution level index>", "The resolution level at which to reconstruct the image (0 means the "
							+ "lowest available resolution whereas the maximum resolution level corresponds to the "
							+ "original image resolution). If the given index is greater than the number of available "
							+ "resolution levels of the compressed image, the image is reconstructed at its highest "
							+ "resolution (among all tile-components). Note that this option affects only the inverse "
							+ "wavelet transform and not the number of bytes read by the codestream parser: this "
							+ "number of bytes depends only on options '-nbytes' or '-rate'.", null },
			{ "i", "<filename or url>", "The file containing the JPEG 2000 compressed data. This can be either a "
							+ "JPEG 2000 codestream or a JP2 file containing a JPEG 2000 codestream. In the latter "
							+ "case the first codestream in the file will be decoded. If an URL is specified (e.g., "
							+ "http://...) the data will be downloaded and cached in memory before decoding. This is "
							+ "intended for easy use in applets, but it is not a very efficient way of decoding "
							+ "network served data.", null },
			{ "o", "<filename>", "This is the name of the file to which the decompressed image "
							+ "is written. If no output filename is given, the image is displayed on the screen. "
							+ "Output file format is PGX by default. If the extension"
							+ " is '.pgm' then a PGM file is written as output, however this is "
							+ "only permitted if the component bitdepth does not exceed 8. If "
							+ "the extension is '.ppm' then a PPM file is written, however this "
							+ "is only permitted if there are 3 components and none of them has "
							+ "a bitdepth of more than 8. If there is more than 1 component, "
							+ "suffices '-1', '-2', '-3', ... are added to the file name, just "
							+ "before the extension, except for PPM files where all three "
							+ "components are written to the same file.", null },
			{ "rate", "<decoding rate in bpp>", "Specifies the decoding rate in bits per pixel (bpp) where the "
							+ "number of pixels is related to the image's original size (Note:"
							+ " this number is not affected by the '-res' option). If it is equal"
							+ "to -1, the whole codestream is decoded. "
							+ "The codestream is either parsed (default) or truncated depending "
							+ "the command line option '-parsing'. To specify the decoding "
							+ "rate in bytes, use '-nbytes' options instead.", "-1" },
			{ "nbytes", "<decoding rate in bytes>", "Specifies the decoding rate in bytes. The codestream is either "
							+ "parsed (default) or truncated depending the command line option '-parsing'. To specify "
							+ "the decoding rate in bits per pixel, use '-rate' options instead.", "-1" },
			{ "parsing", null, "Enable or not the parsing mode when decoding rate is specified "
							+ "('-nbytes' or '-rate' options). If it is false, the codestream "
							+ "is decoded as if it were truncated to the given rate. If it is "
							+ "true, the decoder creates, truncates and decodes a virtual layer"
							+ " progressive codestream with the same truncation points in each code-block.", "on" },
			{ "ncb_quit", "<max number of code blocks>", "Use the ncb and lbody quit conditions. If state information "
							+ "is found for more code blocks than is indicated with this option, the decoder "
							+ "will decode using only information found before that point. "
							+ "Using this otion implies that the 'rate' or 'nbyte' parameter "
							+ "is used to indicate the lbody parameter which is the number of "
							+ "packet body bytes the decoder will decode.", "-1" },
			{ "l_quit", "<max number of layers>",
					"Specifies the maximum number of layers to decode for any code-block", "-1" },
			{ "m_quit", "<max number of bit planes>",
					"Specifies the maximum number of bit planes to decode for any code-block", "-1" },
			{ "poc_quit", null, "Specifies the whether the decoder should only decode code-blocks "
							+ "included in the first progression order.", "off" },
			{ "one_tp", null,
					"Specifies whether the decoder should only decode the first tile part of each tile.", "off" },
			{ "comp_transf", null,
					"Specifies whether the component transform indicated in the codestream should be used.", "on" },
			{ "debug", null, "Print debugging messages when an error is encountered.", "off" },
			{ "cdstr_info", null, "Display information about the codestream. This information is: "
							+ "\n- Marker segments value in main and tile-part headers,"
							+ "\n- Tile-part length and position within the code-stream.", "off" },
			{ "nocolorspace", null, "Ignore any colorspace information in the image.", "off" },
			{ "colorspace_debug", null,
					"Print debugging messages when an error is encountered in the colorspace module.", "off" } };

	/** The parameter list (arguments) */
	protected ParameterList pl;

	/** Parses the inputstream to analyze the box structure of the JP2 file. */
	protected ColorSpace csMap = null;

	protected BitstreamReaderAgent breader = null;
	protected HeaderDecoder hd = null;
	protected DecoderSpecs decSpec = null;

	/** Information contained in the codestream's headers */
	private HeaderInfo hi;
	

	public ImgDecoder(ParameterList pl)
	{
		this.pl = pl;
	}
	
	/**
	 * Returns the parameters that are used in this class. It returns a 2D
	 * String array. Each of the 1D arrays is for a different option, and they
	 * have 3 elements. The first element is the option name, the second one is
	 * the synopsis and the third one is a long description of what the
	 * parameter is. The synopsis or description may be 'null', in which case it
	 * is assumed that there is no synopsis or description of the option,
	 * respectively.
	 * 
	 * @return the options name, their synopsis and their explanation.
	 */
	public static String[][] getParameterInfo()
	{
		return pinfo;
	}

	public BlkImgDataSrc decode(RandomAccessIO in, FileFormatReader ff, boolean verbose) throws IOException, ICCProfileException
	{		
		EntropyDecoder entdec;
		ROIDeScaler roids;
		Dequantizer deq;
		InverseWT invWT;
		InvCompTransf ictransf;
		ImgDataConverter converter;
		BlkImgDataSrc palettized;
		BlkImgDataSrc channels;
		BlkImgDataSrc resampled;
		BlkImgDataSrc color;
		int i;

		// +----------------------------+
		// | Instantiate decoding chain |
		// +----------------------------+

		// **** Header decoder ****
		// Instantiate header decoder and read main header
		hi = new HeaderInfo();
		try
		{
			hd = new HeaderDecoder(in, pl, hi);
		}
		catch (EOFException e)
		{
			error("Codestream too short or bad header, unable to decode.", 2, e);
			return null;
		}

		int nCompCod = hd.getNumComps();
		int nTiles = hi.siz.getNumTiles();
		decSpec = hd.getDecoderSpecs();

		// Report information
		if (verbose)
		{
			String info = nCompCod + " component(s) in codestream, " + nTiles + " tile(s)\n";
			info += "Image dimension: ";
			for (int c = 0; c < nCompCod; c++)
			{
				info += hi.siz.getCompImgWidth(c) + "x" + hi.siz.getCompImgHeight(c) + " ";
			}

			if (nTiles != 1)
			{
				info += "\nNom. Tile dim. (in canvas): " + hi.siz.xtsiz + "x" + hi.siz.ytsiz;
			}
			FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, info);
		}
		if (pl.getBooleanParameter("cdstr_info"))
		{
			FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, "Main header:\n" + hi.toStringMainHeader());
		}

		// Get demixed bitdepths
		int [] depth = new int[nCompCod];
		for (i = 0; i < nCompCod; i++)
		{
			depth[i] = hd.getOriginalBitDepth(i);
		}

		// **** Bit stream reader ****
		try
		{
			breader = BitstreamReaderAgent.createInstance(in, hd, pl, decSpec, pl.getBooleanParameter("cdstr_info"), hi);
		}
		catch (IOException e)
		{
			error("Error while reading bit stream header or parsing packets"
					+ ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""), 4, e);
			return null;
		}
		catch (IllegalArgumentException e)
		{
			error("Cannot instantiate bit stream reader"
					+ ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""), 2, e);
			return null;
		}

		// **** Entropy decoder ****
		try
		{
			entdec = hd.createEntropyDecoder(breader, pl);
		}
		catch (IllegalArgumentException e)
		{
			error("Cannot instantiate entropy decoder" + ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""),
					2, e);
			return null;
		}

		// **** ROI de-scaler ****
		try
		{
			roids = hd.createROIDeScaler(entdec, pl, decSpec);
		}
		catch (IllegalArgumentException e)
		{
			error("Cannot instantiate roi de-scaler." + ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""),
					2, e);
			return null;
		}

		// **** Dequantizer ****
		try
		{
			deq = hd.createDequantizer(roids, depth, decSpec);
		}
		catch (IllegalArgumentException e)
		{
			error("Cannot instantiate dequantizer" + ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""), 2, e);
			return null;
		}

		// **** Inverse wavelet transform ***
		try
		{
			// full page inverse wavelet transform
			invWT = InverseWT.createInstance(deq, decSpec);
		}
		catch (IllegalArgumentException e)
		{
			error("Cannot instantiate inverse wavelet transform"
					+ ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""), 2, e);
			return null;
		}

		invWT.setImgResLevel(breader.getImgRes());

		// **** Data converter **** (after inverse transform module)
		converter = new ImgDataConverter(invWT, 0);

		// **** Inverse component transformation ****
		ictransf = new InvCompTransf(converter, decSpec, depth, pl);

		// **** Color space mapping ****
		if (ff.JP2FFUsed && pl.getParameter("nocolorspace").equals("off"))
		{
			try
			{
				csMap = new ColorSpace(in, hd, pl);
				channels = hd.createChannelDefinitionMapper(ictransf, csMap);
				resampled = hd.createResampler(channels, csMap);
				palettized = hd.createPalettizedColorSpaceMapper(resampled, csMap);
				color = hd.createColorSpaceMapper(palettized, csMap);

				if (csMap.debugging())
				{
					FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, "" + csMap);
					FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, "" + channels);
					FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, "" + resampled);
					FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, "" + palettized);
					FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, "" + color);
				}
			}
			catch (IllegalArgumentException e)
			{
				error("Could not instantiate ICC profiler"
						+ ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""), 1, e);
				return null;
			}
			catch (ColorSpaceException e)
			{
				error("error processing jp2 colorspace information"
						+ ((e.getMessage() != null) ? (": " + e.getMessage()) : "    "), 1, e);
				return null;
			}
		}
		else
		{ // Skip colorspace mapping
			return ictransf;
		}

		// This is the last image in the decoding chain and should be
		// assigned by the last transformation:
		return color != null ? color : ictransf;
	}

	/**
	 * Return the information found in the COM marker segments encountered in
	 * the decoded codestream.
	 */
	public String[] getCOMInfo()
	{
		if (hi == null)
		{ // The codestream has not been read yet
			return null;
		}

		int nCOMMarkers = hi.getNumCOM();
		Enumeration<COM> com = hi.com.elements();
		String[] infoCOM = new String[nCOMMarkers];
		for (int i = 0; i < nCOMMarkers; i++)
		{
			infoCOM[i] = com.nextElement().toString();
		}
		return infoCOM;
	}
	
	/**
	 * Prints the error message 'msg' to standard err, prepending "ERROR" to it,
	 * and sets the exitCode to 'code'. An exit code different than 0 indicates
	 * that there where problems.
	 * 
	 * @param msg
	 *            The error message
	 * 
	 * @param code
	 *            The exit code to set
	 */
	protected void error(String msg, int code)
	{
		exitCode = code;
		FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, msg);
	}

	/**
	 * Prints the error message 'msg' to standard err, prepending "ERROR" to it,
	 * and sets the exitCode to 'code'. An exit code different than 0 indicates
	 * that there where problems. Either the stacktrace or a "details" message
	 * is output depending on the data of the "debug" parameter.
	 * 
	 * @param msg
	 *            The error message
	 * 
	 * @param code
	 *            The exit code to set
	 * 
	 * @param ex
	 *            The exception associated with the call
	 */
	protected void error(String msg, int code, Throwable ex)
	{
		FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, msg);
		if (pl.getParameter("debug").equals("on"))
		{
			exitCode = code;
			ex.printStackTrace();
		}
		else
		{
			error("Use '-debug' option for more details", 2);
		}
	}
}
