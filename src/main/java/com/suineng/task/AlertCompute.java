package com.suineng.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.suineng.bean.Alert;
import com.suineng.bean.Device;
import com.suineng.bean.ThresholdValue;
import com.suineng.cache.DeviceCache;
import com.suineng.common.AlertConst;
import com.suineng.common.SuinengConst;
import com.suineng.service.AlertService;
import com.suineng.service.AlertThresholdConfig;

@Component
public class AlertCompute {

    @Autowired
    private AlertThresholdConfig thresholdConfig;

    @Autowired
    private DeviceCache deviceCache;

    @Autowired
    private AlertService alertHandler;
    
    private static final String METRIC_SPILT = "_";
    /**
     *
     * @param deviceId RQT_011
     * @param timestamp 指标时间，精确到毫秒
     * @param value  指标值
     */
    public void computeData(String deviceId, long timestamp, float value)
    {
        try
        {
            String metricType = this.getMetricType(deviceId);
            ThresholdValue thresholdValue = thresholdConfig.getThreshold(metricType, timestamp);
            if(null == thresholdValue)
            {
                return;
            }
            if(value < thresholdValue.lowThreshold)
            {
                this.recordLowAlert(deviceId, timestamp, value);
            }
            if(value > thresholdValue.highThreshold)
            {
                this.recordHighAlert(deviceId, timestamp, value);
            }
        }catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private Device genDevice(String tmpStr, String num)
    {
        System.out.println("tmpStr: " + tmpStr);
        Device device = new Device();
        // 从指标中获取舱信息
        for (String module : SuinengConst.MODULE_LIST)
        {
            if (tmpStr.startsWith(module))
            {
                device.setModuleId(module);
                break;
            }
        }
        if (device.getModuleId().isEmpty())
        {
            System.out.println("not valid type " + tmpStr);
            // 不是三种仓位中的一种，则认为是无效数据
            return null;
        }

        // FJG
        tmpStr = tmpStr.substring(device.getModuleId().length());
        for (String deviceType : SuinengConst.DEVICE_LIST)
        {
            if (tmpStr.contains(deviceType))
            {
                device.setDeviceType(deviceType);
                break;
            }
        }

        // 生成设备id
        StringBuilder sb = new StringBuilder();
        sb.append(device.getModuleId()).append(device.getDeviceType()).append("_").append(num);
        device.setId(sb.toString());

        device.setZoneId(getZone(num));

        device.setUpdateTime(System.currentTimeMillis());
        return device;
    }

    private int getZone(String num)
    {
        return Integer.valueOf(num.substring(1, 2));
    }


    /**
     * 记录底限阈值
     * @param deviceId
     * @param timestamp
     * @param value
     */
    private void recordLowAlert(String deviceId, long timestamp, float value)
    {
        Alert alert = new Alert();
        alert.setId(deviceId);
        alert.setCreateTime(timestamp);
        alert.setMetricValue(value);
        alert.setStatus(AlertConst.STATUS_NOT_CONFIRM);
        //-1表示低于阈值，1表示高于阈值
        alert.setAlertType(-1);

        String[] str = deviceId.split("_");
        // DLFJG_042
        String deviceStr = str[0].toUpperCase();
        String num = str[1];
        Device device = this.genDevice(deviceStr, num);
        alert.setModuleId(device.getModuleId());
        alert.setZoneId(device.getZoneId());
        if(deviceId.contains("H"))
        {
            alert.setMetricType("H");
        }
        if(deviceId.contains("T"))
        {
            alert.setMetricType("T");
        }
        if(deviceId.contains("O"))
        {
            alert.setMetricType("O");
        }
        if(deviceId.contains("Y"))
        {
            alert.setMetricType("Y");
        }
        this.alertHandler.insertAlert(alert);
        deviceCache.updateDeviceAlarm(deviceId, true);
    }

    /**
     * 记录超限阈值
     * @param deviceId
     * @param timestamp
     * @param value
     */
    private void recordHighAlert(String deviceId, long timestamp, float value)
    {
        Alert alert = new Alert();
        alert.setId(deviceId);
        alert.setCreateTime(timestamp);
        alert.setMetricValue(value);
        alert.setStatus(AlertConst.STATUS_NOT_CONFIRM);
        //-1表示低于阈值，1表示高于阈值
        alert.setAlertType(1);

        String[] str = deviceId.split("_");
        // DLFJG_042
        String deviceStr = str[0].toUpperCase();
        String num = str[1];
        Device device = this.genDevice(deviceStr, num);
        alert.setZoneId(device.getZoneId());
        alert.setModuleId(device.getModuleId());
        if(deviceId.contains("H"))
        {
            alert.setMetricType("H");
        }
        if(deviceId.contains("T"))
        {
            alert.setMetricType("T");
        }
        if(deviceId.contains("O"))
        {
            alert.setMetricType("O");
        }
        if(deviceId.contains("Y"))
        {
            alert.setMetricType("Y");
        }
        this.alertHandler.insertAlert(alert);
        deviceCache.updateDeviceAlarm(deviceId, true);
    }

    private String getMetricType(String deviceId)
    {
        return deviceId.split(METRIC_SPILT)[0];
    }
}
