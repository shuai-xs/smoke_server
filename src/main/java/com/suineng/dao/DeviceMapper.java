package com.suineng.dao;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.suineng.bean.Device;


@Mapper
public interface DeviceMapper
{
    int insert(Device device);
    
    List<Device> getDevice(Device device);
    
    List<Device> getZone();

    void updateAlarm(Device device);

    void updateStatus(Device device);

    void updateOpstate(Device device);
}