package ch.arrg.javabot;

import java.io.FileInputStream;
import java.util.Properties;

import ch.arrg.javabot.util.Logging;

/** Application configuration.
 *
 * @author tgi */
public class Const {
	private static final Properties props = new Properties();

	public static void init(String configPath) {
		Logging.log("Loading config from " + configPath);
		try (FileInputStream fis = new FileInputStream(configPath)) {
			props.load(fis);
			loadBaseProps();
		} catch (Exception e) {
			Logging.logException(e);
		}
	}

	private static void loadBaseProps() {
		BOT_NAME = str("bot.name");
		SERVER_URL = str("server.url");
		SERVER_PORT = asInt("server.port");
		CHANNEL = str("channel");
		DATA_FILE = str("data.file");

		QUIT_MESSAGE = str("quit.message");
		DB_DRIVER = str("db.driver");
		DB_CONN_STRING = str("db.conn.string");
		DB_TABLE = str("db.table");
	}

	public static String str(String key) {
		return props.getProperty(key);
	}

	public static String[] strArray(String string) {
		return str(string).split(",");
	}

	private static int asInt(String key) {
		return Integer.parseInt(props.getProperty(key).trim());
	}

	public static String BOT_NAME;
	public static String SERVER_URL;
	public static int SERVER_PORT;
	public static String CHANNEL;
	public static String DATA_FILE;
	public static String QUIT_MESSAGE;
	public static String DB_DRIVER;
	public static String DB_CONN_STRING;
	public static String DB_TABLE;
}
