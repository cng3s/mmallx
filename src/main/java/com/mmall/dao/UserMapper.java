package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    /* 检查数据库中是否存在username,如存在则返回有多少个 */
    int checkUsername(String username);

    int checkEmail(String email);

    int checkEmailByUserId(@Param(value = "email") String email, @Param(value = "userId") Integer userId);

    int checkPassword(@Param("password") String password, @Param("userId") Integer userId);

    User selectLogin(@Param("username") String username, @Param("password") String password);

    String selectQuestionByUsername(String username);

    int checkAnswer(@Param("username") String username
            , @Param("question") String question, @Param("answer") String answer);

    int updatePasswordByUsername(@Param("username") String username
            , @Param("passwordNew") String passwordNew);
}