package org.example.shopdemo.dto;

import lombok.Data;

/**
 * 分页请求DTO
 * 用于接收分页查询参数
 */
@Data
public class PageRequest {
    
    private Integer pageNum = 1;
    
    private Integer pageSize = 10;
    
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
    
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum != null && pageNum > 0 ? pageNum : 1;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize != null && pageSize > 0 ? pageSize : 10;
        if (this.pageSize > 100) {
            this.pageSize = 100;
        }
    }
}
