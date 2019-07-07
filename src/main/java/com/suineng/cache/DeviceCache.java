package com.suineng.cache;


import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suineng.bean.Device;
import com.suineng.common.AlertConst;
import com.suineng.common.SuinengConst;
import com.suineng.dao.DeviceMapper;


@Component
public class DeviceCache
{
    // <id, device>
    private volatile Map<String, Device> deviceMap = new ConcurrentHashMap<String, Device>();

    @Autowired
    private DeviceMapper deviceMapper;

    /**
     * 根据舱名称以及区信息获取所有的设备id
     * 
     * @param moduleId
     * @param zoneId
     * @return
     */
    public List<String> getDeviceIdList(String moduleId, String zoneId)
    {
        List<String> idList = new LinkedList<String>();
        for (Map.Entry<String, Device> entry : deviceMap.entrySet())
        {
            Device device = entry.getValue();
            if (moduleId.equals(device.getModuleId())
                && zoneId.equals(String.valueOf(device.getZoneId())))
            {
                idList.add(entry.getValue().getId());
            }
        }
        return idList;
    }

    /**
     * 获取所有idList列表
     * 
     * @return
     */
    public List<String> getAllDeviceIdList()
    {
        List<String> idList = new LinkedList<String>();
        for (Map.Entry<String, Device> entry : deviceMap.entrySet())
        {
            idList.add(entry.getValue().getId());
        }
        return idList;
    }

    public String getControlInfo(String moduleId, int zoneId)
    {
    	JSONArray array = new JSONArray();
        for (Map.Entry<String, Device> entry : deviceMap.entrySet())
        {
        	JSONObject object = new JSONObject();
            Device device = entry.getValue();
            if(!moduleId.equals(device.getModuleId()))
            {
                continue;
            }
            if (SuinengConst.FAN.equals(device.getDeviceType()))
            {
                getDevice("风机",  device,  object);
                if(!object.isEmpty())
                {
                    array.add(object);
                }
                continue;
            }
            if (SuinengConst.FF.equals(device.getDeviceType()))
            {
                getDevice("风阀",  device,  object);
                if(!object.isEmpty())
                {
                    array.add(object);
                }
                continue;
            }
            if (SuinengConst.WATER_PUMP.equals(device.getDeviceType()))
            {
            	getDevice("水泵",  device,  object);
                if(!object.isEmpty())
                {
                    array.add(object);
                }
                continue;
            }

        }
        return array.toJSONString();
    }
    
    private JSONObject getDevice(String type, Device device, JSONObject object)
    {
    	object.put("type", type);
    	object.put("id", device.getId());
    	
    	object.put("controlType", device.getControlType());
    	object.put("controlSwitch", device.getControlSwitch());

    	
    	if(device.getHighState() == 1D)
    	{
    		object.put("opState", "high");
    	}
    	
    	if(device.getLowState() == 1D)
    	{
    		object.put("opState", "low");
    	}

    	if(type.equals("风机"))
        {
            if(device.getLowState() == 0D && device.getHighState() == 0D)
            {
                object.put("switchStatus", "close");
                object.put("opState", "close");
            }
            else {
                object.put("switchStatus", "open");
            }
        }
    	else
        {
            object.put("switchStatus", device.getSwitchStatus());
        }


    	return object;
    }

    public String getModuleInfo(String moduleId)
    {
        int deviceNum = 0;
        int alarmDevice = 0;
        int localDevice = 0;
        for (Map.Entry<String, Device> entry : deviceMap.entrySet())
        {
            Device device = entry.getValue();
            if (device.getModuleId().startsWith(moduleId))
            {
                deviceNum++ ;
                if (device.isAlarm())
                {
                    alarmDevice++ ;
                }
                if (AlertConst.LOCAL.equals(device.getControlType()))
                {
                    localDevice++ ;
                }
            }
        }
        JSONObject resultObj = new JSONObject();
        resultObj.put("num", deviceNum);
        resultObj.put("alarm", alarmDevice);
        resultObj.put("local", localDevice);
        return resultObj.toJSONString();
    }

