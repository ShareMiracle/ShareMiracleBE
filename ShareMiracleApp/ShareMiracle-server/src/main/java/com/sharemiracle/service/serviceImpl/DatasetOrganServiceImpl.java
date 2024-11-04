package com.sharemiracle.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sharemiracle.dto.DownloadDTO;
import com.sharemiracle.entity.DatasetOrgan;
import com.sharemiracle.mapper.DatasetOrganMapper;
import com.sharemiracle.result.Result;
import com.sharemiracle.service.DatasetDownloadService;
import com.sharemiracle.service.DatasetOrganService;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Service;

@Service
public class DatasetOrganServiceImpl implements DatasetDownloadService {

    private HashMap<String, String> downloadSession = new HashMap<String, String>();
    private ReadWriteLock downloadSessionRwLock = new ReentrantReadWriteLock();
    
    
    @Override
    public Result<String> downloadFromGoogleDriver(DownloadDTO downloadDTO) {

        

        return Result.success("test");
    }
}
