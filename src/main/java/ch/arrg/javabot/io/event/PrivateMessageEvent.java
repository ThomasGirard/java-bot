package ch.arrg.javabot.io.event;

import ch.arrg.javabot.io.BotIO;

public class PrivateMessageEvent extends BotIOEvent {
	
	public final String sender;
	public final String message;

	public PrivateMessageEvent(BotIO source, String sender, String message) {
		this.source = source;
		this.sender = sender;
		this.message = message;
	}
	
}
