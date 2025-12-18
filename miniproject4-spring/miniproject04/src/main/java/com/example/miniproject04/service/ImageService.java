package com.example.miniproject04.service;

import com.example.miniproject04.Entity.Book;
import com.example.miniproject04.Entity.GeneratedImage;
import com.example.miniproject04.repository.BookRepository;
import com.example.miniproject04.repository.GeneratedImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final GeneratedImageRepository imageRepository;
    private final BookRepository bookRepository;

    // ✅ application.yml에서 주입
    @Value("${image.upload-dir}")
    private String imageSaveDir;

    /**
     * =======================================================
     * 1. 이미지 등록
     * =======================================================
     */
    @Transactional
    public String createImage(String tempUrl, Long bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("책을 찾을 수 없습니다."));

        String imageUrl = downloadImageToLocal(tempUrl, bookId);

        GeneratedImage img = new GeneratedImage();
        img.setBook(book);
        img.setImageUrl(imageUrl);

        imageRepository.save(img);

        return imageUrl;
    }

    /**
     * =======================================================
     * 2. 이미지 조회
     * =======================================================
     */
    @Transactional(readOnly = true)
    public GeneratedImage getImage(Long bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("삭제된 목록입니다."));

        GeneratedImage img = imageRepository.findByBook(book);

        if (img == null) {
            throw new IllegalArgumentException("이미지가 없습니다.");
        }

        return img;
    }

    /**
     * =======================================================
     * 3. 이미지 수정
     * =======================================================
     */
    @Transactional
    public String updateImage(Long bookId, String tempUrl, Long userId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("삭제된 목록입니다."));

        if (!book.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한 없음");
        }

        GeneratedImage img = imageRepository.findByBook(book);

        if (img == null) {
            throw new IllegalArgumentException("이미지가 없습니다.");
        }

        String newImageUrl = downloadImageToLocal(tempUrl, bookId);

        img.setImageUrl(newImageUrl);
        imageRepository.save(img);

        return newImageUrl;
    }

    /**
     * =======================================================
     * tempUrl → 로컬 저장
     * =======================================================
     */
    private String downloadImageToLocal(String tempUrl, Long bookId) {

        try (InputStream in = new URL(tempUrl).openStream()) {

            Path saveDir = Paths.get(imageSaveDir);
            Files.createDirectories(saveDir);

            String fileName = "book_" + bookId + "_" + UUID.randomUUID() + ".png";
            Path destination = saveDir.resolve(fileName);

            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);

            // ⭐ DB에는 상대경로만
            return "/images/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("이미지 다운로드 실패", e);
        }
    }

    /**
     * =======================================================
     * 이미지 삭제
     * =======================================================
     */
    @Transactional
    public void deleteImageByBookId(Long bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("삭제된 목록입니다."));

        GeneratedImage img = imageRepository.findByBook(book);
        if (img == null) return;

        deleteLocalFile(img.getImageUrl());
        imageRepository.delete(img);
    }

    private void deleteLocalFile(String imageUrl) {
        try {
            String fileName = imageUrl.replace("/images/", "");
            Path filePath = Paths.get(imageSaveDir, fileName);
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            System.out.println("이미지 파일 삭제 실패: " + e.getMessage());
        }
    }
}
