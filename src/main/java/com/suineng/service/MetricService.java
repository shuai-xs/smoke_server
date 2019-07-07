package com.suineng.service;


import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suineng.bean.Metric;
import com.suineng.bean.ThresholdValue;
import com.suineng.cache.DeviceCache;
import com.suineng.common.ParseUtil;
import com.suineng.dao.MetricMapper;
import com.suineng.dao.PositionMapper;


@Service
public class MetricService
{

    private static final DecimalFormat df3 = new DecimalFormat("###.00");

    @Autowired
    private AlertThresholdConfig alertThresholdConfig;

    @Autowired
    private MetricMapper metricMapper;

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private DeviceCache deviceCache;

    public Long getPosition()
    {
        return positionMapper.getPosition();
    }

    public void insert(Long lastTime)
    {
        positionMapper.insert(lastTime);
    }

    public void insert(Metric metric)
    {
        metricMapper.insert(metric);
    }

    public List<Metric> getMetricData(String moduleId, String zoneId, long startTime, long endTime,
                                      int pageIndex, int pageSize)
    {
        List<String> idList = deviceCache.getDeviceIdList(moduleId, zoneId);
        List<Metric> list = metricMapper.getMetricLimit(idList, startTime, endTime,
            pageIndex * pageSize, pageSize);
        DateFormat format1 = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        JSONArray array = new JSONArray();
        for (Metric metric : list)
        {
            JSONObject object = new JSONObject();
            object.put("id", metric.getId());
            object.put("value0", metric.getValue0());
            object.put("time", format1.format(metric.getTime()));
            array.add(object);
        }
        return list;
    }

    /**
     * 获取当月每个舱的平均温度显示图 { "RQT":xx, "DLT":xx, "ZHT":xx }
     * 
     * @return
     */
    public String getCurMonthTempInfo()
    {
        long endTime = System.currentTimeMillis();
        long startTime = getMonthStart(0, endTime);
        List<Metric> metricList = metricMapper.getMetric(deviceCache.getAllDeviceIdList(),
            startTime, endTime);
        int rqNum = 0;
        int zhNum = 0;
        int dlNum = 0;
        float rqTotal = 0;
        float zhTotal = 0;
        float dlTotal = 0;
        for (Metric metric : metricList)
        {
            if (metric.getId().startsWith("RQT"))
            {
                rqNum++ ;
                rqTotal += metric.getValue0();
                continue;
            }
            if (metric.getId().startsWith("DLT"))
            {
                dlNum++ ;
                dlTotal += metric.getValue0();
                continue;
            }
            if (metric.getId().startsWith("ZHT"))
            {
                zhNum++ ;
                zhTotal += metric.getValue0();
                continue;
            }
        }
        JSONObject object = new JSONObject();
        float rqt = 0;
        if(0 != rqNum)
        {
            rqt = rqTotal / rqNum;
        }
        ThresholdValue thresholdValue = alertThresholdConfig.getThreshold("RQT", endTime);
        object.put("RQT_D", "");
        if (null != thresholdValue)
        {
            if (rqt > thresholdValue.highThreshold)
            {
                double value = (rqt - thresholdValue.highThreshold) / thresholdValue.highThreshold;
                object.put("RQT_D", "比目标值偏高" + NumberFormat.getPercentInstance().format(value));
            }
            else
            {
                if (rqt < thresholdValue.lowThreshold)
                {
                    double value = (thresholdValue.lowThreshold - rqt) / thresholdValue.lowThreshold;
                    object.put("RQT_D", "比目标值偏低" + NumberFormat.getPercentInstance().format(value));
                }else
                {
                    object.put("RQT_D", "和目标值一样");
                }
            }


        }
        DecimalFormat df = new DecimalFormat("0.0");
        object.put("RQT", df.format(rqt));

        float dlt = 0;
        if(0 != dlNum)
        {
            dlt = dlTotal / dlNum;
        }
        thresholdValue = alertThresholdConfig.getThreshold("DLT", endTime);
        object.put("DLT_D", "");
        if (null != thresholdValue)
        {
            if (dlt > thresholdValue.highThreshold)
            {
                double value = (dlt - thresholdValue.highThreshold) / thresholdValue.highThreshold;
                object.put("DLT_D", "比目标值偏高" + NumberFormat.getPercentInstance().format(value));
            }
            else
            {
                if (dlt < thresholdValue.lowThreshold)
                {
                    double value = (thresholdValue.lowThreshold - dlt) / thresholdValue.lowThreshold;
                    object.put("DLT_D", "比目标值偏低" + NumberFormat.getPercentInstance().format(value));
                }
                else
                {
                    object.put("DLT_D", "和目标值一样");
                }
            }


        }

        object.put("DLT", df.format(dlt));

        float zht = 0;
        if(0 != zhNum)
        {
            zht = zhTotal / zhNum;
        }
        thresholdValue = alertThresholdConfig.getThreshold("ZHT", endTime);
        object.put("ZHT_D", "");
        if (null != thresholdValue)
        {
            if (zht > thresholdValue.highThreshold)
            {
                double value = (zht - thresholdValue.highThreshold) / thresholdValue.highThreshold;
                object.put("ZHT_D", "比目标值偏高" + NumberFormat.getPercentInstance().format(value));
            }
            else
            {
                if (zht < thresholdValue.lowThreshold)
                {
                    double value = (thresholdValue.lowThreshold - zht) / thresholdValue.lowThreshold;
                    object.put("ZHT_D", "比目标值偏低" + NumberFormat.getPercentInstance().format(value));
                }
                else
                {
                    object.put("ZHT_D", "和目标值一样");
                }
            }
        }
        object.put("ZHT", df.format(zht));
        return object.toJSONString();
    }

