/*****************************************************************************
 *
 * $Id: ICCProfile.java,v 1.1 2002/07/25 14:56:55 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 *****************************************************************************/

package icc;

import java.io.RandomAccessFile;
import java.io.IOException;

import colorspace.ColorSpace;
import icc.types.ICCProfileHeader;
import icc.tags.ICCTagTable;
import icc.tags.ICCCurveType;
import icc.tags.ICCXYZType;
import icc.types.XYZNumber;
import icc.types.ICCProfileVersion;
import icc.types.ICCDateTime;
import jj2000.j2k.util.FacilityManager;
import jj2000.j2k.util.MsgLogger;

/**
 * This class models the ICCProfile file. This file is a binary file which is
 * divided into two parts, an ICCProfileHeader followed by an ICCTagTable. The
 * header is a straightforward list of descriptive parameters such as profile
 * size, version, date and various more esoteric parameters. The tag table is a
 * structured list of more complexly aggragated data describing things such as
 * ICC curves, copyright information, descriptive text blocks, etc.
 * 
 * Classes exist to model the header and tag table and their various constituent
 * parts the developer is refered to these for further information on the
 * structure and contents of the header and tag table.
 * 
 * @see jj2000.j2k.icc.types.ICCProfileHeader
 * @see jj2000.j2k.icc.tags.ICCTagTable
 * @version 1.0
 * @author Bruce A. Kern
 */

public abstract class ICCProfile
{

	private static final String eol = System.getProperty("line.separator");

	// Renamed for convenience:
	/** Gray index. */
	public static final int GRAY = 0;
	/** RGB index. */
	public static final int RED = 0;
	/** RGB index. */
	public static final int GREEN = 1;
	/** RGB index. */
	public static final int BLUE = 2;

	/** Size of native type */
	public final static int boolean_size = 1;
	/** Size of native type */
	public final static int byte_size = 1;
	/** Size of native type */
	public final static int char_size = 2;
	/** Size of native type */
	public final static int short_size = 2;
	/** Size of native type */
	public final static int int_size = 4;
	/** Size of native type */
	public final static int float_size = 4;
	/** Size of native type */
	public final static int long_size = 8;
	/** Size of native type */
	public final static int double_size = 8;

	/* Bit twiddling constant for integral types. */public final static int BITS_PER_BYTE = 8;
	/* Bit twiddling constant for integral types. */public final static int BITS_PER_SHORT = 16;
	/* Bit twiddling constant for integral types. */public final static int BITS_PER_INT = 32;
	/* Bit twiddling constant for integral types. */public final static int BITS_PER_LONG = 64;
	/* Bit twiddling constant for integral types. */public final static int BYTES_PER_SHORT = 2;
	/* Bit twiddling constant for integral types. */public final static int BYTES_PER_INT = 4;
	/* Bit twiddling constant for integral types. */public final static int BYTES_PER_LONG = 8;

	/**
	 * Creates an int from a 4 character String
	 * 
	 * @param fourChar
	 *            string representation of an integer
	 * @return the integer which is denoted by the input String.
	 */
	public static int getIntFromString(String fourChar)
	{
		byte[] bytes = fourChar.getBytes();
		return getInt(bytes, 0);
	}

	/**
	 * Create an XYZNumber from byte [] input
	 * 
	 * @param data
	 *            array containing the XYZNumber representation
	 * @param offset
	 *            start of the rep in the array
	 * @return the created XYZNumber
	 */
	public static XYZNumber getXYZNumber(byte[] data, int offset)
	{
		int x, y, z;
		x = getInt(data, offset);
		y = getInt(data, offset + int_size);
		z = getInt(data, offset + 2 * int_size);
		return new XYZNumber(x, y, z);
	}

