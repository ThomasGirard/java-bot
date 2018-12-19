package ch.arrg.javabot.handlers.quiz;

import java.util.HashMap;

import com.google.common.base.Joiner;

import ch.arrg.javabot.CommandHandler;
import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.util.CommandMatcher;

/** QuizHandler lets users start and participate in quizzes.
 *
 * Only one quiz can be played at a time. Each Quiz is represented by a QuizGame
 * instance. Thus the handler
 * is only responsible for managing the instances, and forwarding the right
 * commands to the instances.
 *
 * The handler also knows about QuizFlavors that are available to be played. */
public class QuizHandler implements CommandHandler {

	private static final HashMap<String, QuizFlavor> flavors = new HashMap<String, QuizFlavor>();

	static {
		flavors.put("guesswho", new GuessWhoQuiz());
		flavors.put("guessword", new GuessWordQuiz());
		// flavors.put("test", new TestQuiz());
	}

	// TODO QuizHandler is not compatible with multiple channels
	private QuizGame currentQuiz;

	@Override
	public void handle(BotContext ctx) {
		String message = ctx.message;

		// Any command starting with + is considered a quiz answer as long as a
		// quiz is running
		if(currentQuiz != null && !currentQuiz.finished) {
			if(message.startsWith("+")) {
				currentQuiz.onReply(ctx);
			}
		}

		// TODO QuizHandler : BUG if a player cancels with +quiz they lose a point
		// Other commands start with +quiz (for now only quiz creation and cancellation)
		CommandMatcher cm = CommandMatcher.make("+quiz");
		if(cm.matches(message)) {
			onCreateRequest(cm, ctx);
		}
	}

	private void onCreateRequest(CommandMatcher cm, final BotContext ctx) {
		if(currentQuiz == null || currentQuiz.finished) {
			currentQuiz = createQuiz(cm, ctx);
			if(currentQuiz != null) {
				currentQuiz.begin(ctx);
			}
		} else {
			currentQuiz.cancel(ctx);
			currentQuiz = null;
		}
	}

	private QuizGame createQuiz(CommandMatcher cm, BotContext ctx) {
		String quizName = cm.nextWord();
		if(flavors.containsKey(quizName)) {
			QuizFlavor flavor = flavors.get(quizName);
			return new QuizGame(flavor);
		} else {
			ctx.reply("This quiz doesn't exist.");
			help(ctx);
			return null;
		}
	}

	@Override
	public String getName() {
		return "+quiz";
	}

	@Override
	public void help(BotContext ctx) {
		String message = ctx.message;

		CommandMatcher cm = CommandMatcher.make("+help +quiz");
		if(cm.matches(message)) {
			String quizName = cm.remaining();
			if(flavors.containsKey(quizName)) {
				flavors.get(quizName).help(ctx);
				return;
			}
		}

		ctx.reply("Play quizzes. Use +quiz <quizName> to start.");
		ctx.reply("Try +help +quiz <quizName> to learn more about a quiz.");
		String quizzes = Joiner.on(", ").join(flavors.keySet());
		ctx.reply("Available quizzes: " + quizzes);
	}
}