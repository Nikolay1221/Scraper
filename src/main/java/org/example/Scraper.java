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
        int numPagesToParse = 2;
        System.setProperty("webdriver.chrome.driver", "/home/nilk/chromedriver-linux64/chromedriver");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        WebDriver driver = new ChromeDriver(chromeOptions);

        String connectionString = "mongodb://localhost:27017";
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

        parseMultiplePages(driver, numPagesToParse, database, collectionName);
        database.close();

        driver.quit();
    }

    public static void parseMultiplePages(WebDriver driver, int numPagesToParse, MongoDBDatabase database, String collectionName) {
        for (int page = 1; page <= numPagesToParse; page++) {
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

//                System.out.println("Title: " + title);
//                System.out.println("Description: " + description);
//                System.out.println("Price: " + price);
//                System.out.println("URL: " + "https://avito.ru/" + adUrl);
//                System.out.println("--------------------------------------------------");

                try {
                    Thread.sleep(100); // Здесь 200 миллисекунд (0.2 секунды)
                    recordCount++;
                    if (recordCount >= 100000) {
                        int tableIndex = recordCount / 200;
                        collectionName = "smartphone_pricing_all_" + tableIndex;
                        recordCount = 0;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}