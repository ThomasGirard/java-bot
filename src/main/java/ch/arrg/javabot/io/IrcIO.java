package ch.arrg.javabot.io;

import java.io.IOException;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;

import ch.arrg.javabot.Const;
import ch.arrg.javabot.io.event.LoggingOnlyEvent;
import ch.arrg.javabot.io.event.MessageReceivedEvent;
import ch.arrg.javabot.io.event.PrivateMessageEvent;
import ch.arrg.javabot.io.event.UserJoinEvent;
import ch.arrg.javabot.log.LogEvent;
import ch.arrg.javabot.util.Logging;

/** IRC IO
 *
 * @author tgi */
public class IrcIO extends PircBot implements BotIO {
	
	private static final String ENCODING = "utf-8";
	
	private BotIOEventListener listener;
	
	public IrcIO() throws Exception {
		Logging.log("Building IRC bot " + Const.BOT_NAME);
		
		setName(Const.BOT_NAME);
		setLogin(Const.BOT_NAME);
		setEncoding(ENCODING);
		
	}
	
	@Override
	public void start(BotIOEventListener listener) {
		this.listener = listener;
		
		Logging.log("Starting bot on " + Const.SERVER_URL + ":" + Const.SERVER_PORT);
		try {
			connect(Const.SERVER_URL, Const.SERVER_PORT);
		} catch (IOException | IrcException e) {
			// EXCP IrcIO.start
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onConnect() {
		super.onConnect();
		Logging.log("Bot connected. Identifying...");
		identify(Const.str("bot.identify.password"));
		
		Logging.log("Joining " + Const.str("irc.channels"));
		String[] chans = Const.strArray("irc.channels");
		for(String chan : chans) {
			joinChannel(chan);
		}
	}
	
	@Override
	public void quit() {
		Logging.log("Disconnecting from IRC");
		quitServer(Const.QUIT_MESSAGE);
	}
	
	@Override
	protected void onPrivateMessage(String sender, String login, String hostname, String message) {
		PrivateMessageEvent event = new PrivateMessageEvent(this, sender, message);
		listener.onEvent(event);
	}
	
	@Override
	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
		MessageReceivedEvent event = new MessageReceivedEvent(this, Const.CHANNEL, sender, message);
		listener.onEvent(event);
	}
	
	@Override
	protected void onJoin(String channel, String sender, String login, String hostname) {
		if(sender.equals(Const.BOT_NAME)) {
			onSelfJoin(channel);
		}
		
		UserJoinEvent event = new UserJoinEvent(this, Const.CHANNEL, sender);
		listener.onEvent(event);
	}
	
	private void onSelfJoin(String channel) {
		// Do nothing
	}
	
	@Override
	protected void onQuit(String sender, String login, String hostname, String reason) {
		LoggingOnlyEvent event = new LoggingOnlyEvent(this, LogEvent.QUIT, Const.CHANNEL, login, reason);
		listener.onEvent(event);
	}
	
	@Override
	protected void onPart(String channel, String sender, String login, String hostname) {
		LoggingOnlyEvent event = new LoggingOnlyEvent(this, LogEvent.PART, Const.CHANNEL, login, "part");
		listener.onEvent(event);
	}
	
	@Override
	protected void onAction(String sender, String login, String hostname, String channel, String action) {
		LoggingOnlyEvent event = new LoggingOnlyEvent(this, LogEvent.ACTION, Const.CHANNEL, login, action);
		listener.onEvent(event);
	}
	
	@Override
	protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		LoggingOnlyEvent event = new LoggingOnlyEvent(this, LogEvent.TOPIC, Const.CHANNEL, setBy, topic);
		listener.onEvent(event);
	}
	
	@Override
	protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
		LoggingOnlyEvent event = new LoggingOnlyEvent(this, LogEvent.NICK, Const.CHANNEL, oldNick, newNick);
		listener.onEvent(event);
	}
	
}
