package org.tuiken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.tuiken.model.DownloadBatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Slf4j
public class Main {
    private static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) {

        if (args.length != 1) {
            log.error("Usage: java -jar parallel-downloader.jar <path>");
            return;
        }

        DownloadBatch batch = loadBatchFromFile(args[0]).orElse(null);
        if (batch == null) return;

        File folder = new File(batch.getTargetPath());
        if (!folder.exists() || !folder.isDirectory()) {
            log.error("Target folder does not exist or is not a directory");
            return;
        }

        long startTime = System.currentTimeMillis();
        try (ExecutorService executor = Executors.newFixedThreadPool(batch.getMaxConcurrent())) {
            final String targetPath = batch.getTargetPath();
            final int timeout = batch.getMaxDuration();
            for (String url : batch.getUrls()) {
                executor.submit(() -> downloadFile(url, targetPath, timeout));
            }
            executor.shutdown();
        } catch (Exception e) {
            log.error("Executor error: " + e.getMessage());
            return;
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info(String.format("Downloaded %s of %s files. Total time in milliseconds: %s",
                countFiles(batch.getTargetPath()), batch.getUrls().size(), elapsedTime));
    }

    private static int countFiles(String path) {
        try (Stream<Path> stream = Files.walk(Path.of(path))) {
            return (int) stream
                    .filter(Files::isRegularFile)
                    .count();
        } catch (IOException e) {
            return 0;
        }
    }

    private static Optional<DownloadBatch> loadBatchFromFile(String input_path) {
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(input_path)));
        } catch (IOException ex) {
            log.error("Can't read batch file at given path: " + ex.getMessage());
            return Optional.empty();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return Optional.of(objectMapper.readValue(content, DownloadBatch.class));
        } catch (JsonProcessingException ex) {
            log.error("Can't parse batch file: " + ex.getMessage());
            return Optional.empty();
        }
    }

    private static void downloadFile(String url, String targetPath, int timeout) {
        String filename = url.substring(url.lastIndexOf("/") + 1);

        try (InputStream rbc = new URL(url).openStream()) {
            try (FileOutputStream fos = new FileOutputStream(targetPath + filename)) {
                long startTime = System.currentTimeMillis();

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = rbc.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    if (System.currentTimeMillis() - startTime > timeout) {
                        throw new SocketTimeoutException("Download timed out");
                    }
                }

                long elapsedTime = System.currentTimeMillis() - startTime;
                log.info("Downloaded " + filename + " elapsed time in milliseconds: " + elapsedTime);
            } catch (SocketTimeoutException ex) {
                throw new IOException(ex.getMessage());
            } catch (IOException ex) {
                log.info("Failed to create file at the target path " + ex.getMessage());
            }
        } catch (IOException ex) {
            log.error(String.format("Failed to load file %s: %s", filename, ex.getMessage()));
        }
    }

}