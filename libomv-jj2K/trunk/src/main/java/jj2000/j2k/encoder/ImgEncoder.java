package jj2000.j2k.encoder;

import java.io.IOException;

import jj2000.j2k.codestream.writer.CodestreamWriter;
import jj2000.j2k.codestream.writer.HeaderEncoder;
import jj2000.j2k.entropy.encoder.EntropyCoder;
import jj2000.j2k.entropy.encoder.PostCompRateAllocator;
import jj2000.j2k.fileformat.writer.FileFormatWriter;
import jj2000.j2k.image.BlkImgDataSrc;
import jj2000.j2k.image.ImgDataConverter;
import jj2000.j2k.image.Tiler;
import jj2000.j2k.image.forwcomptransf.ForwCompTransf;
import jj2000.j2k.quantization.quantizer.Quantizer;
import jj2000.j2k.roi.encoder.ROIScaler;
import jj2000.j2k.util.CodestreamManipulator;
import jj2000.j2k.util.FacilityManager;
import jj2000.j2k.util.MsgLogger;
import jj2000.j2k.util.ParameterList;
import jj2000.j2k.wavelet.analysis.ForwardWT;

public class ImgEncoder
{
	/** The exit code of the run method */
	private int exitCode;

	/**
	 * Returns the exit code of the class. This is only initialized after the
	 * constructor and when the run method returns.
	 * 
	 * @return The exit code of the constructor and the run() method.
	 * */
	public int getExitCode()
	{
		return exitCode;
	}

	/** The parameter list (arguments) */
	protected ParameterList pl;

	public ImgEncoder(ParameterList pl)
	{
		this.pl = pl;
	}

	public int encode(BlkImgDataSrc imgsrc, int refx, int refy, int trefx, int trefy, int tw, int th, int ncomp,
								   boolean ppminput, String outname, CodestreamWriter bwriter, float rate, boolean imsigned[],
								   boolean useFileFormat, boolean pphTile, boolean pphMain, boolean tempSop, boolean tempEph,
								   int pktspertp) throws IOException
	{
		Tiler imgtiler;
		ForwCompTransf fctransf;
		ImgDataConverter converter;
		EncoderSpecs encSpec;
		ForwardWT dwt;
		Quantizer quant;
		ROIScaler rois;
		EntropyCoder ecoder;
		PostCompRateAllocator ralloc;
		HeaderEncoder headenc;

		// Instantiate tiler
		try
		{
			imgtiler = new Tiler(imgsrc, refx, refy, trefx, trefy, tw, th);
		}
		catch (IllegalArgumentException e)
		{
			error("Could not tile image" + ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""), 2, e);
			return -1;
		}
		int ntiles = imgtiler.getNumTiles();

		// **** Encoder specifications ****
		encSpec = new EncoderSpecs(ntiles, ncomp, imgsrc, pl);

		// **** Component transformation ****
		if (ppminput && pl.getParameter("Mct") != null && pl.getParameter("Mct").equals("off"))
		{
			FacilityManager.getMsgLogger().printmsg(MsgLogger.WARNING,
					"Input image is RGB and no color transform has been specified. Compression performance and "
					+ "image quality might be greatly degraded. Use the 'Mct' option to specify a color transform");
		}
		try
		{
			fctransf = new ForwCompTransf(imgtiler, encSpec);
		}
		catch (IllegalArgumentException e)
		{
			error("Could not instantiate forward component transformation"
					+ ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""), 2, e);
			return -1;
		}

		// **** ImgDataConverter ****
		converter = new ImgDataConverter(fctransf);

		// **** ForwardWT ****
		try
		{
			dwt = ForwardWT.createInstance(converter, pl, encSpec);
		}
		catch (IllegalArgumentException e)
		{
			error("Could not instantiate wavelet transform"
					+ ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""), 2, e);
			return -1;
		}

		// **** Quantizer ****
		try
		{
			quant = Quantizer.createInstance(dwt, encSpec);
		}
		catch (IllegalArgumentException e)
		{
			error("Could not instantiate quantizer" + ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""), 2, e);
			return -1;
		}

		// **** ROIScaler ****
		try
		{
			rois = ROIScaler.createInstance(quant, pl, encSpec);
		}
		catch (IllegalArgumentException e)
		{
			error("Could not instantiate ROI scaler" + ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""), 2, e);
			return -1;
		}

		// **** EntropyCoder ****
		try
		{
			ecoder = EntropyCoder.createInstance(rois, pl, encSpec.cblks, encSpec.pss, encSpec.bms, encSpec.mqrs,
					encSpec.rts, encSpec.css, encSpec.sss, encSpec.lcs, encSpec.tts);
		}
		catch (IllegalArgumentException e)
		{
			error("Could not instantiate entropy coder"
					+ ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""), 2, e);
			return -1;
		}

		// **** Rate allocator ****
		try
		{
			ralloc = PostCompRateAllocator.createInstance(ecoder, pl, rate, bwriter, encSpec);
		}
		catch (IllegalArgumentException e)
		{
			error("Could not instantiate rate allocator"
					+ ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""), 2, e);
			return -1;
		}

		// **** HeaderEncoder ****
		headenc = new HeaderEncoder(imgsrc, imsigned, dwt, imgtiler, encSpec, rois, ralloc, pl);
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

		// **** Tile-parts and packed packet headers ****
		if (pktspertp > 0 || pphTile || pphMain)
		{
			try
			{
				CodestreamManipulator cm = new CodestreamManipulator(outname, ntiles, pktspertp, pphMain, pphTile,
						tempSop, tempEph);
				fileLength += cm.doCodestreamManipulation();
				if (pktspertp > 0)
				{
					FacilityManager.getMsgLogger().println(
							"Created tile-parts containing at most " + pktspertp + " packets per tile.", 4, 6);
				}
				if (pphTile)
				{
					FacilityManager.getMsgLogger().println("Moved packet headers to tile headers", 4, 6);
				}
				if (pphMain)
				{
					FacilityManager.getMsgLogger().println("Moved packet headers to main header", 4, 6);
				}
			}
			catch (IOException e)
			{
				error("Error while creating tileparts or packed packet headers"
						+ ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""), 2, e);
				return -1;
			}
		}
		// **** File Format ****
		if (useFileFormat)
		{
			try
			{
				int nc = imgsrc.getNumComps();
				int[] bpc = new int[nc];
				for (int comp = 0; comp < nc; comp++)
				{
					bpc[comp] = imgsrc.getNomRangeBits(comp);
				}

				FileFormatWriter ffw = new FileFormatWriter(outname, imgsrc.getImgHeight(), imgsrc.getImgWidth(), nc, bpc, fileLength);
				fileLength += ffw.writeFileFormat();
			}
			catch (IOException e)
			{
				throw new Error("Error while writing JP2 file format");
			}
		}
		return fileLength;
	}

	/**
	 * Prints the warning message 'msg' to standard err, prepending "WARNING" to
	 * it.
	 * 
	 * @param msg
	 *            The error message
	 * */
	protected void warning(String msg)
	{
		FacilityManager.getMsgLogger().printmsg(MsgLogger.WARNING, msg);
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
	 * */
	protected void error(String msg, int code)
	{
		exitCode = code;
		FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, msg);
	}

	protected void error(String msg, int code, Throwable e) 
	{
		FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, msg);
		if (pl.getParameter("debug").equals("on"))
		{
			exitCode = code;
			e.printStackTrace();
		}
		else
		{
			error("Use '-debug' option for more details", 2);
		}
	}
}
