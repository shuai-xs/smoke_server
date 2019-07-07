package com.suineng.task;


import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.suineng.bean.Device;
import com.suineng.bean.Metric;
import com.suineng.cache.DeviceCache;
import com.suineng.common.SuinengConst;
import com.suineng.service.MetricService;


public class MetricHandler implements Callable<Boolean>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricHandler.class);

    private Metric metric;

    private DeviceCache deviceCache;

    private MetricService metricService;

    public MetricHandler(MetricService metricService, Metric metric, DeviceCache deviceCache)
    {
        this.deviceCache = deviceCache;
        this.metric = metric;
        this.metricService = metricService;
    }

    private void updateOpState(Metric metric)
    {
        String metricId = metric.getId();
        int value = (int)metric.getValue0();
        String deviceId = this.getDeviceId(metricId);
        if(deviceId.startsWith("RQFJ") || deviceId.startsWith("RQFF") || deviceId.startsWith("RQSB")|| deviceId.startsWith("DLFJ") || deviceId.startsWith("DLFF") || deviceId.startsWith("DLSB") || deviceId.startsWith("ZHFJ") || deviceId.startsWith("ZHFF") || deviceId.startsWith("ZHSB"))
        {
            deviceCache.updateDeviceOpState(deviceId, metricId, value);
        }
    }

    private String getDeviceId(String metricId)
    {
        String[] str = metricId.split("_");
        if (str.length != 2)
        {
            return null;
        }
        // 转为大写保证数据准确性
        // DLFJG_042
        String deviceStr = str[0].toUpperCase();
        String num = str[1];

        Device device = new Device();
        // 从指标中获取舱信息
        for (String module : SuinengConst.MODULE_LIST)
        {
            if (deviceStr.startsWith(module))
            {
                device.setModuleId(module);
                break;
            }
        }
        if (device.getModuleId().isEmpty())
        {
            // 不是三种仓位中的一种，则认为是无效数据
            return null;
        }

        // FJG
        deviceStr = deviceStr.substring(device.getModuleId().length());
        for (String deviceType : SuinengConst.DEVICE_LIST)
        {
            if (deviceStr.startsWith(deviceType))
            {
                device.setDeviceType(deviceType);
                break;
            }
        }

        // 生成设备id
        StringBuilder sb = new StringBuilder();
        return sb.append(device.getModuleId()).append(device.getDeviceType()).append("_").append(num).toString();
    }
    @Override
    public Boolean call()
        throws Exception
    {
        try
        {
            LOGGER.info("metric id is: " + metric.getId());
            updateOpState(metric);
            metricService.insert(metric);
        }
        catch (Exception e)
        {
            LOGGER.error("insert fail", e);
        }

        return true;
    }

}
