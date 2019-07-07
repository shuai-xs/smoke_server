package com.suineng.task;

import java.util.Random;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.suineng.bean.Device;
import com.suineng.bean.Metric;
import com.suineng.cache.DeviceCache;
import com.suineng.common.SuinengConst;
import com.suineng.dao.DeviceMapper;
import com.suineng.dao.MetricMapper;

public class TestData {


    @Value("${test.insert:false}")
    private boolean isInsert;
    @Autowired
    private AlertCompute alertCompute;
    
    @Autowired
    private MetricMapper metricMapper;
    
    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private DeviceCache deviceCache;

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
            if (deviceStr.contains(deviceType))
            {
                device.setDeviceType(deviceType);
                break;
            }
        }

        // 生成设备id
        StringBuilder sb = new StringBuilder();
        return sb.append(device.getModuleId()).append(device.getDeviceType()).append("_").append(num).toString();
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

        device.setLowState(0);
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

    private void insertData()
    {
        long time = System.currentTimeMillis();
        time = time - time%(3600*1000);
        System.out.println("RQT:"+time);
        Random random = new Random(100);
        for(int i=0; i<100; i++)
        {
            time -= i*3600*1000;
            Metric metric = new Metric();
            metric.setId("RQT_011");
            metric.setTime(time);
            metric.setValue0(random.nextFloat()*100);
            metricMapper.insert(metric);
            String[] str = metric.getId().toString().split("_");
            // DLFJG_042
            String deviceStr = str[0].toUpperCase();
            String num = str[1];
            Device device = this.genDevice(deviceStr, num);
            // 更新缓存
            deviceCache.getDeviceMap().put(device.getId(), device);
            deviceMapper.insert(device);
            String deviceId = getDeviceId(metric.getId());
            alertCompute.computeData(metric.getId(), metric.getTime(), metric.getValue0());

            metric = new Metric();
            metric.setId("DLH_011");
            metric.setTime(time);
            metric.setValue0(random.nextFloat()*100);
            metricMapper.insert(metric);
            str = metric.getId().toString().split("_");
            // DLFJG_042
            deviceStr = str[0].toUpperCase();
            num = str[1];
            device = this.genDevice(deviceStr, num);
            // 更新缓存
            deviceCache.getDeviceMap().put(device.getId(), device);
            deviceMapper.insert(device);
            deviceId = getDeviceId(metric.getId());
            alertCompute.computeData(metric.getId(), metric.getTime(), metric.getValue0());

            metric = new Metric();
            metric.setId("RQH_011");
            metric.setTime(time);
            metric.setValue0(random.nextFloat()*100);
            metricMapper.insert(metric);
            str = metric.getId().toString().split("_");
            // DLFJG_042
            deviceStr = str[0].toUpperCase();
            num = str[1];
            device = this.genDevice(deviceStr, num);
            // 更新缓存
            deviceCache.getDeviceMap().put(device.getId(), device);
            deviceMapper.insert(device);
            deviceId = getDeviceId(metric.getId());
            alertCompute.computeData(metric.getId(), metric.getTime(), metric.getValue0());


            metric = new Metric();
            metric.setId("ZHH_011");
            metric.setTime(time);
            metric.setValue0(random.nextFloat()*100);
            metricMapper.insert(metric);
            str = metric.getId().toString().split("_");
            // DLFJG_042
            deviceStr = str[0].toUpperCase();
            num = str[1];
            device = this.genDevice(deviceStr, num);
            // 更新缓存
            deviceCache.getDeviceMap().put(device.getId(), device);
            deviceMapper.insert(device);
            deviceId = getDeviceId(metric.getId());
            alertCompute.computeData(metric.getId(), metric.getTime(), metric.getValue0());
            //deviceCache.updateDeviceOpState(deviceId, String.valueOf(metric.getValue0()));

            metric = new Metric();
            metric.setId("RQO_011");
            metric.setTime(time);
            metric.setValue0(random.nextFloat()*100);
            metricMapper.insert(metric);
            str = metric.getId().toString().split("_");
            // DLFJG_042
            deviceStr = str[0].toUpperCase();
            num = str[1];
            device = this.genDevice(deviceStr, num);
            // 更新缓存
            deviceCache.getDeviceMap().put(device.getId(), device);
            deviceMapper.insert(device);
            deviceId = getDeviceId(metric.getId());
            alertCompute.computeData(metric.getId(), metric.getTime(), metric.getValue0());

            metric = new Metric();
            metric.setId("ZHO_011");
            metric.setTime(time);
            metric.setValue0(random.nextFloat()*100);
            metricMapper.insert(metric);
            str = metric.getId().toString().split("_");
            // DLFJG_042
            deviceStr = str[0].toUpperCase();
            num = str[1];
            device = this.genDevice(deviceStr, num);
            // 更新缓存
            deviceCache.getDeviceMap().put(device.getId(), device);
            deviceMapper.insert(device);
            deviceId = getDeviceId(metric.getId());
            alertCompute.computeData(metric.getId(), metric.getTime(), metric.getValue0());

            metric = new Metric();
            metric.setId("DLO_011");
            metric.setTime(time);
            metric.setValue0(random.nextFloat()*100);
            metricMapper.insert(metric);
            str = metric.getId().toString().split("_");
            // DLFJG_042
            deviceStr = str[0].toUpperCase();
            num = str[1];
            device = this.genDevice(deviceStr, num);
            // 更新缓存
            deviceCache.getDeviceMap().put(device.getId(), device);
            deviceMapper.insert(device);
            deviceId = getDeviceId(metric.getId());
            alertCompute.computeData(metric.getId(), metric.getTime(), metric.getValue0());

            metric = new Metric();
            metric.setId("DLT_012");
            metric.setTime(time);
            metric.setValue0(random.nextFloat()*100);
            metricMapper.insert(metric);
            str = metric.getId().toString().split("_");
            // DLFJG_042
            deviceStr = str[0].toUpperCase();
            num = str[1];
            device = this.genDevice(deviceStr, num);
            // 更新缓存
            deviceCache.getDeviceMap().put(device.getId(), device);
            deviceMapper.insert(device);
            deviceId = getDeviceId(metric.getId());
            alertCompute.computeData(metric.getId(), metric.getTime(), metric.getValue0());
//            deviceCache.updateDeviceOpState(deviceId, String.valueOf(metric.getValue0()));

            metric = new Metric();
            metric.setId("ZHT_012");
            metric.setTime(time);
            metric.setValue0(random.nextFloat()*100);
            metricMapper.insert(metric);
            str = metric.getId().toString().split("_");
            // DLFJG_042
            deviceStr = str[0].toUpperCase();
            num = str[1];
            device = this.genDevice(deviceStr, num);
            // 更新缓存
            deviceCache.getDeviceMap().put(device.getId(), device);
            deviceMapper.insert(device);
            deviceId = getDeviceId(metric.getId());
            alertCompute.computeData(metric.getId(), metric.getTime(), metric.getValue0());
            //deviceCache.updateDeviceOpState(deviceId, String.valueOf(metric.getValue0()));


            metric = new Metric();
            metric.setId("RQFJGS_012");
            metric.setTime(time);
            metric.setValue0(1);
            metricMapper.insert(metric);
            str = metric.getId().toString().split("_");
            // DLFJG_042
            deviceStr = str[0].toUpperCase();
            num = str[1];
            device = this.genDevice(deviceStr, num);
            // 更新缓存
            deviceCache.getDeviceMap().put(device.getId(), device);
            deviceMapper.insert(device);
            deviceId = getDeviceId(metric.getId());
            if(deviceId.startsWith("RQFJ") || deviceId.startsWith("RQFF") || deviceId.startsWith("RQSB")|| deviceId.startsWith("DLFJ") || deviceId.startsWith("DLFF") || deviceId.startsWith("DLSB") || deviceId.startsWith("ZHFJ") || deviceId.startsWith("ZHFF") || deviceId.startsWith("ZHSB"))
            {
                deviceCache.updateDeviceOpState(deviceId, deviceId, metric.getValue0());
            }
            alertCompute.computeData(metric.getId(), metric.getTime(), metric.getValue0());

            metric = new Metric();
            metric.setId("RQFJD_022");
            metric.setTime(time);
            metric.setValue0(1);
            metricMapper.insert(metric);
            str = metric.getId().toString().split("_");
            // DLFJG_042
            deviceStr = str[0].toUpperCase();
            num = str[1];
            device = this.genDevice(deviceStr, num);
            // 更新缓存
            deviceCache.getDeviceMap().put(device.getId(), device);
            deviceMapper.insert(device);
            deviceId = getDeviceId(metric.getId());
            if(deviceId.startsWith("RQFJ") || deviceId.startsWith("RQFF") || deviceId.startsWith("RQSB")|| deviceId.startsWith("DLFJ") || deviceId.startsWith("DLFF") || deviceId.startsWith("DLSB") || deviceId.startsWith("ZHFJ") || deviceId.startsWith("ZHFF") || deviceId.startsWith("ZHSB"))
            {
                deviceCache.updateDeviceOpState(deviceId, deviceId, metric.getValue0());
            }
            alertCompute.computeData(metric.getId(), metric.getTime(), metric.getValue0());


            metric = new Metric();
            metric.setId("RQFJG_022");
            metric.setTime(time);
            metric.setValue0(2);
            metricMapper.insert(metric);
            str = metric.getId().toString().split("_");
            // DLFJG_042
            deviceStr = str[0].toUpperCase();
            num = str[1];
            device = this.genDevice(deviceStr, num);
            // 更新缓存
            deviceCache.getDeviceMap().put(device.getId(), device);
            deviceMapper.insert(device);
            deviceId = getDeviceId(metric.getId());
            if(deviceId.startsWith("RQFJ") || deviceId.startsWith("RQFF") || deviceId.startsWith("RQSB")|| deviceId.startsWith("DLFJ") || deviceId.startsWith("DLFF") || deviceId.startsWith("DLSB") || deviceId.startsWith("ZHFJ") || deviceId.startsWith("ZHFF") || deviceId.startsWith("ZHSB"))
            {
                deviceCache.updateDeviceOpState(deviceId, deviceId, metric.getValue0());
            }
            alertCompute.computeData(metric.getId(), metric.getTime(), metric.getValue0());
            //deviceCache.updateDeviceOpState(deviceId, String.valueOf(metric.getValue0()));

        }
    }

    private void insertAlert()
    {

    }

    @PostConstruct
    public void init()
    {
        if(isInsert)
        {
            insertData();
        }
    }
}
