package com.suineng.bean;


import java.text.SimpleDateFormat;
import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.suineng.common.AlertConst;


////设备id， 报警信息，告警值(指标值)，状态，告警时间，确认时间
// String id, String alertInfo, float metricValue, int status, long createTime, long confirmTime);
public class Alert
{

    private long startTime = -1;

    private long endTime = -1;

    private String id;

    // 设备类型，燃气舱，电力舱，综合舱
    private String moduleId;

    // 区id，表示一区，二区，三区，四区
    private int zoneId = 0;

    // 指标类型，O表示氧气浓度，Y表示液位，T表示温度，H表示湿度
    private String metricType;

    // 告警类型，-1表示低于阈值，1表示高于阈值
    private int alertType = 0;

    private float metricValue;

    // 告警状态,2表示已处理、1表示已确认、0表示待确认
    private int status = -1;

    private long createTime;

    private long confirmTime;

    private boolean checked = false;

    private int pageSize = 10;

    public boolean isChecked()
    {
        return checked;
    }

    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    public long getEndTime()
    {
        return endTime;
    }

    public void setEndTime(long endTime)
    {
        this.endTime = endTime;
    }

    public String getModuleId()
    {
        return moduleId;
    }

    public void setModuleId(String moduleId)
    {
        this.moduleId = moduleId;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public float getMetricValue()
    {
        return metricValue;
    }

    public void setMetricValue(float metricValue)
    {
        this.metricValue = metricValue;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        if (null == status)
        {
            return;
        }
        this.status = status;
    }

    public long getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(long createTime)
    {
        this.createTime = createTime;
    }

    public long getConfirmTime()
    {
        return confirmTime;
    }

    public void setConfirmTime(long confirmTime)
    {
        this.confirmTime = confirmTime;
    }

    public int getZoneId()
    {
        return zoneId;
    }

    public void setZoneId(Integer zoneId)
    {
        if (null == zoneId)
        {
            return;
        }
        this.zoneId = zoneId;
    }

    public int getAlertType()
    {
        return alertType;
    }

    public void setAlertType(int alertType)
    {
        this.alertType = alertType;
    }

    public String getMetricType()
    {
        return metricType;
    }

    public void setMetricType(String metricType)
    {
        this.metricType = metricType;
    }

    public JSONObject toJsonObj()
    {
        JSONObject object = new JSONObject();
        object.put(AlertConst.ID, id);
        object.put(AlertConst.ADDRESS, getAddress());
        object.put(AlertConst.ALERT_NAME, this.getAlertName());
        object.put(AlertConst.ALERT_INFO, getAlertInfo());
        object.put(AlertConst.ALERT_VALUE, this.getMetricValue());
        object.put(AlertConst.ALERT_STATUS, getAlertStatus());
        object.put(AlertConst.ALERT_TIME, longToDate(this.createTime));
        object.put(AlertConst.CONFIRM_TIME, longToDate(this.confirmTime));
        return null;
    }

    private String longToDate(long value)
    {
        Date date = new Date(value);
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sd.format(date);

    }

    private String getAlertStatus()
    {
        // 1表示已处理、0表示已确认、-1表示待确认
        if (AlertConst.STATUS_PROCESSED == this.status)
        {
            return "已处理";
        }
        if (AlertConst.STATUS_CONFIRMED == this.status)
        {
            return "已确认";
        }
        if (AlertConst.STATUS_NOT_CONFIRM == status)
        {
            return "待确认";
        }
        return null;
    }

    private String getAlertInfo()
    {
        String idInfo = id.split("_")[0];
        char[] infoArray = idInfo.toCharArray();
        char type = infoArray[2];
        String metricType = "";
        if (type == 'H')
        {
            metricType = "湿度";
        }
        if (type == 'T')
        {
            metricType = "温度";
        }
        if (type == 'Q')
        {
            metricType = "氧气浓度";
        }
        if (type == 'Y')
        {
            metricType = "液位";
        }

        if (-1 == this.alertType)
        {
            return metricType + "过低";
        }
        return metricType + "过高";
    }

    private String getAlertName()
    {
        String idInfo = id.split("_")[0];
        char[] infoArray = idInfo.toCharArray();
        char type = infoArray[2];
        if (type == 'H')
        {
            return "湿度检测仪";
        }
        if (type == 'T')
        {
            return "温度检测仪";
        }
        if (type == 'Q')
        {
            return "氧气浓度检测仪";
        }
        if (type == 'Y')
        {
            return "液位检测仪";
        }
        return "";
    }

    // RQT_011
    private String getAddress()
    {
        StringBuilder builder = new StringBuilder();
        return builder.append(getDeviceName(id)).append(getDeviceName(id)).toString();
    }

    private String getDeviceName(String id)
    {
        if (id.startsWith("RQ"))
        {
            return "燃气舱";
        }
        if (id.startsWith("DL"))
        {
            return "电力舱";
        }
        if (id.startsWith("ZH"))
        {
            return "综合舱";
        }
        return "Unknow";
    }

    private String getDeviceNum(String id)
    {
        String idInfo = id.split("_")[1];
        char[] infoArray = idInfo.toCharArray();
        int deviceNum = Integer.valueOf(String.valueOf(infoArray[1]));
        int plcNum = Integer.valueOf(String.valueOf(infoArray[2]));
        return deviceNum + "号PLC" + plcNum + "号";
    }
}
