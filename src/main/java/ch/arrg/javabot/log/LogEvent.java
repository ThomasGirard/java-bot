package ch.arrg.javabot.log;

public enum LogEvent {
	MESSAGE("pubmsg"), //
	ACTION("action"), //
	JOIN("join"), //
	QUIT("quit"), //
	PART("part"), //
	NICK("nick"), //
	TOPIC("topic"), //
	ATTACHMENT("pubmsg");

	final String dbName;

	private LogEvent(String dbName) {
		this.dbName = dbName;
	}
}