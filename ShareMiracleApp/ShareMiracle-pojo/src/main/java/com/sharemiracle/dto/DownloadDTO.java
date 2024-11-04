package com.sharemiracle.dto;
import lombok.Data;

import java.io.Serializable;

@Data
public class DownloadDTO implements Serializable {
    private String url;
}