    /**
     * 获取设备详细 { "totalNum":xx, "温度检测仪":xx, "湿度检测仪":xx, "氧气浓度检测仪":xx, "液位检测仪":xx, "风机":xx,
     * "风阀检测仪":xx, "水泵检测仪":xx }
     * 
     * @return
     */
    public String getDeviceDesc()
    {
        Map<String, Integer> deviceTypeCountMap = new HashMap<String, Integer>();
        for (Map.Entry<String, Device> entry : deviceMap.entrySet())
        {
            String deviceType = entry.getValue().getDeviceType();
            Integer value = deviceTypeCountMap.get(deviceType);
            if (null == value)
            {
                deviceTypeCountMap.put(deviceType, 0);
            }
            deviceTypeCountMap.put(deviceType, (value == null ? 0 : value) + 1);
        }
        JSONObject object = new JSONObject();
        int totalSize = 0;
        for (Map.Entry<String, Integer> entry : deviceTypeCountMap.entrySet())
        {
            totalSize += entry.getValue();
        }
        object.put("totalNum", totalSize);
        object.put("温度检测仪", deviceTypeCountMap.get(SuinengConst.TEMPERATURE));
        object.put("湿度检测仪", deviceTypeCountMap.get(SuinengConst.HUMIDITY));
        object.put("氧气浓度检测仪", deviceTypeCountMap.get(SuinengConst.OXYGEN_CONCENTRATION));
        object.put("液位检测仪", deviceTypeCountMap.get(SuinengConst.LEVEL));
        object.put("风机", deviceTypeCountMap.get(SuinengConst.FAN));
        object.put("风阀检测仪", deviceTypeCountMap.get(SuinengConst.FF));
        object.put("水泵检测仪", deviceTypeCountMap.get(SuinengConst.WATER_PUMP));
        return object.toJSONString();
    }

    /**
     * 获取异常比例最高的前5种设备 [ { "moduleId":xx, "deviceType":xx, "errorRate":xx } ]
     * 
     * @return
     */
    public String getTop5FaultDevice()
    {
        Map<FaultDevice, Integer> faultDeviceMap = new HashMap<FaultDevice, Integer>();
        for (Map.Entry<String, Device> entry : deviceMap.entrySet())
        {
            Device device = entry.getValue();
            String moduleId = device.getModuleId();
            String deviceType = device.getDeviceType();
            FaultDevice faultDevice = new FaultDevice();
            faultDevice.deviceType = deviceType;
            faultDevice.moduleId = moduleId;
            Integer totalValue = faultDeviceMap.get(faultDevice);
            if (null == totalValue)
            {
                faultDeviceMap.put(faultDevice, 0);
            }
            // 设备发生告警
            if (device.isAlarm())
            {
                faultDevice.erroNum++ ;
            }
            faultDevice.totalNum++ ;
        }
        List<FaultDevice> faultDeviceList = new LinkedList<FaultDevice>();
        for (Map.Entry<FaultDevice, Integer> entry : faultDeviceMap.entrySet())
        {
            FaultDevice faultDevice = entry.getKey();
            int totalValue = entry.getValue();
            faultDevice.errorRate = (Float)((float)faultDevice.erroNum / faultDevice.totalNum);
            faultDeviceList.add(faultDevice);
        }
        Collections.sort(faultDeviceList);
        JSONArray array = new JSONArray();
        int num = 5;
        Iterator<FaultDevice> ite = faultDeviceList.iterator();
        int index = 1;
        while (ite.hasNext() && num-- > 0)
        {
            FaultDevice faultDevice = ite.next();
            JSONObject object = new JSONObject();
            object.put("id", index++ );
            object.put("requirementName", getDeviceName(faultDevice.deviceType));
            object.put("warehouse", getModuleName(faultDevice.moduleId));
            object.put("percent", NumberFormat.getPercentInstance().format(faultDevice.errorRate));
            array.add(object);
        }
        return array.toJSONString();
    }

    private String getDeviceName(String deviceType)
    {
        if (SuinengConst.FAN.equals(deviceType))
        {
            return "风机";
        }
        if (SuinengConst.TEMPERATURE.equals(deviceType))
        {
            return "温度检测仪";
        }
        if (SuinengConst.HUMIDITY.equals(deviceType))
        {
            return "湿度检测仪";
        }
        if (SuinengConst.LEVEL.equals(deviceType))
        {
            return "液位检测仪";
        }
        if (SuinengConst.OXYGEN_CONCENTRATION.equals(deviceType))
        {
            return "氧气浓度检测仪";
        }
        return "未知";
    }

    private String getModuleName(String moduleId)
    {
        if (SuinengConst.RQ_MODULE.equals(moduleId))
        {
            return "燃气舱";
        }
        if (SuinengConst.DL_MODULE.equals(moduleId))
        {
            return "电力舱";
        }
        if (SuinengConst.ZH_MODULE.equals(moduleId))
        {
            return "综合舱";
        }
        return "未知";
    }

