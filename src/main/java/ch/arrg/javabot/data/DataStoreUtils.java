package ch.arrg.javabot.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ch.arrg.javabot.util.Logging;

/** Save/load user database to/from disk.
 *
 * @author tgi */
public class DataStoreUtils {
	public static UserDb fromFile(String file) throws Exception {
		File f = new File(file);
		if(!f.exists()) {
			return new UserDb();
		}

		try (FileInputStream fis = new FileInputStream(f)) {
			try (ObjectInputStream ois = new ObjectInputStream(fis)) {
				return (UserDb) ois.readObject();
			}
		}
	}

	public static void toFile(String file, UserDb data) throws Exception {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
				oos.writeObject(data);
				oos.flush();
			}
		}
	}

	/** This method registers a shutdownhook that will attempt to save user data
	 * to disk on application shutdown. */
	public static void saveOnQuit(final String file, final UserDb data) {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Logging.log("Writing UserDb to file.");
					toFile(file, data);
				} catch (Exception e) {
					Logging.logException(e);
				}
			}
		}));
	}
}
