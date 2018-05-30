package ch.arrg.javabot.handlers;

import java.util.function.Function;
import java.util.regex.Pattern;

import ch.arrg.javabot.CommandHandler;
import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.util.CommandMatcher;

public class TextConversionHandler implements CommandHandler {
	public static enum Mode {
		FULLWIDTH(TextConversionHandler::toFullwidth),
		FLAGLETTERS(TextConversionHandler::toFlagLetters);
		
		private Function<String, String> convFun;
		
		private Mode(Function<String, String> convFun) {
			this.convFun = convFun;
		}
		
		public String convert(String text) {
			return convFun.apply(text);
		}
	};
	
	@Override
	public void handle(BotContext ctx) {
		
		CommandMatcher matcher = CommandMatcher.make("+text");
		if(matcher.matches(ctx.message)) {
			Mode mode = getMode(matcher.nextWord());
			if(mode == null) {
				ctx.reply("No such mode.");
				// TODO list available modes
			} else {
				String result = mode.convert(matcher.remaining());
				ctx.reply(result);
			}
		}
	}
	
	private Mode getMode(String nextWord) {
		try {
			return Mode.valueOf(nextWord.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public String getName() {
		return "+text";
	}
	
	@Override
	public void help(BotContext ctx) {
		ctx.reply("Convert text to different formats : +text <format> <text>.");
		ctx.reply("Supported formats: fullwidth, flagletters.");
	}
	
	private static String toFullwidth(String text) {
		char firstFull = '！';
		// char lastFull = 'ｚ';
		char firstIn = '!';
		char lastIn = 'z';
		
		// TODO performance is bad
		char out = firstFull;
		for(char in = firstIn; in <= lastIn; in++) {
			text = text.replaceAll(Pattern.quote(in + ""), out + "");
			out++;
		}
		text = text.replaceAll(" ", "   ");
		
		return text;
	}
	
	private static String toFlagLetters(String text) {
		String flagA = "🇦";
		
		// TODO performance is bad
		StringBuilder sb = new StringBuilder();
		char[] chars = text.toCharArray();
		for(int i = 0; i < chars.length; i++) {
			char c = chars[i];
			int d = -1;
			if(c >= 'a' && c <= 'z') {
				d = c - 'a';
			} else if(c >= 'A' && c <= 'Z') {
				d = c - 'A';
			}
			
			if(d >= 0) {
				sb = sb.append(flagA.charAt(0)).append((char) (flagA.charAt(1) + d));
			} else {
				sb = sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
}
