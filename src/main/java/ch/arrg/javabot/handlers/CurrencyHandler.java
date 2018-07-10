package ch.arrg.javabot.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.arrg.javabot.CommandHandler;
import ch.arrg.javabot.Const;
import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.util.HtmlReaderHelper;
import ch.arrg.javabot.util.Logging;

// TODO currency API has changed and needs to be replaced
public class CurrencyHandler implements CommandHandler {
	
	private final static String[] CURRENCIES = Const.strArray("CurrencyHandler.allowedCurrencies");
	
	private final static String NUM_REGEX = "([\\d ]*\\d([.,]\\d+)?)";
	private final static String TARGET_CURRENCY = "EUR";
	
	private static List<Pattern> PREFIX = new ArrayList<>();
	private static List<Pattern> SUFFIX = new ArrayList<>();
	
	static {
		makePatterns();
	}
	
	private static void makePatterns() {
		for(String curr : CURRENCIES) {
			String wb = needsWordBoundaries(curr) ? "\\b" : "";
			
			String currWithParen = "(" + Pattern.quote(curr) + ")";
			String regexPrefix = wb + currWithParen + " ?" + NUM_REGEX + wb;
			String regexSuffix = wb + NUM_REGEX + " ?" + currWithParen + wb;
			
			PREFIX.add(Pattern.compile(regexPrefix));
			SUFFIX.add(Pattern.compile(regexSuffix));
		}
	}
	
	/** letter patterns need word boundaries so that for instance "I want 3
	 * nokia phones" doesn't get matched as a currency. */
	private static boolean needsWordBoundaries(String curr) {
		return curr.matches("\\w+");
	}

	@Override
	public void handle(BotContext ctx) {
		String m = ctx.message;
		
		match(ctx, PREFIX, m, true);
		match(ctx, SUFFIX, m, false);
	}
	
	private void match(BotContext ctx, List<Pattern> pats, String msg, boolean isPrefix) {
		boolean foundIt = false;
		for(Pattern p : pats) {
			Matcher matcher = p.matcher(msg.toUpperCase());
			if(matcher.find()) {
				if(isPrefix) {
					foundIt = onMatch(ctx, matcher.group(1), matcher.group(2));
				} else {
					foundIt = onMatch(ctx, matcher.group(3), matcher.group(1));
				}

				if(foundIt)
					break;
			}
		}
	}
	
	private boolean onMatch(BotContext ctx, String currencyS, String amountS) {
		try {
			String currency = normalizeCurrency(currencyS);
			amountS = cleanupNumber(amountS);
			double amount = Double.parseDouble(amountS.replace(" ", ""));
			Double rate = getRate(currency);
			if(rate != null) {
				double amountEuro = amount / rate;
				String amountEuroS = String.format("%.2f", amountEuro);
				ctx.reply("(" + amountS + " " + currencyS + " = " + amountEuroS + " " + TARGET_CURRENCY + ")");
				return true;
			}
		} catch (NumberFormatException e) {
			Logging.logException(e);
		}

		return false;
	}

	private String cleanupNumber(String number) {
		number = number.replace(" ", "");
		number = number.replace(',', '.');

		// Remove trailing zeroes in decimals
		if(number.contains(".")) {
			for(int i = number.length() - 1; i >= 0; i--) {
				if(number.charAt(i) == '0') {
					number = number.substring(0, i);
				} else {
					break;
				}
			}
		}
		
		return number;
	}
	
	private Double getRate(String currency) {
		String req = "http://api.fixer.io/latest?symbols=" + currency;
		// {"base":"EUR","date":"2016-07-21","rates":{"NOK":9.3541}}
		
		Pattern pat = Pattern.compile("\\{\"" + currency + "\":(\\d+(\\.\\d+)?)\\}");
		
		try (BufferedReader in = HtmlReaderHelper.openUrlForRead(req)) {
			String inputLine;
			
			while((inputLine = in.readLine()) != null) {
				Matcher matcher = pat.matcher(inputLine);
				if(matcher.find()) {
					return Double.parseDouble(matcher.group(1));
				}
			}
		} catch (IOException e) {
			Logging.logException(e);
		}
		
		return null;
	}
	
	private String normalizeCurrency(String currency) {
		if(currency.equals("A$"))
			return "AUD";
		if(currency.equals("$"))
			return "USD";
		if(currency.equals("£"))
			return "GBP";
		return currency;
	}
	
	@Override
	public String getName() {
		return "currency";
	}
	
	@Override
	public void help(BotContext ctx) {
		ctx.reply("Automatically converts currencies to €.");
	}
	
}
