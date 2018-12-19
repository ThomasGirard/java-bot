package ch.arrg.javabot.handlers.quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.log.LogLine;
import ch.arrg.javabot.util.LogLines;

public class GuessWordQuiz implements QuizFlavor {
	private static final int MIN_WORD_SIZE = 5;
	private static final int MAX_WORD_SIZE = 12;
	
	private static List<String> ignoredUsers = new ArrayList<>();
	
	static {
		// TODO make configurable
		Collections.addAll(ignoredUsers, "me_too_thanks", "me_beta_thanks", "Bretton_Woods", "JeanDuPlessi");
	}
	
	@Override
	public void help(BotContext ctx) {
		ctx.reply("Braisnchat trivia word game !!");
		ctx.reply("Each sentence will have a word replaced with ****. Guess the missing word.");
		ctx.reply("Hint: the length of the missing work is indicated in brackets.");
	}
	
	@Override
	public void onBegin(BotContext ctx) {
		ctx.reply("Starting GuessWord quiz!");
		ctx.reply("Each sentence will have a word replaced with ****. Guess the missing word.");
		ctx.reply("For each question guess the missing word (prefix answers with '+').");
		ctx.reply("Hint: the length of the missing work is indicated in brackets.");
		ctx.reply("2 points per correct answer, first to 7 wins.");
	}
	
	@Override
	public QuizQuestionFlavor getNewQuestion() {
		List<LogLine> lines = LogLines.getLogLines();
		// Valid sentences must be long enough
		// And have at least one valid word
		
		LogLine sentence = null;
		while(sentence == null) {
			int lIdx = (int) (Math.random() * lines.size());
			LogLine tmp = lines.get(lIdx);
			if(isValidLine(tmp)) {
				sentence = tmp;
			}
		}
		
		String missingWord = chooseWord(sentence);
		return new GuessWordQuestion(sentence, missingWord);
	}
	
	private static String chooseWord(LogLine sentence) {
		String[] words = splitLine(sentence);
		while(true) {
			// Terminates because line is valid
			int idx = (int) (Math.random() * words.length);
			String word = words[idx];
			if(isValidWord(word)) {
				return word;
			}
		}
	}
	
	private static boolean isValidLine(LogLine tmp) {
		if(!"pubmsg".equals(tmp.kind)) {
			return false;
		}
		if(tmp.message.length() < 30) {
			return false;
		}
		
		if(tmp.message.contains("http")) {
			return false;
		}
		
		if(ignoredUsers.contains(tmp.user))
			return false;
		
		String[] words = splitLine(tmp);
		for(String word : words) {
			if(isValidWord(word)) {
				// There's a valid word
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean isValidWord(String word) {
		return word.length() > MIN_WORD_SIZE && word.length() < MAX_WORD_SIZE;
	}
	
	private static String[] splitLine(LogLine tmp) {
		// TODO improve what a 'word' is (use \b ?)
		return tmp.message.split("[\\s(\\),.\":!?\\[\\]<>]");
	}
	
	private static class GuessWordQuestion implements QuizQuestionFlavor {
		
		private final LogLine logLine;
		private final String missingWord;
		private final String censored;
		
		GuessWordQuestion(LogLine logLine, String missingWord) {
			this.logLine = logLine;
			this.missingWord = missingWord;
			
			String replacement = Strings.repeat("*", missingWord.length());
			replacement = replacement + "[" + missingWord.length() + "]";
			this.censored = logLine.message.replaceAll(Pattern.quote(missingWord), replacement);
		}
		
		@Override
		public void onSuccess(BotContext ctx, int score) {
			ctx.reply("Correct ! The word was \"" + missingWord + "\". " + ctx.sender + " now has " + score
					+ ". Next question...");
		}
		
		@Override
		public void onAsk(BotContext ctx) {
			ctx.reply("Guess : [" + logLine.user + ": " + censored + "]");
		}
		
		@Override
		public void onCancel(BotContext ctx) {
			ctx.reply("The missing word was \"" + missingWord + "\". Losers");
		}
		
		@Override
		public void onTimeout(BotContext ctx) {
			ctx.reply("The missing word was \"" + missingWord + "\". Losers");
		}
		
		@Override
		public boolean isCorrect(String guess) {
			String canonGuess = guess.toLowerCase();
			String canonCorrect = missingWord.toLowerCase();
			
			return canonCorrect.equals(canonGuess);
		}
	}
}