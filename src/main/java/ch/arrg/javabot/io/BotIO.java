package ch.arrg.javabot.io;

public interface BotIO {

	void start(BotIOEventListener listener);
	
	void quit();

	void sendMessage(String channel, String message);
	
}
