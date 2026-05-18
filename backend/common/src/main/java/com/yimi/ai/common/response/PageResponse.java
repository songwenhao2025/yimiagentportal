package com.yimi.ai.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> list;
    private long total;
    private int page;
    private int size;

    public static <T> PageResponse<T> of(List<T> list, long total, int page, int size) {
        return new PageResponse<>(list, total, page, size);
    }
}