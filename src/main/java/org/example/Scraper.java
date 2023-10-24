package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;


public class Scraper {
    private static int recordCount = 0;

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "chromedriver-linux64/chromedriver");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        WebDriver driver = new ChromeDriver(chromeOptions);

        String connectionString = "mongodb://10.128.0.29:27017";
        String dbName = "avito";

        MongoDBDatabase database = new MongoDBDatabase(connectionString, dbName);

        String condition = "all";

        String collectionName;
        if ("all".equals(condition)) {
            collectionName = "avito_collection";
        } else if ("chita".equals(condition)) {
            collectionName = "smartphone_pricing_chgita";
        } else {
            collectionName = "smartphone_pricing_default";
        }

        int numPagesToParse = 100;
        int currentPage = 1;

        while (true) {
            parseSinglePage(driver, currentPage, database, collectionName);

            // Increase current page
            currentPage++;

            // Check if we've reached the end and need to start over
            if (currentPage > numPagesToParse) {
                currentPage = 1;
            }
        }
    }

    public static void parseSinglePage(WebDriver driver, int page, MongoDBDatabase database, String collectionName) {
        // Формируем URL для каждой страницы
        String url;

        // В зависимости от collectionName, выбираем URL для парсинга
        if ("avito_collection".equals(collectionName)) {
            url = "https://www.avito.ru/all/telefony/mobile-ASgBAgICAUSwwQ2I_Dc?p=" + page + "&s=104";
        } else if ("smartphone_pricing_chita".equals(collectionName)) {
            url = "https://www.avito.ru/chita/telefony/mobile-ASgBAgICAUSwwQ2I_Dc?cd=1&p=" + page + "&s=104";
        } else {
            // Дополнительные условия, если необходимо
            url = "https://www.avito.ru/default-url"; // Замените на URL по умолчанию
        }

        // Откройте сайт Avito.ru для текущей страницы
        driver.get(url);

        // Удалите cookies
        driver.manage().deleteAllCookies();

        // Получите содержимое страницы
        String pageSource = driver.getPageSource();

        // Парсинг данных с использованием Jsoup
        Document document = Jsoup.parse(pageSource);
        Elements itemElements = document.select("div[data-marker=item]");

        for (Element itemElement : itemElements) {
            String description = itemElement.select("meta[itemprop=description]").attr("content");
            String title = itemElement.select("h3[itemprop=name]").text();
            String price = itemElement.select("meta[itemprop=price]").attr("content");
            String adUrl = itemElement.select("a[itemprop=url]").attr("href");

            // Создайте объект Ad
            Ad ad = new Ad(title, description, Integer.parseInt(price), "https://avito.ru/" + adUrl);

            // Добавьте объявление в базу данных с учетом collectionName
            database.insertAd(ad, collectionName);

            try {
                Thread.sleep(10000); // Здесь 10000 миллисекунд (10 секунд)
                recordCount++;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
