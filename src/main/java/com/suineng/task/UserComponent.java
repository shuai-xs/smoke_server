package com.suineng.task;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.suineng.bean.UserInfo;
import com.suineng.common.DigestUtil;
import com.suineng.service.LoginService;

@Component
public class UserComponent {

    @Autowired
    private DigestUtil digestUtil;

    @Autowired
    private LoginService loginService;

    @Value("${account.name:admin}")
    private String userName;

    @Value("${account.passwd:admin}")
    private String passwd;

    @PostConstruct
    public void init()
    {
        createUser();
    }

    private void createUser()
    {
        UserInfo userInfo = loginService.queryUser(userName);
        if(null == userInfo || null == userInfo.getPasswd())
        {
            String value = digestUtil.md5(passwd);
            loginService.insertUser(userName, value);
        }

    }


}
