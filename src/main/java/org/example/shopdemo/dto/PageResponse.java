package org.example.shopdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应DTO
 * 用于返回分页查询结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    private List<T> records;
    
    private Integer pageNum;
    
    private Integer pageSize;
    
    private Long total;
    
    private Integer totalPages;
    
    public static <T> PageResponse<T> of(List<T> records, Integer pageNum, Integer pageSize, Long total) {
        PageResponse<T> response = new PageResponse<>();
        response.setRecords(records);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setTotal(total);
        response.setTotalPages((int) Math.ceil((double) total / pageSize));
        return response;
    }
}
