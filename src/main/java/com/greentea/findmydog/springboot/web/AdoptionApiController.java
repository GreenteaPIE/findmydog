package com.greentea.findmydog.springboot.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.greentea.findmydog.springboot.sevice.adoption.AdoptionQuery;
import com.greentea.findmydog.springboot.sevice.adoption.AdoptionResult;
import com.greentea.findmydog.springboot.sevice.adoption.AdoptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AdoptionApiController {

    private final AdoptionService adoptionService;

    @GetMapping("/api/adoption")
    public ResponseEntity<?> search(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                    @RequestParam(required = false) String sido,
                                    @RequestParam(required = false) String sigungu,
                                    @RequestParam(required = false) String breed,
                                    @RequestParam(defaultValue = "1") int page) {
        try {
            AdoptionResult result = adoptionService.search(new AdoptionQuery(from, to, sido, sigungu, breed, page));
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.warn("입양 공고 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", "입양 공고를 불러오지 못했어요. 잠시 뒤 다시 시도해 주세요"));
        }
    }

    @GetMapping("/api/adoption/codes")
    public ResponseEntity<JsonNode> codes() {
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic()).body(adoptionService.codes());
    }
}
