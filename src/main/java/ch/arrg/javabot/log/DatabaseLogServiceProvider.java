package ch.arrg.javabot.log;

import ch.arrg.javabot.Const;
import ch.arrg.javabot.util.Logging;

public class DatabaseLogServiceProvider {

	private static DatabaseLogService INST;

	public static void init() {
		if("true".equals(Const.str("db.enable"))) {
			Logging.log("Database will be enabled.");
			INST = new DatabaseLogServiceImpl();
		} else {
			Logging.log("Database will be disabled.");
			INST = new DatabaseLogServiceDummy();
		}
	}

	public static DatabaseLogService get() {
		return INST;
	}
}
