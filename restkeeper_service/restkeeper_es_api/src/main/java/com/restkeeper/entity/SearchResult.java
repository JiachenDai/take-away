package com.restkeeper.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchResult<T> implements Serializable {
    private List<T> records;
    private long total;
}

