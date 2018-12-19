package ch.arrg.javabot.handlers.quiz;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.data.UserDb;

// TODO quiz idea : a game where you're given a word, and need to figure out who uses it most often.

/** A QuizGame instance represents a full game from beginning to end. It manages scoring, asking questions, checking
 * answers, etc.
 *
 * Each QuizGame instance has a QuizFlavor that determines the questions to ask, and how the bot will interact with
 * players.
 *
 * When a question has been asked, it remains active until either a player provides the correct answer, or a timeout
 * expires. After which a new question is asked. Each player can only answer once, further answers are ignored. */
public class QuizGame {

	// TODO let scoring and timing be decided by the flavor
	private int QUESTION_TIMEOUT = 25;

	private int QUESTION_DELAY = 4;

	private int SCORE_NO = -1;

	private int SCORE_YES = 2;

	private int SCORE_LIMIT = 7;

	// TODO give players a negative score for NOT answering
	
	private Map<String, Integer> scores = new HashMap<>();

	private QuizQuestion currentQuestion;

	private final QuizFlavor quizFlavor;

	boolean finished = false;

	public QuizGame(QuizFlavor quizFlavor) {
		this.quizFlavor = quizFlavor;
	}

	public void begin(BotContext ctx) {
		quizFlavor.onBegin(ctx);
		askNewQuestion(ctx);
	}

	private void askNewQuestion(final BotContext ctx) {
		final QuizQuestion newQuestion = new QuizQuestion(quizFlavor.getNewQuestion());

		scheduleTimeout(ctx, newQuestion);

		currentQuestion = newQuestion;
		currentQuestion.getFlavor().onAsk(ctx);
	}

	public void cancel(BotContext ctx) {
		currentQuestion.getFlavor().onCancel(ctx);
		ctx.reply("Game has been cancelled.");
		endgame(ctx);
	}

	private void endgame(BotContext ctx) {
		finished = true;
		currentQuestion = null;

		// Change scores <String, Int> to a scoreboard <Int, [String]>
		Map<Integer, Set<String>> scoreBoard = new TreeMap<>(Collections.reverseOrder());
		for(Entry<String, Integer> e : scores.entrySet()) {
			Integer score = e.getValue();
			if(!scoreBoard.containsKey(score)) {
				scoreBoard.put(score, new TreeSet<String>());
			}

			scoreBoard.get(score).add(e.getKey());
		}

		// Display scoreboard
		int rank = 1;
		int eq = 0;
		for(Entry<Integer, Set<String>> e : scoreBoard.entrySet()) {
			if(rank == 1 && eq == 0) {
				ctx.reply("Game is over, " + e.getValue().iterator().next() + " won !");
				ctx.reply("Scoreboard: ");
			}

			int score = e.getKey();
			for(String user : e.getValue()) {
				ctx.reply("#" + rank + " " + user + " (" + score + ")");
				eq++;
			}

			rank += eq;
			eq = 0;
		}
	}

	public void onReply(final BotContext ctx) {
		String sender = ctx.sender;

		Boolean success = currentQuestion.onGuess(ctx);
		if(success == null) {
			return;
		}

		int score = updateScore(sender, success);

		if(score >= SCORE_LIMIT) {
			endgame(ctx);
			return;
		}

		if(success) {
			currentQuestion.getFlavor().onSuccess(ctx, score);
			// Display new question after a delay
			Executors.newScheduledThreadPool(1).schedule(new Runnable() {
				@Override
				public void run() {
					askNewQuestion(ctx);
				}
			}, QUESTION_DELAY, TimeUnit.SECONDS);
		}
	}

	private void scheduleTimeout(final BotContext ctx, final QuizQuestion newQuestion) {
		Runnable timeout = new Runnable() {
			@Override
			public void run() {
				// TODO bug : this sometimes allows the question to timeout at
				// the same time as being answered, therefore asking two new
				// questions at the same time
				if(currentQuestion == newQuestion) {
					onTimeout(ctx);
				}
			}
		};

		Executors.newScheduledThreadPool(1).schedule(timeout, QUESTION_TIMEOUT, TimeUnit.SECONDS);
	}

	private void onTimeout(BotContext rep) {
		currentQuestion.getFlavor().onTimeout(rep);
		currentQuestion = null;
		if(!finished) {
			askNewQuestion(rep);
		}
	}

	private int updateScore(String sender, boolean success) {
		if(!scores.containsKey(sender)) {
			scores.put(sender, 0);
		}

		int curr = scores.get(sender);
		curr += success ? SCORE_YES : SCORE_NO;

		scores.put(sender, curr);
		return scores.get(sender);
	}

	/** Each QuizQuestion wraps a flavor. It keeps track of who has already tried to answer. */
	private static class QuizQuestion {
		private final QuizQuestionFlavor flavor;

		private boolean solved = false;

		private Set<String> playersWhoAlreadyTried = new HashSet<>();

		QuizQuestion(QuizQuestionFlavor flavor) {
			this.flavor = flavor;
		}

		public QuizQuestionFlavor getFlavor() {
			return flavor;
		}

		public Boolean onGuess(BotContext ctx) {
			String sender = ctx.sender;
			String guess = ctx.message.substring(1); // Remove initial +

			if(solved)
				return null;

			String cSender = UserDb.canonize(sender);
			if(playersWhoAlreadyTried.contains(cSender)) {
				return null;
			}
			playersWhoAlreadyTried.add(cSender);

			if(flavor.isCorrect(guess)) {
				solved = true;
				return true;
			}

			return false;
		}

	}
}