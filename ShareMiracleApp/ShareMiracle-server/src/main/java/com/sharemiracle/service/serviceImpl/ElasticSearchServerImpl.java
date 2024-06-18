package com.sharemiracle.service.serviceImpl;

import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import com.sharemiracle.constant.ElasticSearchConstant;
import com.sharemiracle.constant.MessageConstant;
import com.sharemiracle.dto.*;
import com.sharemiracle.result.Result;
import com.sharemiracle.service.ElasticSearchService;
import com.sharemiracle.vo.EsSearchVO;
import com.sharemiracle.vo.MdataMetaStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.extern.slf4j.Slf4j;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
// import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;


@Slf4j
@Service
public class ElasticSearchServerImpl implements ElasticSearchService {

    @Autowired
    private ElasticsearchClient esClient;

    //@Resource
    //private ElasticSearchMapper esMapper;

    /**
     * 1.查询Es数据库
     */
    @Override
    public EsSearchVO search(SearchDTO searchDTO) throws IOException {
        // 解析前端DTO
        // int pageId = searchDTO.getPage_id();
        // String name = searchDTO.getName();
        List<Integer> task_ids = searchDTO.getTask_ids();
        List<Integer> modality_ids = searchDTO.getModality_ids();
        List<Integer> organ_ids = searchDTO.getOrgan_ids();
        // String description = searchDTO.getDescription();
        // String sort = searchDTO.getSort();
        int pageSize = searchDTO.getPage_size();

        //构建筛选条件
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        if (task_ids != null) {
            for (int task_id : task_ids) {
                boolQueryBuilder.should(MatchQuery.of(m -> m
                    .field("task_ids")
                    .query(task_id))._toQuery()
                );
            }
        }


        if (modality_ids != null) {
            for (int modality_id : modality_ids) {
                boolQueryBuilder.should(MatchQuery.of(m -> m
                    .field("modality_ids")
                    .query(modality_id))._toQuery()
                );
            }
        }

        if (organ_ids != null) {
            for (int organ_id :  organ_ids) {
                boolQueryBuilder.should(MatchQuery.of(m -> m
                    .field("organ_ids")
                    .query(organ_id))._toQuery()
                );
            }
        }


        // if (tags != null) {
        //     for (String tag : tags) {
        //         tagBuilder.append(String.format("%s ", tag));
        //     }
        // }
        // if (queryStr != null)
        //     tagBuilder.append(String.format("%s ",queryStr));

        // 使用循环将 que 列表中的每个查询添加到 must 子句中

        // 查询es数据库
        // TODO: 实现分页查询
        SearchResponse<ElasticSearchItemDTO> searchResponse = esClient.search(s -> s.index(ElasticSearchConstant.MdataMetaDB)
                        .query(q -> q.bool(boolQueryBuilder.build())),
                ElasticSearchItemDTO.class);

        log.info("find result",searchResponse);

        //解析查询结果
        List<Hit<ElasticSearchItemDTO>> hits = searchResponse.hits().hits();
        List<ElasticSearchItemDTO> results = new ArrayList<>();
        List<Double> scores = new ArrayList<>();
        for (Hit<ElasticSearchItemDTO> hit: hits) {
            ElasticSearchItemDTO items = hit.source();
            results.add(items);
            scores.add(hit.score());
        }

        int page_num = (hits.size() + pageSize - 1) / pageSize;
        // 构建前端视图
        return new EsSearchVO(
                page_num,
                results
        );
    }

