package com.turkerozturk;

import java.sql.*;

public class CategoriesTable {


    public void createCategoriesTableIfNotExists() throws SQLException {
        Connection conn = DriverManager.getConnection(EventScraper.DB_URL);
        Statement stmt = conn.createStatement();

        String createTableSQL = "CREATE TABLE IF NOT EXISTS categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "categoryId TEXT, " +
                "categoryName TEXT, " +
                "fkEventId TEXT" +
                ");";
        stmt.execute(createTableSQL);

        stmt.close();
        conn.close();
    }

    public void addCategoryToTable(Category category) throws SQLException {

        Connection conn = DriverManager.getConnection(EventScraper.DB_URL);
        String insertSQL = "INSERT INTO categories (id, categoryId, categoryName, fkEventId) " +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement pstmt = conn.prepareStatement(insertSQL);
        pstmt.setInt(1, category.getTableId());
        pstmt.setString(2, category.getCategoryId());
        pstmt.setString(3, category.getCategoryName());
        pstmt.setString(4, category.getForeignId());
        pstmt.executeUpdate();
        pstmt.close();

        conn.close();

    }

}
