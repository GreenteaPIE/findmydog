package com.greentea.findmydog.springboot.sevice.posts;

import com.greentea.findmydog.springboot.web.dto.AnimalDetailData;
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

    public List<AnimalDetailData> scrapeAnimalData(String searchSDate, String searchEDate, String searchUprCd, String searchOrgCd, String searchKindCd) {
        String url = "https://www.animal.go.kr/front/awtis/protection/protectionList.do?"
                + "csSignature=tvleUQetGVcNSX%2BxTmAv6Q%3D%3D"
                + "&boardId="
                + "&page=1"
                + "&pageSize=100"
                + "&desertionNo="
                + "&menuNo=1000000060"
                + "&searchSDate=" + searchSDate
                + "&searchEDate=" + searchEDate
                + "&searchUprCd=" + searchUprCd
                + "&searchOrgCd=" + searchOrgCd
                + "&searchCareRegNo="
                + "&searchUpKindCd=417000"
                + "&searchKindCd=" + searchKindCd
                + "&searchSexCd="
                + "&searchRfid=";

        WebDriver driver = new ChromeDriver(options);
        System.out.println(url);
        driver.get(url);

        List<AnimalDetailData> animalDetailDataList = new ArrayList<>();
        List<WebElement> elements = driver.findElements(By.cssSelector("li"));

        for (WebElement element : elements) {
            try {
                String onclickValue = element.findElement(By.tagName("a")).getAttribute("onclick");
                String desertionNo = onclickValue.split("'")[1];

                AnimalDetailData animalDetailData = scrapeAnimalDetailData(desertionNo);
                animalDetailDataList.add(animalDetailData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        driver.quit();
        return animalDetailDataList;
    }

    private AnimalDetailData scrapeAnimalDetailData(String desertionNo) {
        String detailUrl = "https://www.animal.go.kr/front/awtis/protection/protectionDtl.do?desertionNo=" + desertionNo;
        WebDriver driver = new ChromeDriver(options);
        System.out.println(detailUrl);
        driver.get(detailUrl);

        try {
            String imageUrl = driver.findElement(By.cssSelector(".photoArea")).getAttribute("src");
            String noticeNo = driver.findElement(By.xpath("//th[text()='공고번호']/following-sibling::td")).getText();
            String breed = driver.findElement(By.xpath("//th[text()='품종']/following-sibling::td")).getText();
            String color = driver.findElement(By.xpath("//th[text()='색상']/following-sibling::td")).getText();
            String sex = driver.findElement(By.xpath("//th[text()='성별']/following-sibling::td")).getText();
            String neuterStatus = driver.findElement(By.xpath("//th[text()='중성화']/following-sibling::td")).getText();
            String foundLocation = driver.findElement(By.xpath("//th[text()='발생장소']/following-sibling::td")).getText();
            String receivedDate = driver.findElement(By.xpath("//th[text()='접수일시']/following-sibling::td")).getText();
            //String ageWeight = driver.findElement(By.xpath("//th[text()='나이/체중']/following-sibling::td")).getText();
            String jurisdiction = driver.findElement(By.xpath("//th[text()='관할기관']/following-sibling::td")).getText();
            String shelterName = driver.findElement(By.xpath("//th[text()='보호센터']/following-sibling::td")).getText();
            //String shelterContact = driver.findElement(By.xpath("//th[text()='보호센터연락처']/following-sibling::td/a")).getText();
            String shelterAddress = driver.findElement(By.xpath("//th[text()='보호장소']/following-sibling::td")).getText();

            return new AnimalDetailData(imageUrl, noticeNo, breed, color, sex, neuterStatus, foundLocation, receivedDate, jurisdiction, shelterName, shelterAddress);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return null;
    }
}