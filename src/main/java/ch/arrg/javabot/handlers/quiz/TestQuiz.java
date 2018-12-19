package ch.arrg.javabot.handlers.quiz;

import ch.arrg.javabot.data.BotContext;

public class TestQuiz implements QuizFlavor {
	
	@Override
	public void help(BotContext ctx) {
		ctx.reply("Test quiz");
	}
	
	@Override
	public void onBegin(BotContext ctx) {
		ctx.reply("Starting Test quiz!");
	}
	
	@Override
	public QuizQuestionFlavor getNewQuestion() {
		return new TestQuizQuestion();
	}
	
	private static class TestQuizQuestion implements QuizQuestionFlavor {
		private final String answer;
		
		public TestQuizQuestion() {
			this.answer = "" + (int) (Math.random() * 100);
		}
		
		@Override
		public void onCancel(BotContext ctx) {
			ctx.reply("It was " + answer + ".");
		}
		
		@Override
		public void onTimeout(BotContext ctx) {
			ctx.reply("It was " + answer + ".");
		}
		
		@Override
		public void onSuccess(BotContext ctx, int score) {
			ctx.reply("Correct ! It was " + answer + ". " + ctx.sender
					+ " now has " + score + ". Next question...");
		}
		
		@Override
		public void onAsk(BotContext ctx) {
			ctx.reply("How much is : " + answer + " ?");
		}
		
		@Override
		public boolean isCorrect(String guess) {
			return guess.equals(answer);
		}
	}
}