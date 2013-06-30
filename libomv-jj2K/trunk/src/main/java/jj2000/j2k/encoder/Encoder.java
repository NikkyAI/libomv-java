/*
 * CVS identifier:
 *
 * $Id: Encoder.java,v 1.68 2002/05/22 14:59:08 grosbois Exp $
 *
 * Class:                   Encoder
 *
 * Description:             The encoder object
 *
 *                          [from CmdLnEncoder, Diego SANTA CRUZ, May-19-1999]
 *
 *
 * COPYRIGHT:
 * 
 * This software module was originally developed by Raphaël Grosbois and
 * Diego Santa Cruz (Swiss Federal Institute of Technology-EPFL); Joel
 * Askelöf (Ericsson Radio Systems AB); and Bertrand Berthelot, David
 * Bouchard, Félix Henry, Gerard Mozelle and Patrice Onno (Canon Research
 * Centre France S.A) in the course of development of the JPEG2000
 * standard as specified by ISO/IEC 15444 (JPEG 2000 Standard). This
 * software module is an implementation of a part of the JPEG 2000
 * Standard. Swiss Federal Institute of Technology-EPFL, Ericsson Radio
 * Systems AB and Canon Research Centre France S.A (collectively JJ2000
 * Partners) agree not to assert against ISO/IEC and users of the JPEG
 * 2000 Standard (Users) any of their rights under the copyright, not
 * including other intellectual property rights, for this software module
 * with respect to the usage by ISO/IEC and Users of this software module
 * or modifications thereof for use in hardware or software products
 * claiming conformance to the JPEG 2000 Standard. Those intending to use
 * this software module in hardware or software products are advised that
 * their use may infringe existing patents. The original developers of
 * this software module, JJ2000 Partners and ISO/IEC assume no liability
 * for use of this software module or modifications thereof. No license
 * or right to this software module is granted for non JPEG 2000 Standard
 * conforming products. JJ2000 Partners have full right to use this
 * software module for his/her own purpose, assign or donate this
 * software module to any third party and to inhibit third parties from
 * using this software module for non JPEG 2000 Standard conforming
 * products. This copyright notice must be included in all copies or
 * derivative works of this software module.
 * 
 * Copyright (c) 1999/2000 JJ2000 Partners.
 */
package jj2000.j2k.encoder;

import jj2000.j2k.quantization.quantizer.*;
import jj2000.j2k.image.forwcomptransf.*;
import jj2000.j2k.codestream.writer.*;
import jj2000.j2k.fileformat.writer.*;
import jj2000.j2k.wavelet.analysis.*;
import jj2000.j2k.entropy.encoder.*;
import jj2000.j2k.image.input.*;
import jj2000.j2k.roi.encoder.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;
import jj2000.j2k.*;

import java.util.*;
import java.io.*;

/**
 * This class is the main class of JJ2000's encoder. It instantiates all objects
 * of the chain and launchs the encoding process. It then writes the header and
 * the compressed bit stream to the output file. Finally, packed packet headers
 * (through codestream post-manipulation) and file-format may be created if
 * needed.
 * 
 * <p>
 * First the encoder should be initialized with a ParameterList object provided
 * through the constructor. Then, the run() method is invoked and the encoder
 * executes. The exit code of the class can be obtained with the getExitCode()
 * method, after the constructor and after the run method. A non-zero value
 * indicates that an error has occurred.
 * 
 * <p>
 * The modules are inserted in the encoding chain with the following order:
 * 
 * <ul>
 * <li>ImgReader</li>
 * <li>ImgDataJoiner (if multiple image readers)</li>
 * <li>Tiler</li>
 * <li>ForwCompTransf</li>
 * <li>ImgDataConverter</li>
 * <li>ForwardWT</li>
 * <li>Quantizer</li>
 * <li>ROIScaler</li>
 * <li>EntropyCoder</li>
 * <li>PostCompRateAllocator</li>
 * </ul>
 * 
 * <p>
 * The encoder uses a pull model. This means that the last module
 * (PostCompRateAllocator) requests data from its source (EntropyCoder), ...
 * 
 * <p>
 * Writing of the codestream writing (header+bit stream) is realized by
 * HeaderEncoder and CodestreamWriter modules.
 * 
 * <p>
 * Packed packet headers and file-format creation are carried out by
 * CodestreamManipulator and FileFormatWriter modules respectively.
 * 
 * <p>
 * Many modules of the encoder may behave differently depending on the
 * tile-component. The specifications of their behaviour are kept in specialized
 * modules extending ModuleSpec class. All these modules are accessible through
 * an instance of EncoderSpecs class.
 * 
 * @see ImgReader
 * @see ImgDataJoiner
 * @see ForwCompTransf
 * @see Tiler
 * @see ImgDataConverter
 * @see ForwardWT
 * @see Quantizer
 * @see ROIScaler
 * @see EntropyCoder
 * @see PostCompRateAllocator
 * @see HeaderEncoder
 * @see CodestreamWriter
 * @see CodestreamManipulator
 * @see FileFormatWriter
 * @see ModuleSpec
 * @see EncoderSpecs
 */
