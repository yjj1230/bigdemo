package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.*;
import org.example.shopdemo.entity.Notification;
import java.util.List;

@Mapper
public interface NotificationMapper {
    
    @Insert("INSERT INTO notification (user_id, type, title, content, is_read, related_order_id, create_time) " +
            "VALUES (#{userId}, #{type}, #{title}, #{content}, #{isRead}, #{relatedOrderId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);
    
    @Select("SELECT * FROM notification WHERE id = #{id}")
    Notification getById(Long id);
    
    @Select("SELECT * FROM notification WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Notification> getByUserId(Long userId);
    
    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND is_read = #{isRead} ORDER BY create_time DESC")
    List<Notification> getByUserIdAndReadStatus(@Param("userId") Long userId, @Param("isRead") Boolean isRead);
    
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = false")
    Integer getUnreadCountByUserId(Long userId);
    
    @Update("UPDATE notification SET is_read = true WHERE id = #{id}")
    int markAsRead(Long id);
    
    @Update("UPDATE notification SET is_read = true WHERE user_id = #{userId}")
    int markAllAsRead(Long userId);
    
    @Delete("DELETE FROM notification WHERE id = #{id}")
    int deleteById(Long id);
}