package autoTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;

public class test {
	static String defaultFilePath = "/花蓮";

	public static void main(String[] args) {
		test app = new test();
		app.printAll("config.properties");
	}

	private void printAll(String filename) {

		try (InputStream input = getClass().getClassLoader().getResourceAsStream(filename)) {

			Properties prop = new Properties();

			if (input == null) {
				System.out.println("Sorry, unable to find " + filename);
				return;
			}

			prop.load(input);
			// Java 8 , print key and values
			prop.forEach((key, value) -> {
				try {
					System.out.println("Key : " + new String(key.toString().getBytes("ISO-8859-1"), "UTF-8") + ", Value : " + value);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});

			// Get all keys
//			prop.keySet().forEach(x -> System.out.println(x));

			Set<Object> objects = prop.keySet();

			/*
			 * Enumeration e = prop.propertyNames(); while (e.hasMoreElements()) { String
			 * key = (String) e.nextElement(); String value = prop.getProperty(key);
			 * System.out.println("Key : " + key + ", Value : " + value); }
			 */

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

}
