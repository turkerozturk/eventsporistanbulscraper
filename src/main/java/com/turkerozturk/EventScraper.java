package com.turkerozturk;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.*;

public class EventScraper {

    //tag::sqlite[]
    public static final String DB_URL = "jdbc:sqlite:runningResults.sqlite";
    //end::sqlite[]

    public void scrapeDataAndStoreInSQLite(int id, Document doc) {
        try {

            Elements rows = doc.select("table.TableResult tr.TableResultRow");
            System.out.println(rows.size());

            createTableIfNotExists();

            for (Element row : rows) {
                Elements cols = row.select("td");

                String ranking = cols.get(0).text();
                String bibNumber = cols.get(1).text();
                String fullName = cols.get(2).text();
                String country = cols.get(3).text();
                String city = cols.get(4).text();
                String district = cols.get(5).text();
                String ageAndGender = cols.get(6).text();
                String timeAsHHMMSS = cols.get(7).text();

                int timeAsSeconds = convertTimeToSeconds(timeAsHHMMSS);

                saveToSQLite(ranking, bibNumber, fullName, country, city, district, ageAndGender, timeAsHHMMSS, timeAsSeconds, id);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createTableIfNotExists() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        String createTableSQL = "CREATE TABLE IF NOT EXISTS results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ranking TEXT, " +
                "bibNumber TEXT, " +
                "fullName TEXT, " +
                "country TEXT, " +
                "city TEXT, " +
                "district TEXT, " +
                "ageAndGender TEXT, " +
                "timeAsHHMMSS TEXT, " +
                "timeAsSeconds INTEGER, " +
                "eventId INTEGER" +
                ");";
        stmt.execute(createTableSQL);
        stmt.close();
        conn.close();
    }

    private void saveToSQLite(String ranking, String bibNumber, String fullName, String country, String city,
                              String district, String ageAndGender, String timeAsHHMMSS, int timeAsSeconds, int eventId) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        String insertSQL = "INSERT INTO results (ranking, bibNumber, fullName, country, city, district, ageAndGender, timeAsHHMMSS, timeAsSeconds, eventId) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(insertSQL);
        pstmt.setString(1, ranking);
        pstmt.setString(2, bibNumber);
        pstmt.setString(3, fullName);
        pstmt.setString(4, country);
        pstmt.setString(5, city);
        pstmt.setString(6, district);
        pstmt.setString(7, ageAndGender);
        pstmt.setString(8, timeAsHHMMSS);
        pstmt.setInt(9, timeAsSeconds);
        pstmt.setInt(10, eventId);

        pstmt.executeUpdate();
        pstmt.close();
        conn.close();
    }

    private int convertTimeToSeconds(String timeAsHHMMSS) {
        String[] parts = timeAsHHMMSS.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }

}
