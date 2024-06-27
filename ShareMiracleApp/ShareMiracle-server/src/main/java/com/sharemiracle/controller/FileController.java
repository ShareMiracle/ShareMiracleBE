package com.sharemiracle.controller;

import com.sharemiracle.result.Result;
import com.sharemiracle.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    /**
     * 下载文件
     * @param filePath
     * @return
     */
    @GetMapping("/download")
    public Result<String> download(@RequestParam String filePath) {
        log.info("资源路径: {}", filePath);
        return fileService.downLoad(filePath);
    }

    /**
     * 展示该路径文件夹下的所有文件
     * @param directoryPath
     * @return
     * @throws IOException
     */
    @GetMapping("/files")
    public List<String> getAllFilesInDirectory(@RequestParam String directoryPath) throws IOException {
        log.info("文件夹路径: {}", directoryPath);
        return fileService.getAllFiles(directoryPath);
    }

}
