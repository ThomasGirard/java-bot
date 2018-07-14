package ch.arrg.javabot.handlers;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import ch.arrg.javabot.CommandHandler;
import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.util.CommandMatcher;

public class FeatureRequestHandler implements CommandHandler {

	private static final String DATA_KEY = "FeatureRequestHandler";

	@Override
	public void handle(BotContext ctx) {

		CommandMatcher matcher = CommandMatcher.make("+feature");
		if(matcher.matches(ctx.message)) {
			String command = matcher.peekWord();

			if("rm".equals(command)) {
				// Consume the 'rm'
				matcher.nextWord();
				String id = matcher.nextWord();
				removeMessage(ctx, id);
			} else if("ls".equals(command)) {
				showFeatureRequests(ctx);
			} else {
				FeatureRequest msg = new FeatureRequest(ctx.sender, new Date(), matcher.remaining());
				String id = storeMessage(ctx, msg);
				ctx.reply("Your feedback is important to us (" + id + ")");
			}
		}

	}

	private void showFeatureRequests(BotContext ctx) {
		Map<String, FeatureRequest> frs = getFeatureRequests(ctx);
		if(frs.isEmpty()) {
			ctx.reply("No feature requests.");
		}

		TreeMap<String, FeatureRequest> sortedFeatures = new TreeMap<String, FeatureRequest>(frs);

		for(Entry<String, FeatureRequest> e : sortedFeatures.descendingMap().entrySet()) {
			ctx.reply(e.getKey() + ": " + e.getValue().msg);
		}
	}

	private void removeMessage(BotContext ctx, String id) {
		Map<String, FeatureRequest> frs = getFeatureRequests(ctx);
		FeatureRequest fr = frs.remove(id);
		if(fr != null) {
			ctx.reply("Removed : " + fr.msg);
		} else {
			ctx.reply("No such ID");
		}
	}

	private static String storeMessage(BotContext ctx, FeatureRequest msg) {
		// TODO this uses a fake user to store data. We should have a mechanism
		// to store global data
		Map<String, FeatureRequest> frs = getFeatureRequests(ctx);
		String key = getId(msg);
		frs.put(key, msg);
		return key;
	}

	private static String getId(FeatureRequest msg) {
		DateFormat df = new SimpleDateFormat("yyMMdd-hhmmss");
		return df.format(msg.when);
	}

	private static Map<String, FeatureRequest> getFeatureRequests(BotContext ctx) {
		Map<String, FeatureRequest> frs = ctx.getUserData(DATA_KEY).getOrInit(DATA_KEY,
				new HashMap<String, FeatureRequest>());
		return frs;
	}

	static class FeatureRequest implements Serializable {
		private final String from;
		private final Date when;
		private final String msg;

		public FeatureRequest(String from, Date when, String msg) {
			super();
			this.from = from;
			this.when = when;
			this.msg = msg;
		}
	}

	@Override
	public String getName() {
		return "+feature";
	}

	@Override
	public void help(BotContext ctx) {
		ctx.reply("Store feature requests.");
		ctx.reply("Use +feature (ls|rm <ID>|<feature>) to list, remove or save a feature request.");
	}
}
