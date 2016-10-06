package ch.arrg.javabot.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.github.goive.steamapi.SteamApi;
import com.github.goive.steamapi.SteamApiFactory;
import com.github.goive.steamapi.data.Price;
import com.github.goive.steamapi.data.SteamApp;
import com.github.goive.steamapi.data.SteamId;
import com.github.goive.steamapi.enums.CountryCode;
import com.github.goive.steamapi.exceptions.SteamApiException;
import com.lukaspradel.steamapi.data.json.ownedgames.GetOwnedGames;
import com.lukaspradel.steamapi.webapi.client.SteamWebApiClient;
import com.lukaspradel.steamapi.webapi.request.GetOwnedGamesRequest;
import com.lukaspradel.steamapi.webapi.request.builders.SteamWebApiRequestFactory;

import ch.arrg.javabot.CommandHandler;
import ch.arrg.javabot.Const;
import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.handlers.AksHandler.AksGameInfo;
import ch.arrg.javabot.util.CachedResource;
import ch.arrg.javabot.util.CommandMatcher;
import ch.arrg.javabot.util.Logging;

public class SteamHandler implements CommandHandler {

	// TODO make these configurable
	private final static CountryCode STORE_COUNTRY = CountryCode.FR;
	private final static String API_KEY = Const.str("SteamHandler.apiKey");
	private final static String GAMES_LIST_URL = "http://api.steampowered.com/ISteamApps/GetAppList/v0001/";

	private final String[] confUsersNames = Const.strArray("SteamHandler.users");
	private final String[] confUsersIds = Const.strArray("SteamHandler.ids");

	// TODO provide interface to steam64 IDs
	private CachedResource<TreeMap<String, Integer>> gamesIds;
	private Map<String, String> steamers = new HashMap<String, String>();

	public SteamHandler() {
		gamesIds = CachedResource.make(SteamHandler::getGamesInfo, 6, TimeUnit.HOURS);

		for(int i = 0; i < confUsersNames.length; i++) {
			steamers.put(confUsersNames[i], confUsersIds[i]);
		}
	}

	private static TreeMap<String, Integer> getGamesInfo() {
		TreeMap<String, Integer> out = new TreeMap<String, Integer>();

		URL wikiRequest;
		try {
			wikiRequest = new URL(GAMES_LIST_URL);
		} catch (MalformedURLException e) {
			Logging.logException(e);
			return null;
		}

		try (InputStream is = wikiRequest.openStream()) {
			JSONObject gamesList = new JSONObject(new JSONTokener(is));
			JSONArray apps = gamesList.getJSONObject("applist").getJSONObject("apps").getJSONArray("app");
			for(Object oApp : apps) {
				JSONObject app = (JSONObject) oApp;
				int appid = app.getInt("appid");
				String name = app.getString("name");
				out.put(name, appid);
			}

			Logging.log("Read games list : " + out.size() + " games.");
		} catch (IOException e) {
			Logging.logException(e);
		} catch (Exception e) {
			Logging.log("Couldn't read games list");
			Logging.logException(e);
		}

		return out;
	}

	private Entry<String, Integer> findGame(String name) {
		TreeMap<String, Integer> gamesMap = gamesIds.get();
		if(gamesMap.containsKey(name)) {
			return gamesMap.floorEntry(name);
		}

		for(Entry<String, Integer> e : gamesMap.entrySet()) {
			if(e.getKey().toLowerCase().contains(name.toLowerCase())) {
				return e;
			}
		}

		return null;
	}

	@Override
	public void handle(BotContext ctx) {
		CommandMatcher matcher = CommandMatcher.make("+steam");

		if(!matcher.matches(ctx.message)) {
			return;
		}

		String cmd = matcher.nextWord();

		if(cmd.equals("price")) {
			handlePrice(ctx, matcher);
		} else if(cmd.equals("owned") || cmd.equals("played")) {
			handleOwned(ctx, matcher);
		} else {
			help(ctx);
		}
	}

	private static class PlayedInfo implements Comparable<PlayedInfo> {
		final int playedMin;
		final String player;

		public PlayedInfo(String player, Integer playedMin) {
			this.playedMin = (playedMin == null) ? -1 : playedMin;
			this.player = player;
		}

		@Override
		public int compareTo(PlayedInfo o) {
			return -Double.compare(playedMin, o.playedMin);
		}
	}

