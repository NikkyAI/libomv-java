package libomv;

import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;
import libomv.io.LibSettings;

public class LibraryVersionIT extends TestCase {

	public void testLibraryVersion() throws IOException {
		final Properties properties = new Properties();
		properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
		assertEquals("The library version does not match the project version", properties.getProperty("version"),
				LibSettings.LIBRARY_VERSION);
	}
}
