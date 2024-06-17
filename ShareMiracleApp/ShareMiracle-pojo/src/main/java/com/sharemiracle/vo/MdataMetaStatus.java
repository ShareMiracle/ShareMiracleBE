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
}