	/**
	 * Create an ICCProfileVersion from byte [] input
	 * 
	 * @param data
	 *            array containing the ICCProfileVersion representation
	 * @param offset
	 *            start of the rep in the array
	 * @return the created ICCProfileVersion
	 */
	public static ICCProfileVersion getICCProfileVersion(byte[] data, int offset)
	{
		byte major = data[offset];
		byte minor = data[offset + byte_size];
		byte resv1 = data[offset + 2 * byte_size];
		byte resv2 = data[offset + 3 * byte_size];
		return new ICCProfileVersion(major, minor, resv1, resv2);
	}

	/**
	 * Create an ICCDateTime from byte [] input
	 * 
	 * @param data
	 *            array containing the ICCProfileVersion representation
	 * @param offset
	 *            start of the rep in the array
	 * @return the created ICCProfileVersion
	 */
	public static ICCDateTime getICCDateTime(byte[] data, int offset)
	{
		short wYear = getShort(data, offset); // Number of the actual year (i.e.
												// 1994)
		short wMonth = getShort(data, offset + ICCProfile.short_size); // Number
																		// of
																		// the
																		// month
																		// (1-12)
		short wDay = getShort(data, offset + 2 * ICCProfile.short_size); // Number
																			// of
																			// the
																			// day
		short wHours = getShort(data, offset + 3 * ICCProfile.short_size); // Number
																			// of
																			// hours
																			// (0-23)
		short wMinutes = getShort(data, offset + 4 * ICCProfile.short_size); // Number
																				// of
																				// minutes
																				// (0-59)
		short wSeconds = getShort(data, offset + 5 * ICCProfile.short_size); // Number
																				// of
																				// seconds
																				// (0-59)
		return new ICCDateTime(wYear, wMonth, wDay, wHours, wMinutes, wSeconds);
	}

	/**
	 * Create a String from a byte []. Optionally swap adjacent byte pairs.
	 * Intended to be used to create integer String representations allowing for
	 * endian translations.
	 * 
	 * @param bfr
	 *            data array
	 * @param offset
	 *            start of data in array
	 * @param length
	 *            length of data in array
	 * @param swap
	 *            swap adjacent bytes?
	 * @return String rep of data
	 */
	public static String getString(byte[] bfr, int offset, int length, boolean swap)
	{

		byte[] result = new byte[length];
		int incr = swap ? -1 : 1;
		int start = swap ? offset + length - 1 : offset;
		for (int i = 0, j = start; i < length; ++i)
		{
			result[i] = bfr[j];
			j += incr;
		}
		return new String(result);
	}

	/**
	 * Create a short from a two byte [], with optional byte swapping.
	 * 
	 * @param bfr
	 *            data array
	 * @param off
	 *            start of data in array
	 * @param swap
	 *            swap bytes?
	 * @return native type from representation.
	 */
	public static short getShort(byte[] bfr, int off, boolean swap)
	{

		int tmp0 = bfr[off] & 0xff; // Clear the sign extended bits in the int.
		int tmp1 = bfr[off + 1] & 0xff;

		return (short) (swap ? (tmp1 << BITS_PER_BYTE | tmp0) : (tmp0 << BITS_PER_BYTE | tmp1));
	}

	/**
	 * Create a short from a two byte [].
	 * 
	 * @param bfr
	 *            data array
	 * @param off
	 *            start of data in array
	 * @return native type from representation.
	 */
	public static short getShort(byte[] bfr, int off)
	{
		int tmp0 = bfr[off] & 0xff; // Clear the sign extended bits in the int.
		int tmp1 = bfr[off + 1] & 0xff;
		return (short) (tmp0 << BITS_PER_BYTE | tmp1);
	}

	/**
	 * Separate bytes in an int into a byte array lsb to msb order.
	 * 
	 * @param d
	 *            integer to separate
	 * @return byte [] containing separated int.
	 */
	public static byte[] setInt(int d)
	{
		return setInt(d, new byte[BYTES_PER_INT]);
	}

