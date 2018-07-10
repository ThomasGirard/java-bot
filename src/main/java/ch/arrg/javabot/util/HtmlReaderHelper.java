package ch.arrg.javabot.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HtmlReaderHelper {

	public static BufferedReader openUrlForRead(String urlS) {
		HttpURLConnection conn;
		try {
			URL url = new URL(urlS);
			conn = (HttpURLConnection) url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			return in;
		} catch (IOException e) {
			Logging.logException(e);
			return null;
		}

	}

	public static String readTitle(String urlS) {
		try {
			Document document = Jsoup.connect(urlS).get();
			Elements elems = document.select("title");
			if(elems.size() > 0) {
				return elems.get(0).text();
			}
		} catch (HttpStatusException e) {
			Logging.log("Got HTTP status code " + e.getStatusCode() + " for URL " + urlS);
		} catch (IOException e) {
			Logging.logException(e);
		}
		
		return null;
	}
}
