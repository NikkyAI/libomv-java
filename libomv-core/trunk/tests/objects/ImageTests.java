package objects;

import java.io.InputStream;

import junit.framework.TestCase;
import libomv.imaging.J2KImage;

public class ImageTests extends TestCase
{
	public void testJ2KImage() throws Exception
	{
		InputStream is = getClass().getResourceAsStream("/res/relax.jp2");
        J2KImage image = new J2KImage(is);
        assertTrue("Loading of image failed", image != null);
        assertTrue("Loading of image failed", image.Channels == 3);
	}
}
