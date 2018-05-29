package ch.arrg.javabot.util;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

public class CommandMatcher {
	
	private final String pattern;
	private String[] split;
	private int splitIdx;
	
	private CommandMatcher(String pattern) {
		this.pattern = pattern;
	}
	
	/** Returns true iff the line matches the pattern, if true, consumes the
	 * pattern. */
	public boolean matches(String line) {
		boolean matches = line.startsWith(pattern);
		if(matches) {
			String remaining = line.replaceFirst(Pattern.quote(pattern) + "\\s*", "");
			split = remaining.split("\\s+");
			splitIdx = 0;
		}
		
		return matches;
	}
	
	public static CommandMatcher make(String... items) {
		String line = Joiner.on(" ").join(items);
		return new CommandMatcher(line);
	}
	
	public String nextWord() {
		if(splitIdx < split.length) {
			String word = split[splitIdx];
			splitIdx++;
			return word;
		}
		
		return "";
	}
	
	public String remaining() {
		String[] remainingWords = Arrays.copyOfRange(split, splitIdx, split.length);
		return Joiner.on(' ').join(remainingWords);
	}

	public String peekWord() {
		if(splitIdx < split.length) {
			String word = split[splitIdx];
			return word;
		}
		
		return "";
	}
	
}
