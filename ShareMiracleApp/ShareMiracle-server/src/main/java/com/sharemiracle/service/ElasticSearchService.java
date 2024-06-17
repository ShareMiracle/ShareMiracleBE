package com.sharemiracle.service;

import com.sharemiracle.dto.SearchDTO;
import com.sharemiracle.dto.ElasticSearchItemDTO;
import com.sharemiracle.dto.GetMdataMetaByIdDTO;
import com.sharemiracle.result.Result;
import com.sharemiracle.vo.EsSearchVO;
import com.sharemiracle.vo.MdataMetaStatus;

import java.io.IOException;
import java.util.List;

public interface ElasticSearchService {

    EsSearchVO search(SearchDTO searchDTO) throws IOException;

    Result<String> addItem(ElasticSearchItemDTO elasticSearchItemDTO);

    List<MdataMetaStatus> allDataset();

    ElasticSearchItemDTO getMdataMetaById(GetMdataMetaByIdDTO mdataMetaByIdDTO);
}
