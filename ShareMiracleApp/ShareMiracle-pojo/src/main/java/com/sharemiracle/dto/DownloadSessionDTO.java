package com.sharemiracle.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class DownloadSessionDTO implements Serializable {
    private String url;
    private String datasetName;
    private String username;
    private Float percent;
}