package org.example.user.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.common.domain.User;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author : lzp
 * @date :
 */
@Repository
@Mapper
public interface UserDao {

    void insertUser(User user);

    User selectUser(String userName);

    List<User> listUser(String userName);

}