public class Encoder extends ImgEncoder implements Runnable
{
	/** The valid list of options prefixes */
	public final static char vprfxs[] = { ForwCompTransf.OPT_PREFIX, // Mixer
																		// module
			AnWTFilter.OPT_PREFIX, // Filters type spec
			ForwardWT.OPT_PREFIX, // Wavelets module
			Quantizer.OPT_PREFIX, // Quantizer module
			ROIScaler.OPT_PREFIX, // ROI module
			HeaderEncoder.OPT_PREFIX, // HeaderEncoder module
			EntropyCoder.OPT_PREFIX, // Coding modules
			PostCompRateAllocator.OPT_PREFIX, // Rate allocator
			PktEncoder.OPT_PREFIX, // Packet encoder
	};

	/**
	 * Instantiates an encoder object, width the ParameterList object given as
	 * argument. It also retrieves the default ParameterList.
	 * 
	 * @param pl
	 *            The ParameterList for this decoder (contains also defaults
	 *            values);
	 */
	public Encoder(ParameterList pl)
	{
		super(pl);
	}

	/**
	 * Runs the encoder. After completion the exit code is set, a non-zero value
	 * indicates that an error ocurred.
	 * 
	 * @see #getExitCode
	 */
	@Override
	public void run()
	{
		boolean verbose;
		boolean useFileFormat = false;
		ImgReader imreader[];
		String inext, infile;
		StringTokenizer sgtok;
		int ncomp;
		boolean ppminput;
		Vector<ImgReader> imreadervec;
		boolean imsigned[];
		BlkImgDataSrc imgsrc;
		int i;
		int imgcmpidxs[];
		String outname;

		try
		{

			// **** Usage and version ****
			try
			{
				// Do we print version information?
				if (pl.getBooleanParameter("v"))
				{
					printVersionAndCopyright();
				}
				// Do we print usage information?
				if (pl.getParameter("u").equals("on"))
				{
					printUsage();
					return; // When printing usage => exit
				}
				// Do we print info ?
				verbose = pl.getBooleanParameter("verbose");
			}
			catch (StringFormatException e)
			{
				error("An error occured while parsing the arguments:\n" + e.getMessage(), 1, e);
				return;
			}
			catch (NumberFormatException e)
			{
				error("An error occured while parsing the arguments:\n" + e.getMessage(), 1, e);
				return;
			}

			// **** Get general parameters ****

			// Check that we have the mandatory parameters
			if (pl.getParameter("i") == null)
			{
				error("Mandatory input file is missing (-i option)", 2);
				return;
			}

			if (pl.getParameter("o") == null)
			{
				error("Mandatory output file is missing (-o option)", 2);
				return;
			}
			outname = pl.getParameter("o");

			if (pl.getParameter("file_format").equals("on"))
			{
				useFileFormat = true;
				if (pl.getParameter("rate") != null && pl.getFloatParameter("rate") != defpl.getFloatParameter("rate"))
				{
					warning("Specified bit-rate applies only on the codestream but not on the whole file.");
				}
			}

			if (useFileFormat)
			{
				String outext = null;
				String outns = outname;
				if (outname.lastIndexOf('.') != -1)
				{
					outext = outname.substring(outname.lastIndexOf('.'), outname.length());
					outns = outname.substring(0, outname.lastIndexOf('.'));
				}

				if (outext == null || !outext.equalsIgnoreCase(".jp2"))
				{
					if (!pl.getBooleanParameter("disable_jp2_extension"))
					{
						FacilityManager
								.getMsgLogger()
								.printmsg(
										MsgLogger.INFO,
										"JPEG 2000 file names end with .jp2 extension when using the file format of part 1. This "
												+ "extension is automatically added by JJ2000. Use '-disable_jp2_extension' to disable it.");

						outname = outns + ".jp2";
					}
				}
			}

			if (pl.getBooleanParameter("lossless") && pl.getParameter("rate") != null
					&& pl.getFloatParameter("rate") != defpl.getFloatParameter("rate"))
				throw new IllegalArgumentException("Cannot use '-rate' and '-lossless' option at " + " the same time.");

			// **** ImgReader ****
			sgtok = new StringTokenizer(pl.getParameter("i"), ",");
			ncomp = 0;
			ppminput = false;
			imreadervec = new Vector<ImgReader>();
			int nTokens = sgtok.countTokens();

			for (int n = 0; n < nTokens; n++)
			{
				infile = sgtok.nextToken();
				try
				{
					if (imreadervec.size() < ncomp)
					{
						error("With PPM input format only 1 input file can be specified", 2);
						return;
					}
					if (infile.lastIndexOf('.') != -1)
					{
						inext = infile.substring(infile.lastIndexOf('.'), infile.length());
					}
					else
					{
						inext = null;
					}
					if (".PGM".equalsIgnoreCase(inext))
					{ // PGM file
						imreadervec.addElement(new ImgReaderPGM(infile));
						ncomp += 1;
					}
					else if (".PPM".equalsIgnoreCase(inext))
					{ // PPM file
						if (ncomp > 0)
						{
							error("With PPM input format only 1 input file can be specified", 2);
							return;
						}
						imreadervec.addElement(new ImgReaderPPM(infile));
						ppminput = true;
						ncomp += 3;
					}
					else
					{ // Should be PGX
						imreadervec.addElement(new ImgReaderPGX(infile));
						ncomp += 1;
					}
				}
				catch (IOException e)
				{
					error("Could not open or read from file " + infile
							+ ((e.getMessage() != null) ? (":\n" + e.getMessage()) : ""), 3, e);
					return;
				}
				finally
				{
					if (getExitCode() != 0)
					{
						// Close the other files
						while (imreadervec.size() > 0)
						{
							try
							{
								imreadervec.elementAt(imreadervec.size() - 1).close();
								imreadervec.removeElementAt(imreadervec.size() - 1);
							}
							catch (Exception e)
							{
							}
						}
					}
				}
			}
			imreader = new ImgReader[imreadervec.size()];
			imreadervec.copyInto(imreader);
			imreadervec.removeAllElements();
			imreadervec = null;
			imsigned = new boolean[ncomp];

			// **** ImgDataJoiner (if needed) ****
			if (ppminput || ncomp == 1)
			{ // Just one input
				imgsrc = imreader[0];
				for (i = 0; i < ncomp; i++)
				{
					imsigned[i] = imreader[0].isOrigSigned(i);
				}
			}
			else
			{ // More than one reader => join all readers into 1
				imgcmpidxs = new int[ncomp];
				for (i = 0; i < ncomp; i++)
				{
					imsigned[i] = imreader[i].isOrigSigned(0);
				}
				imgsrc = new ImgDataJoiner(imreader, imgcmpidxs);
			}

			int fileLength = encode(imgsrc, imsigned, ncomp, ppminput, new File(outname), useFileFormat, verbose);
			if (fileLength < 0)
				return;

			// **** Close image reader(s) ***
			for (i = 0; i < imreader.length; i++)
			{
				imreader[i].close();
			}
		}
		catch (IllegalArgumentException e)
		{
			error(e.getMessage(), 2);
			if (pl.getParameter("debug").equals("on"))
			{
				e.printStackTrace();
			}
			return;
		}
		catch (Error e)
		{
			error("An uncaught error has occurred: " + e.getMessage(), 2, e);
		}
		catch (RuntimeException e)
		{
			error("An uncaught runtime exception has occurred: " + e.getMessage(), 2, e);
		}
		catch (Throwable e)
		{
			error("An unchecked exception has occurred: " + e.getMessage(), 2, e);
		}
	}