	/**
	 * Separate bytes in an int into a byte array lsb to msb order. Return the
	 * result in the provided array
	 * 
	 * @param d
	 *            integer to separate
	 * @param b
	 *            return output here.
	 * @return reference to output.
	 */
	public static byte[] setInt(int d, byte[] b)
	{
		if (b == null)
			b = new byte[BYTES_PER_INT];
		for (int i = 0; i < BYTES_PER_INT; ++i)
		{
			b[i] = (byte) (d & 0x0ff);
			d = d >> BITS_PER_BYTE;
		}
		return b;
	}

	/**
	 * Separate bytes in a long into a byte array lsb to msb order.
	 * 
	 * @param d
	 *            long to separate
	 * @return byte [] containing separated int.
	 */
	public static byte[] setLong(long d)
	{
		return setLong(d, new byte[BYTES_PER_INT]);
	}

	/**
	 * Separate bytes in a long into a byte array lsb to msb order. Return the
	 * result in the provided array
	 * 
	 * @param d
	 *            long to separate
	 * @param b
	 *            return output here.
	 * @return reference to output.
	 */
	public static byte[] setLong(long d, byte[] b)
	{
		if (b == null)
			b = new byte[BYTES_PER_LONG];
		for (int i = 0; i < BYTES_PER_LONG; ++i)
		{
			b[i] = (byte) (d & 0x0ff);
			d = d >> BITS_PER_BYTE;
		}
		return b;
	}

	/**
	 * Create an int from a byte [4], with optional byte swapping.
	 * 
	 * @param bfr
	 *            data array
	 * @param off
	 *            start of data in array
	 * @param swap
	 *            swap bytes?
	 * @return native type from representation.
	 */
	public static int getInt(byte[] bfr, int off, boolean swap)
	{

		int tmp0 = getShort(bfr, off, swap) & 0xffff; // Clear the sign extended
														// bits in the int.
		int tmp1 = getShort(bfr, off + 2, swap) & 0xffff;

		return (swap ? (tmp1 << BITS_PER_SHORT | tmp0) : (tmp0 << BITS_PER_SHORT | tmp1));
	}

	/**
	 * Create an int from a byte [4].
	 * 
	 * @param bfr
	 *            data array
	 * @param off
	 *            start of data in array
	 * @return native type from representation.
	 */
	public static int getInt(byte[] bfr, int off)
	{

		int tmp0 = getShort(bfr, off) & 0xffff; // Clear the sign extended bits
												// in the int.
		int tmp1 = getShort(bfr, off + 2) & 0xffff;

		return (tmp0 << BITS_PER_SHORT | tmp1);
	}

	/**
	 * Create an long from a byte [8].
	 * 
	 * @param bfr
	 *            data array
	 * @param off
	 *            start of data in array
	 * @return native type from representation.
	 */
	public static long getLong(byte[] bfr, int off)
	{

		long tmp0 = getInt(bfr, off) & 0xffffffff; // Clear the sign extended
													// bits in the int.
		long tmp1 = getInt(bfr, off + 4) & 0xffffffff;

		return (tmp0 << BITS_PER_INT | tmp1);
	}

	// Define the set of standard signature and type values
	// Because of the endian issues and byte swapping, the profile codes must
	// be stored in memory and be addressed by address. As such, only those
	// codes required for Restricted ICC use are defined here

	/** signature */
	public final static int kdwProfileSignature = ICCProfile.getInt(new String("acsp").getBytes(), 0);
	/** signature */
	public final static int kdwProfileSigReverse = ICCProfile.getInt(new String("psca").getBytes(), 0);
	/** profile type */
	public final static int kdwInputProfile = ICCProfile.getInt(new String("scnr").getBytes(), 0);
	/** tag type */
	public final static int kdwDisplayProfile = ICCProfile.getInt(new String("mntr").getBytes(), 0);
	/** tag type */
	public final static int kdwRGBData = ICCProfile.getInt(new String("RGB ").getBytes(), 0);
	/** tag type */
	public final static int kdwGrayData = ICCProfile.getInt(new String("GRAY").getBytes(), 0);
	/** tag type */
	public final static int kdwXYZData = ICCProfile.getInt(new String("XYZ ").getBytes(), 0);
	/** input type */
	public final static int kMonochromeInput = 0;
	/** input type */
	public final static int kThreeCompInput = 1;

