package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.*;
import org.example.shopdemo.entity.Logistics;
import java.util.List;

@Mapper
public interface LogisticsMapper {
    
    @Insert("INSERT INTO logistics (order_id, logistics_no, logistics_company, status, description, location, create_time, update_time) " +
            "VALUES (#{orderId}, #{logisticsNo}, #{logisticsCompany}, #{status}, #{description}, #{location}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Logistics logistics);
    
    @Select("SELECT * FROM logistics WHERE order_id = #{orderId} ORDER BY create_time DESC")
    List<Logistics> getByOrderId(Long orderId);
    
    @Select("SELECT * FROM logistics WHERE logistics_no = #{logisticsNo} ORDER BY create_time DESC")
    List<Logistics> getByLogisticsNo(String logisticsNo);
    
    @Update("UPDATE logistics SET status = #{status}, description = #{description}, location = #{location}, update_time = NOW() WHERE id = #{id}")
    int update(Logistics logistics);
    
    @Delete("DELETE FROM logistics WHERE id = #{id}")
    int deleteById(Long id);
}