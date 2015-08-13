package ch.arrg.javabot;

import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.data.DataStoreUtils;
import ch.arrg.javabot.data.UserData;
import ch.arrg.javabot.data.UserDb;
import ch.arrg.javabot.handlers.HelloHandler;
import ch.arrg.javabot.handlers.QuestionHandler;
import ch.arrg.javabot.handlers.AdminHandler;
import ch.arrg.javabot.handlers.RecordHandler;
import ch.arrg.javabot.handlers.TimeHandler;
import ch.arrg.javabot.handlers.UserInfoHandler;
import ch.arrg.javabot.handlers.quiz.GuessWhoHandler;
import ch.arrg.javabot.handlers.quiz.GuessWordHandler;
import ch.arrg.javabot.util.CommandMatcher;

public class BotLogic {

	private Map<String, CommandHandler> handlers = new TreeMap<>();

	private final UserDb userDb;

	public BotLogic() throws Exception {
		addHandler(new HelloHandler());
		addHandler(new TimeHandler());
		addHandler(new RecordHandler());
		addHandler(new UserInfoHandler());
		addHandler(new QuestionHandler());
		addHandler(new GuessWhoHandler());
		addHandler(new GuessWordHandler());
		addHandler(new AdminHandler());

		userDb = DataStoreUtils.fromFile(Const.DATA_FILE);
		DataStoreUtils.saveOnQuit(Const.DATA_FILE, userDb);
	}

	private void addHandler(CommandHandler h) {
		handlers.put(h.getName(), h);
	}

	protected void onMessage(BotContext ctx) {
		String message = ctx.message.trim();

		CommandMatcher matcher = CommandMatcher.make("+help");
		if (matcher.matches(message)) {
			onHelp(ctx, matcher.nextWord());
			return;
		}

		for (CommandHandler handler : handlers.values()) {
			try {
				handler.handle(ctx);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void onHelp(BotContext ctx, String topic) {

		if (handlers.containsKey(topic)) {
			handlers.get(topic).help(ctx);
		} else {

			ctx.reply("Here are known handlers: ");
			StringJoiner join = new StringJoiner(" ");
			for (CommandHandler handler : handlers.values()) {
				join.add(handler.getName());
			}
			ctx.reply(join.toString());
			ctx.reply("Send +help <handler> to get more info on a particular one.");
		}
	}

	public UserData getUserData(String user) {
		return userDb.getOrCreateUserData(user);
	}

}
