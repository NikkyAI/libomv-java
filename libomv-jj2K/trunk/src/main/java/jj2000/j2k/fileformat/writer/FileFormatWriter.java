/*
 * cvs identifier:
 *
 * $Id: FileFormatWriter.java,v 1.13 2001/02/16 11:53:54 qtxjoas Exp $
 * 
 * Class:                   FileFormatWriter
 *
 * Description:             Writes the file format
 *
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
package jj2000.j2k.fileformat.writer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jj2000.j2k.fileformat.FileFormatBoxes;


/**
 * This class writes the file format wrapper that may or may not exist around a
 * valid JPEG 2000 codestream. This class writes the simple possible legal
 * fileformat
 * 
 * @see jj2000.j2k.fileformat.reader.FileFormatReader
 */
public class FileFormatWriter implements FileFormatBoxes
{
	/**
	 * The byte buffer to which to write the fileformat header
	 */
	private DataOutputStream out;
	private ByteArrayOutputStream baos;

	/** Image height */
	private int height;

	/** Image width */
	private int width;

	/** Number of components */
	private int nc;

	/** Bits per component */
	private int bpc[];

	/** Flag indicating whether number of bits per component varies */
	private boolean bpcVaries;

	/** Length of Colour Specification Box */
	private static final int CSB_LENGTH = 15;

	/** Length of File Type Box */
	private static final int FTB_LENGTH = 20;

	/** Length of Image Header Box */
	private static final int IHB_LENGTH = 22;

	/** base length of Bits Per Component box */
	private static final int BPC_LENGTH = 8;

	/**
	 * The constructor of the FileFormatWriter. It receives all the information
	 * necessary about a codestream to generate a legal JP2 file
	 * 
	 * @param os
	 *            The output stream to write the file format header into
	 * 
	 * @param height
	 *            The height of the image
	 * 
	 * @param width
	 *            The width of the image
	 * 
	 * @param nc
	 *            The number of components
	 * 
	 * @param bpc
	 *            The number of bits per component
	 * 
	 * @param clength
	 *            Length of codestream
	 * @throws IOException 
	 */
	public FileFormatWriter(OutputStream os, int height, int width, int nc, int[] bpc, int clength) throws IOException
	{
		this.height = height;
		this.width = width;
		this.nc = nc;
		this.bpc = bpc;
		this.baos = new ByteArrayOutputStream();
		this.out = new DataOutputStream(baos);

		bpcVaries = false;
		int fixbpc = bpc[0];
		for (int i = nc - 1; i > 0; i--)
		{
			if (bpc[i] != fixbpc)
				bpcVaries = true;
		}

		// Write the JP2_SIGNATURE_BOX
		out.writeInt(0x0000000c);
		out.writeInt(JP2_SIGNATURE_BOX);
		out.writeInt(0x0d0a870a);

		// Write File Type box
		writeFileTypeBox();

		// Write JP2 Header box
		writeJP2HeaderBox();
			
		// Write JP2 Codestream header
		writeContiguousCodeStreamBoxHeader(clength);
		
		os.write(baos.toByteArray());
	}
	
	public int length()
	{
		return baos.size();
	}
	
	/**
	 * This method writes the File Type box
	 * 
	 * @exception java.io.IOException
	 *                If an I/O error occurred.
	 */
	private void writeFileTypeBox() throws IOException
	{
		// Write box length (LBox)
		// LBox(4) + TBox (4) + BR(4) + MinV(4) + CL(4) = 20
		out.writeInt(FTB_LENGTH);

		// Write File Type box (TBox)
		out.writeInt(FILE_TYPE_BOX);

		// Write File Type data (DBox)
		// Write Brand box (BR)
		out.writeInt(FT_BR);

		// Write Minor Version
		out.writeInt(0);

		// Write Compatibility list
		out.writeInt(FT_BR);

	}

	/**
	 * This method writes the JP2Header box
	 * 
	 * @exception java.io.IOException
	 *                If an I/O error occurred.
	 */
	private void writeJP2HeaderBox() throws IOException
	{

		// Write box length (LBox)
		// if the number of bits per components varies, a bpcc box is written
		if (bpcVaries)
			out.writeInt(8 + IHB_LENGTH + CSB_LENGTH + BPC_LENGTH + nc);
		else
			out.writeInt(8 + IHB_LENGTH + CSB_LENGTH);

		// Write a JP2Header (TBox)
		out.writeInt(JP2_HEADER_BOX);       // 4 bytes

		// Write image header box
		writeImageHeaderBox();             // 22 bytes

		// Write Colour Bpecification Box
		writeColourSpecificationBox();     // 15 Bytes

		// if the number of bits per components varies write bpcc box
		if (bpcVaries)
			writeBitsPerComponentBox();    // 8 Byte + nc Bytes
	}

	/**
	 * This method writes the Bits Per Component box
	 * 
	 * @exception java.io.IOException
	 *                If an I/O error occurred.
	 * 
	 */
	private void writeBitsPerComponentBox() throws IOException
	{
		// Write box length (LBox)
		out.writeInt(BPC_LENGTH + nc);

		// Write a Bits Per Component box (TBox)
		out.writeInt(BITS_PER_COMPONENT_BOX);

		// Write bpc fields
		for (int i = 0; i < nc; i++)
		{
			out.writeByte(bpc[i] - 1);
		}
	}

	/**
	 * This method writes the Colour Specification box
	 * 
	 * @exception java.io.IOException
	 *                If an I/O error occurred.
	 * 
	 */
	private void writeColourSpecificationBox() throws IOException
	{
		// Write box length (LBox)
		out.writeInt(CSB_LENGTH);

		// Write a Bits Per Component box (TBox)
		out.writeInt(COLOUR_SPECIFICATION_BOX);

		// Write METH field
		out.writeByte(CSB_METH);

		// Write PREC field
		out.writeByte(CSB_PREC);

		// Write APPROX field
		out.writeByte(CSB_APPROX);

		// Write EnumCS field
		if (nc > 1)
			out.writeInt(CSB_ENUM_SRGB);
		else
			out.writeInt(CSB_ENUM_GREY);
	}

	/**
	 * This method writes the Image Header box
	 * 
	 * @exception java.io.IOException
	 *                If an I/O error occurred.
	 */
	private void writeImageHeaderBox() throws IOException
	{

		// Write box length
		out.writeInt(IHB_LENGTH);

		// Write ihdr box name
		out.writeInt(IMAGE_HEADER_BOX);

		// Write HEIGHT field
		out.writeInt(height);

		// Write WIDTH field
		out.writeInt(width);

		// Write NC field
		out.writeShort(nc);

		// Write BPC field
		// if the number of bits per component varies write 0xff else write
		// number of bits per components
		if (bpcVaries)
			out.writeByte(0xff);
		else
			out.writeByte(bpc[0] - 1);

		// Write C field
		out.writeByte(IMB_C);

		// Write UnkC field
		out.writeByte(IMB_UnkC);

		// Write IPR field
		out.writeByte(IMB_IPR);
	}

	/**
	 * This method writes the Contiguous codestream box header which is directly followed by the codestream data
	 * Call this function with the actual number of codestream data bytes after the codestream has been written
	 * 
	 * @param clength
	 *            The contiguous codestream length
	 * 
	 * @exception java.io.IOException
	 *                If an I/O error occurred.
	 */
	private void writeContiguousCodeStreamBoxHeader(int clength) throws IOException
	{
		// Write box length (LBox)
		out.writeInt(clength + 8);

		// Write contiguous codestream box name (TBox)
		out.writeInt(CONTIGUOUS_CODESTREAM_BOX);
	}
}
