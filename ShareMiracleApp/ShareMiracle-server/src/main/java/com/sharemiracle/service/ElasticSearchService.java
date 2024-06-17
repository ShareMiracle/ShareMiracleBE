package com.sharemiracle.service;

import com.sharemiracle.dto.SearchDTO;
import com.sharemiracle.dto.ElasticSearchItemDTO;
import com.sharemiracle.dto.SearchDTO;
import com.sharemiracle.result.Result;
import com.sharemiracle.result.SearchResult;
import com.sharemiracle.vo.EsAllDatasetIdVO;
import com.sharemiracle.vo.EsSearchVO;

import java.io.IOException;

public interface ElasticSearchService {

    EsSearchVO search(SearchDTO searchDTO) throws IOException;

    Result<String> addItem(ElasticSearchItemDTO elasticSearchItemDTO);

    EsAllDatasetIdVO allDataset();
}
