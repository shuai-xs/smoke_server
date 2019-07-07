package com.suineng.cache;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class UserSessionCache
{
    private Map<String, String> sessionMap = new ConcurrentHashMap<String, String>();

    private static UserSessionCache instance = new UserSessionCache();

    private UserSessionCache()
    {

    }

    public static UserSessionCache getInstance()
    {
        return instance;
    }

    public void insertSession(String sessionId, String userName)
    {
        sessionMap.put(sessionId, userName);
    }

    public String getUserName(String sessionId)
    {
        return sessionMap.get(sessionId);
    }
}