	/**
	 * Prints version and copyright information to stdout, using the MsgPrinter.
	 */
	private void printVersionAndCopyright()
	{
		FacilityManager.getMsgLogger().println("JJ2000's JPEG 2000 Encoder\n", 2, 4);
		FacilityManager.getMsgLogger().println("Version: " + JJ2KInfo.version + "\n", 2, 4);
		FacilityManager.getMsgLogger().println("Copyright:\n\n" + JJ2KInfo.copyright + "\n", 2, 4);
		FacilityManager.getMsgLogger().println("Send bug reports to: " + JJ2KInfo.bugaddr + "\n", 2, 4);
	}

	/**
	 * Prints the usage information to stdout. The usage information is written
	 * for all modules in the encoder.
	 */
	private void printUsage()
	{
		MsgLogger ml = FacilityManager.getMsgLogger();

		ml.println("Usage:", 0, 0);
		ml.println("JJ2KEncoder args...\n", 10, 12);
		ml.println("The exit code of the encoder is non-zero if an error occurs.\n", 2, 4);
		ml.println("Note: Many encoder modules accept tile-component "
				+ "specific parameters. These parameters must be provided "
				+ "according to the pattern:\n \"[<tile-component idx>] "
				+ "<param>\" (repeated as many time as needed). ", 2, 4);
		ml.println("\n<tile-component idx> respect the following policy"
				+ " according to the degree of priority: \n  (1) t<idx> c<idx> : Tile-component specification.\n"
				+ "  (2) t<idx> : Tile specification.\n  (3) c<idx> : Component specification\n"
				+ "  (4) <void> : Default specification.\n\nWhere the priorities of the specifications are:\n"
				+ "(1) > (2) > (3) > (4), (\'>\' means \"overrides\")\n", 2, 4);
		ml.println("  <idx>: ',' separates indexes, '-' separates bounds of "
				+ "indexes list. (ex: 0,2-4 means indexes 0,2,3 and  4).\n", 2, 4);
		ml.println("The following arguments are recognized:", 2, 4);

		// Info of each encoder parameter
		printParamInfo(ml, getAllParameters());

		// Print bug-report address
		FacilityManager.getMsgLogger().println("\n\n", 0, 0);
		FacilityManager.getMsgLogger().println("Send bug reports to: " + JJ2KInfo.bugaddr + "\n", 2, 4);
	}

