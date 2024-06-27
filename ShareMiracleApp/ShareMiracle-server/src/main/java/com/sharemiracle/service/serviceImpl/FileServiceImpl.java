package com.sharemiracle.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sharemiracle.entity.FileEntity;
import com.sharemiracle.mapper.FileMapper;
import com.sharemiracle.result.Result;
import com.sharemiracle.service.FileService;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;


@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, FileEntity> implements FileService {


    @Override
    public Result<String> downLoad(String filePath) {
        //"http://139.196.106.190:8002/10_Decathlon/Task02_Heart/imagesTr/";
        String[] parts = filePath.split("/");
        String fileName = parts[parts.length - 1];

        String fileDownPath = "d:/test";
        //创建不同的文件夹目录
        File file = new File(fileDownPath);
        //判断文件夹是否存在
        if (!file.exists()) {
            //如果文件夹不存在，则创建新的的文件夹
            file.mkdirs();
        }
        FileOutputStream fileOut = null;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        try {
            // 建立链接
            URL httpUrl=new URL(filePath);
            conn=(HttpURLConnection) httpUrl.openConnection();
            //以Post方式提交表单，默认get方式
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            // post方式不能使用缓存
            conn.setUseCaches(false);
            //连接指定的资源
            conn.connect();
            //获取网络输入流
            inputStream=conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            //判断文件的保存路径后面是否以/结尾
            if (!fileDownPath.endsWith("/")) {
                fileDownPath += "/";
            }
            //写入到文件（注意文件保存路径的后面一定要加上文件的名称）
            fileOut = new FileOutputStream(fileDownPath+fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fileOut);

            byte[] buf = new byte[4096];
            int length = bis.read(buf);
            //保存文件
            while(length != -1) {
                bos.write(buf, 0, length);
                length = bis.read(buf);
            }
            bos.close();
            bis.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("抛出异常！！");
            return Result.error("下载失败！");

        }

        return Result.success();
    }

    @Override
    public List<String> getAllFiles(String directoryPath) {
        HttpClient httpClient = HttpClients.createDefault();
        String remoteBaseUrl = "http://139.196.106.190:8002";  // 外网反向代理地址
        String endpoint = remoteBaseUrl + directoryPath;
        HttpGet request = new HttpGet(endpoint);

        // 发起请求并获取响应
        org.apache.http.HttpResponse response = null;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            // TODO:自定义异常处理
            throw new RuntimeException(e);
        }

        // 读取响应内容
        String responseBody = null;
        try {
            responseBody = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            // TODO:自定义异常处理
            throw new RuntimeException(e);
        }

        // 使用 Jsoup 解析 HTML
        Document doc = Jsoup.parse(responseBody);

        // 提取文件名
        Elements elements = doc.select("pre a[href]");
        List<String> fileNames = new ArrayList<>();

        for (Element element : elements) {
            String href = element.attr("href");
            if (!href.equals("../")) {  // 排除上级目录链接
                String fileName = href.replaceAll("/$", "");
                fileNames.add(fileName);
            }
        }

        //TODO:返回的参数增加资源的路径/标识符（而不仅仅是文件(夹)名）
        return fileNames;
    }
}
