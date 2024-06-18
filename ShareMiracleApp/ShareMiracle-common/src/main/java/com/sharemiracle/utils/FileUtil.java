package com.sharemiracle.utils;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * 文件工具类
 * @author HTT
 */
@Component
public class FileUtil {

    /**
     * 使用Spring框架自带的下载方式
     * @param filePath
     * @param fileName
     * @return
     */
    public ResponseEntity<Resource> download(String filePath,String fileName) throws Exception {
        fileName = URLEncoder.encode(fileName,"UTF-8");
        File file = new File(filePath);
        if(!file.exists()){
            throw new Exception("文件不存在");
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + fileName ).body(new FileSystemResource(filePath));
    }

    /**
     * 通过IOUtils以流的形式下载
     * @param filePath
     * @param fileName
     * @param response
     */
    public void download(String filePath , String fileName, HttpServletResponse response) throws Exception {
        fileName = URLEncoder.encode(fileName,"UTF-8");
        File file=new File(filePath);
        if(!file.exists()){
            throw new Exception("文件不存在");
        }
        response.setHeader("Content-disposition","attachment;filename="+ fileName);
        FileInputStream fileInputStream = new FileInputStream(file);
        IOUtils.copy(fileInputStream,response.getOutputStream());
        response.flushBuffer();
        fileInputStream.close();
    }

    /**
     * 原始的方法，下载一些小文件，边读边下载的
     * @param filePath
     * @param fileName
     * @param response
     * @throws Exception
     */
    public void downloadTinyFile(String filePath,String fileName, HttpServletResponse response)throws Exception{
        File file = new File(filePath);
        fileName = URLEncoder.encode(fileName, "UTF-8");
        if(!file.exists()){
            throw new Exception("文件不存在");
        }
        FileInputStream in = new FileInputStream(file);
        response.setHeader("Content-Disposition", "attachment;filename="+fileName);
        OutputStream out = response.getOutputStream();
        byte[] b = new byte[1024];
        int len = 0;
        while((len = in.read(b))!=-1){
            out.write(b, 0, len);
        }
        out.flush();
        out.close();
        in.close();
    }

    /**
     * 上传文件
     * @param multipartFile
     * @param storagePath
     * @return
     * @throws Exception
     */
    public String upload(MultipartFile multipartFile, String storagePath) throws Exception{
        if (multipartFile.isEmpty()) {
            throw new Exception("文件不能为空！");
        }
        String originalFilename = multipartFile.getOriginalFilename();
        String newFileName = UUID.randomUUID()+"_"+originalFilename;
        String filePath = storagePath+newFileName;
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        multipartFile.transferTo(file);
        return filePath;
    }

}
