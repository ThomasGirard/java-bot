package ch.arrg.javabot.handlers.quiz;

import ch.arrg.javabot.data.BotContext;

// TODO change interface to return List<String>s
/** A QuizQuestionFlavor represents a single question in the quiz.
 *
 * Methods are called when events are happening in the game ("on" methods) and the QQF is expected to have the bot
 * inform players of how the game is proceeding at that point.
 *
 * The only exception is isCorrect, which is called after a player tries to answer. */
public interface QuizQuestionFlavor {

	/** Ask the question to players. This is called once, when the question is selected. */
	public abstract void onAsk(BotContext ctx);

	/** Called after a successful answer has been provided. */
	public abstract void onSuccess(BotContext ctx, int correctPlayerScore);

	/** Called when the question gets cancelled (because the quiz is cancelled). */
	public abstract void onCancel(BotContext ctx);

	/** Called when no correct answer has been found and the question times out. */
	public abstract void onTimeout(BotContext ctx);

	/** @return true iff the answer given is correct. */
	public abstract boolean isCorrect(String guess);

}