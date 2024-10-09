package com.turkerozturk;

import com.turkerozturk.Completer.MissingDataCompleter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.turkerozturk.EventScraper.DB_NAME;


public class EventScraperWithSelenium {


    public static String pathToChromeDriver;


    public static void main(String[] args) throws SQLException {

        //tag::selenium[]
        String configFileName = "config.txt";
        //end::selenium[]
        try {
            //tag::selenium[]
            pathToChromeDriver = readChromeDriverPath(configFileName);
            //end::selenium[]
            if (!Files.exists(Paths.get(pathToChromeDriver))) {
                System.err.println(String.format("Hata: '%s' dosyasi bulunamadi.", pathToChromeDriver));
                System.err.println("Lutfen https://developer.chrome.com/docs/chromedriver adresinden ChromeDriver yazılımını edinin.");
                System.err.println(String.format("Edindiyseniz %s dosyasinda belirttiginiz '%s' klasor yolunun chromedriver.exe dosyasini isaret ettiginden emin olun."
                        , configFileName, pathToChromeDriver));
                System.err.println(String.format("Ornek:", configFileName));
                System.err.println("C:\\PROGRAMMING\\SELENIUM\\CHROMEDRIVERS\\129\\chromedriver-win32\\chromedriver.exe");
                return; // Programi durdur

            }

        } catch (NoSuchFileException e) {
            // Dosya bulunamazsa kullanıcıya bilgi ver
            System.err.println(String.format("Hata: '%s' dosyasi bulunamadi.", configFileName));
            System.err.println(String.format("Lutfen 'config.txt' adinda bir dosya olusturun ve icine ChromeDriver yolunu yazin.", configFileName));
            System.err.println(String.format("Ornek:", configFileName));
            System.err.println("C:\\PROGRAMMING\\SELENIUM\\CHROMEDRIVERS\\129\\chromedriver-win32\\chromedriver.exe");
            return; // Programi durdur
        } catch (IOException e) {
            System.err.println("Dosya okunurken bir hata olustu: " + e.getMessage());
            return; // Programi durdur
        }

        if (!Files.exists(Paths.get(DB_NAME))) {
            System.out.println(String.format("%s veritabani ilk kez olusturuluyor ve tum veriler indiriliyor..", DB_NAME));
            System.out.println("Tum verilerin indiginden emin olmak icin programi sonra en az bir kez daha calistiriniz.");
            parseAll();
        } else {
            System.out.println(String.format("Yeni kosularla birlikte tamamen en bastan %s veritabani olusturabilmek icin mevcut veritabani olmadan programi calistirmaniz gerekir.", DB_NAME));
            System.out.println(String.format("Su anda mevcut %s veritabaninda eksik kosu verisi varsa tamamlanacak, fakat yeni kosular varsa indirilmeyecek.", DB_NAME));
            parseMissing();
        }
    }

    private static void parseMissing() throws SQLException {
        MissingDataCompleter completer = new MissingDataCompleter();
        completer.completeMissingData();
    }

    private static void parseAll() throws SQLException {
        EventScraperWithSelenium eventScraperWithSelenium = new EventScraperWithSelenium();
        eventScraperWithSelenium.parse();
    }

    private static String readChromeDriverPath(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath))).trim();
    }


    public void parse() throws SQLException {

        //tag::selenium[]
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

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement optionOfEvent = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//option[@value='" + event.getEventId() + "']")));
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
            CategoriesTable categoriesTable = new CategoriesTable();
            categoriesTable.addCategoryToTable(category);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement optionOfCategory = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//option[@value='" + category.getCategoryId() + "']")));
            optionOfCategory.click();

            // Sayfanin en altina kadar scroll et
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

            // Sayfanin tamamen yuklendiginden emin olmak icin birkac saniye bekliyoruz
            // wait.until(ExpectedConditions.presenceOfElementLocated(By.id("TODO BURAYA mumkunse sayfanin tamamen yuklendiginden emin olunabilecek bir ELEMENT IDsi gelecek")));
            // VEYA
            try {
                Thread.sleep(3000); // 3 saniye bekler
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // TODO bir baska yontem, varsa sayfayi sira noya gore yuksekten alcaga siralamak, boylece en yuksek satir noyu elde etmek.

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


