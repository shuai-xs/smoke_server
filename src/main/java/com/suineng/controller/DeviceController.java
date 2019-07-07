package com.suineng.controller;


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.suineng.bean.Device;
import com.suineng.common.SuinengConst;
import com.suineng.service.DeviceService;
import com.suineng.service.MetricService;


@RestController
public class DeviceController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    private DeviceService deviceService;

//    @Autowired
//    private DeviceCache deviceCache;

    @Autowired
    private MetricService metricQueryHandler;

    /**
     * get all the device, or get device by module and zone
     * 
     * @param id
     *            device id
     * @param moduleId
     *            module_id
     * @param zoneId
     *            zone_id
     * @return List<Device>
     */
    @RequestMapping(value = "/v1/device", method = RequestMethod.GET)
    @ResponseBody
    public List<Device> getDevice(@RequestParam(required = false, value = "id") String id,
                                  @RequestParam(required = false, value = "moduleId") String moduleId,
                                  @RequestParam(required = false, value = "zoneId") Integer zoneId)
    {
        Device device = new Device();

        device.setId(id);
        device.setModuleId(moduleId);
        if (zoneId != null)
        {
            device.setZoneId(zoneId);
        }

        LOGGER.info("getDevice, param is:" + device);
        return deviceService.getDevice(device);
    }

    /**
     * get all zone info
     * 
     * @return List<Device>
     */
    @RequestMapping(value = "/v1/zone", method = RequestMethod.GET)
    @ResponseBody
    public List<Device> getZone()
    {
        return deviceService.getZone();
    }

    /**
     * 获取分舱信息，包括分舱总设备数，分舱总预警数，分舱内就地控制设备总数 { "num":xx, "alarm":xx, "local":xx }
     * 
     * @return List<Device>
     */
    @RequestMapping(value = "/v1/module/info", method = RequestMethod.GET)
    @ResponseBody
    public String getModuleInfo(@RequestParam(required = false, value = "moduleId") String moduleId)
    {
        return deviceService.getModuleInfo(moduleId);
    }

    /**
     * 获取设备控制信息
     * 
     * @return List<Device>
     */
    @RequestMapping(value = "/v1/device/control/info", method = RequestMethod.GET)
    @ResponseBody
    public String getControlInfo(@RequestParam(required = false, value = "moduleId") String moduleId,
                                 @RequestParam(required = false, value = "zoneId") int zoneId)
    {
        return deviceService.getControlInfo(moduleId, zoneId);
    }

    /*
     * 获取当前设备详细（首页左上角功能），包括设备故障数和设备预警数 { "faultDeviceNum":xx, "alarmDeviceNum":xx }
     */
    @RequestMapping(value = "/v1/device/status", method = RequestMethod.GET)
    @ResponseBody
    public String getDeviceStatus()
    {
        // 故障设备数
        int faultDeviceNum = deviceService.getFaultDeviceNum();
        // 预警设备数
        int alarmDeviceNum = deviceService.getAlarmDeviceNum();
        JSONObject object = new JSONObject();
        object.put("faultDeviceNum", faultDeviceNum);
        object.put("alarmDeviceNum", alarmDeviceNum);
        return object.toJSONString();
    }
    
    /**
     * 更新设备控制信息
     * 
     */
    @RequestMapping(value = "/v1/device/control/info", method = RequestMethod.POST)
    @ResponseBody
    public boolean controlInfo(@RequestBody(required = true) Map<String, String> opMap)
    {
    	String id = (String) opMap.get("id");
    	String opState = (String) opMap.get("opState");
    	String field = id;
    	String[] fields = id.split("_");
    	String value = "0";
    	String switchStatus = (String) opMap.get("switchStatus");
    	// 处理风机
    	if(opState != null)
    	{
    		boolean high = opState.equals("high");
    		if(high || opState.equals("low"))
            {
                field = fields[0] + (high?"GS":"DS");
            }
    		else
            {
                Device device = deviceService.getDevice(id);
                if(device.getHighState() == 1D)
                {
                    field = fields[0] + "GS";
                }
                else if(device.getLowState() == 1D)
                {
                    field = fields[0] + "DS";
                }
                else
                {
                    field = fields[0] + "GS";
                }
            }

            // 风机打开
            if(high)
            {
                value = "1";
                deviceService.setHighStatus(id, 1D);
                deviceService.setSwitchStatus(id, "open");
            }
            else if(opState.equals("low"))
            {
                value = "1";
                deviceService.setLowStatus(id, 1D);
                deviceService.setSwitchStatus(id, "open");
            }
            else
            {
                value = "0";
                deviceService.setHighStatus(id, 0D);
                deviceService.setLowStatus(id, 0D);
                deviceService.setSwitchStatus(id, "close");
            }
    	}
    	else
    	{
            field = fields[0] + "KG";
    		// 其他设备启停
    		if(switchStatus.equals("open"))
        	{
        		value = "1";
        		deviceService.setSwitchStatus(id, "open");
        	}
    		else
    		{
    			value = "0";
    			deviceService.setSwitchStatus(id, "close");
    		}
    	}
        deviceService.control(field + "_" + fields[1], value);
    	// 更新数据库信息
    	deviceService.updateOpState(id);
    	
    	LOGGER.info("field is {} and value is {} ", field, value);
    	// TODO
    	return true;
    }

    /**
     * 获取top5异常占比设备的详细 [ { "moduleId":xx, "deviceType":xx, "errorRate":xx }, { "moduleId":xx,
     * "deviceType":xx, "errorRate":xx } ]
     * 
     * @return
     */
    @RequestMapping(value = "/v1/device/info/alarm/tp5", method = RequestMethod.GET)
    @ResponseBody
    public String getTp5AlarmDevice()
    {
        return deviceService.getTop5FaultDevice();
    }

    /**
     * 获取设备使用情况 { "totalNum":176, "温度检测仪":20, "湿度检测仪":xx, "氧气浓度检测仪":xx, "液位检测仪":xx, "风机高速启停检测仪":xx,
     * "风机低速启动检测仪":xx, "风阀检测仪":xx, "水泵检测仪":xx }
     * 
     * @return
     */
    @RequestMapping(value = "/v1/device/info", method = RequestMethod.GET)
    @ResponseBody
    public String getDeviceInfo()
    {
        return deviceService.getDeviceDesc();
    }

    /**
     * 获取本月各个舱的平均温度 { "RQT":xx, "DLT":xx, "ZHT":xx }
     * 
     * @return
     */
    @RequestMapping(value = "/v1/temprature/info", method = RequestMethod.GET)
    @ResponseBody
    public String getCurTemprature()
    {
        return metricQueryHandler.getCurMonthTempInfo();
    }

    /**
     * 获取最近6日的湿度详细 [ { "4.6":{ "RQH":xx, "RQO":xx, "ZHH":xx, "ZHO":xx, "DLH":xx, "DLO":xx } },
     * "4.5":{ "RQH":xx, "RQO":xx, "ZHH":xx, "ZHO":xx, "DLH":xx, "DLO":xx } ]
     * 
     * @return
     */
    @RequestMapping(value = "/v1/humidity/6days/info", method = RequestMethod.GET)
    @ResponseBody
    public String getHumidityInfo()
    {
        return metricQueryHandler.get6daysHumidityInfo();
    }

    /**
     * 获取当前某个舱下的某个分区的指标详细,包括温度，湿度，氧气浓度，液位 { "1":{ "T":xx, "H":xx, "O":xx, "Y":xx }, "2":{ "T":xx,
     * "H":xx, "O":xx, "Y":xx } }
     * 
     * @return
     */
    @RequestMapping(value = "/v1/zone/metric/info", method = RequestMethod.GET)
    @ResponseBody
    public String getZoneMetricInfo(@RequestParam(required = false, value = "moduleId") String moduleId,
                                    @RequestParam(required = false, value = "zoneId") Integer zoneId)
    {
        long endTime = System.currentTimeMillis();
        // 一小时前的时间，指标每小时上报一次
        long startTime = endTime - SuinengConst.PERIOD;
        return metricQueryHandler.getCurMetricInfo(moduleId, String.valueOf(zoneId), startTime,
            endTime);
    }
}
