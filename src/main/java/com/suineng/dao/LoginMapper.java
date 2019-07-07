package com.suineng.dao;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.suineng.bean.UserInfo;


@Mapper
public interface LoginMapper
{
    public int createUser(@Param("userName") String userName, @Param("passwd") String passwd);

    public int updatePasswd(@Param("userName") String userName, @Param("passwd") String passwd);

    public UserInfo queryUser(String userName);
}
