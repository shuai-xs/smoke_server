package com.suineng.bean;

public class Device
{

    // 详细设备id
    private String id;

    // 舱位id
    private String moduleId = "";

    // 分区id
    private int zoneId;

    // 设备类型
    private String deviceType = "";

    // 设备是否正常上报数据,上报(true)，不上报(false)
    private boolean status = true;

    // 告警(true)/正常(false)
    private boolean alarm = false;

    private long updateTime;

    /**
     * 控制类型，remote表示远程，local表示就地
     */
    private String controlType= "local";

    /**
     * 控制方式，auto表示自动，manual表示手动
     */
    private String controlSwitch="manual";

    /**
     * 开关状态，open表示打开，close表示关闭
     */
    private String switchStatus = "close";

    /**
     * 操作状态，high表示高速开启，low表示低速开启
     */
    private double highState = 0;

    private double lowState = 0;


    public double getHighState() {
        return highState;
    }

    public void setHighState(double highState) {
        this.highState = highState;
    }

    public double getLowState() {
        return lowState;
    }

    public void setLowState(double lowState) {
        this.lowState = lowState;
    }

    public String getControlType() {
        return controlType;
    }

    public void setControlType(String controlType) {
        this.controlType = controlType;
    }

    public String getControlSwitch() {
        return controlSwitch;
    }

    public void setControlSwitch(String controlSwitch) {
        this.controlSwitch = controlSwitch;
    }

    public String getSwitchStatus() {
        return switchStatus;
    }

    public void setSwitchStatus(String switchStatus) {
        this.switchStatus = switchStatus;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getModuleId()
    {
        return moduleId;
    }

    public void setModuleId(String moduleId)
    {
        this.moduleId = moduleId;
    }

    public int getZoneId()
    {
        return zoneId;
    }

    public void setZoneId(int zoneId)
    {
        this.zoneId = zoneId;
    }

    public String getDeviceType()
    {
        return deviceType;
    }

    public void setDeviceType(String deviceType)
    {
        this.deviceType = deviceType;
    }

    public boolean isStatus()
    {
        return status;
    }

    public void setStatus(boolean status)
    {
        this.status = status;
    }

    public boolean isAlarm()
    {
        return alarm;
    }

    public void setAlarm(boolean alarm)
    {
        this.alarm = alarm;
    }

    public long getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(long updateTime)
    {
        this.updateTime = updateTime;
    }

}
