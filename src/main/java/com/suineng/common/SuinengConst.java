package com.suineng.common;

import java.util.Arrays;
import java.util.List;

public interface SuinengConst
{
    // 燃气舱
    String RQ_MODULE = "RQ";
    
    // 电力舱
    String DL_MODULE = "DL";
    
    // 综合舱
    String ZH_MODULE = "ZH";
    
    List<String> MODULE_LIST = Arrays.asList(RQ_MODULE, DL_MODULE, ZH_MODULE);
    
    // 温度指标
    String TEMPERATURE = "T";
    
    // 湿度指标
    String HUMIDITY = "H";
    
    // 氧气浓度
    String OXYGEN_CONCENTRATION = "O";
    
    // 液位
    String LEVEL = "Y";
    
    // 水泵
    String WATER_PUMP = "SB";
    
    // 风机
    String FAN = "FJ";

    //风机高速
    String FANG = "FJG";

    //风机低速
    String FAND = "FJD";

    // 风阀
    String FF = "FF";
    
    List<String> DEVICE_LIST = Arrays.asList(WATER_PUMP, FAN, FF, TEMPERATURE, HUMIDITY, OXYGEN_CONCENTRATION, LEVEL);
    
    // 高
    String HIGHT = "G";
    
    // 低
    String LOW = "D";

    //指标上报周期
    int PERIOD = 3600*1000;

    String METRIC_SPILT = "_";
}
