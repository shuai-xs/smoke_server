package com.suineng.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.suineng.bean.UserInfo;
import com.suineng.dao.LoginMapper;


@Service
public class LoginService
{

    @Autowired
    private LoginMapper loginMapper;

    public int updatePasswd(String userName, String passwd)
    {
        return loginMapper.updatePasswd(userName, passwd);
    }

    public UserInfo queryUser(String userName)
    {
        UserInfo userInfo = loginMapper.queryUser(userName);
        return userInfo;
    }

    public int insertUser(String userName, String passwd)
    {
        int result = loginMapper.createUser(userName, passwd);
        return result;
    }
}
