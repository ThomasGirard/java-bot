package ch.arrg.javabot.log;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.arrg.javabot.Const;
import ch.arrg.javabot.data.BotContext;
import ch.arrg.javabot.util.Logging;

// TODO Database logging as a handler ?

public class DatabaseLogServiceImpl implements DatabaseLogService {
	
	public void logEvent(LogEvent inType, BotContext ctx) {
		try (Connection conn = getConnection()) {
			String query = "INSERT INTO `" + Const.DB_TABLE
					+ "` (channel, name, time, message, type, hidden, source) VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement preparedStatement = conn.prepareStatement(query);
			
			String channel = ctx.channel;
			String name = ctx.sender;
			Timestamp time = new Timestamp(new java.util.Date().getTime());
			String message = ctx.message;
			String type = inType.dbName;
			String hidden = "F";
			String source = "mtt";
			
			preparedStatement.setString(1, channel);
			preparedStatement.setString(2, name);
			preparedStatement.setTimestamp(3, time);
			preparedStatement.setString(4, message);
			preparedStatement.setString(5, type);
			preparedStatement.setString(6, hidden);
			preparedStatement.setString(7, source);
			
			preparedStatement.executeUpdate();
			
		} catch (ClassNotFoundException | SQLException e) {
			Logging.logException(e);
		}
	}
	
	private Connection getConnection() throws ClassNotFoundException, SQLException {
		// Load DB driver
		Connection connect = DriverManager.getConnection(Const.DB_CONN_STRING);
		
		return connect;
	}
	
	public List<LogLine> readAllLog(String channel) {
		List<LogLine> lines = new ArrayList<>();
		
		try (Connection conn = getConnection()) {
			String query = "SELECT * FROM `main` WHERE type = 'pubmsg' "
					+ "AND hidden = 'F' AND channel = ? ORDER BY time ASC";
			PreparedStatement preparedStatement = conn.prepareStatement(query);
			
			preparedStatement.setString(1, channel);
			lines = executeSelect(preparedStatement);
			
		} catch (ClassNotFoundException | SQLException | UnsupportedEncodingException e) {
			Logging.logException(e);
		}
		
		Logging.log("Read " + lines.size() + " lines from database.");
		return lines;
	}
	
	private List<LogLine> executeSelect(PreparedStatement preparedStatement)
			throws SQLException, UnsupportedEncodingException {
		List<LogLine> lines = new ArrayList<>();
		ResultSet rs = preparedStatement.executeQuery();
		
		while(rs.next()) {
			LogLine line = readLine(rs);
			if(line != null) {
				lines.add(line);
			}
		}
		
		return lines;
	}
	
	private LogLine readLine(ResultSet rs) throws SQLException, UnsupportedEncodingException {
		// (channel, name, time, message, type, hidden)
		LogLine out = new LogLine();
		out.id = rs.getInt("id");
		out.user = rs.getString("name");
		out.date = new Date(rs.getTimestamp("time").getTime());
		out.kind = rs.getString("type");
		out.message = rs.getString("message");
		
		return out;
	}
	
	public LogLine lastMessageByUser(String channel, String user) {
		channel = escapeChannel(channel);
		
		List<LogLine> lines = new ArrayList<>();
		
		try (Connection conn = getConnection()) {
			String query = "SELECT * FROM `main` WHERE type = 'pubmsg' "
					+ "AND hidden = 'F' AND channel = ? AND name LIKE ? ORDER BY time DESC LIMIT 1";
			PreparedStatement preparedStatement = conn.prepareStatement(query);
			
			preparedStatement.setString(1, channel);
			preparedStatement.setString(2, "%" + user + "%");
			
			lines = executeSelect(preparedStatement);
			
		} catch (ClassNotFoundException | SQLException | UnsupportedEncodingException e) {
			Logging.logException(e);
		}
		
		if(lines.size() == 1) {
			return lines.get(0);
		} else {
			return null;
		}
	}
	
	public int getNumberOfMessagesSince(String channel, int lastId) {
		channel = escapeChannel(channel);
		
		try (Connection conn = getConnection()) {
			String query = "SELECT COUNT(*) FROM `main` WHERE type = 'pubmsg' "
					+ "AND hidden = 'F' AND channel = ? AND id > ?";
			PreparedStatement preparedStatement = conn.prepareStatement(query);
			
			preparedStatement.setString(1, channel);
			preparedStatement.setInt(2, lastId);
			
			ResultSet rs = preparedStatement.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			
		} catch (ClassNotFoundException | SQLException e) {
			Logging.logException(e);
		}
		
		throw new IllegalStateException();
	}
	
	public List<LogLine> getMessagesSinceId(String channel, int lastId) {
		channel = escapeChannel(channel);
		
		List<LogLine> lines = new ArrayList<>();
		
		try (Connection conn = getConnection()) {
			String query = "SELECT * FROM `main` WHERE type = 'pubmsg' "
					+ "AND hidden = 'F' AND channel = ? AND id > ? ORDER BY id ASC";
			PreparedStatement preparedStatement = conn.prepareStatement(query);
			
			preparedStatement.setString(1, channel);
			preparedStatement.setInt(2, lastId);
			
			lines = executeSelect(preparedStatement);
			
		} catch (ClassNotFoundException | SQLException | UnsupportedEncodingException e) {
			Logging.logException(e);
		}
		
		return lines;
	}
	
	// TODO fix that mess of having # in channel names escaped sometimes
	private String escapeChannel(String channel) {
		if(channel == null)
			return null;
		
		if(channel.equals("##braisnchat"))
			return "#braisnchat";
		return channel;
	}
	
	public LogLine getById(String channel, Integer lineId) {
		channel = escapeChannel(channel);
		
		List<LogLine> lines = new ArrayList<>();
		
		try (Connection conn = getConnection()) {
			String query = "SELECT * FROM `main` WHERE type = 'pubmsg' "
					+ "AND hidden = 'F' AND channel = ? AND id = ?";
			PreparedStatement preparedStatement = conn.prepareStatement(query);
			
			preparedStatement.setString(1, channel);
			preparedStatement.setInt(2, lineId);
			
			lines = executeSelect(preparedStatement);
			
		} catch (ClassNotFoundException | SQLException | UnsupportedEncodingException e) {
			Logging.logException(e);
		}
		
		if(lines.size() == 1) {
			return lines.get(0);
		} else {
			return null;
		}
	}
}