	/**
	 * Prints the parameters in 'pinfo' to the provided output, 'out', showing
	 * the existing defaults. The 'pinfo' argument is a 2D String array. The
	 * first dimension contains String arrays, 1 for each parameter. Each of
	 * these arrays has 3 elements, the first element is the parameter name, the
	 * second element is the synopsis for the parameter and the third one is a
	 * long description of the parameter. If the synopsis or description is
	 * 'null' then no synopsis or description is printed, respectively. If there
	 * is a default value for a parameter it is also printed.
	 * 
	 * @param out
	 *            Where to print.
	 * 
	 * @param pinfo
	 *            The parameter information to write.
	 */
	private void printParamInfo(MsgLogger out, String pinfo[][])
	{
		String defval;

		if (pinfo == null)
		{
			return;
		}

		for (int i = 0; i < pinfo.length; i++)
		{
			defval = defpl.getParameter(pinfo[i][0]);
			if (defval != null)
			{ // There is a default value
				out.println("-" + pinfo[i][0] + ((pinfo[i][1] != null) ? " " + pinfo[i][1] + " " : " ") + "(default = "
						+ defval + ")", 4, 8);
			}
			else
			{ // There is no default value
				out.println("-" + pinfo[i][0] + ((pinfo[i][1] != null) ? " " + pinfo[i][1] : ""), 4, 8);
			}
			// Is there an explanatory message?
			if (pinfo[i][2] != null)
			{
				out.println(pinfo[i][2], 6, 6);
			}
		}
	}
}