    @Override
    public Result<String> addItem(ElasticSearchItemDTO elasticSearchItemDTO) {
        try {
            log.info("build elastic item token:{}",elasticSearchItemDTO.toJson());
            
            // 将数据录入 "dataset" 表中， id 为主键
            esClient.index(i -> i
                .index(ElasticSearchConstant.MdataMetaDB)
                .id(String.valueOf(elasticSearchItemDTO.getId()))
                .document(elasticSearchItemDTO)
            );

            // 如果在 mdata-management 表格中没有项目，则创建
            GetResponse<MdataMetaStatus> mdataRes = esClient.get(i -> i
                .index(ElasticSearchConstant.MdataMetaManageDB)
                .id(String.valueOf(elasticSearchItemDTO.getId()))
            , MdataMetaStatus.class);

            log.info("create mdata {}", elasticSearchItemDTO.toJson());

            if (!mdataRes.found()) {
                MdataMetaStatus metaStatus = new MdataMetaStatus();
                metaStatus.setId(elasticSearchItemDTO.getId());
                metaStatus.setName(elasticSearchItemDTO.getName());
                metaStatus.setStatus(0);
                long ts = System.currentTimeMillis();
                metaStatus.setCreateTS(ts);
                metaStatus.setModifyTS(ts);

                esClient.index(i -> i
                    .index(ElasticSearchConstant.MdataMetaManageDB)
                    .id(String.valueOf(elasticSearchItemDTO.getId()))
                    .document(metaStatus)
                );
            }
            
            return Result.success(MessageConstant.ES_COMMON_SUCCESS);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<MdataMetaStatus> allDataset(){
        try {
            // TODO : 增加一个分页 id 选项
            log.info("find all dataset id");
            SearchResponse<MdataMetaStatus> searchResponse = esClient.search(s -> s
                .index(ElasticSearchConstant.MdataMetaManageDB)
                .query(q -> q.matchAll(m -> m))
                .size(500),
                MdataMetaStatus.class
            );

            List<Hit<MdataMetaStatus>> hits = searchResponse.hits().hits();
            List<MdataMetaStatus> results = new ArrayList<MdataMetaStatus>();
            log.info("hits length: {}", hits.size());
            for (Hit<MdataMetaStatus> hit: hits) {
                MdataMetaStatus items = hit.source();
                results.add(items);
            }

            // 排序
            results.sort((o1, o2) -> o1.getId() - o2.getId());

            return results;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ElasticSearchItemDTO getMdataMetaById(GetMdataMetaByIdDTO mdataMetaByIdDTO) {
        try {
            GetResponse<ElasticSearchItemDTO> response = esClient.get(i -> i
                .index(ElasticSearchConstant.MdataMetaDB)
                .id(String.valueOf(mdataMetaByIdDTO.getId()))
            , ElasticSearchItemDTO.class);

            log.info("getMdataMetaById {}", response.toString());

            if (response.found()) {
                return response.source();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Result<String> deleteItem(GetMdataMetaByIdDTO mdataMetaByIdDTO) {
        try {
            esClient.delete(i -> i
                .index(ElasticSearchConstant.MdataMetaDB)
                .id(String.valueOf(mdataMetaByIdDTO.getId()))
            );
            // 如果 manage 里面有也要删除
            GetResponse<MdataMetaStatus> mdataRes = esClient.get(i -> i
                .index(ElasticSearchConstant.MdataMetaManageDB)
                .id(String.valueOf(mdataMetaByIdDTO.getId()))
            , MdataMetaStatus.class);

            if (mdataRes.found()) {
                esClient.delete(i -> i
                    .index(ElasticSearchConstant.MdataMetaManageDB)
                    .id(String.valueOf(mdataMetaByIdDTO.getId()))
                );
            }

            return Result.success(MessageConstant.ES_COMMON_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.success(MessageConstant.ES_COMMON_FAIL);
        }
    }

    @Override
    public Result<String> updateMdataManagementInfo(MdataMetaStatus mdataMetaStatus) {
        try {
            mdataMetaStatus.setModifyTS(System.currentTimeMillis());

            IndexResponse response = esClient.index(i -> i
                .index(ElasticSearchConstant.MdataMetaManageDB)
                .id(String.valueOf(mdataMetaStatus.getId()))
                .document(mdataMetaStatus)
            );

            log.info("update mdata manage info, response: {}", response.toString());

            return Result.success(MessageConstant.ES_COMMON_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.success(MessageConstant.ES_COMMON_FAIL);
        }
    }
}