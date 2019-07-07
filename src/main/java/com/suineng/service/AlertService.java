package com.suineng.service;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suineng.bean.Alert;
import com.suineng.common.AlertConst;
import com.suineng.common.SuinengConst;
import com.suineng.dao.AlertMapper;

@Service
public class AlertService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertService.class);

    @Autowired
    private AlertMapper alertMapper;


    /**
     * 确认告警
     * @param alert
     * @return
     */
    public void confirmAlert(Alert alert)
    {
        alert.setStatus(AlertConst.STATUS_CONFIRMED);
        alert.setConfirmTime(System.currentTimeMillis());
        alertMapper.updateAlert(alert);
//        session.getSession().flushStatements();
//        session.getSession().commit(true);
    }

    /**
     * 清除告警
     * @param alert
     * @return
     */
    public void cleanAlert(Alert alert)
    {
        alert.setStatus(AlertConst.STATUS_PROCESSED);
        alert.setConfirmTime(System.currentTimeMillis());
        alertMapper.updateAlert(alert);
//        session.getSession().flushStatements();
//        session.getSession().commit(true);
    }


    /**
     * 获取上周时间
     * @return
     */
    private long getLstWeekTime()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long lstWeekTime = calendar.getTimeInMillis();
        return lstWeekTime;
    }


    /**
     * {
     *     "totalCount":xx,
     *     "result":[{},{}]
     * }
     * @param startTime
     * @param endTime
     * @param status
     * @param zoneId
     * @param curIndex
     * @param pageSize
     * @return
     */
    public String getAlertList(Long startTime, Long endTime, Integer status, String mouduleId, Integer zoneId, Integer curIndex, Integer pageSize)
    {
        JSONArray array = new JSONArray();
        try
        {
            Alert alert = new Alert();
            if(null != mouduleId)
            {
                alert.setModuleId(mouduleId);
            }
            if(null != zoneId)
            {
                alert.setZoneId(zoneId);
            }
            if(null != startTime)
            {
                alert.setStartTime(startTime);
            }
            if(null != endTime)
            {
                alert.setEndTime(endTime);
            }
            if(null != status)
            {
                alert.setStatus(status);
            }
            List<Alert> alertList = this.alertMapper.getAlert(alert,  curIndex * pageSize,  pageSize);
           
            for(Alert key : alertList)
            {
                JSONObject object = new JSONObject();
                DateFormat format1 = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                object.put("id", key.getId());
                object.put("value", key.getMetricValue());
                object.put("position", getPosition(key.getModuleId(), key.getZoneId()));
                object.put("name", getAlertName(key.getMetricType()));
                object.put("info", getAlertInfo(key.getMetricType(), key.getAlertType()));
                object.put("status", getStatusName(key.getStatus()));
                //object.put("occurTime", format1.format(key.getCreateTime()));
                object.put("occurTime", key.getCreateTime());
                object.put("clearTime", key.getConfirmTime());
                object.put("remark", "");
                object.put("checked", false);
                array.add(object);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return array.toJSONString();
    }

    private String getStatusName(int status)
    {
        //告警状态,2表示已处理、1表示已确认、0表示待确认
        if(2 == status)
        {
            return "已处理";
        }
        if(1 == status)
        {
            return "已确认";
        }
        if(0 == status)
        {
            return "待确认";
        }
        return "未知";
    }
    private String getAlertInfo(String metricType, int type)
    {
        String info = null;
        if(SuinengConst.TEMPERATURE.equals(metricType))
        {
            info= "温度";
        }
        if(SuinengConst.HUMIDITY.equals(metricType))
        {
            info= "湿度";
        }
        if(SuinengConst.LEVEL.equals(metricType))
        {
            info= "液位";
        }
        if(SuinengConst.OXYGEN_CONCENTRATION.equals(metricType))
        {
            info= "氧气浓度";
        }
        if(type>0)
        {
            info = info + "偏高";
        }
        else
        {
            info = info +"偏低";
        }
        return info;
    }
    private String getAlertName(String type)
    {
        if(SuinengConst.TEMPERATURE.equals(type))
        {
            return "温度检测仪";
        }
        if(SuinengConst.HUMIDITY.equals(type))
        {
            return "湿度检测仪";
        }
        if(SuinengConst.LEVEL.equals(type))
        {
            return "液位检测仪";
        }
        if(SuinengConst.OXYGEN_CONCENTRATION.equals(type))
        {
            return "氧气浓度检测仪";
        }
        return "未知";
    }
    private String getPosition(String moduleId, int zoneId)
    {
        if(SuinengConst.RQ_MODULE.equals(moduleId))
        {
            return "燃气舱"+zoneId+"号";
        }
        if(SuinengConst.DL_MODULE.equals(moduleId))
        {
            return "电力舱"+zoneId+"号";
        }
        if(SuinengConst.ZH_MODULE.equals(moduleId))
        {
            return "综合舱"+zoneId+"号";
        }
        return "未知";
    }

    /**
     * 告警页面默认显示的告警列表
     * {
     *     "totalCount":xx,
     *     "curIndex":xx,
     *     "pageSize":xx,
     *     "result":[{},{}]
     * }
     * @return
     */
    public String getDefaultList(int curIndex, int pageSize)
    {
        long startTime = -1;
        long endTime = System.currentTimeMillis();
        int totalCount = this.alertCountAll(startTime, endTime);
        JSONArray array = this.queryAlertAll(startTime, endTime, curIndex, pageSize);
        JSONObject resultObj = new JSONObject();
        resultObj.put("totalCount", totalCount);
        resultObj.put("curIndex", curIndex);
        resultObj.put("pageSize", pageSize);
        resultObj.put("result", array);
        return resultObj.toJSONString();
    }
    /**已处理告警，各种设备占比
     * {
     *     "day":{
     *          "oxRate":xx,//氧气浓度检测仪占比
     *          "levelRate":xx,//液位浓度检测仪占比
     *          "tempRate":xx,//温度检测仪占比
     *          "humidityRate":xx,//湿度检测仪占比
     *      },
     *      "week":{
     *          "oxRate":xx,//氧气浓度检测仪占比
     *          "levelRate":xx,//液位浓度检测仪占比
     *          "tempRate":xx,//温度检测仪占比
     *          "humidityRate":xx,//湿度检测仪占比
     *      },
     *      "month":{
     *          "oxRate":xx,//氧气浓度检测仪占比
     *          "levelRate":xx,//液位浓度检测仪占比
     *          "tempRate":xx,//温度检测仪占比
     *          "humidityRate":xx,//湿度检测仪占比
     *      }
     * }
     * @return
     */
    public String getDefaultProcessedInfo()
    {
        long endTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(endTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long dayStartTime = calendar.getTimeInMillis();
        JSONArray dayObj = this.getProcessedDescInfo(dayStartTime, endTime);
        calendar.set(Calendar.DAY_OF_WEEK, 1);
        long weekStartTime = calendar.getTimeInMillis();
        JSONArray weekObj = this.getProcessedDescInfo(weekStartTime, endTime);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        long monthStartTime = calendar.getTimeInMillis();
        JSONArray monthObj = this.getProcessedDescInfo(monthStartTime, endTime);
        JSONObject result = new JSONObject();
        result.put("day", dayObj);
        result.put("week", weekObj);
        result.put("month", monthObj);
        return result.toJSONString();
    }


    /**
     * 告警页面统计已处理告警信息
     {
     *     "oxRate":xx,//氧气浓度检测仪占比
     *     "levelRate":xx,//液位浓度检测仪占比
     *     "tempRate":xx,//温度检测仪占比
     *     "humidityRate":xx,//湿度检测仪占比
     * }
     * @return
     */
    private JSONArray getProcessedDescInfo(long startTime, long endTime)
    {
        //当前已处理告警数
        Alert alert = new Alert();
        alert.setStartTime(startTime);
        alert.setEndTime(endTime);
        alert.setStatus(AlertConst.STATUS_PROCESSED);
        int processedCount =this.alertMapper.countAlertNum(alert);
        alert.setMetricType(AlertConst.OXGEN);
        //int processedCount = this.alertCount(startTime, endTime, AlertConst.STATUS_PROCESSED);
        int oxCount = this.alertMapper.countAlertNum(alert);
        alert.setMetricType(AlertConst.LEVEL);
        int levelCount = this.alertMapper.countAlertNum(alert);
        alert.setMetricType(AlertConst.TEMPERATURE);
        int temperatureCount = this.alertMapper.countAlertNum(alert);
        alert.setMetricType(AlertConst.HUMIDITY);
        int humidityCount = this.alertMapper.countAlertNum(alert);;
        int totalCount = oxCount+levelCount+temperatureCount+humidityCount;
        JSONArray array = new JSONArray();
        JSONObject oxObject = new JSONObject();
        oxObject.put("name","氧气浓度检测仪");
        if(0 == totalCount)
        {
            oxObject.put("value",0);
        }
        else
        {
            oxObject.put("value",oxCount*100 / totalCount);
            //oxObject.put("value",oxCount);
        }
        array.add(oxObject);

        JSONObject levelObject = new JSONObject();
        levelObject.put("name","液位检测仪");
        if(0 == totalCount)
        {
            levelObject.put("value",0);
        }
        else
        {
            levelObject.put("value",levelCount*100 / totalCount);
            //levelObject.put("value",levelCount);
        }
        array.add(levelObject);

        JSONObject tempObject = new JSONObject();
        tempObject.put("name","温度检测仪");
        if(0 == totalCount)
        {
            tempObject.put("value",0);
        }
        else
        {
            tempObject.put("value",temperatureCount*100 / totalCount);
            //tempObject.put("value",temperatureCount);
        }
        array.add(tempObject);

        JSONObject hObject = new JSONObject();
        hObject.put("name","湿度检测仪");
        if(0 == totalCount)
        {
            hObject.put("value",0);
        }
        else
        {
            hObject.put("value",humidityCount*100 / totalCount);
            //hObject.put("value",humidityCount);
        }
        array.add(hObject);

        return array;
    }


    public String getAlertlst6Month()
    {
        JSONObject resultObj = new JSONObject();
        long curTime = System.currentTimeMillis();
        JSONArray timeArray = new JSONArray();
        JSONArray alarmArray = new JSONArray();
        for(int i=5; i>=0; i--)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY.MM");
            long startTime = this.getMonthStart(0-i, curTime);
            long endTime = this.getMonthEnd(0-i, curTime);
            Alert alert = new Alert();
            alert.setStartTime(startTime);
            alert.setEndTime(endTime);
            int num = this.alertMapper.countAlertNum(alert);
            timeArray.add(sdf.format(endTime));
            alarmArray.add(num);
        }
        resultObj.put("time", timeArray);
        resultObj.put("alarm", alarmArray);
        return resultObj.toJSONString();
    }

    private long getMonthStart(int offset, long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.MONTH, offset);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getMonthEnd(int offset, long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.MONTH, offset);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTimeInMillis();
    }
    /**
     * 告警页面概览统计信息
     * {
     *     "totalCount":xx,//累计预警数
     *     "processedCount":xx,//已处理预警数
     *     "processedRate":xx,//已处理预警同比上周增加
     *     "unProcessedCount":xx,//未处理预警数
     *     "unProcessedRate":xx,//未处理预警同比上周增加
     *     "newCount":xx//本周新增加预警数
     *     "newRate":xx//本周预警同比增加
     * }
     *
     * @return
     */
    public String getAlertDescInfo()
    {
        long curTime = System.currentTimeMillis();
        Alert alert = new Alert();
        alert.setStartTime(0);
        alert.setEndTime(curTime);
        //累计预警数
        int count = this.alertCountAll(-1, curTime);
        alert.setStatus(AlertConst.STATUS_PROCESSED);
        //当前已处理告警数
        int processedCount = this.alertMapper.countAlertNum(alert);;
        //未处理告警
        int unprocessedCount = count - processedCount;


        long lstWeekTime = this.getLstWeekTime();
        alert.setStartTime(0);
        alert.setEndTime(lstWeekTime);
        alert.setStatus(-1);
        //截止上周，累计告警数
        int lstWeekCount = this.alertMapper.countAlertNum(alert);
        alert.setStatus(AlertConst.STATUS_PROCESSED);
        //截止上周累计处理告警数
        int lstProcessedCount = this.alertMapper.countAlertNum(alert);
        int lstUnprocessedCount = lstWeekCount - lstProcessedCount;
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        //累计告警数
        object.put("almNum", count);
        array.add(object);

        //本周新增加告警
        JSONObject newCountObject = new JSONObject();
        newCountObject.put("almNum", count);
        if(lstWeekCount>0)
        {
            if(count > lstWeekCount)
            {
                newCountObject.put("percent", NumberFormat.getPercentInstance().format((count - lstWeekCount)/lstWeekCount));
                newCountObject.put("compare", 1);
            }
            else
            {
                newCountObject.put("percent", NumberFormat.getPercentInstance().format((lstWeekCount - count)/lstWeekCount));
                newCountObject.put("compare", -1);
            }
        }
        else {
            if(count > lstWeekCount)
            {
                newCountObject.put("percent", lstWeekCount == 0 ? 0 : NumberFormat.getPercentInstance().format((count - lstWeekCount)/lstWeekCount));
                newCountObject.put("compare", 1);
            }
            else
            {
                newCountObject.put("percent", lstProcessedCount == 0 ? 0 :NumberFormat.getPercentInstance().format((lstWeekCount - count)/lstWeekCount));
                newCountObject.put("compare", -1);
            }
        }
        array.add(newCountObject);

        //已处理告警
        JSONObject processedObject = new JSONObject();
        processedObject.put("almNum", processedCount);
        if(lstProcessedCount > 0) {
            if (processedCount > lstProcessedCount) {
                processedObject.put("percent", NumberFormat.getPercentInstance().format((processedCount - lstProcessedCount) / lstProcessedCount));
                processedObject.put("compare", 1);
            } else {
                processedObject.put("percent", NumberFormat.getPercentInstance().format((lstProcessedCount - processedCount) / lstProcessedCount));
                processedObject.put("compare", -1);
            }
        }
        else
        {
            if (processedCount > lstProcessedCount) {
                processedObject.put("percent", lstProcessedCount == 0 ? 0 : NumberFormat.getPercentInstance().format((processedCount - lstProcessedCount) / lstProcessedCount));
                processedObject.put("compare", 1);
            } else {
                processedObject.put("percent", lstProcessedCount == 0 ? 0 : NumberFormat.getPercentInstance().format((lstProcessedCount - processedCount) / lstProcessedCount));
                processedObject.put("compare", lstProcessedCount == 0 ? 0 :-1);
            }
        }
        array.add(processedObject);



        //未处理预警
        JSONObject unProcessedObject = new JSONObject();
        unProcessedObject.put("almNum", unprocessedCount);
        if(lstUnprocessedCount > 0)
        {
            if(unprocessedCount > lstUnprocessedCount)
            {
                unProcessedObject.put("percent", NumberFormat.getPercentInstance().format((unprocessedCount - lstUnprocessedCount)/lstUnprocessedCount));
                unProcessedObject.put("compare", 1);
            }
            else
            {
                unProcessedObject.put("percent", NumberFormat.getPercentInstance().format((lstUnprocessedCount - unprocessedCount)/lstUnprocessedCount));
                unProcessedObject.put("compare", -1);
            }
        }
        else
        {
            if(unprocessedCount > lstUnprocessedCount)
            {
                unProcessedObject.put("percent", lstUnprocessedCount == 0 ? 0 : NumberFormat.getPercentInstance().format((unprocessedCount - lstUnprocessedCount)/lstUnprocessedCount));
                unProcessedObject.put("compare", 1);
            }
            else
            {
                unProcessedObject.put("percent", lstUnprocessedCount == 0 ? 0 : NumberFormat.getPercentInstance().format((lstUnprocessedCount - unprocessedCount)/lstUnprocessedCount));
                unProcessedObject.put("compare", -1);
            }
        }

        array.add(unProcessedObject);

        return array.toJSONString();
    }
    /**
     * 统计不同状态的告警个数
     * @param startTime
     * @param endTime
     * @param status
     * @return
     */
    private int alertCount(long startTime, long endTime, int status)
    {
        return alertMapper.countAlert(startTime, endTime, status);
    }


    /**
     * 统计告警个数
     * @param startTime
     * @param endTime
     * @return
     */
    private int alertCountAll(long startTime, long endTime)
    {
        return alertMapper.countAlertAll(startTime, endTime);
    }

    /**
     * 计算某个舱的某个区的不同状态的告警条数
     * @param startTime
     * @param endTime
     * @param status  不同的告警状态
     * @param deviceType
     * @return
     */
    private int alertCountDevice(long startTime, long endTime, int status, String deviceType)
    {
        return alertMapper.countWithDevice(startTime, endTime, deviceType, status);
    }

    /**
     * 计算某个舱的某个区的告警条数
     * @param startTime
     * @param endTime
     * @param deviceType
     * @return
     */
    private int alertCountDeviceAll(long startTime, long endTime,String deviceType)
    {
        return alertMapper.countWithDeviceAll(startTime, endTime, deviceType);
    }

    /**
     *  查询某个时间段内的告警
     * @param startTime
     * @param endTime
     * @param curIndex
     * @param pageSize
     * @return
     */
    private JSONArray queryAlert(long startTime, long endTime, int curIndex, int pageSize, int status)
    {
        List<Alert> alertList = alertMapper.queryAlert(curIndex, pageSize, startTime, endTime, status);
        JSONArray array = new JSONArray();
        for(Alert alert : alertList)
        {
            array.add(alert.toJsonObj());
        }
        return array;
    }

    /**
     *  查询某个时间段内的告警
     * @param startTime
     * @param endTime
     * @param curIndex
     * @param pageSize
     * @return
     */
    public JSONArray queryAlertAll(long startTime, long endTime, int curIndex, int pageSize)
    {
        List<Alert> alertList = alertMapper.queryAlertAll(curIndex, pageSize, startTime, endTime);
        JSONArray array = new JSONArray();
        for(Alert alert : alertList)
        {
            array.add(alert.toJsonObj());
        }
        return array;
    }

    /**
     * 查询某个舱的告警详细
     * @param curIndex
     * @param pageSize
     * @param startTime
     * @param endTime
     * @param deviceType
     * @param status
     * @return
     */
    private JSONArray queryAlertWithDevice(int curIndex, int pageSize, long startTime, @Param("endTime") long endTime,
                                        @Param("deviceType") String deviceType, @Param("status") int status)
    {
        List<Alert> alertList = alertMapper.queryAlertWithDevice(curIndex,pageSize, startTime, endTime, deviceType, status);
        JSONArray array = new JSONArray();
        for(Alert alert : alertList)
        {
            array.add(alert.toJsonObj());
        }
        return array;
    }

    private JSONArray queryAlertWithDeviceAll(int curIndex, int pageSize, long startTime, @Param("endTime") long endTime,
                                       @Param("deviceType") String deviceType)
    {
        List<Alert> alertList = alertMapper.queryAlertWithDeviceAll(curIndex,pageSize, startTime, endTime, deviceType);
        JSONArray array = new JSONArray();
        for(Alert alert : alertList)
        {
            array.add(alert.toJsonObj());
        }
        return array;
    }


    /**
     * 查询某个舱下的某个区的告警详细
     * @param curIndex
     * @param pageSize
     * @param startTime
     * @param endTime
     * @param deviceType
     * @param zoneId
     * @param status
     * @return
     */
    private JSONArray queryAlertWithZone(int curIndex, int pageSize, long startTime, @Param("endTime") long endTime,
                                       @Param("deviceType") String deviceType, @Param("zoneId") int zoneId,
                                       @Param("status") int status)
    {
        List<Alert> alertList = alertMapper.queryAlertWithZone(curIndex,pageSize, startTime, endTime, deviceType, zoneId, status);
        JSONArray array = new JSONArray();
        for(Alert alert : alertList)
        {
            array.add(alert.toJsonObj());
        }
        return array;
    }
    public void insertAlert(Alert alert)
    {
        try
        {
            alertMapper.insertAlert(alert);
        }catch (Exception e)
        {
            System.out.println("insert alert fail");
        }
    }

}
