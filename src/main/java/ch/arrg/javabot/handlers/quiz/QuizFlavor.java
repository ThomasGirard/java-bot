package ch.arrg.javabot.handlers.quiz;

import ch.arrg.javabot.data.BotContext;

/** A QuizFlavor determines the contents of a quiz. It is essentially a QuizQuestionFlavor factory, with added methods
 * to provide more information to players. */
public interface QuizFlavor {

	/** Called when a quiz begins. This should have the bot explain what questions are about and the rules of the
	 * game. */
	public void onBegin(BotContext ctx);

	/** Used by +help to describe this flavor. */
	public void help(BotContext ctx);

	/** Returns the next quiz question. */
	public QuizQuestionFlavor getNewQuestion();
}