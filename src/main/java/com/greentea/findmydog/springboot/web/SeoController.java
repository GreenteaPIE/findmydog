package com.greentea.findmydog.springboot.web;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.greentea.findmydog.springboot.sevice.posts.PostsService;
import com.greentea.findmydog.springboot.web.dto.MapMarkerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/** 검색엔진용 sitemap·robots 와 전단지에 넣는 QR 코드 */
@RestController
@RequiredArgsConstructor
public class SeoController {

    private static final String[] PAGES = {"/", "/posts", "/map", "/adoption", "/guide", "/about"};

    private final PostsService postsService;

    @Value("${site.url:}")
    private String siteUrl;

    /** SITE_URL 이 있으면 그것을, 없으면 지금 요청의 주소를 쓴다 */
    private String baseUrl() {
        String base = siteUrl.isBlank() ? ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() : siteUrl;
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String robots() {
        return """
                User-agent: *
                Disallow: /api/
                Disallow: /my/
                Disallow: /admin
                Disallow: /posts/new
                Sitemap: %s/sitemap.xml
                """.formatted(baseUrl());
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        String base = baseUrl();
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (String page : PAGES) {
            xml.append("  <url><loc>").append(base).append(page).append("</loc></url>\n");
        }
        for (MapMarkerDto post : postsService.findMapMarkers()) {
            xml.append("  <url><loc>").append(base).append("/posts/").append(post.id()).append("</loc></url>\n");
        }
        return xml.append("</urlset>\n").toString();
    }

    // 전단지에 인쇄해도 깨지지 않도록 SVG 로 만든다
    @GetMapping(value = "/posts/{id:\\d+}/qr.svg")
    public ResponseEntity<String> qr(@PathVariable Long id) throws WriterException {
        postsService.findById(id); // 없는 글이면 404
        BitMatrix matrix = new QRCodeWriter().encode(baseUrl() + "/posts/" + id, BarcodeFormat.QR_CODE, 0, 0,
                Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M, EncodeHintType.MARGIN, 2));
        StringBuilder path = new StringBuilder();
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                if (matrix.get(x, y)) path.append('M').append(x).append(' ').append(y).append("h1v1h-1z");
            }
        }
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 %d %d" shape-rendering="crispEdges">\
                <rect width="100%%" height="100%%" fill="#fff"/><path fill="#000" d="%s"/></svg>"""
                .formatted(matrix.getWidth(), matrix.getHeight(), path);
        return ResponseEntity.ok().contentType(MediaType.valueOf("image/svg+xml"))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS)).body(svg);
    }
}
