package ch.arrg.javabot;

import java.util.ArrayList;
import java.util.List;

import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.data.UserData;
import ch.arrg.javabot.io.BotIO;
import ch.arrg.javabot.io.BotIOEventListener;
import ch.arrg.javabot.io.DiscordIO;
import ch.arrg.javabot.io.IrcIO;
import ch.arrg.javabot.io.event.BotIOEvent;
import ch.arrg.javabot.io.event.LoggingOnlyEvent;
import ch.arrg.javabot.io.event.MessageReceivedEvent;
import ch.arrg.javabot.io.event.PrivateMessageEvent;
import ch.arrg.javabot.io.event.UserJoinEvent;
import ch.arrg.javabot.log.DatabaseLogServiceProvider;
import ch.arrg.javabot.log.LogEvent;
import ch.arrg.javabot.util.Logging;

/** Manages the IO instances and connects them to the actual BotLogic instance.
 * Also manages logging.
 *
 * @author tgi */

// TODO provide filters that stop command evaluation (i.e. do not evaluate lines
// starting with "!")

public class BotIOManager implements BotIOEventListener {
	
	private BotLogic logic = new BotLogic();
	
	private List<BotIO> ios = new ArrayList<>();
	
	public BotIOManager() throws Exception {
		Logging.log("Building IOManager");
		
		if("true".equals(Const.str("irc.enabled")))
			ios.add(new IrcIO());

		if("true".equals(Const.str("discord.enabled")))
			ios.add(new DiscordIO());
	}
	
	public void start() {
		Logging.log("Starting IOs");

		for(BotIO io : ios) {
			try {
				io.start(this);
			} catch (RuntimeException e) {
				// Should we try to restart?
				Logging.logException(e);
			}
		}
	}
	
	private void quit() {
		Logging.log("Quitting");
		
		for(BotIO io : ios) {
			io.quit();
		}
		
		System.exit(1);
	}
	
	@Override
	public void onEvent(BotIOEvent e) {
		Bot bot = new BotBase() {
			@Override
			public void sendMsg(String target, String message) {
				e.source.sendMessage(target, message);
			}
		};
		
		if(e instanceof MessageReceivedEvent) {
			MessageReceivedEvent event = (MessageReceivedEvent) e;
			onMessage(bot, event.channel, event.sender, event.message);
			
			onLoggableEvent(bot, LogEvent.MESSAGE, event.channel, event.sender, event.message);
			
		} else if(e instanceof PrivateMessageEvent) {
			PrivateMessageEvent event = (PrivateMessageEvent) e;
			onPrivateMessage(bot, event.sender, event.message);
			
		} else if(e instanceof UserJoinEvent) {
			UserJoinEvent event = (UserJoinEvent) e;
			onJoin(bot, event.channel, event.user);
			
			onLoggableEvent(bot, LogEvent.JOIN, event.channel, event.user, "join");
			
		} else if(e instanceof LoggingOnlyEvent) {
			LoggingOnlyEvent event = (LoggingOnlyEvent) e;
			onLoggableEvent(bot, event.eventType, event.channel, event.user, event.message);
			
		} else {
			Logging.log("Unexpected event in BotManager.onEvent : " + e);
		}
	}
	
	private void onPrivateMessage(Bot bot, String sender, String message) {
		BotContext ctx = new BotContext(bot, "private", sender, message);
		
		logic.onMessage(ctx);
	}
	
	private void onMessage(Bot bot, String channel, String sender, String message) {
		BotContext ctx = new BotContext(bot, channel, sender, message);
		
		logic.onMessage(ctx);
	}
	
	private void onJoin(Bot bot, String channel, String sender) {
		
		BotContext ctx = new BotContext(bot, channel, sender, null);
		logic.onJoin(ctx);
	}
	
	private void onLoggableEvent(Bot bot, LogEvent type, String channel, String sender, String message) {
		BotContext ctx = new BotContext(bot, channel, sender, message);
		DatabaseLogServiceProvider.get().logEvent(type, ctx);
	}
	
	abstract class BotBase implements Bot, BotAdmin {
		
		@Override
		public UserData getUserData(String user) {
			return logic.getUserData(user);
		}
		
		@Override
		public BotAdmin admin() {
			return this;
		}
		
		@Override
		public void quit() {
			BotIOManager.this.quit();
		}
		
		@Override
		public void pauseBot() {
			logic.isPaused = true;
		}
		
		@Override
		public void unpauseBot() {
			logic.isPaused = false;
		}
		
		@Override
		public Boolean toggleHandler(String handlerName) {
			return logic.toggleHandler(handlerName);
		}
		
		@Override
		public boolean createHandler(String className) {
			return logic.createHandler(className);
		}
		
	}
	
}
