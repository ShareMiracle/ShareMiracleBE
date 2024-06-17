package com.sharemiracle.controller;

import com.sharemiracle.constant.MessageConstant;
import com.sharemiracle.dto.SearchDTO;
import com.sharemiracle.dto.ElasticSearchItemDTO;
import com.sharemiracle.result.Result;
import com.sharemiracle.service.ElasticSearchService;
import com.sharemiracle.service.serviceImpl.DatasetServiceImpl;
import com.sharemiracle.service.serviceImpl.ElasticSearchServerImpl;
import com.sharemiracle.vo.EsAllDatasetIdVO;
import com.sharemiracle.vo.EsSearchVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
// import java.util.List;


@Slf4j
@RestController
@RequestMapping("/es")
public class ElasticSearchController {
    @Resource
    private ElasticSearchService elasticSearchServerImpl;

    /**
     * 查找数据
     */
    @GetMapping("/search")
    public Result<EsSearchVO> searchController(@RequestBody SearchDTO searchDTO) throws IOException {
        log.info("查找数据：{}", searchDTO);
        EsSearchVO result = elasticSearchServerImpl.search(searchDTO);
        return Result.success(result, MessageConstant.ESSEAERCH_SUCCESS);
    }

    @PutMapping("/addItem")
    public Result<String> addItemController(@RequestBody ElasticSearchItemDTO elasticSearchItemDTO) {
        log.info("添加Es数据:{}", elasticSearchItemDTO);
        // Result<String> result = elasticSearchServerImpl.addItem(elasticSearchItemDTO);
        return elasticSearchServerImpl.addItem(elasticSearchItemDTO);
    }

    @GetMapping("/allDataset")
    public Result<EsAllDatasetIdVO> adllDatasetController() {
        log.info("查找所有数据:{}");
        // Result<String> result = elasticSearchServerImpl.addItem(elasticSearchItemDTO);
        EsAllDatasetIdVO result = elasticSearchServerImpl.allDataset();
        return Result.success(result, MessageConstant.DEFAULT_SUCCESS);
    }

}
