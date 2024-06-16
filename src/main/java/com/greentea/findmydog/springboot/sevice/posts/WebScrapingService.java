package com.greentea.findmydog.springboot.sevice.posts;

import com.greentea.findmydog.springboot.web.dto.AnimalDetailData;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class WebScrapingService {

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

        List<AnimalDetailData> animalDetailDataList = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("li");

            for (Element element : elements) {
                try {
                    String onclickValue = element.select("a").attr("onclick");
                    String[] parts = onclickValue.split("'");
                    if (parts.length > 1) {
                        String desertionNo = parts[1];
                        AnimalDetailData animalDetailData = scrapeAnimalDetailData(desertionNo);
                        if (animalDetailData != null) {
                            animalDetailDataList.add(animalDetailData);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return animalDetailDataList;
    }

    private AnimalDetailData scrapeAnimalDetailData(String desertionNo) {
        String detailUrl = "https://www.animal.go.kr/front/awtis/protection/protectionDtl.do?desertionNo=" + desertionNo;

        try {
            Document doc = Jsoup.connect(detailUrl).get();
            String imageUrl = doc.select("ul.thum-list img.photoArea").attr("src");
            imageUrl = "https://www.animal.go.kr" + imageUrl;
            String noticeNo = doc.select("th:contains(공고번호) + td").text();
            String breed = doc.select("th:contains(품종) + td").text();
            String color = doc.select("th:contains(색상) + td").text();
            String sex = doc.select("th:contains(성별) + td").text();
            String neuterStatus = doc.select("th:contains(중성화) + td").text();
            String foundLocation = doc.select("th:contains(발생장소) + td").text();
            String receivedDate = doc.select("th:contains(접수일시) + td").text();
            String jurisdiction = doc.select("th:contains(관할기관) + td").text();
            String shelterName = doc.select("th:contains(보호센터) + td").text();
            String shelterAddress = doc.select("th:contains(보호장소) + td").text();
            String page = doc.select("th:contains(나이/체중) + td").text();

            return new AnimalDetailData(detailUrl, imageUrl, noticeNo, breed, color, sex, neuterStatus, foundLocation, receivedDate, jurisdiction, shelterName, shelterAddress, page);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
