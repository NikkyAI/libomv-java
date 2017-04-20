package libomv.imaging;

import java.io.IOException;
import java.security.InvalidParameterException;

import jj2000.j2k.JJ2KExceptionHandler;
import jj2000.j2k.image.DataBlk;
import jj2000.j2k.image.DataBlkInt;
import jj2000.j2k.image.input.ImgReader;

public class J2KImageReader extends ImgReader
{
	/**
	 * Temporary DataBlkInt object (needed when encoder uses floating-point
	 * filters). This avoids allocating new DataBlk at each time
	 */
	private DataBlkInt intBlk;
	private ManagedImage image;
	private int rb;
	
	private byte dataPtrs[][];

	public J2KImageReader(ManagedImage managedImage, int numComp)
	{
		nc = numComp;
		image = managedImage;
		w = image.getWidth();
		h = image.getHeight();
		dataPtrs = new byte[nc][];
		
		for (int i = 0; i < nc; i++)
		{
			switch (i)
			{
				case 0:
					dataPtrs[i] = image.getRed();
					break;
				case 1:
					if (nc > 2)
						dataPtrs[i] = image.getGreen();
					else
						dataPtrs[i] = image.getAlpha();
					break;
				case 2:
					dataPtrs[i] = image.getBlue();
					break;
				case 3:
					dataPtrs[i] = image.getAlpha();
					break;
				case 5:
					dataPtrs[i] = image.getBump();
					break;
                default:
                	throw new InvalidParameterException();
			}
		}
		rb = 8;
	}

	
	/* BlkImageDataSrc methods */
	/**
	 * Returns the position of the fixed point in the specified component (i.e.
	 * the number of fractional bits), which is always 0 for this ImgReader.
	 * 
	 * @param c
	 *            The index of the component.
	 * 
	 * @return The position of the fixed-point (i.e. the number of fractional
	 *         bits). Always 0 for this ImgReader.
	 */
	@Override
	public int getFixedPoint(int c)
	{
		// Check component index
		if (c < 0 || c >= nc)
			throw new IllegalArgumentException();
		return 0;
	}

	/**
	 * Returns the number of bits corresponding to the nominal range of the data
	 * in the specified component. This is the value rb (range bits) that was
	 * specified in the constructor, which normally is 8 for non bilevel data,
	 * and 1 for bilevel data.
	 * 
	 * <P>
	 * If this number is <i>b</b> then the nominal range is between -2^(b-1) and
	 * 2^(b-1)-1, since unsigned data is level shifted to have a nominal average
	 * of 0.
	 * 
	 * @param c
	 *            The index of the component.
	 * 
	 * @return The number of bits corresponding to the nominal range of the
	 *         data. For floating-point data this value is not applicable and
	 *         the return value is undefined.
	 */
	@Override
	public int getNomRangeBits(int c)
	{
		// Check component index
		if (c < 0 || c >= nc)
			throw new IllegalArgumentException();
		return rb;
	}


	/**
	 * Returns, in the blk argument, the block of image data containing the
	 * specifed rectangular area, in the specified component. The data is
	 * returned, as a reference to the internal data, if any, instead of as a
	 * copy, therefore the returned data should not be modified.
	 * 
	 * <P>
	 * After being read the coefficients are level shifted by subtracting
	 * 2^(nominal bit range - 1)
	 * 
	 * <P>
	 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
	 * 'h' members of the 'blk' argument, relative to the current tile. These
	 * members are not modified by this method. The 'offset' and 'scanw' of the
	 * returned data can be arbitrary. See the 'DataBlk' class.
	 * 
	 * <P>
	 * If the data array in <tt>blk</tt> is <tt>null</tt>, then a new one is
	 * created if necessary. The implementation of this interface may choose to
	 * return the same array or a new one, depending on what is more efficient.
	 * Therefore, the data array in <tt>blk</tt> prior to the method call should
	 * not be considered to contain the returned data, a new array may have been
	 * created. Instead, get the array from <tt>blk</tt> after the method has
	 * returned.
	 * 
	 * <P>
	 * The returned data always has its 'progressive' attribute unset (i.e.
	 * false).
	 * 
	 * <P>
	 * When an I/O exception is encountered the JJ2KExceptionHandler is used.
	 * The exception is passed to its handleException method. The action that is
	 * taken depends on the action that has been registered in
	 * JJ2KExceptionHandler. See JJ2KExceptionHandler for details.
	 * 
	 * <P>
	 * This method implements buffering for the 3 components: When the first one
	 * is asked, all the 3 components are read and stored until they are needed.
	 * 
	 * @param blk
	 *            Its coordinates and dimensions specify the area to return.
	 *            Some fields in this object are modified to return the data.
	 * 
	 * @param c
	 *            The index of the component from which to get the data. Only 0,
	 *            1 and 3 are valid.
	 * 
	 * @return The requested DataBlk
	 * 
	 * @see #getCompData
	 * 
	 * @see JJ2KExceptionHandler
	 */
	@Override
	public DataBlk getInternCompData(DataBlk blk, int c)
	{
		// Check component index
		if (c < 0 || c >= nc)
			throw new IllegalArgumentException();

		// Check type of block provided as an argument
		if (blk.getDataType() != DataBlk.TYPE_INT)
		{
			if (intBlk == null)
				intBlk = new DataBlkInt(blk.ulx, blk.uly, blk.w, blk.h);
			else
			{
				intBlk.ulx = blk.ulx;
				intBlk.uly = blk.uly;
				intBlk.w = blk.w;
				intBlk.h = blk.h;
			}
			blk = intBlk;
		}

		// Get data array
		int[] barr = (int[]) blk.getData();
		if (barr == null || barr.length < blk.w * blk.h)
		{
			barr = new int[blk.w * blk.h];
			blk.setData(barr);
		}
		
		int i, j, k, mi = blk.uly + blk.h;
		int levShift = 1 << (image.getBitDepth() - 1);
		byte buf[] = dataPtrs[c];

		for (i = blk.uly; i < mi; i++)
		{
			for (k = (i - blk.uly) * blk.w + blk.w - 1, j = blk.w - 1; j >= 0; k--)
				barr[k] = ((buf[j--] & 0xFF) - levShift);
		}
		
		// Turn off the progressive attribute
		blk.progressive = false;
		// Set buffer attributes
		blk.offset = 0;
		blk.scanw = blk.w;
		return blk;
	}

