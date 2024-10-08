package com.turkerozturk;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EventScraperWithSelenium {

    public static void main(String[] args) throws SQLException {
        EventScraperWithSelenium eventScraperWithSelenium = new EventScraperWithSelenium();
        eventScraperWithSelenium.parse();
    }

    public void parse() throws SQLException {

        //tag::selenium[]
        String pathToChromeDriver = "C:\\PROGRAMMING\\SELENIUM\\CHROMEDRIVERS\\129\\chromedriver-win32\\chromedriver.exe";
        System.setProperty("webdriver.chrome.driver", pathToChromeDriver);
        WebDriver driver = new ChromeDriver();
        driver.get("https://event.spor.istanbul/eventresults.aspx");
        //end::selenium[]

        //tag::selectionIds[]
        String ddlEvents = "ddlEvents";
        String ddlCategory = "ddlCategory";
        //end::selectionIds[]


        final Map<String, String> eventsMap = getStringStringMap(driver, ddlEvents);
        EventsTable eventsTable = new EventsTable();
        eventsTable.createEventTableIfNotExists();
        eventsTable.fillEventTable(eventsMap);
        CategoriesTable categoriesTable = new CategoriesTable();
        categoriesTable.createCategoriesTableIfNotExists();


        for (String keyOfEvent : eventsMap.keySet()) {
            Event event = new Event(keyOfEvent, eventsMap.get(keyOfEvent));
            System.out.println(event.getEventName() + ",\t" + event.getEventId());
            WebElement optionOfEvent = driver.findElement(By.xpath("//option[@value='" + event.getEventId() + "']"));
            optionOfEvent.click();

            final Map<String, String> categoriesMap = getStringStringMap(driver, ddlCategory);
            extracted(event, categoriesMap, driver);
        }

        driver.quit();
    }

    private int tableId = 0;


    private void extracted(Event event, Map<String, String> categoriesMap, WebDriver driver) throws SQLException {

        for (String keyOfCategory : categoriesMap.keySet()) {

            String nameOfCategory = categoriesMap.get(keyOfCategory);
            tableId++;
            Category category = new Category(tableId, keyOfCategory, nameOfCategory, event.getEventId());
            System.out.println(nameOfCategory + ",\t" + category.getCategoryId());
            WebElement optionOfCategory = driver.findElement(By.xpath("//option[@value='" + category.getCategoryId() + "']"));

            CategoriesTable categoriesTable = new CategoriesTable();
            categoriesTable.addCategoryToTable(category);

            optionOfCategory.click();

            String pageSource = driver.getPageSource();

            //tag::jsoup[]
            assert pageSource != null;
            Document doc = Jsoup.parse(pageSource);
            //end::jsoup[]

            EventScraper scraper = new EventScraper();
            scraper.scrapeDataAndStoreInSQLite(tableId, doc);


            // Burada gerekirse belirli bir sure beklemek için Thread.sleep() kullanilabilir.

        }

    }

    private static Map<String, String> getStringStringMap(WebDriver driver, String elementName) {
        Map<String, String> map = new HashMap<>();
        WebElement webElementSelect = driver.findElement(By.id(elementName));
        List<WebElement> optionsEvent = webElementSelect.findElements(By.tagName("option"));
        for (WebElement webElementOption : optionsEvent) {
            map.put(webElementOption.getAttribute("value"), webElementOption.getText());
        }
        return map;
    }

}


