package libomv;
import java.security.cert.X509Certificate;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.IOException;

public class SLXMLLogin
{
	// Login information
	public static String firstname = "Firstname";
	public static String lastname = "Lastname";
	public static String password = "Password";

	public static String server_uri = "https://login.agni.lindenlab.com/cgi-bin/login.cgi";

	// Client version
	public static String client_rev_major = "1";
	public static String client_rev_minor = "10";
	public static String client_rev_patch = "3";
	public static String client_rev_build = "4";

	public static String client_author = "aleenaelyn@gmail.com";
	public static String client_platform = "Win";
	public static String client_viewerdigest = "68920a26-9f41-b742-a5e6-db6a713dcd96";
	public static String client_loc_start = "last";
	public static String client_useragent = "MyTest";
	public static String client_mac = "00:D0:4D:28:1A:49";

	public static String[] client_options =
					{"inventory-root","inventory-skeleton","inventory-lib-root",
					 "inventory-lib-owner","inventory-skel-lib","gestures",
					 "event_categories","event_notifications",
					 "classified_categories","buddy-list","ui-config",
					 "login-flags","global-textures"};

	public static void main(String args[])
	{
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]
		{
			new X509TrustManager()
			{
				public X509Certificate[] getAcceptedIssuers()
				{
					return null;
				}
 
				public void checkClientTrusted(X509Certificate[] certs,
								String authType)
				{
				}
 
				public void checkServerTrusted(X509Certificate[] certs,
								String authType)
				{
				}
			}
		};

		// Install the all-trusting trust manager
		try
		{
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
		}

		// Now you can access an https URL without having the certificate in the truststore
		try
		{
			URL url = new URL(server_uri);
			URLConnection conn = url.openConnection();

			// Send content-type header
			conn.setRequestProperty("Content-Type", "text/xml");

			// Tell URLConnection we are going to be writing data to it
			conn.setDoOutput(true);

			String packetToSend = PacketBuilder.xmlLogin(firstname, lastname, password,
					client_loc_start, client_rev_major, client_rev_minor,
					client_rev_patch, client_rev_build, client_platform,
					client_mac, client_viewerdigest, client_useragent,
					client_author, client_options);

			System.out.println(packetToSend);

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(packetToSend);
			wr.flush();

			// Get the response
			BufferedReader rd = new BufferedReader(new
							InputStreamReader(conn.getInputStream()));
			String line;

			while ((line = rd.readLine()) != null)
			{
				System.out.println(line);
			}

			wr.close();
			rd.close();
		}
		catch (MalformedURLException e)
		{
			System.out.println(e.toString());
		}
		catch (IOException e)
		{
			System.out.println(e.toString());
		}
	}
}

class PacketBuilder
{
	public static String xmlLogin(String firstname, String lastname, String password,
					String start, String major, String minor, String patch,
					String build, String platform, String mac,
					String viewer_digest, String useragent,
					String author, Object[] options) throws IOException
	{
		String data = "";

		// Create an MD5 Hash of the password
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			password = "$1$" + hexEncode(md.digest(password.getBytes()));
		}
		catch (NoSuchAlgorithmException e)
		{
			System.out.println(e.toString());
		}

		// Construct the XML to send
		data += "<?xml version=\"1.0\"?>\n";
		data += "<methodCall><methodName>login_to_simulator</methodName><params>\n";
		data += "<param><value><struct>\n";
		data += XMLCreateMember("first", firstname);
		data += XMLCreateMember("last", lastname);
		data += XMLCreateMember("passwd", password);
		data += XMLCreateMember("start", start);
		data += XMLCreateMember("major", major);
		data += XMLCreateMember("minor", minor);
		data += XMLCreateMember("patch", patch);
		data += XMLCreateMember("build", build);
		data += XMLCreateMember("platform", platform);
		data += XMLCreateMember("mac", mac);
		data += XMLCreateMember("viewer_digest", viewer_digest);
		data += XMLCreateMember("user-agent", useragent);
		data += XMLCreateMember("author", author);
		if (options != null)
		{
			data += XMLCreateMember("options", options);
		}
		data += "\n</struct>\n</value>\n</param>\n</params>\n</methodCall>";
		return data;
	}

	public static String XMLCreateMember(String name, Object value) throws IOException
	{
		String xml = "";

		xml += "<member><name>" + name + "</name>\n";
		xml += XMLCreateValue(value);
		xml += "</member>\n";

		return xml;
	}

	public static String XMLCreateMember(String name, Object[] value) throws IOException
	{
		String xml = "";
 
		xml += "<member><name>" + name + "</name>\n";
		xml += "<value><array><data>\n";
		for (Object id : value)
		{
   			xml += XMLCreateValue(id);
		}
		xml += "</data></array></value>\n";
		xml += "</member>\n";
 
		return xml;
	}

	public static String XMLCreateValue(Object value) throws IOException
	{
		if (value instanceof String)
			return "<value><string>" + (String)value + "</string></value>\n";

		throw new IOException("XMLCreateValue: Bad value type");
	}

	static private String hexEncode(byte[] aInput)
l p	{
		StringBuffer result = new StringBuffer();
		char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 
				'c', 'd', 'e', 'f'};

		for (int idx = 0; idx < aInput.length; ++idx)
		{
			byte b = aInput[idx];
			result.append(digits[(b & 0xf0) >> 4]);
			result.append(digits[b & 0x0f]);
		}

		return result.toString();
	}
}
