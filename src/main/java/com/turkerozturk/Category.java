package com.turkerozturk;

public class Category {

    private int tableId;

    private String categoryId;

    private String categoryName;

    private String foreignId;

    public Category(int tableId, String categoryId, String categoryName, String foreignId) {
        this.tableId = tableId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.foreignId = foreignId; // events.id
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getForeignId() {
        return foreignId;
    }

    public void setForeignId(String foreignId) {
        this.foreignId = foreignId;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }
}
