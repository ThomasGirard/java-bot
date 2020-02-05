package ch.arrg.javabot.io.event;

import ch.arrg.javabot.io.BotIO;
import ch.arrg.javabot.log.LogEvent;

public class LoggingOnlyEvent extends BotIOEvent {
	
	public final LogEvent eventType;
	public final String channel;
	public final String user;
	public final String message;
	
	public LoggingOnlyEvent(BotIO source, LogEvent eventType, String channel, String user, String message) {
		this.source = source;
		this.eventType = eventType;
		this.channel = channel;
		this.user = user;
		this.message = message;
	}
	
}
