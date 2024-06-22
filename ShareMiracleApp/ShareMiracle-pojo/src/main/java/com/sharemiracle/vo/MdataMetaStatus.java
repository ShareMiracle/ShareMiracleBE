package com.sharemiracle.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdataMetaStatus implements Serializable {
    private int id;
    private String name;
    private int status;
    private long createTS;
    private long modifyTS;

    @Builder.Default
    private long counter_trending = 0;

    @Builder.Default
    private long counter_likes = 0;

    @Builder.Default
    private long counter_downloads = 0;
}
