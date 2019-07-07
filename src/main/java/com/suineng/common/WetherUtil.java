package com.suineng.common;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suineng.bean.Wether;


public class WetherUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WetherUtil.class);

    private static Map<String, String> cityCode = new HashMap<String, String>();

    
    static
    {
        BufferedReader reader = null;
        String laststr = "";
        try
        {
            ClassPathResource resource = new ClassPathResource("config/_city.json");
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream(), "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null)
            {
                laststr += tempString;
            }
            reader.close();

        }
        catch (Exception e)
        {
            LOGGER.error("init error", e);
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        JSONArray array = JSONArray.parseArray(laststr);
        for (int i = 0; i < array.size(); ++i)
        {
            cityCode.put(array.getJSONObject(i).getString("city_name"),
                array.getJSONObject(i).getString("city_code"));
        }
    }

    /**
     * 设定post方法获取网络资源,如果参数为null,实际上设定为get方法
     * 
     * @param url
     *            网络地址
     * @param param
     *            请求参数键值对
     * @return 返回读取数据
     */
    public static String getWether(String url)
    {
        HttpURLConnection conn = null;
        try
        {
            URL u = new URL(url);
            conn = (HttpURLConnection)u.openConnection();
            StringBuffer sb = null;
            conn.setConnectTimeout(10000);
            conn.connect();// 建立连接
            sb = new StringBuffer();
            // 获取连接状态码
            int recode = conn.getResponseCode();
            BufferedReader reader = null;
            if (recode == 200)
            {
                // Returns an input stream that reads from this open connection
                // 从连接中获取输入流
                InputStream in = conn.getInputStream();
                // 对输入流进行封装
                reader = new BufferedReader(new InputStreamReader(in));
                String str = null;
                sb = new StringBuffer();
                // 从输入流中读取数据
                while ((str = reader.readLine()) != null)
                {
                    sb.append(str).append(System.getProperty("line.separator"));
                }
                // 关闭输入流
                reader.close();
                if (sb.toString().length() == 0)
                {
                    return null;
                }
                return sb.toString().substring(0,
                    sb.toString().length() - System.getProperty("line.separator").length());
            }
        }
        catch (Exception e)
        {
            LOGGER.error("getWether error", e);
            return null;
        }
        finally
        {
            if (conn != null)// 关闭连接
            {
                conn.disconnect();
            }
        }
        return null;
    }

    /**
     * 调用获取城市列表接口,返回所有数据
     * 
     * @return 返回接口数据
     */
    public static String excute(String city)
    {
        String url = "http://t.weather.sojson.com/api/weather/city/" + cityCode.get(city);// 接口URL
        String result = getWether(url);
        if (result != null)
        {
            Wether wether = new Wether();
            JSONObject wetherObj = JSONObject.parseObject(result);
            wether.setTemperature(wetherObj.getJSONObject("data").getString("wendu"));
            JSONObject detail = wetherObj.getJSONObject("data").getJSONArray(
                "forecast").getJSONObject(0);
            wether.setWeekday(detail.getString("week"));
            wether.setType(detail.getString("type"));
            wether.setDay(detail.getString("ymd"));
            return JSON.toJSONString(wether);
        }
        else
        {
            return null;
        }
    }

//    public static void main(String[] args)
//    {
//        System.out.println(excute("延安"));
//    }
}
