package com.suineng.service;


import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.suineng.bean.Device;
import com.suineng.cache.DeviceCache;
import com.suineng.common.SuinengConst;
import com.suineng.dao.DeviceMapper;
import com.suineng.mttq.ServerMQTTUtil;
import com.suineng.task.Handler;


/**
 * 处理设备基础信息
 */
@Service
public class DeviceService extends Handler
{
    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private DeviceCache deviceCache;
    
    private static final String topic = "DOWNDATA/123";

    @Value("${model.host:3600}")
    private String serverIp = "tcp://192.168.1.103:61613";

    @Value("${model.userName:admin}")
    private String userName = "admin";

    @Value("${model.passWord:passWord}")
    private String passWord = "password";

	public void control(String field, String state) 
	{
		try
		{
			String str = "[{\"PHONE\":\"123\",\"IDX\":\"576934d7b1004b459a357c0964f63210a\",\"FUNC\":6,\"body\":{\"A\":{\"REG_VAL\":{\"%s\":%s}}}}]";
            str = String.format(str,field, state);
			//str.replaceAll("$field", field).replaceAll("$state", state);
			logger.info("control string is: " + str);
			
			// 将信息写入消息体
			MqttMessage mqttMessage = new MqttMessage();
			mqttMessage.setQos(0);
			mqttMessage.setRetained(true);
			mqttMessage.setPayload(str.getBytes("UTF-8"));

			ServerMQTTUtil serverMQTTUtil = new ServerMQTTUtil(topic);
            serverMQTTUtil.HOST = this.serverIp;
            serverMQTTUtil.userName = this.userName;
            serverMQTTUtil.passWord = this.passWord;
			serverMQTTUtil.publish(topic, mqttMessage);
		} 
		catch (Exception e) 
		{
			logger.error("controll failed", e);
		}

	}
	
    public List<Device> getDevice(Device device)
    {
        return deviceMapper.getDevice(device);
    }

    public List<Device> getZone()
    {
        return deviceMapper.getZone();
    }

    public String getModuleInfo(String moduleId)
    {
        return deviceCache.getModuleInfo(moduleId);
    }

    public String getControlInfo(String moduleId, int zoneId)
    {
        return deviceCache.getControlInfo(moduleId, zoneId);
    }

    public int getFaultDeviceNum()
    {
        return deviceCache.getFaultDeviceNum();
    }

    public int getAlarmDeviceNum()
    {
        return deviceCache.getAlarmDeviceNum();
    }

    public String getTop5FaultDevice()
    {
        return deviceCache.getTop5FaultDevice();
    }

    public String getDeviceDesc()
    {
        return deviceCache.getDeviceDesc();
    }
    
    @Override
    public boolean handlerRequest(Object field)
    {
        try
        {
            String[] str = field.toString().split("_");
            if (str.length != 2)
            {
                return false;
            }

            // 转为大写保证数据准确性
            // DLFJG_042
            String deviceStr = str[0].toUpperCase();
            String num = str[1];

            Device device = genDevice(deviceStr, num);
            if (device == null)
            {
                return false;
            }
            // 先根据缓存判断设备是否已经存在
            if (!deviceCache.getDeviceMap().containsKey(device.getId()))
            {

                System.out.println("factory: " + device);
//                DeviceMapper deviceMapper = session.getSession().getMapper(DeviceMapper.class);
                // 不存在则更新缓存
                deviceMapper.insert(device);
                // 更新缓存
                deviceCache.getDeviceMap().put(device.getId(), device);
            }
            else
            {
                // 更新心跳时间
                deviceCache.getDeviceMap().get(device.getId()).setUpdateTime(
                    device.getUpdateTime());
            }
        }
        catch (Exception e)
        {
            logger.error("", e);
        }

        return true;
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

    public void setHighStatus(String id, double status)
    {
    	Device device = deviceCache.getDeviceMap().get(id);
    	if(device == null)
    	{
    		return;
    	}
    	device.setHighState(status);
    }
    
    public void setLowStatus(String id, double status)
    {
    	Device device = deviceCache.getDeviceMap().get(id);
    	if(device == null)
    	{
    		return;
    	}
    	device.setLowState(status);
    }
    
    public void setSwitchStatus(String id, String status)
    {
    	Device device = deviceCache.getDeviceMap().get(id);
    	if(device == null)
    	{
    		return;
    	}
    	device.setSwitchStatus(status);
    }
    
    public void updateOpState(String id)
    {
    	Device device = deviceCache.getDeviceMap().get(id);
    	if(device == null)
    	{
    		return;
    	}
    	deviceMapper.updateOpstate(device);
    }

    public Device getDevice(String id)
    {
        return deviceCache.getDeviceMap().get(id);
    }

    private int getZone(String num)
    {
        return Integer.valueOf(num.substring(1, 2));
    }

}
