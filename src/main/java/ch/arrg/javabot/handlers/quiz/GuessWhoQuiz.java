package ch.arrg.javabot.handlers.quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.data.UserDb;
import ch.arrg.javabot.log.LogLine;
import ch.arrg.javabot.util.LogLines;

public class GuessWhoQuiz implements QuizFlavor {
	
	private static List<String> ignoredUsers = new ArrayList<>();
	private static List<Character> ignoredPrefixes = new ArrayList<>();
	
	static {
		// TODO make configurable
		Collections.addAll(ignoredUsers, "me_too_thanks", "me_beta_thanks", "Bretton_Woods", "JeanDuPlessi");
		Collections.addAll(ignoredPrefixes, '\'', '"', '`', '“');
	}
	
	@Override
	public void help(BotContext ctx) {
		ctx.reply("Braisnchat trivia game !!");
		ctx.reply("Guess who said specific sentences from the logs. Smell the delicious nostalgia.");
	}
	
	@Override
	public void onBegin(BotContext ctx) {
		ctx.reply("Starting GuessWho quiz!");
		ctx.reply("For each question guess who said it (prefix answers with '+').");
		ctx.reply("2 points per correct answer, first to 7 wins.");
	}
	
	@Override
	public QuizQuestionFlavor getNewQuestion() {
		while(true) {
			int idx = (int) (Math.random() * LogLines.getLogLines().size());
			// TODO : bug if no log lines or no valid log lines
			LogLine tmp = LogLines.getLogLines().get(idx);
			if(isValid(tmp)) {
				return new GuessWhoQuestion(tmp);
			}
		}
	}
	
	private boolean isValid(LogLine tmp) {
		if(!"pubmsg".equals(tmp.kind))
			return false;
		
		if(tmp.message.length() <= 20)
			return false;
		
		if(ignoredUsers.contains(tmp.user))
			return false;
		
		if(ignoredPrefixes.contains(tmp.message.charAt(0)))
			return false;
		
		return true;
	}
	
	private static class GuessWhoQuestion implements QuizQuestionFlavor {
		private final LogLine logLine;
		
		public GuessWhoQuestion(LogLine line) {
			this.logLine = line;
		}
		
		@Override
		public void onCancel(BotContext ctx) {
			ctx.reply("It was " + logLine.user + " who said \"" + logLine.message
					+ "\". Losers");
		}
		
		@Override
		public void onTimeout(BotContext ctx) {
			ctx.reply("It was " + logLine.user + " who said \"" + logLine.message
					+ "\". Losers");
		}
		
		@Override
		public void onSuccess(BotContext ctx, int score) {
			ctx.reply("Correct ! It was " + logLine.user + " who said it. " + ctx.sender
					+ " now has " + score + ". Next question...");
		}
		
		@Override
		public void onAsk(BotContext ctx) {
			ctx.reply("Who said: \"" + logLine.message + "\" ?");
		}
		
		@Override
		public boolean isCorrect(String guess) {
			String canonGuess = UserDb.canonize(guess);
			String canonCorrect = UserDb.canonize(logLine.user);
			
			return canonCorrect.equals(canonGuess);
		}
	}
}