package org.tuiken.model;

import lombok.Data;

import java.util.List;

@Data
public class DownloadBatch {
    List<String> urls;
    String targetPath;
    int maxDuration;
    int maxConcurrent;
}
