package com.sharemiracle.vo;

import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.ingest.simulate.Ingest;

import com.sharemiracle.dto.ElasticSearchItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsAllDatasetIdVO implements Serializable {
    /**
     * 检索到的项
     */
    private List<Integer> ids;
}
