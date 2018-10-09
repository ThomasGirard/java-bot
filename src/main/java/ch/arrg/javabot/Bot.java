package ch.arrg.javabot;

import ch.arrg.javabot.data.UserData;

/** This is the interface seeb by handlers to interact with the IRC server. Only
 * a small set of operations are available.
 *
 * @author tgi */
public interface Bot {
	
	public void sendMsg(String target, String message);
	
	public UserData getUserData(String user);
	
	public BotAdmin admin();
}
