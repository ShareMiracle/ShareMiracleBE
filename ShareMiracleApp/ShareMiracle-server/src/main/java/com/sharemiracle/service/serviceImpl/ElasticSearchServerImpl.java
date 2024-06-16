package com.sharemiracle.service.serviceImpl;

import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import com.sharemiracle.constant.MessageConstant;
import com.sharemiracle.dto.*;
import com.sharemiracle.result.Result;
//import com.sharemiracle.mapper.ElasticSearchMapper;
import com.sharemiracle.service.ElasticSearchService;
import com.sharemiracle.vo.EsSearchVO;
import org.springframework.stereotype.Service;
import org.elasticsearch.client.RequestOptions;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.extern.slf4j.Slf4j;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
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
        int pageId = searchDTO.getPage_id();
        String name = searchDTO.getName();
        List<Integer> task_ids = searchDTO.getTask_ids();
        List<Integer> modality_ids = searchDTO.getModality_ids();
        List<Integer> organ_ids = searchDTO.getOrgan_ids();
        String description = searchDTO.getDescription();
        String sort = searchDTO.getSort();
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
        SearchResponse<ElasticSearchItemDTO> searchResponse = esClient.search(s -> s.index("dataset")
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
            IndexResponse response = esClient.index(i -> i
                .index("dataset")
                .id(String.valueOf(elasticSearchItemDTO.getId()))
                .document(elasticSearchItemDTO)
            );
            return Result.success(MessageConstant.ES_INSERT_SUCCESS);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

}


