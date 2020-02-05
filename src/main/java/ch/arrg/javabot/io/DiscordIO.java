package ch.arrg.javabot.io;

import java.util.List;

import javax.security.auth.login.LoginException;

import ch.arrg.javabot.Const;
import ch.arrg.javabot.DiscordUtil;
import ch.arrg.javabot.io.event.AttachmentReceivedEvent;
import ch.arrg.javabot.io.event.BotIOEvent;
import ch.arrg.javabot.io.event.UserJoinEvent;
import ch.arrg.javabot.util.Logging;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/** Main bot logic
 *
 * @author tgi */
public class DiscordIO implements BotIO {
	
	private JDA discord;
	
	private BotIOEventListener listener;
	
	public DiscordIO() {
	}
	
	public void start(BotIOEventListener listener) {
		this.listener = listener;
		
		try {
			discord = new JDABuilder()
					.setToken(Const.str("discord.token"))
					.addEventListeners(new DiscordEventListener())
					.build();
		} catch (LoginException e) {
			Logging.logException(e);
		}
	}
	
	@Override
	public void sendMessage(String target, String message) {
		MessageChannel channel = DiscordUtil.decodeChannel(discord, target);
		if(channel != null) {
			channel.sendMessage(message).queue();
		} else {
			Logging.log("DiscordIO.sendMessage : No channel for target " + target + ".");
		}
	}
	
	@Override
	public void quit() {
		discord.shutdown();
	}
	
	private class DiscordEventListener extends ListenerAdapter {
		
		@Override
		public void onMessageReceived(MessageReceivedEvent event) {
			String channel = DiscordUtil.encodeChannel(event.getChannel());
			String sender = event.getAuthor().getName();
			String message = event.getMessage().getContentDisplay();
			
			BotIOEvent ioEvent = new ch.arrg.javabot.io.event.MessageReceivedEvent(DiscordIO.this, channel, sender,
					message);
			listener.onEvent(ioEvent);

			// Handle attachments
			List<Attachment> attachments = event.getMessage().getAttachments();
			if(!attachments.isEmpty()) {
				for(Attachment attachment : attachments) {
					AttachmentReceivedEvent arEvent = new AttachmentReceivedEvent(DiscordIO.this, channel, sender,
							attachment.getUrl());
					listener.onEvent(arEvent);
				}
			}

		}
		
		@Override
		public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
			if(event.getNewOnlineStatus() == OnlineStatus.ONLINE) {
				// TODO channel doesn't make sense here
				String channel = "";
				String sender = event.getMember().getEffectiveName();
				
				UserJoinEvent ioEvent = new UserJoinEvent(DiscordIO.this, channel, sender);
				listener.onEvent(ioEvent);
			}
		}
		
		// TODO handle more events
		
	}
	
}
