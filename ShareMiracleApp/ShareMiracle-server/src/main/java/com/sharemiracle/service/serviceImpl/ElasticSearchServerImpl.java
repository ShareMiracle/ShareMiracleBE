package com.sharemiracle.service.serviceImpl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import com.sharemiracle.constant.ElasticSearchConstant;
import com.sharemiracle.constant.MessageConstant;
import com.sharemiracle.dto.*;
import com.sharemiracle.entity.Dataset;
import com.sharemiracle.mapper.DatasetMapper;
import com.sharemiracle.result.Result;
import com.sharemiracle.service.ElasticSearchService;
import com.sharemiracle.vo.EsSearchVO;
import com.sharemiracle.vo.MdataMetaStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
// import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;


@Slf4j
@Service
public class ElasticSearchServerImpl implements ElasticSearchService {

    @Autowired
    private ElasticsearchClient esClient;

    @Resource
    private DatasetMapper datasetMapper;

    //@Resource
    //private ElasticSearchMapper esMapper;

    /**
     * 1.查询Es数据库
     */
    @Override
    public EsSearchVO search(SearchDTO searchDTO) throws IOException {
        log.info("begin search Es Item");
        List<ElasticSearchItemDTO> datasets = searchDatasetsFromES(searchDTO);
        log.info("finished search Es Item");
        List<Integer> datasetIds = datasets.stream()
                .map(dto -> Integer.valueOf(dto.getId()))  // 将Integer转换为Long
                .collect(Collectors.toList());
        log.info("all dataset num:{}",datasetIds.size());

        Map<Integer, Long> sortData = getSortingData(datasetIds, searchDTO.getSort());

        // 根据MySQL中的排序字段对结果进行排序
        datasets.sort(Comparator.comparingLong(d -> sortData.getOrDefault(d.getId(), 0L)));
        Collections.reverse(datasets);  // 假设默认是降序

        // 计算页数
        int pageNum = -1;
        
        // 当 page_id 为 0 时，计算当前页数
        if (searchDTO.getPage_id() == 0) {
            Query query = this.getESSearchQuery(searchDTO);
            CountRequest countRequest = CountRequest.of(c -> c
                .index(ElasticSearchConstant.MdataMetaDB)
                .query(query)
            );
            CountResponse countResponse = esClient.count(countRequest);
            long count = countResponse.count();
            pageNum = Math.ceilDiv((int)count, searchDTO.getPage_size());
        }

        return new EsSearchVO(pageNum, datasets);
    }

    private Query getESSearchQuery(SearchDTO searchDTO) {
        return QueryBuilders.bool(b -> {

            // // 如果description不为空，则添加到查询条件中
            // if (searchDTO.getDescription() != null && !searchDTO.getDescription().isEmpty()) {
            //     b.must(m -> m.match(mt -> mt.field("description").query(searchDTO.getDescription())));
            // }

            // // 如果name不为空，则添加到查询条件中
            // if (searchDTO.getName() != null && !searchDTO.getName().isEmpty()) {
            //     b.must(m -> m.match(mt -> mt.field("name").query(searchDTO.getName())));
            // }

            // 如果modality_ids不为空，则添加到过滤条件中
            if (searchDTO.getModality_ids() != null && !searchDTO.getModality_ids().isEmpty()) {
                b.must(f -> f.terms(t -> t.field("modality_ids").terms(tms -> tms.value(searchDTO.getModality_ids().stream().map(FieldValue::of).collect(Collectors.toList())))));
            }

            // 如果task_ids不为空，则添加到过滤条件中
            if (searchDTO.getTask_ids() != null && !searchDTO.getTask_ids().isEmpty()) {
                b.must(f -> f.terms(t -> t.field("task_ids").terms(tms -> tms.value(searchDTO.getTask_ids().stream().map(FieldValue::of).collect(Collectors.toList())))));
            }

            // 如果organ_ids不为空，则添加到过滤条件中
            if (searchDTO.getOrgan_ids() != null && !searchDTO.getOrgan_ids().isEmpty()) {
                b.must(f -> f.terms(t -> t.field("organ_ids").terms(tms -> tms.value(searchDTO.getOrgan_ids().stream().map(FieldValue::of).collect(Collectors.toList())))));
            }

            return b;
        });
    }


    public List<ElasticSearchItemDTO> searchDatasetsFromES(SearchDTO searchDTO) throws IOException {
        // 构建布尔查询条件
        Query query = this.getESSearchQuery(searchDTO);

        // 使用searchDTO中的值
        int pageId = searchDTO.getPage_id(); // 默认页码
        int pageSize = searchDTO.getPage_size(); // 使用searchDTO提供的每页数量

        // 构建搜索请求
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(ElasticSearchConstant.MdataMetaDB)
                .query(query)
                .from(pageId * pageSize)
                .size(searchDTO.getPage_size())
                .build();

        SearchResponse<ElasticSearchItemDTO> response = esClient.search(searchRequest, ElasticSearchItemDTO.class);
        
        
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    public Map<Integer, Long> getSortingData(List<Integer> ids, String sortField) {
        if (ids.isEmpty()) return Collections.emptyMap();

        return ids.stream().collect(Collectors.toMap(
                i -> i,  // key
                i -> {
                    try{
                        // 查找id对应文档
                        GetResponse<MdataMetaStatus> mdataRes = esClient.get(item -> item
                            .index(ElasticSearchConstant.MdataMetaManageDB)
                            .id(String.valueOf(i))
                            , MdataMetaStatus.class);       

                        if (mdataRes.found()) {
                            //根据关键字选择排序属性
                            Long value = switch (sortField) {
                                case "trending" -> mdataRes.source().getCounter_trending();
                                case "downloads" -> mdataRes.source().getCounter_downloads();
                                case "likes" -> mdataRes.source().getCounter_likes();
                                case "created" -> mdataRes.source().getCreateTS();
                                case "updated" -> mdataRes.source().getModifyTS();
                                default -> mdataRes.source().getCounter_trending();
                            };
                            return value;
                        } else {
                            return -1L;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                    
                }
            ));

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