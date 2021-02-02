package com.restkeeper.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DetailDTO implements Serializable {
    private String detailId;
    private List<String> remarks;
}
