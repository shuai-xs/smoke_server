package com.suineng.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suineng.bean.MonthInfo;
import com.suineng.bean.ThresholdInfo;
import com.suineng.bean.ThresholdValue;
import com.suineng.common.AlertConst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 告警阈值配置
 */
@Component
public class AlertThresholdConfig {

    private static final Logger logger = LoggerFactory.getLogger(AlertThresholdConfig.class);

    /**
     * 设备指标阈值配置文件路径
     */
    @Value("${alert.config:3600}")
    private String configPath;

    /**
     * 设备指标阈值
     */
    private Map<String, ThresholdInfo> thresholdMap = new ConcurrentHashMap<String, ThresholdInfo>();

    private static final String MONTH_SPLIT = ".";
    @PostConstruct
    public void init()
    {
        System.out.println(configPath);
        this.loadConfig();
    }

    /**
     *
     * @param metricType RQH, RQT
     * @param timestamp 指标上报时间
     * @return
     */
    public ThresholdValue getThreshold(String metricType, long timestamp)
    {
        ThresholdInfo info = thresholdMap.get(metricType);
        if(null == info)
        {
            return null;
        }
        return info.getThreshold(timestamp);
    }

    public String readToString(String fileName) {
        String encoding = "utf8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (Exception e) {
            logger.error("read file error", e);
        }
        finally {
            if(null != in)
            {
                try
                {
                    in.close();
                }catch (Exception e)
                {
                    logger.error("close fail", e);
                }
            }
        }
        String text = new String(filecontent);
        return text;
    }

    /**
     * 加载阈值配置文件
     */
    private void loadConfig()
    {
        String text = this.readToString(this.configPath);
        JSONArray array = JSONArray.parseArray(text);
        Iterator<Object> ite = array.iterator();
        while(ite.hasNext())
        {
            JSONObject obj = (JSONObject)ite.next();
            ThresholdInfo info = new ThresholdInfo();
            info.metricType = obj.getString(AlertConst.METRICNAMTE);
            MonthInfo monthInfo = new MonthInfo();
            monthInfo.start = obj.getInteger(AlertConst.START_MONTH);
            monthInfo.end = obj.getInteger(AlertConst.END_MONTH);
            ThresholdValue thresholdValue = new ThresholdValue();
            thresholdValue.highThreshold = obj.getDouble(AlertConst.HIGH_THRESHOLD);
            thresholdValue.lowThreshold = obj.getDouble(AlertConst.LOW_THRESHOLD);

            ThresholdInfo thresholdInfo = thresholdMap.get(info.metricType);
            if(null == thresholdInfo)
            {
                info.thresholdMap.put(monthInfo, thresholdValue);
                thresholdMap.put(info.metricType, info);
                continue;
            }
            thresholdInfo.thresholdMap.put(monthInfo, thresholdValue);
        }
    }
}
