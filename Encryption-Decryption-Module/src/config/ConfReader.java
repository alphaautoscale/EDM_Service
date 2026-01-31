package config;

import java.io.InputStream;
import java.util.Properties;

public class ConfReader {
	private static Properties prop = new Properties();

	static {
		try (InputStream is = ConfReader.class.getClassLoader().getResourceAsStream("config/conf.properties")) {
			if (is != null) {
				prop.load(is);
			} else {
				System.out.println("Config Not Found: config/conf.properties");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getKey(String key) {
		return prop.getProperty(key);
	}

}