    /**
     * 更新设备状态
     * 
     * @param deviceId
     */
    public void updateDeviceOpState(String deviceId, String metricId, double value)
    {
        Device device = this.deviceMap.get(deviceId);
        metricId = metricId.split("_")[0];
        // JD或者YC必选一个
        if(metricId.endsWith("JD"))
        {
            if(0 == (int)value)
            {
                device.setControlType(AlertConst.LOCAL);
            }
            else
            {
                device.setControlType(AlertConst.REMOTE);
            }
            device.setControlSwitch("open");
        }

        //风机远程控制状态，1表示自动，0表示手动
        if(metricId.endsWith("YC"))
        {
            if(1 == (int)value)
            {
                device.setControlSwitch(AlertConst.AUTO);
            }
            else
            {
                device.setControlSwitch(AlertConst.MANAUL);
            }
        }
        
        //风机开关状态，1表示开，0表示关
        if(metricId.endsWith("KG"))
        {
        	 if(1 == (int)value)
        	 {
        		 device.setSwitchStatus("open");
        	 }
            if(0 == (int)value)
            {
                device.setSwitchStatus("close");
            }
        }
        
        if(metricId.endsWith("GS"))
        {
            device.setHighState(value);
            if(1 == (int)value)
            {
            	device.setSwitchStatus("open");
            }
        }

        if(metricId.endsWith("DS"))
        {
        	if(1 == (int)value)
            {
            	device.setSwitchStatus("open");
            }
        }
        this.deviceMapper.updateOpstate(device);
    }

    /**
     * 获取某个舱下，某个区的故障设备数
     * 
     * @param moduleId
     * @param zoneId
     * @return
     */
    public int getFaultDeviceNum(String moduleId, String zoneId)
    {
        return 0;
    }

    /**
     * 获取故障设备数
     * 
     * @return
     */
    public int getFaultDeviceNum()
    {
        int num = 0;
        for (Map.Entry<String, Device> entry : deviceMap.entrySet())
        {
            // 设备故障
            if (!entry.getValue().isStatus())
            {
                num++ ;
            }
        }
        return num;
    }

    /**
     * 获取告警设备数
     * 
     * @return
     */
    public int getAlarmDeviceNum()
    {
        int num = 0;
        for (Map.Entry<String, Device> entry : deviceMap.entrySet())
        {
            if (entry.getValue().isAlarm())
            {
                num++ ;
            }
        }
        return num;
    }

    public DeviceCache()
    {

    }

    @PostConstruct
    private void init()
    {
        // 获取所有的设备信息及状态
        List<Device> deviceList = deviceMapper.getDevice(new Device());
        for (Device device : deviceList)
        {
            this.deviceMap.put(device.getId(), device);
        }
    }

    public Map<String, Device> getDeviceMap()
    {
        return deviceMap;
    }

    // 刷新设备状态，是否正常上报数据
    public void updateDeviceStatus(String deviceId, boolean status)
    {
        Device device = this.deviceMap.get(deviceId);
        if (status != device.isStatus())
        {
            device.setStatus(status);
            this.deviceMapper.updateStatus(device);
        }
    }

    // 更新设备告警状态
    public void updateDeviceAlarm(String deviceId, boolean alarmStatus)
    {
        Device device = this.deviceMap.get(deviceId);
        if (alarmStatus != device.isAlarm())
        {
            device.setAlarm(alarmStatus);
            this.deviceMapper.updateAlarm(device);
        }
    }

    public void setDeviceMap(Map<String, Device> deviceMap)
    {
        this.deviceMap = deviceMap;
    }

    public void addDevice(Device device)
    {
        deviceMap.putIfAbsent(device.getId(), device);
    }

    private class FaultDevice implements Comparable<FaultDevice>
    {
        // 设备类型
        public String deviceType;

        // 舱位名称
        public String moduleId;

        // 异常设备百分比
        Float errorRate;

        // 设备总数
        int totalNum = 0;

        // 异常设备数
        int erroNum = 0;

        // 从大到小排序
        @Override
        public int compareTo(FaultDevice other)
        {
            return other.errorRate.compareTo(errorRate);
            // return errorRate.compareTo(other.errorRate);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            FaultDevice that = (FaultDevice)o;
            return Objects.equals(deviceType, that.deviceType)
                   && Objects.equals(moduleId, that.moduleId);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(deviceType, moduleId);
        }
    }

}
