package com.sharemiracle.service.serviceImpl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import com.sharemiracle.constant.DatasetDownload;
import com.sharemiracle.dto.DownloadDTO;
import com.sharemiracle.result.Result;
import com.sharemiracle.service.DatasetDownloadService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DatasetDownloadImpl implements DatasetDownloadService {
    private HashMap<String, DownloadSession> downloadSessions = new HashMap<String, DownloadSession>();
    private ReadWriteLock downloadLock = new ReentrantReadWriteLock();
    private static String DatasetRoot = "/home/";

    @Override
    public Result<String> downloadFromGoogleDriver(DownloadDTO downloadDTO) {
        String downloadUrl = downloadDTO.getUrl();
        // TODO: 从数据库中判断数据是否已经下载完成
        
        // 判断是 storage 中否存在链接
        Lock readLock = downloadLock.readLock();
        readLock.lock();
        if (downloadSessions.containsKey(downloadUrl)) {
            readLock.unlock();
            return Result.success(DatasetDownload.SomeoneElseDownloading);
        }
        readLock.unlock();

        // 没有则创建，先进行 google url 的令牌解析
        String confirmCode;
        String fileId;
        try {
            fileId = extractFileIdFromUrl(downloadUrl);
        } catch (Exception e) {
            log.info("从 {} 中提取 fileId 出现错误 {}", downloadUrl, e.toString());
            return Result.success(DatasetDownload.FailToGetFileId);
        }

        try {
            confirmCode = getConfirmCode(fileId);
        } catch (Exception e) {
            log.info("从 {} 中提取 confirmCode 出现错误 {}", fileId, e.toString());
            return Result.success(DatasetDownload.FailToGetConfirmCode);
        }

        DownloadThread downloaThread = new DownloadThread(downloadUrl, confirmCode);
        DownloadSession session = new DownloadSession(confirmCode, downloadUrl, downloaThread);

        Lock writeLock = downloadLock.writeLock();
        writeLock.lock();
        downloadSessions.put(downloadUrl, session);
        writeLock.unlock();
    
        return Result.success(DatasetDownload.SuccessCreate);
    }

    
    private String extractFileIdFromUrl(String downloadUrl) throws Exception {
        // example: https://drive.google.com/file/d/1gSUgRmcmUKhn7rE5UYKsdKvz6cZhRo_L/view?usp=drive_link
        try {
            URI uri = new URI(downloadUrl);
            URL url = uri.toURL();
            String path = url.getPath();
            String[] names = path.split("/");
            int i = 0;

            for (String name : path.split("/")) {
                if (name == "d") {
                    break;
                }
                i ++;
            }

            if (i < names.length - 1) {
                String fileId = names[i + 1];
                return fileId;
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
        return null;
    }

    private String getConfirmCode(String fileId) throws IOException {
        CookieStore cookieStore = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("https://docs.google.com/uc?export=download&id=" + fileId);
            try (CloseableHttpResponse response = httpClient.execute(request, context)) {
                String responseBody = EntityUtils.toString(response.getEntity());

                // 提取确认码
                Pattern pattern = Pattern.compile("confirm=([0-9A-Za-z_]+)");
                Matcher matcher = pattern.matcher(responseBody);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return null;
    }

    private class DownloadSession {
        public String name;
        public String url;
        public DownloadThread thread;

        public DownloadSession(String name, String url, DownloadThread thread) {
            this.name = name;
            this.url = url;
            this.thread = thread;
        }

        public float getPercent() {

            return 0;
        } 
    }

    private class DownloadThread implements Runnable {
        private static final int DOWNLOAD_BUFFER_SIZE = 8192;

        private String downloadUrl;
        private String confirmCode;
        private float progress;
        private Boolean stoped;

        public DownloadThread(String downloadUrl, String confirmCode) {
            this.downloadUrl = downloadUrl;
            this.confirmCode = confirmCode;
            this.progress = 0;
            this.stoped = false;
        }

        public Boolean getStoped() {
            return stoped;
        }

        public float getProgress() {
            return progress;
        }

        @Override
        public void run() {
            try {
                downloadGoogleDriverFile(confirmCode, DatasetRoot);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }

        private void downloadGoogleDriverFile(String confirmCode, String fileId) throws IOException {
            CookieStore cookieStore = new BasicCookieStore();
            HttpClientContext context = HttpClientContext.create();
            context.setCookieStore(cookieStore);
    
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                String downloadUrl = "https://docs.google.com/uc?export=download&confirm=" + confirmCode + "&id=" + fileId;
                HttpGet request = new HttpGet(downloadUrl);
    
                try (CloseableHttpResponse response = httpClient.execute(request, context)) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        // 获取文件名
                        String contentDisposition = response.getFirstHeader("Content-Disposition").getValue();
                        String fileName = contentDisposition.split("filename=")[1].replace("\"", "");
                        
                        HttpEntity entity = response.getEntity();
                        // 获取文件大小
                        long totalLength = entity.getContentLength();

                        // 下载文件
                        try (FileOutputStream outputStream = new FileOutputStream("./" + fileName)) {
                            InputStream inputStream = entity.getContent();
                            byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
                            int bytesRead;
                            long totalBytesRead = 0;

                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                                totalBytesRead += bytesRead;
                                double progress = (double) totalBytesRead / totalLength * 100;
                                System.out.printf("Download progress: %.2f%%\n", progress);
                            }

                            
                            entity.writeTo(outputStream);
                        }
                        log.info("Google Driver 文件下载: " + fileName);
                    } else {
                        log.info("无法下载文件，状态码如下 " + response.getStatusLine().getStatusCode());
                    }
                }
            }
        }
    }
}