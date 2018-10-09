package ch.arrg.javabot;

/** This is the interface seen by the AdminHandler only to interact with the IRC
 * server.
 *
 * @author tgi */
public interface BotAdmin {

	public void quit();

	public void pauseBot();

	public void unpauseBot();

	public boolean isBotPaused();

	public Boolean toggleHandler(String handlerName);
	
	public boolean createHandler(String handlerName);
}