	/** tag signature */
	public final static int kdwGrayTRCTag = ICCProfile.getInt(new String("kTRC").getBytes(), 0);
	/** tag signature */
	public final static int kdwRedColorantTag = ICCProfile.getInt(new String("rXYZ").getBytes(), 0);
	/** tag signature */
	public final static int kdwGreenColorantTag = ICCProfile.getInt(new String("gXYZ").getBytes(), 0);
	/** tag signature */
	public final static int kdwBlueColorantTag = ICCProfile.getInt(new String("bXYZ").getBytes(), 0);
	/** tag signature */
	public final static int kdwRedTRCTag = ICCProfile.getInt(new String("rTRC").getBytes(), 0);
	/** tag signature */
	public final static int kdwGreenTRCTag = ICCProfile.getInt(new String("gTRC").getBytes(), 0);
	/** tag signature */
	public final static int kdwBlueTRCTag = ICCProfile.getInt(new String("bTRC").getBytes(), 0);
	/** tag signature */
	public final static int kdwCopyrightTag = ICCProfile.getInt(new String("cprt").getBytes(), 0);
	/** tag signature */
	public final static int kdwMediaWhiteTag = ICCProfile.getInt(new String("wtpt").getBytes(), 0);
	/** tag signature */
	public final static int kdwProfileDescTag = ICCProfile.getInt(new String("desc").getBytes(), 0);

	private ICCProfileHeader header = null;
	private ICCTagTable tags = null;
	private byte[] profile = null;

	private int getProfileClass()
	{
		return header.dwProfileClass;
	}

	private int getPCSType()
	{
		return header.dwPCSType;
	}

	private int getProfileSignature()
	{
		return header.dwProfileSignature;
	}

	protected ICCProfile() throws ICCProfileException
	{
		throw new ICCProfileException("illegal to invoke empty constructor");
	}

	/**
	 * ParameterList constructor
	 * 
	 * @param csm
	 *            provides colorspace information
	 */
	protected ICCProfile(ColorSpace csm) throws ICCProfileInvalidException
	{
		profile = csm.getICCProfile();
		initProfile(profile);
	}

	/**
	 * Read the header and tags into memory and verify that the correct type of
	 * profile is being used. for encoding.
	 * 
	 * @param data
	 *            ICCProfile
	 * @exception ICCProfileInvalidException
	 *                for bad signature and class and bad type
	 */
	private void initProfile(byte[] data) throws ICCProfileInvalidException
	{
		header = new ICCProfileHeader(data);
		tags = ICCTagTable.createInstance(data);

		// Verify that the data pointed to by icc is indeed a valid profile
		// and that it is possibly of one of the Restricted ICC types. The
		// simplest way to check
		// this is to verify that the profile signature is correct, that it is
		// an input profile,
		// and that the PCS used is XYX.

		// However, a common error in profiles will be to create Monitor
		// profiles rather
		// than input profiles. If this is the only error found, it's still
		// useful to let this
		// go through with an error written to stderr.

		if (getProfileClass() == kdwDisplayProfile)
		{
			String message = "NOTE!! Technically, this profile is a Display profile, not an"
					+ " Input Profile, and thus is not a valid Restricted ICC profile."
					+ " However, it is quite possible that this profile is usable as"
					+ " a Restricted ICC profile, so this code will ignore this state"
					+ " and proceed with processing.";

			FacilityManager.getMsgLogger().printmsg(MsgLogger.WARNING, message);
		}

		if ((getProfileSignature() != kdwProfileSignature)
				|| ((getProfileClass() != kdwInputProfile) && (getProfileClass() != kdwDisplayProfile))
				|| (getPCSType() != kdwXYZData))
		{
			throw new ICCProfileInvalidException();
		}
	}

