package ch.arrg.javabot.handlers;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import ch.arrg.javabot.BotAdmin;
import ch.arrg.javabot.CommandHandler;
import ch.arrg.javabot.Const;
import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.data.UserDb;
import ch.arrg.javabot.util.CommandMatcher;
import ch.arrg.javabot.util.Logging;

// TODO this should be a special kind of handler so that BotAdmin is not available to all handlers
public class AdminHandler implements CommandHandler {

	private static final String[] ADMIN_NAMES = Const.strArray("AdminHandler.adminNames");

	@Override
	public void handle(BotContext ctx) {

		if(!isAdmin(ctx)) {
			return;
		}

		BotAdmin admin = ctx.admin();

		CommandMatcher matcher = CommandMatcher.make("+admin");
		if(matcher.matches(ctx.message)) {
			String botName = matcher.nextWord();
			String action = matcher.nextWord();

			if(!botName.equals(Const.BOT_NAME))
				return;

			if("pause".equals(action)) {
				ctx.reply("zzz");
				admin.pauseBot();
			}

			if("unpause".equals(action)) {
				ctx.reply("I'm back bitches");
				admin.unpauseBot();
			}

			if("quit".equals(action)) {
				ctx.reply("Thanks mr skeltal");
				admin.quit();
			}

			if("exception".equals(action)) {
				printException(ctx);
			}

			if("loadhandler".equals(action)) {
				String handlerName = matcher.nextWord();
				Boolean loaded = admin.createHandler(handlerName);
				if(loaded) {
					ctx.reply("Handler loaded");
				} else {
					ctx.reply("Failed to load handler (check logs).");
				}
			}

			if("toggle".equals(action)) {
				String handlerName = matcher.nextWord();
				Boolean result = admin.toggleHandler(handlerName);
				if(result == null) {
					ctx.reply("No such handler");
				} else if(result) {
					ctx.reply("Handler enabled.");
				} else {
					ctx.reply("Handler disabled.");
				}
			}
		}
	}

	private void printException(final BotContext ctx) {
		Exception e = Logging.getLastException();
		if(e != null) {
			Writer wr = new Writer() {
				@Override
				public void write(char[] cbuf, int off, int len) throws IOException {
					ctx.sendMsg(ctx.sender, new String(cbuf, off, len));
				}

				@Override
				public void flush() throws IOException {
					return;
				}

				@Override
				public void close() throws IOException {
					return;
				}
			};
			e.printStackTrace(new PrintWriter(wr));
		} else {
			ctx.sendMsg(ctx.sender, "No exception");
		}
	}

	private static boolean isAdmin(BotContext ctx) {
		for(String adminName : ADMIN_NAMES) {
			if(UserDb.canonize(ctx.sender).equals(adminName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getName() {
		return "+admin";
	}

	@Override
	public void help(BotContext ctx) {
		// TODO automatic help for admin command
		ctx.reply("Administration commands: +admin <bot_name> <command> <args...>.");
		ctx.reply("Available commands: quit, pause, unpause, toggle, exception, loadhandler <fqcn>");
	}
}
