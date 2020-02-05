package ch.arrg.javabot.io.event;

import ch.arrg.javabot.io.BotIO;
import ch.arrg.javabot.log.LogEvent;

public class AttachmentReceivedEvent extends LoggingOnlyEvent {

	public AttachmentReceivedEvent(BotIO source, String channel, String sender, String url) {
		super(source, LogEvent.ATTACHMENT, channel, sender, "Attached: " + url);
	}

}
