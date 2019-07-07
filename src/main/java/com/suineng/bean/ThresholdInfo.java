package com.suineng.bean;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ThresholdInfo {

    public String metricType;

    /**
     * 告警閾值配置，不同的月份，配置值不一樣
     */
    public Map<MonthInfo, ThresholdValue> thresholdMap = new HashMap<MonthInfo, ThresholdValue>();

    private int getMonth(long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar.get(Calendar.MONTH);
    }
    public ThresholdValue getThreshold(long timestamp)
    {
        int month = this.getMonth(timestamp);
        for(Map.Entry<MonthInfo, ThresholdValue> entry : thresholdMap.entrySet())
        {
            MonthInfo monthInfo = entry.getKey();
            if(monthInfo.end >= month && monthInfo.start <= month)
            {
                return entry.getValue();
            }
        }
        return null;
    }
}
