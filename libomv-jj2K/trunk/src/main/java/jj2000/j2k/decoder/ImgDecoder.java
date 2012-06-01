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
