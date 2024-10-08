package com.turkerozturk;

import java.sql.*;
import java.util.Map;

public class EventsTable {


    public void createEventTableIfNotExists() throws SQLException {
        Connection conn = DriverManager.getConnection(EventScraper.DB_URL);
        Statement stmt = conn.createStatement();

        String createTableSQL = "CREATE TABLE IF NOT EXISTS events (" +
                "eventId TEXT PRIMARY KEY, " +
                "eventName TEXT" +
                ");";
        stmt.execute(createTableSQL);

        stmt.close();
        conn.close();
    }

    public void fillEventTable(Map<String, String> optionsMap) throws SQLException {

        Connection conn = DriverManager.getConnection(EventScraper.DB_URL);
        String insertSQL = "INSERT INTO events (eventId, eventName) " +
                "VALUES (?, ?)";

        for (String key : optionsMap.keySet()) {
            PreparedStatement pstmt = conn.prepareStatement(insertSQL);
            pstmt.setString(1, key);
            pstmt.setString(2, optionsMap.get(key));
            pstmt.executeUpdate();
            pstmt.close();

        }
        conn.close();

    }


}
