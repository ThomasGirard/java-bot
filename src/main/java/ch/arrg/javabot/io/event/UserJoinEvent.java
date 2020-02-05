package ch.arrg.javabot.io.event;

import ch.arrg.javabot.io.BotIO;

public class UserJoinEvent extends BotIOEvent {

	public final String channel;
	public final String user;

	public UserJoinEvent(BotIO source, String channel, String user) {
		this.source = source;
		this.channel = channel;
		this.user = user;

	}

}
