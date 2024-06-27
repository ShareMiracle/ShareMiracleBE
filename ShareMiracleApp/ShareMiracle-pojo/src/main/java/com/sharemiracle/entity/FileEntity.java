package com.sharemiracle.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文件实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键（自增）
     */
    private Long id;
    /**
     * 文件名
     */
    private String name;
    /**
     * 文件类型
     */
    private Integer type;
    /**
     * 文件资源url地址
     */
    private String url;
    /**
     * 文件资源本地地址
     */
    private String path;

}