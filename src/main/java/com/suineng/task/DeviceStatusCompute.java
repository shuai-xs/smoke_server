package com.suineng.task;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.suineng.cache.DeviceCache;


@Component
public class DeviceStatusCompute
{

    private static final Logger logger = LoggerFactory.getLogger(DeviceStatusCompute.class);

    @Autowired
    private DeviceCache deviceCache;

    // 连续xx周期不上报数据，则认为设备故障
    @Value("${alert.times:2}")
    private int times;

    // 统计周期,單位秒
    @Value("${alert.period:3600}")
    private long period;

    /**
     * 设备上报状态，连续xx周期不上报数据则认为设备故障
     */
    private Map<String, Long> deviceMap = new ConcurrentHashMap<String, Long>();

//    private Set<String> errorDeviceSet = new HashSet<String>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @PostConstruct
    public void init()
    {
        this.reloadAllData();
        scheduler.scheduleAtFixedRate(new RefreshTask(), period / 2, period, TimeUnit.SECONDS);
    }

    /**
     * 从数据库加载所有数据到缓存，包括所有故障设备信息，还有所有设备信息
     */
    private void reloadAllData()
    {

    }

    public void updateData(String deviceId, long timestamp)
    {
        deviceMap.put(deviceId, timestamp);
    }

    private class RefreshTask implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
//                Map<String, Long> errorDeviceMap = new HashMap<String, Long>();
                long curTime = System.currentTimeMillis();
                for (Map.Entry<String, Long> entry : deviceMap.entrySet())
                {
                    long time = entry.getValue();
                    if (curTime - time > period * times * 1000)
                    {
                        deviceCache.updateDeviceStatus(entry.getKey(), false);
                    }
                    else
                    {
                        deviceCache.updateDeviceStatus(entry.getKey(), true);
                    }
                }
            }
            catch (Throwable e)
            {
                logger.error("refresh task fail", e);
            }
        }

    }
}
