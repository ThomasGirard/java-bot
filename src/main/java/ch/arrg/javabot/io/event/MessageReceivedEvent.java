package ch.arrg.javabot.io.event;

import ch.arrg.javabot.io.BotIO;

public class MessageReceivedEvent extends BotIOEvent {

	public final String channel;
	public final String sender;
	public final String message;
	
	public MessageReceivedEvent(BotIO source, String channel, String sender, String message) {
		this.source = source;
		this.channel = channel;
		this.sender = sender;
		this.message = message;
	}

}
