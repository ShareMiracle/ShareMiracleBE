package com.sharemiracle.controller;

import com.sharemiracle.constant.MessageConstant;
import com.sharemiracle.dto.SearchDTO;
import com.sharemiracle.dto.ElasticSearchItemDTO;
import com.sharemiracle.dto.GetMdataMetaByIdDTO;
import com.sharemiracle.result.Result;
import com.sharemiracle.service.ElasticSearchService;
import com.sharemiracle.vo.EsSearchVO;
import com.sharemiracle.vo.MdataMetaStatus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
// import java.util.List;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/mdata")
public class ElasticSearchController {
    @Resource
    private ElasticSearchService elasticSearchServerImpl;

    /**
     * 查找数据
     */
    @PostMapping("/search-meta-info")
    public Result<EsSearchVO> searchController(@RequestBody SearchDTO searchDTO) throws IOException {
        log.info("查找数据：{}", searchDTO);
        EsSearchVO result = elasticSearchServerImpl.search(searchDTO);
        return Result.success(result, MessageConstant.ES_COMMON_SUCCESS);
    }

    @PutMapping("/add-meta-info")
    public Result<String> addMdataMetaById(@RequestBody ElasticSearchItemDTO elasticSearchItemDTO) {
        log.info("添加Es数据:{}", elasticSearchItemDTO);
        // Result<String> result = elasticSearchServerImpl.addItem(elasticSearchItemDTO);
        return elasticSearchServerImpl.addItem(elasticSearchItemDTO);
    }

    @PostMapping("/get-all-meta-status")
    public Result<List<MdataMetaStatus>> adllDatasetController() {
        // log.info("查找所有数据:{}");
        List<MdataMetaStatus> result = elasticSearchServerImpl.allDataset();
        return Result.success(result, MessageConstant.ES_COMMON_SUCCESS);
    }

    @PostMapping("/get-mdata-meta-by-id")
    public Result<ElasticSearchItemDTO> getMdataMetaById(@RequestBody GetMdataMetaByIdDTO mdataMetaByIdDTO) {
        ElasticSearchItemDTO result = elasticSearchServerImpl.getMdataMetaById(mdataMetaByIdDTO);
        return Result.success(result, MessageConstant.ES_COMMON_SUCCESS);
    }

    @PostMapping("/modify-mdata-meta-by-id")
    public Result<String> modifyMdataMetaById(@RequestBody ElasticSearchItemDTO elasticSearchItemDTO) {
        return this.addMdataMetaById(elasticSearchItemDTO);
    }
    
    @DeleteMapping("/delete-mdata-meta-by-id")
    public Result<String> modifyMdataMetaById(@RequestBody GetMdataMetaByIdDTO mdataMetaByIdDTO) {
        return elasticSearchServerImpl.deleteItem(mdataMetaByIdDTO);
    }

    @PostMapping("/update-mdata-management-info")
    public Result<String> updateMdataManagementInfo(@RequestBody MdataMetaStatus mdataMetaStatus) {
        return elasticSearchServerImpl.updateMdataManagementInfo(mdataMetaStatus);
    }
}
