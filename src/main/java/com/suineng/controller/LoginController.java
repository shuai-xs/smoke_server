package com.suineng.controller;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;
import com.suineng.bean.UserInfo;
import com.suineng.cache.UserSessionCache;
import com.suineng.common.DigestUtil;
import com.suineng.common.LoginConst;
import com.suineng.service.LoginService;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private LoginService loginService;

    @Autowired
    private DigestUtil digestUtil;

    @Autowired
    private LoginService loginHandler;

    @Value("${login.uri:/login}")
    private String LOGIN_PAGE;

    @Value("${login.index:/html/login.html}")
    private String LOGIN_INDEX;

    @RequestMapping(value="/login", method= RequestMethod.GET)
    public void htmlLogin(HttpServletRequest request, HttpServletResponse response)
    {
        String userName = request.getParameter(LoginConst.USERNAME);
        String passwd = request.getParameter(LoginConst.PASSWD);
        String decode = digestUtil.md5(passwd);
        UserInfo userInfo = loginService.queryUser(userName);
        String result = null;
        if(isAuth(userInfo, decode))
        {
            String sessionId = request.getSession(true).getId();
            result = this.getResult(true);
            UserSessionCache.getInstance().insertSession(sessionId, userName);
        }
        else
        {
            result = this.getResult(false);
            try
            {
                response.sendRedirect(LOGIN_INDEX);
            }catch (Exception e)
            {
                logger.error("redirect fail", e);
            }

        }
        writeResponse(response, result);
    }
    
    /**
     * 登录验证
     * @param request
     * @param response
     */
    @RequestMapping(value="/login", method= RequestMethod.POST)
    public void login(HttpServletRequest request, HttpServletResponse response, @RequestBody String param)
    {
        String userName = null;
        String passwd = null;
        try {
            JSONObject object = JSONObject.parseObject(param);
            userName = object.getString("userName");
            passwd = object.getString("passwd");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        String decode = digestUtil.md5(passwd);
        UserInfo userInfo = loginService.queryUser(userName);
        String result = null;
        if(isAuth(userInfo, decode))
        {
            String sessionId = request.getSession(true).getId();
            result = this.getResult(true);
            UserSessionCache.getInstance().insertSession(sessionId, userName);
        }
        else
        {
            result = this.getResult(false);
        }
        writeResponse(response, result);

    }


    /**
     * 登录验证
     * @param request
     * @param response
     */
    @RequestMapping(value="/reset", method= RequestMethod.POST)
    @ResponseBody
    public String reset(HttpServletRequest request, HttpServletResponse response, @RequestBody String param)
    {
        System.out.println(param);
        String userName = null;
        String oldPasswd = null;
        String newPasswd = null;
        String confirmPasswd = null;
        try {
            JSONObject object = JSONObject.parseObject(param);
            userName = object.getString("userName");
            oldPasswd = object.getString("oldPasswd");
            newPasswd = object.getString("newPasswd");
            confirmPasswd = object.getString("confirmPasswd");
            if(null == newPasswd || null == confirmPasswd || newPasswd.isEmpty() || confirmPasswd.isEmpty() || !newPasswd.equals(confirmPasswd))
            {
                return getResult(false);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        String decode = digestUtil.md5(oldPasswd);
        UserInfo userInfo = loginService.queryUser(userName);
        String result = null;
        if(isAuth(userInfo, decode))
        {
            String newDecode = digestUtil.md5(newPasswd);
            int num = loginService.updatePasswd(userName, newDecode);
            return getResult(true);
        }
        else
        {
            return this.getResult(false);
        }
    }
    private void writeResponse(HttpServletResponse response, String result)
    {
        OutputStream outputStream = null;
        try
        {
            outputStream = response.getOutputStream();
            outputStream.write(result.getBytes());
        }catch (Exception e)
        {
            logger.error("write fail",e);
        }finally {
            if(null != outputStream)
            {
                try
                {
                    outputStream.close();
                }catch (Exception e)
                {
                    logger.error("close stream fail", e);
                }

            }
        }

    }

    private boolean isAuth(UserInfo userInfo, String decode)
    {
        if(null == userInfo)
        {
            return false;
        }
        if(null == userInfo.getPasswd())
        {
            return false;
        }
        if(!userInfo.getPasswd().equals(decode))
        {
            return false;
        }
        return true;
    }
    private String getResult(boolean auth)
    {
        JSONObject object = new JSONObject();
        if(auth)
        {
            object.put(LoginConst.RESULT, LoginConst.SUCCESS);
        }else
        {
            object.put(LoginConst.RESULT, LoginConst.FAIL);
        }
        return object.toJSONString();
    }
}
