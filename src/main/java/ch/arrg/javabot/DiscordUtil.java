package ch.arrg.javabot;

import ch.arrg.javabot.util.Logging;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;

public class DiscordUtil {
	public static String encodeChannel(MessageChannel channel) {
		return channel.getId();
	}
	
	public static MessageChannel decodeChannel(JDA jda, String encoded) {
		MessageChannel priv = jda.getPrivateChannelById(encoded);
		if(priv != null) {
			return priv;
		}

		MessageChannel text = jda.getTextChannelById(encoded);
		if(text != null) {
			return text;
		}

		Logging.log("DiscordUtil.decodeChannel : channel not found for encoded: " + encoded);
		return null;
	}
}
