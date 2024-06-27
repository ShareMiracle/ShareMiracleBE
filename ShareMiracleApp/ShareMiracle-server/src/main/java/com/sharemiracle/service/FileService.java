package com.sharemiracle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sharemiracle.entity.FileEntity;
import com.sharemiracle.result.Result;

import java.util.List;

public interface FileService extends IService<FileEntity> {

    Result<String> downLoad(String filePath);

    List<String> getAllFiles(String directoryPath);

}
