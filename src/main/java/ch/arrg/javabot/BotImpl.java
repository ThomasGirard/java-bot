package ch.arrg.javabot;

import java.io.IOException;
import java.util.List;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

import com.google.common.collect.Lists;

import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.data.UserData;
import ch.arrg.javabot.log.DatabaseLogServiceProvider;
import ch.arrg.javabot.log.LogEvent;
import ch.arrg.javabot.util.Logging;

/** Main bot logic
 *
 * @author tgi */
public class BotImpl extends PircBot implements Bot {
	
	private static final String ENCODING = "utf-8";
	
	private BotLogic logic = new BotLogic();
	
	private boolean isPaused = false;
	
	private List<String> ignoredUsers = Lists.newArrayList(Const.strArray("bot.ignoredUsers"));
	
	public BotImpl() throws Exception {
		Logging.log("Building bot " + Const.BOT_NAME);
		setName(Const.BOT_NAME);
		setLogin(Const.BOT_NAME);
		setEncoding(ENCODING);
	}
	
	public void start() throws NickAlreadyInUseException, IOException, IrcException {
		Logging.log("Starting bot on " + Const.SERVER_URL + ":" + Const.SERVER_PORT);
		connect(Const.SERVER_URL, Const.SERVER_PORT);
	}
	
	@Override
	protected void onConnect() {
		super.onConnect();
		Logging.log("Bot connected, joining " + Const.str("channels"));
		String[] chans = Const.strArray("channels");
		for(String chan : chans) {
			joinChannel(chan);
		}
	}
	
	@Override
	protected void onPrivateMessage(String sender, String login, String hostname, String message) {
		// TODO hack : this will make all replies go the private conv, but it'd
		// be better if the context was privmsg aware.
		String channel = sender;
		BotContext ctx = new BotContext(this, channel, sender, login, hostname, message);
		
		// DatabaseLogServiceProvider.get().logEvent(LogEvent.MESSAGE, ctx);
		
		if(ignoredUsers.contains(sender)) {
			return;
		}
		
		logic.onMessage(ctx);
	}
	
	@Override
	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
		BotContext ctx = new BotContext(this, channel, sender, login, hostname, message);
		
		DatabaseLogServiceProvider.get().logEvent(LogEvent.MESSAGE, ctx);
		
		if(ignoredUsers.contains(sender)) {
			return;
		}
		
		logic.onMessage(ctx);
	}
	
	@Override
	protected void onJoin(String channel, String sender, String login, String hostname) {
		BotContext ctx = new BotContext(this, channel, sender, login, hostname, null);
		DatabaseLogServiceProvider.get().logEvent(LogEvent.JOIN, ctx);
		logic.onJoin(ctx);
	}
	
	@Override
	protected void onQuit(String sender, String login, String hostname, String reason) {
		BotContext ctx = new BotContext(this, Const.CHANNEL, sender, login, hostname, reason);
		DatabaseLogServiceProvider.get().logEvent(LogEvent.QUIT, ctx);
	}
	
	@Override
	protected void onPart(String channel, String sender, String login, String hostname) {
		BotContext ctx = new BotContext(this, channel, sender, login, hostname, "part");
		DatabaseLogServiceProvider.get().logEvent(LogEvent.PART, ctx);
	}
	
	@Override
	protected void onAction(String sender, String login, String hostname, String channel, String action) {
		BotContext ctx = new BotContext(this, channel, sender, login, hostname, action);
		DatabaseLogServiceProvider.get().logEvent(LogEvent.JOIN, ctx);
	}
	
	@Override
	protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		// TODO logging topic : hostname and co ?
		BotContext ctx = new BotContext(this, channel, setBy, setBy, setBy, topic);
		DatabaseLogServiceProvider.get().logEvent(LogEvent.TOPIC, ctx);
	}
	
	@Override
	protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
		BotContext ctx = new BotContext(this, Const.CHANNEL, oldNick, login, hostname, newNick);
		
		DatabaseLogServiceProvider.get().logEvent(LogEvent.NICK, ctx);
	}
	
	@Override
	public void sendMsg(String target, String message) {
		sendMessage(target, message);
	}
	
	@Override
	public UserData getUserData(String user) {
		return logic.getUserData(user);
	}
	
	@Override
	public void quit() {
		Logging.log("Quitting");
		quitServer(Const.QUIT_MESSAGE);
		System.exit(1);
	}
	
	@Override
	public void adminPause() {
		isPaused = true;
	}
	
	@Override
	public void adminUnpause() {
		isPaused = false;
	}
	
	@Override
	public boolean isPaused() {
		return isPaused;
	}
	
	@Override
	public Boolean toggleHandler(String handlerName) {
		return logic.toggleHandler(handlerName);
	}
}
