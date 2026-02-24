package org.example.shopdemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 搜索历史实体类
 */
@Data
public class SearchHistory {
    /** 搜索历史ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 搜索关键词 */
    private String keyword;
    /** 搜索次数 */
    private Integer searchCount;
    /** 最后搜索时间 */
    private LocalDateTime lastSearchTime;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
