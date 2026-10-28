package com.greentea.findmydog.springboot.storage;

import com.greentea.findmydog.springboot.exception.InvalidImageException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 올린 사진을 긴 변 1280px 이하의 JPEG 로 다시 저장한다.
 * 다시 그리기 때문에 사진 속 위치 정보(EXIF) 같은 메타데이터도 함께 지워진다.
 */
@Component
public class ImageProcessor {

    static final int MAX_SIDE = 1280;
    private static final float QUALITY = 0.85f;

    public byte[] toJpeg(MultipartFile file) {
        if (ImageNames.extensionOf(file.getOriginalFilename()).isEmpty()) {
            throw new InvalidImageException("사진은 JPG, PNG 파일만 올릴 수 있어요");
        }
        BufferedImage source;
        try (InputStream in = file.getInputStream()) {
            source = ImageIO.read(in);
        } catch (IOException e) {
            throw new InvalidImageException("사진을 읽을 수 없어요: " + file.getOriginalFilename());
        }
        if (source == null) {
            throw new InvalidImageException("사진 파일이 아니에요: " + file.getOriginalFilename());
        }
        return encode(resize(source));
    }

    private BufferedImage resize(BufferedImage source) {
        double scale = Math.min(1.0, (double) MAX_SIDE / Math.max(source.getWidth(), source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));

        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = target.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(Color.WHITE); // 투명 PNG 는 흰 바탕으로
        g.fillRect(0, 0, width, height);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return target;
    }

    private byte[] encode(BufferedImage image) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(QUALITY);
            writer.write(null, new IIOImage(image, null, null), param);
        } catch (IOException e) {
            throw new IllegalStateException("사진을 저장하지 못했어요", e);
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }
}
