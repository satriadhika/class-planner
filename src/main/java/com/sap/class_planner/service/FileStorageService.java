package com.sap.class_planner.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Penyimpanan file di disk server. Nama file asli tidak dipakai di path (anti path traversal);
 * DB hanya menyimpan storage key. Ganti implementasi ini kalau nanti pindah ke S3/MinIO.
 */
@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${app.storage.dir:./storage}") String dir) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
    }

    /** Simpan file, kembalikan storage key, contoh: questionnaire/12/3f2a...e1.pdf */
    public String store(String folder, String extension, byte[] content) {
        String key = folder + "/" + UUID.randomUUID() + "." + extension;
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal menyimpan file", e);
        }
        return key;
    }

    public Resource load(String key) {
        return new FileSystemResource(resolve(key));
    }

    public void deleteQuietly(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ignored) {
            // file yatim di disk tidak mengganggu data; bisa dibersihkan terjadwal
        }
    }

    private Path resolve(String key) {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Storage key tidak valid");
        }
        return path;
    }
}
