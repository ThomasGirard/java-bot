package ch.arrg.javabot;

import ch.arrg.javabot.log.DatabaseLogServiceProvider;

/** Main class
 *
 * @author tgi */
public class JavaBot {
	
	// TODO load/save configuration via commands at runtime
	
	public static void main(String[] args) throws Exception {
		String configPath = "data/config.properties";
		Const.init(configPath);

		DatabaseLogServiceProvider.init();
		
		new BotImpl().start();
	}
}