	/**
	 * Returns, in the blk argument, a block of image data containing the
	 * specifed rectangular area, in the specified component. The data is
	 * returned, as a copy of the internal data, therefore the returned data can
	 * be modified "in place".
	 * 
	 * <P>
	 * After being read the coefficients are level shifted by subtracting
	 * 2^(nominal bit range - 1)
	 * 
	 * <P>
	 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
	 * 'h' members of the 'blk' argument, relative to the current tile. These
	 * members are not modified by this method. The 'offset' of the returned
	 * data is 0, and the 'scanw' is the same as the block's width. See the
	 * 'DataBlk' class.
	 * 
	 * <P>
	 * If the data array in 'blk' is 'null', then a new one is created. If the
	 * data array is not 'null' then it is reused, and it must be large enough
	 * to contain the block's data. Otherwise an 'ArrayStoreException' or an
	 * 'IndexOutOfBoundsException' is thrown by the Java system.
	 * 
	 * <P>
	 * The returned data has its 'progressive' attribute unset (i.e. false).
	 * 
	 * <P>
	 * When an I/O exception is encountered the JJ2KExceptionHandler is used.
	 * The exception is passed to its handleException method. The action that is
	 * taken depends on the action that has been registered in
	 * JJ2KExceptionHandler. See JJ2KExceptionHandler for details.
	 * 
	 * @param blk
	 *            Its coordinates and dimensions specify the area to return. If
	 *            it contains a non-null data array, then it must have the
	 *            correct dimensions. If it contains a null data array a new one
	 *            is created. The fields in this object are modified to return
	 *            the data.
	 * 
	 * @param c
	 *            The index of the component from which to get the data. Between null and numComp -1.
	 * 
	 * @return The requested DataBlk
	 * 
	 * @see #getInternCompData
	 * 
	 * @see JJ2KExceptionHandler
	 */
	@Override
	public final DataBlk getCompData(DataBlk blk, int c)
	{
		// NOTE: can not directly call getInternCompData since that returns
		// internally buffered data.
		int w, h;

		// Check type of block provided as an argument
		if (blk.getDataType() != DataBlk.TYPE_INT)
		{
			DataBlkInt tmp = new DataBlkInt(blk.ulx, blk.uly, blk.w, blk.h);
			blk = tmp;
		}

		int bakarr[] = (int[]) blk.getData();
		// Save requested block size
		w = blk.w;
		h = blk.h;
		// Force internal data buffer to be different from external
		blk.setData(null);
		getInternCompData(blk, c);
		// Copy the data
		if (bakarr == null)
		{
			bakarr = new int[w * h];
		}
		if (blk.offset == 0 && blk.scanw == w)
		{
			// Requested and returned block buffer are the same size
			System.arraycopy(blk.getData(), 0, bakarr, 0, w * h);
		}
		else
		{ 
			// Requested and returned block are different
			for (int i = h - 1; i >= 0; i--)
			{ 
				// copy line by line
				System.arraycopy(blk.getData(), blk.offset + i * blk.scanw, bakarr, i * w, w);
			}
		}
		blk.setData(bakarr);
		blk.offset = 0;
		blk.scanw = blk.w;
		return blk;
	}

	/* ImgReader methods */
	/**
	 * Closes the underlying file from where the image data is being read. No
	 * operations are possible after a call to this method.
	 * 
	 * @exception IOException
	 *                If an I/O error occurs.
	 */
	@Override
	public void close() throws IOException
	{
		// Nothing to do here.		
	}

	/**
	 * Returns true if the data read was originally signed in the specified
	 * component, false if not. This method always returns false since PPM data
	 * is always unsigned.
	 * 
	 * @param c
	 *            The index of the component, from 0 to N-1.
	 * 
	 * @return always false, since PPM data is always unsigned.
	 */
	@Override
	public boolean isOrigSigned(int c)
	{
		// Check component index
		if (c < 0 || c >= nc)
			throw new IllegalArgumentException();
		return false;
	}
}