	/** Provide a suitable string representation for the class */
	@Override
	public String toString()
	{
		StringBuffer rep = new StringBuffer("[ICCProfile:");
		StringBuffer body = new StringBuffer();
		body.append(eol).append(header);
		body.append(eol).append(eol).append(tags);
		rep.append(ColorSpace.indent("  ", body));
		return rep.append("]").toString();
	}

	/**
	 * Create a two character hex representation of a byte
	 * 
	 * @param i
	 *            byte to represent
	 * @return representation
	 */
	public static String toHexString(byte i)
	{
		String rep = (i >= 0 && i < 16 ? "0" : "") + Integer.toHexString(i);
		if (rep.length() > 2)
			rep = rep.substring(rep.length() - 2);
		return rep;
	}

	/**
	 * Create a 4 character hex representation of a short
	 * 
	 * @param i
	 *            short to represent
	 * @return representation
	 */
	public static String toHexString(short i)
	{
		String rep;

		if (i >= 0 && i < 0x10)
			rep = "000" + Integer.toHexString(i);
		else if (i >= 0 && i < 0x100)
			rep = "00" + Integer.toHexString(i);
		else if (i >= 0 && i < 0x1000)
			rep = "0" + Integer.toHexString(i);
		else
			rep = "" + Integer.toHexString(i);

		if (rep.length() > 4)
			rep = rep.substring(rep.length() - 4);
		return rep;
	}

	/**
	 * Create a 8 character hex representation of a int
	 * 
	 * @param i
	 *            int to represent
	 * @return representation
	 */
	public static String toHexString(int i)
	{
		String rep;

		if (i >= 0 && i < 0x10)
			rep = "0000000" + Integer.toHexString(i);
		else if (i >= 0 && i < 0x100)
			rep = "000000" + Integer.toHexString(i);
		else if (i >= 0 && i < 0x1000)
			rep = "00000" + Integer.toHexString(i);
		else if (i >= 0 && i < 0x10000)
			rep = "0000" + Integer.toHexString(i);
		else if (i >= 0 && i < 0x100000)
			rep = "000" + Integer.toHexString(i);
		else if (i >= 0 && i < 0x1000000)
			rep = "00" + Integer.toHexString(i);
		else if (i >= 0 && i < 0x10000000)
			rep = "0" + Integer.toHexString(i);
		else
			rep = "" + Integer.toHexString(i);

		if (rep.length() > 8)
			rep = rep.substring(rep.length() - 8);
		return rep;
	}

	public static String toString(byte[] data)
	{

		int i, row, col, rem, rows, cols;

		StringBuffer rep = new StringBuffer();
		StringBuffer rep0 = null;
		StringBuffer rep1 = null;
		StringBuffer rep2 = null;

		cols = 16;
		rows = data.length / cols;
		rem = data.length % cols;

		byte[] lbytes = new byte[8];
		for (row = 0, i = 0; row < rows; ++row)
		{
			rep1 = new StringBuffer();
			rep2 = new StringBuffer();

			for (i = 0; i < 8; ++i)
				lbytes[i] = 0;
			byte[] tbytes = Integer.toHexString(row * 16).getBytes();
			for (int t = 0, l = lbytes.length - tbytes.length; t < tbytes.length; ++l, ++t)
				lbytes[l] = tbytes[t];

			rep0 = new StringBuffer(new String(lbytes));

			for (col = 0; col < cols; ++col)
			{
				byte b = data[i++];
				rep1.append(toHexString(b)).append(i % 2 == 0 ? " " : "");
				if (Character.isJavaIdentifierStart((char) b))
					rep2.append((char) b);
				else
					rep2.append(".");
			}
			rep.append(rep0).append(" :  ").append(rep1).append(":  ").append(rep2).append(eol);
		}

		rep1 = new StringBuffer();
		rep2 = new StringBuffer();

		for (i = 0; i < 8; ++i)
			lbytes[i] = 0;
		byte[] tbytes = Integer.toHexString(row * 16).getBytes();
		for (int t = 0, l = lbytes.length - tbytes.length; t < tbytes.length; ++l, ++t)
			lbytes[l] = tbytes[t];

		rep0 = new StringBuffer(new String(lbytes));

		for (col = 0; col < rem; ++col)
		{
			byte b = data[i++];
			rep1.append(toHexString(b)).append(i % 2 == 0 ? " " : "");
			if (Character.isJavaIdentifierStart((char) b))
				rep2.append((char) b);
			else
				rep2.append(".");
		}
		for (col = rem; col < 16; ++col)
			rep1.append("  ").append(col % 2 == 0 ? " " : "");

		rep.append(rep0).append(" :  ").append(rep1).append(":  ").append(rep2).append(eol);

		return rep.toString();
	}

