package ch.arrg.javabot.log;

import java.util.ArrayList;
import java.util.List;

import ch.arrg.javabot.data.BotContext;

public class DatabaseLogServiceDummy implements DatabaseLogService {

	@Override
	public void logEvent(LogEvent inType, BotContext ctx) {

	}

	@Override
	public List<LogLine> readAllLog(String channel) {
		return new ArrayList<LogLine>();
	}

	@Override
	public LogLine lastMessageByUser(String channel, String user) {
		return null;
	}

	@Override
	public int getNumberOfMessagesSince(String channel, int lastId) {
		return 0;
	}

	@Override
	public List<LogLine> getMessagesSinceId(String channel, int lastId) {
		return new ArrayList<LogLine>();
	}

	@Override
	public LogLine getById(String channel, Integer lineId) {
		return null;
	}
	
}