	private void handleOwned(BotContext ctx, CommandMatcher matcher) {
		String gameName = matcher.remaining();
		Entry<String, Integer> gameEntry = findGame(gameName);

		if(gameEntry == null) {
			ctx.reply("Game not found.");
			return;
		}

		List<Integer> gamesList = Collections.singletonList(gameEntry.getValue());
		List<PlayedInfo> played = new ArrayList<SteamHandler.PlayedInfo>();
		try {

			SteamWebApiClient CLIENT = new SteamWebApiClient.SteamWebApiClientBuilder(API_KEY).build();

			for(Entry<String, String> e : steamers.entrySet()) {
				GetOwnedGamesRequest request = SteamWebApiRequestFactory.createGetOwnedGamesRequest(e.getValue(), true,
						true, gamesList);
				GetOwnedGames response = CLIENT.<GetOwnedGames> processRequest(request);
				PlayedInfo pi;
				if(response.getResponse().getGameCount() == 0) {
					pi = new PlayedInfo(e.getKey(), null);
				} else {
					pi = new PlayedInfo(e.getKey(), response.getResponse().getGames().get(0).getPlaytimeForever());
				}
				played.add(pi);
			}
		} catch (com.lukaspradel.steamapi.core.exception.SteamApiException e) {
			Logging.logException(e);
			ctx.reply("Couldn't read from Steam community API.");
		}

		Collections.sort(played);

		StringBuilder sb = new StringBuilder();
		for(PlayedInfo pi : played) {

			String time;
			if(pi.playedMin == -1) {
				time = "n.a.";
			} else {
				int minutes = pi.playedMin;
				if(minutes > 60) {
					int h = minutes / 60;
					int m = minutes % 60;
					time = h + "h " + m + "m";
				} else {
					time = minutes + "m";
				}
			}

			sb.append(pi.player).append(": ").append(time).append(", ");
		}

		String out = sb.toString().replaceAll(", $", "");
		ctx.reply(out);
	}

	private void handlePrice(BotContext ctx, CommandMatcher matcher) {
		String gameName = matcher.remaining();
		Entry<String, Integer> gameEntry = findGame(gameName);

		if(gameEntry == null) {
			ctx.reply("Game not found.");
			return;
		}

		SteamApp steamApp = getSteamInfo(gameEntry);
		if(steamApp == null) {
			ctx.reply("Couldn't read from Steam storefront API.");
			return;
		}

		AksGameInfo aksInfo = null;
		try {
			aksInfo = AksHandler.getGameInfo(gameName);
		} catch (IOException e) {
			Logging.log("Couldn't get AKS info");
			Logging.logException(e);
		}

		reply(ctx, steamApp, aksInfo);
	}

	private void reply(BotContext ctx, SteamApp steamApp, AksGameInfo aksInfo) {
		
		Price price = steamApp.getPrice();
		String currPrice = price.getFinalPrice().toPlainString();
		String discount = "";
		if(price.getDiscountPercent() != 0) {
			discount = "(-" + price.getDiscountPercent() + "%) ";
		}

		String storeUrl = "http://store.steampowered.com/app/" + steamApp.getAppId();

		// Output
		ctx.reply(steamApp.getName() + " is " + currPrice + " € " + discount + "(" + storeUrl + ")");
		if(aksInfo != null) {
			ctx.reply("Russian price: " + aksInfo.price + " (" + aksInfo.url + ")");
		}

		String metacritic = "";
		if(steamApp.getMetacriticScore() != null) {
			metacritic = "Metacritic score: " + steamApp.getMetacriticScore() + " / 100" + " — ";
		}
		ctx.reply(metacritic + cleanDescription(steamApp.getAboutTheGame()));
	}

	private SteamApp getSteamInfo(Entry<String, Integer> gameEntry) {
		try {
			SteamApi steamApi = SteamApiFactory.createSteamApi(STORE_COUNTRY);
			SteamId steamId = SteamId.create(gameEntry.getValue());
			SteamApp steamApp = steamApi.retrieveApp(steamId);
			return steamApp;
		} catch (SteamApiException e) {
			Logging.logException(e);
			return null;
		}

	}

	private static String cleanDescription(String aboutTheGame) {
		return Jsoup.clean(aboutTheGame, Whitelist.none());
	}

	@Override
	public String getName() {
		return "+steam";
	}

	@Override
	public void help(BotContext ctx) {
		ctx.reply("Provides info from steam about game, prices, users, etc.");
	}
}
