package ch.arrg.javabot.handlers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.arrg.javabot.CommandHandler;
import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.data.UserData;
import ch.arrg.javabot.data.UserDb;
import ch.arrg.javabot.util.CommandMatcher;
import ch.arrg.javabot.util.HandlerUtils;
import ch.arrg.javabot.util.Logging;

public class RemindMeHandler implements CommandHandler {

	private static final String DATA_KEY = "RemindMeHandler";

	Pattern DATE_PATTERN = Pattern.compile("((\\d+)d)?((\\d+)h)?((\\d+)m)?((\\d+)s)?");

	@Override
	public void handle(BotContext ctx) {
		CommandMatcher matcher = CommandMatcher.make("+remindme");
		if(matcher.matches(ctx.message)) {
			String dateRaw = matcher.nextWord();
			Date targetDate = parseDate(dateRaw);
			if(targetDate == null) {
				ctx.reply("Could not parse date, use e.g. 3d2h35m10s.");
				return;
			}
			RemindMeReminder reminder = new RemindMeReminder(ctx.sender, targetDate, dateRaw, matcher.remaining());
			storeReminder(ctx, reminder);

			String dateFormatted = HandlerUtils.prettyDate(targetDate);
			ctx.reply("Ok I'll remind you on " + dateFormatted + ".");
		}

		checkReminders(ctx);
	}

	private void checkReminders(BotContext ctx) {
		String userCanon = UserDb.canonize(ctx.sender);

		UserData dat = ctx.getUserData(userCanon);
		List<RemindMeReminder> reminders = dat.getOrInit(DATA_KEY, new ArrayList<RemindMeReminder>());
		List<RemindMeReminder> toRemove = new ArrayList<>();
		Date now = new Date();

		for(RemindMeReminder reminder : reminders) {
			if(reminder.targetDate.before(now)) {
				showReminder(ctx, reminder);
				toRemove.add(reminder);
			}
		}

		reminders.removeAll(toRemove);
		dat.setRecord(DATA_KEY, reminders);
	}

	private void showReminder(BotContext ctx, RemindMeReminder reminder) {
		ctx.reply("There's a reminder for you from " + reminder.dateRaw + " ago: " + reminder.message + "");
	}

	int parseIntGroup(Matcher m, int index) {
		String s = m.group(index);
		if(s == null) {
			return 0;
		}

		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			Logging.logException(e);
			return 0;
		}
	}

	private Date parseDate(String raw) {
		Matcher m = DATE_PATTERN.matcher(raw);
		if(m.matches()) {
			int days = parseIntGroup(m, 2);
			int hours = parseIntGroup(m, 4);
			int minutes = parseIntGroup(m, 6);
			int seconds = parseIntGroup(m, 8);

			long totalSeconds = seconds + minutes * 60 + hours * 60 * 60 + days * 24 * 60 * 60;
			long millis = new Date().getTime() + totalSeconds * 1000;
			Date targetDate = new Date(millis);
			return targetDate;
		} else {
			Logging.log("No regex match");
			return null;
		}
	}

	private static void storeReminder(BotContext ctx, RemindMeReminder reminder) {
		String userCanon = UserDb.canonize(reminder.user);

		UserData dat = ctx.getUserData(userCanon);
		List<RemindMeReminder> reminders = dat.getOrInit(DATA_KEY, new ArrayList<RemindMeReminder>());

		reminders.add(reminder);

		dat.setRecord(DATA_KEY, reminders);
	}

	static class RemindMeReminder implements Serializable {
		private final String user;
		private final Date targetDate;
		private final String dateRaw;
		private final String message;

		public RemindMeReminder(String user, Date targetDate, String dateRaw, String message) {
			super();
			this.user = user;
			this.targetDate = targetDate;
			this.dateRaw = dateRaw;
			this.message = message;
		}
	}

	@Override
	public String getName() {
		return "+remindme";
	}

	@Override
	public void help(BotContext ctx) {
		ctx.reply("Store a message for later.");
		ctx.reply("Use +remindme <timespan> <message> to store a message.");
		ctx.reply("Timespan is a string like 3d or 2h30m or 365d10h9m15s.");
	}
}
