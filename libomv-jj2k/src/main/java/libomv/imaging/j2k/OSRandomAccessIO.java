/*
 * CVS Identifier:
 *
 * $Id: RandomAccessIO.java,v 1.15 2001/10/24 12:07:02 grosbois Exp $
 *
 * Interface:           RandomAccessIO.java
 *
 * Description:         Interface definition for random access I/O.
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
package libomv.imaging.j2k;

import java.io.*;

import jj2000.j2k.io.RandomAccessIO;

/**
 * This abstract class defines the interface to perform random access I/O. It
 * implements the <tt>BinaryDataInput</tt> and <tt>BinaryDataOutput</tt>
 * interfaces so that binary data input/output can be performed.
 *
 * <p>
 * This interface supports streams of up to 2 GB in length.
 *
 * @see BinaryDataInput
 * @see BinaryDataOutput
 */
interface OSRandomAccessIO extends RandomAccessIO {

	/**
	 * Writes a byte array to the stream. Prior to writing, the stream is
	 * realigned at the byte level.
	 *
	 * @param b
	 *            The byte array to write. The lower 8 bits of <tt>b</tt> are
	 *            written.
	 *
	 * @exception IOException
	 *                If an I/O error ocurred.
	 */
	public void write(byte[] b) throws IOException;

	/**
	 * Writes a byte array to the stream. Prior to writing, the stream is
	 * realigned at the byte level.
	 *
	 * @param b
	 *            The byte array to write. The lower 8 bits of <tt>b</tt> are
	 *            written.
	 * @param off
	 *            The offset into the array to write from.
	 * @param len
	 *            The number of bytes to write into the stream.
	 *
	 * @exception IOException
	 *                If an I/O error ocurred.
	 */
	public void write(byte[] b, int off, int len) throws IOException;
}
