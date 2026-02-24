package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.*;
import org.example.shopdemo.entity.Points;
import java.util.List;

@Mapper
public interface PointsMapper {
    
    @Insert("INSERT INTO points (user_id, points, type, description, related_order_id, create_time) " +
            "VALUES (#{userId}, #{points}, #{type}, #{description}, #{relatedOrderId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Points points);
    
    @Select("SELECT * FROM points WHERE id = #{id}")
    Points getById(Long id);
    
    @Select("SELECT * FROM points WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Points> getByUserId(Long userId);
    
    @Select("SELECT SUM(points) FROM points WHERE user_id = #{userId}")
    Integer getTotalPointsByUserId(Long userId);
    
    @Select("SELECT * FROM points WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<Points> getRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}