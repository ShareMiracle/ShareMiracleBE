package com.sharemiracle.service;

import com.sharemiracle.dto.DownloadDTO;
import com.sharemiracle.result.Result;

public interface DatasetDownloadService {
    Result<String> downloadFromGoogleDriver(DownloadDTO downloadDTO);
    // Result<List> getAllDownloadSessions();
}
