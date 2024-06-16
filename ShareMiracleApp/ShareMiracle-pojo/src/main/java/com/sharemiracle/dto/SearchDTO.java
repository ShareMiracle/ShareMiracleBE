package com.sharemiracle.dto;

import com.sharemiracle.entity.Organization;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
public class SearchDTO implements Serializable {
    private int page_id;
    private List<Integer> task_ids;
    private List<Integer> modality_ids;
    private List<Integer> organ_ids;
    private String name = "";
    private String description = "";
    private String sort = "trending";
    private int page_size = 15;
}
