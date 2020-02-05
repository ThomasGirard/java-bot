package ch.arrg.javabot.io;

import ch.arrg.javabot.io.event.BotIOEvent;

public interface BotIOEventListener {
	public void onEvent(BotIOEvent e);
}
