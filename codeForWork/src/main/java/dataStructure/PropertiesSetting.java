package dataStructure;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import autoTest.test;

public class PropertiesSetting {
	private static final String propertyFileName = "config.properties";

	public static void main(String[] args) throws IOException {
		PropertiesSetting app = new PropertiesSetting();
//		System.out.println(app.getPropertyByKey("苗栗"));
		System.out.println(
				PropertiesSetting.getPropertyByKey((PropertiesSetting.getPropertyByKey("苗栗") + ".sheetFileId")));
//		app.printAll();
//		System.out.println(PropertiesSetting.getKeyByPropertyVal("Hsinchu"));
	}

	private static Properties props;

	public PropertiesSetting() throws IOException {
		if (props == null) {
			InputStream input = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			props = new Properties();
			props.load(input);
		}
	}

	public static String getPropertyByKey(String key) {
		Optional<Map.Entry<Object, Object>> prop = props.entrySet().stream()
				.filter(entry -> entry.getKey().toString().equals(key)).findFirst();
		return (prop.isEmpty()) ? null : (prop.get().getValue().toString());

	}

	public static String getKeyByPropertyVal(String val) {
		Optional<Map.Entry<Object, Object>> prop = props.entrySet().stream()
				.filter(entry -> entry.getValue().toString().equals(val)).findFirst();
		return (prop.isEmpty()) ? null : prop.get().getKey().toString();
	}

	private static void printAll() {

		// Java 8 , print key and values
		props.forEach((key, value) -> {

			System.out.println("Key : " + key.toString() + ", Value : " + value);

		});

		// Get all keys
//			prop.keySet().forEach(x -> System.out.println(x));

		Set<Object> objects = props.keySet();

		/*
		 * Enumeration e = prop.propertyNames(); while (e.hasMoreElements()) { String
		 * key = (String) e.nextElement(); String value = prop.getProperty(key);
		 * System.out.println("Key : " + key + ", Value : " + value); }
		 */
	}

//	public static String getPropertyByChineseKey(String key) throws UnsupportedEncodingException {
////	String keyInChinese = new String(key.getBytes("ISO-8859-1"), "UTF-8");
//
////	 prop.keySet().stream().forEach(s-> 	System.out.println(keyInISO +"_"+s.toString()));
//	Optional<Object> val = prop.keySet().stream().filter(o -> {
//		try {
//			return UTFtoISOString(key).equals(o.toString());
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
//	}).findFirst();
//		return (val.isEmpty()) ? null : ISOtoUTFString(val.get().toString());
//	
//}
//
//public static String ISOtoUTFString(String inISO) throws UnsupportedEncodingException {
//	return new String(inISO.getBytes("ISO-8859-1"), "UTF-8");
//}
//
//public static String UTFtoISOString(String inUTF) throws UnsupportedEncodingException {
//	return new String(inUTF.getBytes("UTF-8"), "ISO-8859-1");
//}

}
