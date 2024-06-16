package com.sharemiracle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Data
public class ElasticSearchItemDTO implements Serializable {

    private int id;                         //数据集的唯一标识符
    private String name;                    //数据集的名称
    private String origin_rrl;               //数据集的原始链接，提供数据来源 
    private String description;             //详细描述
    private String release_date;             //发布日期
    private List<Integer> task_ids;          //相关的任务的标识符
    private List<Integer> modality_ids;      //涉及的模态类型
    private List<Integer> organ_ids;         //涉及的器官标识
    private int data_num;                    //数据条目
    private int label_num;                   //标签数量
    private SplitInfo split_info;            //数据集的分割信息，包括训练集、验证集和测试集的数量

    // 内部类 SplitInfo
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitInfo {
        private int train_num;               //训练集数目
        private int val_num;                 //验证数据数目
        private int test_num;                //测试数据数目
    }

    public String toJson() {                //转化为json对象
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
