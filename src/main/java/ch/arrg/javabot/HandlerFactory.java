package ch.arrg.javabot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import ch.arrg.javabot.util.Logging;

/** Factory to instantiate handlers based on Fully Qualified Class Names (or
 * files containing them). */
public class HandlerFactory {
	
	static List<CommandHandler> createHandlersFromFile(String path) throws IOException {
		List<CommandHandler> handlers = new ArrayList<CommandHandler>();
		
		List<String> classNames = FileUtils.readLines(new File(path));
		Logging.log("Instantiating " + classNames.size() + " handlers from " + path + ".");
		for(String className : classNames) {
			CommandHandler handler = createHandler(className);
			handlers.add(handler);
		}
		
		return handlers;
	}
	
	// TODO make this able to add the class in the classpath at runtime
	// See e.g.
	// https://stackoverflow.com/questions/60764/how-should-i-load-jars-dynamically-at-runtime
	static CommandHandler createHandler(String className) {
		try {
			Class<?> clazz = Class.forName(className);
			Object newInstance = clazz.newInstance();
			CommandHandler commandHandler = (CommandHandler) newInstance;
			return commandHandler;
			
		} catch (ClassNotFoundException e) {
			Logging.log("Couldn't not instantiate " + className + ": class not found.");
			Logging.logException(e);
		} catch (IllegalAccessException | InstantiationException e) {
			Logging.log("Couldn't not instantiate " + className + ": failure on newInstance.");
			Logging.logException(e);
		} catch (ClassCastException e) {
			Logging.log("Couldn't not instantiate " + className + ": it's not an instance of CommandHandler.");
			Logging.logException(e);
		}
		
		return null;
	}
	
}
