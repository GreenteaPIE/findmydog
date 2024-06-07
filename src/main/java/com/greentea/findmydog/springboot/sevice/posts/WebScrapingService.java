package com.greentea.findmydog.springboot.sevice.posts;

import com.greentea.findmydog.springboot.web.dto.AnimalData;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WebScrapingService {

    private final ChromeOptions options;

    public WebScrapingService() {
        this.options = new ChromeOptions();
        this.options.addArguments("--headless");
        System.setProperty("webdriver.chrome.driver", "C:\\Download\\chromedriver-win32\\chromedriver.exe");
    }

    public List<AnimalData> scrapeAnimalData() {
        WebDriver driver = new ChromeDriver(options);
        driver.get("https://www.animal.go.kr/front/awtis/protection/protectionList.do?csSignature=tvleUQetGVcNSX%2BxTmAv6Q%3D%3D&boardId=&page=1&pageSize=0&desertionNo=&menuNo=1000000060&searchSDate=2024-05-01&searchEDate=2024-05-31&searchUprCd=6110000&searchOrgCd=3240000&searchCareRegNo=&searchUpKindCd=417000&searchKindCd=&searchSexCd=&searchRfid=");

        List<AnimalData> animalDataList = new ArrayList<>();
        List<WebElement> elements = driver.findElements(By.cssSelector("li"));

        for (WebElement element : elements) {
            try {
                String imageUrl = element.findElement(By.cssSelector(".thum-img img")).getAttribute("src");
                String breed = element.findElement(By.cssSelector(".subject")).getText();
                String id = element.findElements(By.cssSelector(".info-item .value")).get(0).getText();
                String sex = element.findElements(By.cssSelector(".info-item .value")).get(1).getText();
                String location = element.findElements(By.cssSelector(".info-item .value")).get(2).getText();
                String characteristics = element.findElements(By.cssSelector(".info-item .value")).get(3).getText();

                AnimalData animalData = new AnimalData(imageUrl, breed, id, sex, location, characteristics);
                animalDataList.add(animalData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        driver.quit();
        return animalDataList;
    }
}