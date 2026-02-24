package org.example.shopdemo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.shopdemo.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {
    int insert(User user);
    User findByUsername(String username);
    User findByEmail(String email);
    User findById(Long id);
    int update(User user);
    int deleteById(Long id);
    List<User> findAll();
}
