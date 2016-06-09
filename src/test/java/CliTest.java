import java.util.Scanner;

import ch.arrg.javabot.Bot;
import ch.arrg.javabot.CommandHandler;
import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.data.UserData;
import ch.arrg.javabot.data.UserDb;
import ch.arrg.javabot.handlers.YoutubeHandler;

public class CliTest {
	public static void main(String[] args) {
		
		Bot bot = new FakeBot();
		
		CommandHandler ch = new YoutubeHandler();
		try (Scanner s = new Scanner(System.in)) {
			while(s.hasNextLine()) {
				String line = s.nextLine();
				if(line.equals("")) {
					break;
				}
				
				BotContext ctx = new BotContext(bot, "##braisnchat", "arrg_ch", "arrg",
						"hostname", line);
				ch.handle(ctx);
			}
		}
	}
	
	static class FakeBot implements Bot {
		UserDb db = new UserDb();
		
		@Override
		public void sendMsg(String target, String message) {
			System.out.println("> " + message);
		}
		
		@Override
		public UserData getUserData(String user) {
			return db.getOrCreateUserData(user);
		}
		
		@Override
		public void quit() {
			System.out.println("Quit");
		}
		
		@Override
		public void adminPause() {
			// TODO generated Bot.adminPause
		}
		
		@Override
		public void adminUnpause() {
			// TODO generated Bot.adminUnpause
		}
		
		@Override
		public boolean isPaused() {
			return false; // TODO generated Bot.isPaused
		}
	}
}
