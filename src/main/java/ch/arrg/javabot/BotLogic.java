package ch.arrg.javabot;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.data.DataStoreUtils;
import ch.arrg.javabot.data.UserData;
import ch.arrg.javabot.data.UserDb;
import ch.arrg.javabot.handlers.AdminHandler;
import ch.arrg.javabot.util.CommandMatcher;
import ch.arrg.javabot.util.Logging;

// TODO ImageDetectionHandler : automatic image description
// TODO auto pause main bot when beta bot joins
// TODO time conversions (?) like bretton
// TODO automatic summary of links ?
// TODO images to ascii art
// TODO random tweets
// TODO new kind of Handler that can actively (not reactively) interact (e.g. for NuclearThrone)

public class BotLogic {

	private Map<String, CommandHandler> handlers = new TreeMap<>();
	private Set<CommandHandler> disabled = new HashSet<>();
	private Map<String, IrcEventHandler> eventHandlers = new TreeMap<>();

	private final UserDb userDb;

	public BotLogic() throws Exception {
		String userDbFile = Const.DATA_FILE;
		Logging.log("Loading UserDb from " + userDbFile);
		userDb = DataStoreUtils.fromFile(userDbFile);
		DataStoreUtils.saveOnQuit(userDbFile, userDb);

		String handlersFile = Const.str("handlers.file");
		List<CommandHandler> handlers = HandlerFactory.createHandlersFromFile(handlersFile);
		handlers.forEach(this::addHandler);
	}

	public boolean createHandler(String className) {
		CommandHandler handler = HandlerFactory.createHandler(className);
		if(handler == null) {
			return false;
		}

		addHandler(handler);
		return true;
	}

	private void addHandler(CommandHandler h) {
		if(h == null) {
			return;
		}

		handlers.put(h.getName(), h);

		if(h instanceof IrcEventHandler) {
			eventHandlers.put(h.getName(), (IrcEventHandler) h);
		}
	}

	protected void onMessage(BotContext ctx) {
		if(ctx.admin().isBotPaused()) {
			onMessagePauseMode(ctx);
			return;
		}

		String message = ctx.message.trim();

		if(message.startsWith(Const.str("bot.ignoredPattern"))) {
			return;
		}

		CommandMatcher matcher = CommandMatcher.make("+help");
		if(matcher.matches(message)) {
			onHelp(ctx, matcher.nextWord());
			return;
		}

		for(CommandHandler handler : handlers.values()) {
			try {
				if(disabled.contains(handler))
					continue;

				handler.handle(ctx);
			} catch (Exception e) {
				Logging.logException(e);
			}
		}
	}

	protected void onMessagePauseMode(BotContext ctx) {
		for(CommandHandler handler : handlers.values()) {
			if(!(handler instanceof AdminHandler))
				continue;

			try {
				handler.handle(ctx);
			} catch (Exception e) {
				Logging.logException(e);
			}
		}
	}

	public void onJoin(BotContext ctx) {
		if(ctx.admin().isBotPaused())
			return;

		for(IrcEventHandler handler : eventHandlers.values()) {
			try {
				handler.onJoin(ctx.sender, ctx);
			} catch (Exception e) {
				Logging.logException(e);
			}
		}
	}

	private void onHelp(BotContext ctx, String topic) {

		if(handlers.containsKey(topic)) {
			handlers.get(topic).help(ctx);
			return;
		}

		if(topic.charAt(0) != '+') {
			String topicPlus = "+" + topic;
			if(handlers.containsKey(topicPlus)) {
				handlers.get(topicPlus).help(ctx);
				return;
			}
		}

		ctx.reply("Here are known handlers: ");
		StringBuilder sb = new StringBuilder();
		for(CommandHandler handler : handlers.values()) {
			sb.append(handler.getName()).append(" ");
		}
		ctx.reply(sb.toString());
		ctx.reply("Send +help <handler> to get more info on a particular one.");
	}

	public UserData getUserData(String user) {
		return userDb.getOrCreateUserData(user);
	}

	/** Return the new state (enabled = true) of the handler, null if the
	 * handler doesn't exist. */
	public Boolean toggleHandler(String handlerName) {
		CommandHandler handler = handlers.get(handlerName);
		if(handler == null)
			return null;

		if(disabled.contains(handler)) {
			disabled.remove(handler);
			return true;
		} else {
			disabled.add(handler);
			return false;
		}
	}

}
