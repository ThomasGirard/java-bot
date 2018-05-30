package ch.arrg.javabot.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.arrg.javabot.CommandHandler;
import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.util.CommandMatcher;
import ch.arrg.javabot.util.Logging;

public class EmojiSearchHandler implements CommandHandler {
	
	private final static String SEARCH_URL = "http://emojipedia.org/search/?q=";
	
	@Override
	public void handle(BotContext ctx) {
		
		CommandMatcher matcher = CommandMatcher.make("+emoji");
		if(matcher.matches(ctx.message)) {
			String searchWords = matcher.remaining();
			List<SearchResult> results = search(searchWords);
			reply(ctx, results);
		}
	}
	
	private void reply(BotContext ctx, List<SearchResult> results) {
		if(results.isEmpty()) {
			ctx.reply("No results found :sadface:");
		} else {
			StringBuilder sb = new StringBuilder("Best matches : ");
			int maxResults = 5;
			for(SearchResult sr : results) {
				sb.append(sr.emoji).append(' ');
				maxResults--;
				if(maxResults <= 0)
					break;
			}
			ctx.reply(sb.toString());
		}
	}
	
	private List<SearchResult> search(String searchWords) {
		List<SearchResult> srs = new ArrayList<>();
		String fullUrl = SEARCH_URL + searchWords.replaceAll(" ", "+");
		try {
			Document document = Jsoup.connect(fullUrl).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0").get();
			
			// Check if any results
			Elements elemHeaders = document.select(".search-results h2");
			if(elemHeaders.size() > 0) {
				Elements elems = document.select(".search-results .emoji");
				for(Element e : elems) {
					SearchResult sr = new SearchResult();
					sr.emoji = e.text();
					srs.add(sr);
				}
			}
		} catch (IOException e) {
			Logging.logException(e);
		}
		
		return srs;
	}
	
	private static class SearchResult {
		String emoji;
	}
	
	@Override
	public String getName() {
		return "+emoji";
	}
	
	@Override
	public void help(BotContext ctx) {
		ctx.reply("Search for an emoji by name +emoji <description>");
	}
}