    private long getDayStart(int offset, long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.DAY_OF_MONTH, offset);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getDayEnd(int offset, long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.DAY_OF_MONTH, offset);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTimeInMillis();
    }

    private long getMonthStart(int offset, long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.add(Calendar.MONTH, offset);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

//    private long getMonthEnd(int offset, long timestamp)
//    {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(timestamp);
//        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
//        calendar.add(Calendar.MONTH, offset);
//        calendar.set(Calendar.HOUR_OF_DAY, 23);
//        calendar.set(Calendar.MINUTE, 59);
//        calendar.set(Calendar.SECOND, 59);
//        return calendar.getTimeInMillis();
//    }

    /**
     * 获取湿度/氧气浓度近6日信息 [ { "4.6":{ "RQH":xx, "RQO":xx, "ZHH":xx, "ZHO":xx, "DLH":xx, "DLO":xx } },
     * "4.5":{ "RQH":xx, "RQO":xx, "ZHH":xx, "ZHO":xx, "DLH":xx, "DLO":xx } ]
     * 
     * @return
     */
    public String get6daysHumidityInfo()
    {
        List<String> idList = deviceCache.getAllDeviceIdList();
        Iterator<String> ite = idList.iterator();
        while (ite.hasNext())
        {
            String idName = ite.next();
            if (idName.startsWith("RQH") || idName.startsWith("DLH") || idName.startsWith("ZHH")
                || idName.startsWith("RQO") || idName.startsWith("DLO")
                || idName.startsWith("ZHO"))
            {

            }
            else
            {
                ite.remove();
            }
        }
        long timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MM.dd");
        JSONArray array = new JSONArray();
        for (int i = 0; i < 6; i++ )
        {
            long startTime = this.getDayStart(0 - i, timestamp);
            long endTime = this.getDayEnd(0 - i, timestamp);
            JSONObject object = this.getHumityInfo(idList, startTime, endTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(endTime);
            String key = sdf.format(calendar.getTime());
            object.put("date", key);
            array.add(object);
        }
        return array.toJSONString();
    }

    public List<Metric> getMetric(List<String> idList, long startTime, long endTime)
    {
        return metricMapper.getMetric(idList, startTime, endTime);
    }

    /**
     * 获取某个月的每个舱的湿度平均值和氧气浓度平均值 { "RQH":xx, "RQO":xx, "ZHH":xx, "ZHO":xx, "DLH":xx, "DLO":xx }
     * 
     * @param idList
     * @param startTime
     * @param endTime
     * @return
     */
    private JSONObject getHumityInfo(List<String> idList, long startTime, long endTime)
    {
        List<Metric> metricList = this.metricMapper.getMetric(idList, startTime, endTime);
        // 燃气舱湿度
        Average rqHAverage = new Average();
        // 综合舱湿度
        Average zhHAverage = new Average();
        // 电力舱湿度
        Average dlHAverage = new Average();
        // 燃气舱氧气浓度
        Average rqOAverage = new Average();
        // 综合舱氧气浓度
        Average zhOAverage = new Average();
        // 电力舱氧气浓度
        Average dlOAverage = new Average();
        for (Metric metric : metricList)
        {
            if (metric.getId().startsWith("RQH"))
            {
                rqHAverage.count++ ;
                rqHAverage.totalValue += metric.getValue0();
                continue;
            }
            if (metric.getId().startsWith("ZHH"))
            {
                zhHAverage.count++ ;
                zhHAverage.totalValue += metric.getValue0();
                continue;
            }
            if (metric.getId().startsWith("DLH"))
            {
                dlHAverage.count++ ;
                dlHAverage.totalValue += metric.getValue0();
                continue;
            }
            if (metric.getId().startsWith("RQO"))
            {
                rqOAverage.count++ ;
                rqOAverage.totalValue += metric.getValue0();
                continue;
            }
            if (metric.getId().startsWith("ZHO"))
            {
                zhOAverage.count++ ;
                zhOAverage.totalValue += metric.getValue0();
                continue;
            }
            if (metric.getId().startsWith("DLO"))
            {
                dlOAverage.count++ ;
                dlOAverage.totalValue += metric.getValue0();
                continue;
            }
        }
        JSONObject object = new JSONObject();
        object.put("RQH", getFixFloat(rqHAverage.totalValue, rqHAverage.count));
        object.put("RQO", getFixFloat(rqOAverage.totalValue , rqOAverage.count));
        object.put("ZHH", getFixFloat(zhHAverage.totalValue, zhHAverage.count));
        object.put("ZHO", getFixFloat(zhOAverage.totalValue, zhOAverage.count));
        object.put("DLH", getFixFloat(dlHAverage.totalValue, dlHAverage.count));
        object.put("DLO", getFixFloat(dlOAverage.totalValue, dlOAverage.count));
        return object;
    }

    private class Average
    {
        int count = 0;

        float totalValue = 0;
    }

    /**
     * 展示某个舱的某个分区下的所有指标，按照num进行切分，每个分片求取对应的平均值。
     * 比如查询当天的指标，num为4，则结果为{0点~6点的平均值，6~12点平均值，12~18点平均值，18~24点平均值} { "{end时间}"： { "T":xx, "H":xx,
     * "O":xx, "Y":xx } } [{ "date":xx, "T":xx, "H",xx, "O":xx, "Y":xx } ]
     * 
     * @param moduleId
     * @param zoneId
     * @param startTime
     * @param endTime
     * @param num
     * @return
     */
    public JSONObject getMetricInfo(String moduleId, String zoneId, long startTime, long endTime,
                                    int num, String formatType)
    {
        List<String> idList = this.deviceCache.getDeviceIdList(moduleId, zoneId);
        long batchTime = (endTime - startTime) / num;
        long end = startTime + batchTime;
        int index = 0;
        JSONArray array = new JSONArray();
        Map<String, Float> maxMap = new HashMap<String, Float>();
        Map<String, Float> minMap = new HashMap<String, Float>();
        while (index++ < num)
        {
            List<Metric> metricList = new LinkedList<>();
            if (!idList.isEmpty())
            {
                metricList = this.metricMapper.getMetric(idList, startTime, end);
            }
            JSONObject metricObj = this.getAverage(metricList, maxMap, minMap);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            if ("hour".equals(formatType))
            {
                sdf = new SimpleDateFormat("yyyy/MM/dd HH");
            }
            metricObj.put("date", sdf.format(end));
            array.add(metricObj);
            startTime += batchTime;
            end += batchTime;
        }
        JSONObject resultObj = new JSONObject();
        resultObj.put("data", array);
        for (Map.Entry<String, Float> entry : maxMap.entrySet())
        {
            String key = entry.getKey();
            String metricType = moduleId + key;
            ThresholdValue thresholdValue = this.alertThresholdConfig.getThreshold(metricType,
                endTime);
            JSONObject object = new JSONObject();
            object.put("maxNum", entry.getValue());
            object.put("minNum", minMap.get(key));
            float keyMinvalue = minMap.get(key);
            if (null != thresholdValue)
            {
                if (entry.getValue() < thresholdValue.highThreshold)
                {
                    object.put("maxCompare", -1);
                    object.put("maxPercent",
                        getFixFloat((thresholdValue.highThreshold - entry.getValue())
                                    , thresholdValue.highThreshold));
                }
                else
                {
                    object.put("maxCompare", 1);
                    object.put("maxPercent",
                        getFixFloat((entry.getValue() - thresholdValue.highThreshold)
                                    , thresholdValue.highThreshold));
                }
                if (keyMinvalue < thresholdValue.lowThreshold)
                {
                    object.put("minCompare", -1);
                    object.put("minPercent",
                        getFixFloat((thresholdValue.lowThreshold - keyMinvalue)
                                    , thresholdValue.lowThreshold));
                }
                else
                {
                    object.put("minCompare", 1);
                    object.put("minPercent",
                        getFixFloat((keyMinvalue - thresholdValue.lowThreshold)
                                    , thresholdValue.lowThreshold));
                }
            }
            resultObj.put(key, object);
        }
        return resultObj;
    }

    private float getFixFloat(double total, double num)
    {
        if(0 == num)
        {
            return 0;
        }
        double value = total/num;
        return value == 0.0 ? 0f : Float.valueOf(df3.format(value));
    }

    private float getFixFloat(float total, int num)
    {
        if(0 == num)
        {
            return 0;
        }
        double value = total/num;
        return value == 0.0 ? 0f : Float.valueOf(df3.format(value));
    }

    /**
     * 求取指标的平均值 { "T":xx, "H":xx, "Y":xx, "O":xx }
     * 
     * @param metricList
     * @return
     */
    private JSONObject getAverage(List<Metric> metricList, Map<String, Float> maxMap,
                                  Map<String, Float> minMap)
    {
        MetricValue TMetric = new MetricValue();
        if (maxMap.containsKey("T"))
        {
            TMetric.maxValue = maxMap.get("T");
        }
        if (minMap.containsKey("T"))
        {
            TMetric.minValue = minMap.get("T");
        }
        MetricValue HMetric = new MetricValue();
        if (maxMap.containsKey("H"))
        {
            HMetric.maxValue = maxMap.get("H");
        }
        if (minMap.containsKey("H"))
        {
            HMetric.minValue = minMap.get("H");
        }
        MetricValue OMetric = new MetricValue();
        if (maxMap.containsKey("O"))
        {
            OMetric.maxValue = maxMap.get("O");
        }
        if (minMap.containsKey("O"))
        {
            OMetric.minValue = minMap.get("O");
        }
        MetricValue YMetric = new MetricValue();
        if (maxMap.containsKey("Y"))
        {
            YMetric.maxValue = maxMap.get("Y");
        }
        if (minMap.containsKey("Y"))
        {
            YMetric.minValue = minMap.get("Y");
        }
        for (Metric metric : metricList)
        {
            String metricType = ParseUtil.getMetricType(metric.getId());
            char group = ParseUtil.getGroupInfo(metric.getId());
            if ("T".equals(metricType))
            {
                TMetric.count++ ;
                TMetric.totalValue += metric.getValue0();
                if (TMetric.maxValue < metric.getValue0())
                {
                    TMetric.maxValue = metric.getValue0();
                }
                if (TMetric.minValue > metric.getValue0())
                {
                    TMetric.minValue = metric.getValue0();
                }
                continue;
            }
            if ("H".equals(metricType))
            {
                HMetric.count++ ;
                HMetric.totalValue += metric.getValue0();
                if (HMetric.maxValue < metric.getValue0())
                {
                    HMetric.maxValue = metric.getValue0();
                }
                if (HMetric.minValue > metric.getValue0())
                {
                    HMetric.minValue = metric.getValue0();
                }
                continue;
            }
            if ("O".equals(metricType))
            {
                OMetric.count++ ;
                OMetric.totalValue += metric.getValue0();
                if (OMetric.maxValue < metric.getValue0())
                {
                    OMetric.maxValue = metric.getValue0();
                }
                if (OMetric.minValue > metric.getValue0())
                {
                    OMetric.minValue = metric.getValue0();
                }
                continue;
            }
            if ("Y".equals(metricType))
            {
                YMetric.count++ ;
                YMetric.totalValue += metric.getValue0();
                if (YMetric.maxValue < metric.getValue0())
                {
                    YMetric.maxValue = metric.getValue0();
                }
                if (YMetric.minValue > metric.getValue0())
                {
                    YMetric.minValue = metric.getValue0();
                }
                continue;
            }
        }
        JSONObject resultObj = new JSONObject();

        resultObj.put("T", TMetric.getValue());
        maxMap.put("T", TMetric.getMaxValue());
        minMap.put("T", TMetric.getMinValue());
        resultObj.put("H", HMetric.getValue());
        maxMap.put("H", HMetric.getMaxValue());
        minMap.put("H", HMetric.getMinValue());
        resultObj.put("O", OMetric.getValue());
        maxMap.put("O", OMetric.getMaxValue());
        minMap.put("O", OMetric.getMinValue());
        resultObj.put("Y", YMetric.getValue());
        maxMap.put("Y", YMetric.getMaxValue());
        minMap.put("Y", YMetric.getMinValue());
        return resultObj;
    }

    /**
     * @param startTime
     * @param endTime
     * @param num
     *            分片数
     * @return
     */
    public String getAvgMetric(String moduleId, String zoneId, long startTime, long endTime,
                               int num, String formatType)
    {
        return getMetricInfo(moduleId, zoneId, startTime, endTime, num, formatType).toJSONString();
    }

    /**
     * 获取当前某个舱的某个分区的具体指标详细 { "1":{ "T":xx, "H":xx, "O":xx, "Y":xx }, "2":{ "T":xx, "H":xx, "O":xx,
     * "Y":xx } }
     * 
     * @param moduleId
     * @param zoneId
     * @return
     */
    public String getCurMetricInfo(String moduleId, String zoneId, long startTime, long endTime)
    {
        List<Metric> metricList = this.getModuleInfo(moduleId, zoneId, startTime, endTime);
        if (metricList == null)
        {
            return null;
        }
        JSONObject object = new JSONObject();
        for (Metric metric : metricList)
        {
            String metricType = ParseUtil.getMetricType(metric.getId());
            if ("T".equals(metricType) || "H".equals(metricType) || "O".equals(metricType)
                || "Y".equals(metricType))
            {
                char group = ParseUtil.getGroupInfo(metric.getId());
                if ('1' == group)
                {
                    object.put(metricType + group, metric.getValue0());
                }
                else
                {
                    object.put(metricType + "2", metric.getValue0());
                }
            }
        }
        for (String key : new String[] {"H1", "H2", "T1", "T2", "O1", "O2", "Y1", "Y2"})
        {
            if (!object.containsKey(key))
            {
                object.put(key, 0);
            }
        }
        return object.toJSONString();
    }

    private List<Metric> getModuleInfo(String moduleId, String zoneId, long startTime,
                                       long endTime)
    {
        List<String> idList = this.deviceCache.getDeviceIdList(moduleId, zoneId);
        if (idList == null || idList.isEmpty())
        {
            return new LinkedList<Metric>();
        }
        return this.metricMapper.getMetric(idList, startTime, endTime);
    }

    private class MetricValue
    {
        // 指标类型,温度，湿度，氧气浓度，液位，T/H/O/Y
        String metricType;

        int count;

        float totalValue = 0;

        float maxValue = Float.MIN_VALUE;

        float minValue = Float.MAX_VALUE;

        public float getMaxValue()
        {
            if (Float.MIN_VALUE == maxValue)
            {
                return 0;
            }
            return maxValue;
        }

        public float getMinValue()
        {
            if (Float.MAX_VALUE == minValue)
            {
                return 0;
            }
            return minValue;
        }

        public float getValue()
        {
            if (0 == totalValue)
            {
                return 0;
            }
            return totalValue / count;
        }

    }

}
