package com.turkerozturk.Completer;

import com.turkerozturk.EventScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.sql.*;
import java.util.*;

import static com.turkerozturk.EventScraper.DB_URL;
import static com.turkerozturk.EventScraperWithSelenium.pathToChromeDriver;


public class MissingDataCompleter {


    public Map<Integer, Ids> getAllIdsInCategoryTable() throws SQLException {

        Connection conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();

        Map<Integer, Ids> idsInCategoryTable = new HashMap<>();


        String query = "select id, categoryId, fkEventId from categories";


        PreparedStatement stmt1 = conn.prepareStatement(query);
        ResultSet rs1 = stmt1.executeQuery();

        while (rs1.next()) {
            //  idsInCategoryTable.add(rs1.getString("ageAndGender"));
            Ids ids = new Ids();
            ids.setId(rs1.getInt("id"));
            ids.setCategoryId(rs1.getString("categoryId"));
            ids.setEventId(rs1.getString("fkEventId"));

            idsInCategoryTable.put(rs1.getInt("id"), ids);


        }
        stmt.execute(query);
        stmt.close();
        conn.close();

        return idsInCategoryTable;

    }

    public Map<Integer, Ids> getMissingIdNumbers(Map<Integer, Ids> allIdNumbers) throws SQLException {

        Connection conn = DriverManager.getConnection(DB_URL);

        String query = "select distinct(eventId) from results";

        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();

        Map<Integer, Ids> missingIdNumbers = new HashMap<>();


        Set<Integer> resultsIdSet = new HashSet<>();
        while (rs.next()) {
            resultsIdSet.add(rs.getInt("eventId"));
        }


        for (int i : allIdNumbers.keySet()) {
            if (!resultsIdSet.contains(i)) {
                missingIdNumbers.put(i, allIdNumbers.get(i));
            }
        }


        stmt.close();
        conn.close();

        return missingIdNumbers;
    }

    public Document parse(String eventId, String categoryId) {
        //tag::selenium[]
        System.setProperty("webdriver.chrome.driver", pathToChromeDriver);
        WebDriver driver = new ChromeDriver();
        driver.get("https://event.spor.istanbul/eventresults.aspx");
        //end::selenium[]

        WebElement optionOfEvent = driver.findElement(By.xpath("//option[@value='" + eventId + "']"));
        optionOfEvent.click();
        WebElement optionOfCategory = driver.findElement(By.xpath("//option[@value='" + categoryId + "']"));
        optionOfCategory.click();

        String pageSource = driver.getPageSource();

        //tag::jsoup[]
        assert pageSource != null;
        Document doc = Jsoup.parse(pageSource);
        //end::jsoup[]

        return doc;

    }


    public void completeMissingData() throws SQLException {
        Map<Integer, Ids> allIdNumbers = getAllIdsInCategoryTable();
        Map<Integer, Ids> missingIdNumbers = getMissingIdNumbers(allIdNumbers);

        for (
                int idNumber : missingIdNumbers.keySet()) {

            System.out.printf("ID no %s kategorisi olmasina ragmen kosu datasi bulunamadi.\n", idNumber);

            Ids ids = missingIdNumbers.get(idNumber);


            String eventId = ids.getEventId();
            String categoryId = ids.getCategoryId();

            Document doc = parse(eventId, categoryId);

            EventScraper scraper = new EventScraper();
            scraper.scrapeDataAndStoreInSQLite(idNumber, doc);

            System.out.println(String.format("ID no %s parsellendi. (categoryId:%s , eventId: %s)", idNumber, categoryId, eventId));
        }

    }



}