	/**
	 * Access the profile header
	 * 
	 * @return ICCProfileHeader
	 */
	public ICCProfileHeader getHeader()
	{
		return header;
	}

	/**
	 * Access the profile tag table
	 * 
	 * @return ICCTagTable
	 */
	public ICCTagTable getTagTable()
	{
		return tags;
	}

	/**
	 * Parse this ICCProfile into a RestrictedICCProfile which is appropriate to
	 * the data in this profile. Either a MonochromeInputRestrictedProfile or
	 * MatrixBasedRestrictedProfile is returned
	 * 
	 * @return RestrictedICCProfile
	 * @exception ICCProfileInvalidException
	 *                no curve data
	 */
	public RestrictedICCProfile parse() throws ICCProfileInvalidException
	{

		// The next step is to determine which Restricted ICC type is used by
		// this profile.
		// Unfortunately, the only way to do this is to look through the tag
		// table for
		// the tags required by the two types.

		// First look for the gray TRC tag. If the profile is indeed an input
		// profile, and this
		// tag exists, then the profile is a Monochrome Input profile

		ICCCurveType grayTag = (ICCCurveType) tags.get(new Integer(kdwGrayTRCTag));
		if (grayTag != null)
		{
			return RestrictedICCProfile.createInstance(grayTag);
		}

		// If it wasn't a Monochrome Input profile, look for the Red Colorant
		// tag. If that
		// tag is found and the profile is indeed an input profile, then this
		// profile is
		// a Three-Component Matrix-Based Input profile

		ICCCurveType rTRCTag = (ICCCurveType) tags.get(new Integer(kdwRedTRCTag));

		if (rTRCTag != null)
		{
			ICCCurveType gTRCTag = (ICCCurveType) tags.get(new Integer(kdwGreenTRCTag));
			ICCCurveType bTRCTag = (ICCCurveType) tags.get(new Integer(kdwBlueTRCTag));
			ICCXYZType rColorantTag = (ICCXYZType) tags.get(new Integer(kdwRedColorantTag));
			ICCXYZType gColorantTag = (ICCXYZType) tags.get(new Integer(kdwGreenColorantTag));
			ICCXYZType bColorantTag = (ICCXYZType) tags.get(new Integer(kdwBlueColorantTag));
			return RestrictedICCProfile.createInstance(rTRCTag, gTRCTag, bTRCTag, rColorantTag, gColorantTag,
					bColorantTag);
		}

		throw new ICCProfileInvalidException("curve data not found in profile");
	}

	/**
	 * Output this ICCProfile to a RandomAccessFile
	 * 
	 * @param os
	 *            output file
	 */
	public void write(RandomAccessFile os) throws IOException
	{
		getHeader().write(os);
		getTagTable().write(os);
	}

	/* end class ICCProfile */
}
