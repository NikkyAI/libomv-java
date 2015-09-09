package objects;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestCase;
import libomv.imaging.J2KImage;

public class ImageTests extends TestCase
{
	public void testJ2KImage() throws Exception
	{
		File file = new File("D:\\Documents\\My Documents\\Downloads\\Bretagne1.j2k");
		FileInputStream is = new FileInputStream(file);
        J2KImage image = new J2KImage(is);
        assertTrue("Loading of image failed", image != null);
        assertTrue("Loading of image failed", image.Channels == 3);
	}
}
