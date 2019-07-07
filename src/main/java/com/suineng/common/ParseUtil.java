package com.suineng.common;

public class ParseUtil {

    /**
     * 从设备id中解析指标类型，温度/湿度/氧气浓度/液位
     * @param deviceId  RQH_011
     * @return  返回H，表示湿度
     */
    public static String getMetricType(String deviceId)
    {
        String key = deviceId.split(SuinengConst.METRIC_SPILT)[0];
        return String.valueOf(key.charAt(key.length() -1));
    }

    /**
     * 每类舱包含多个区，每个区有两组信息，'1'表示第一组，'2'表示第二组
     * @param deviceId  RQH_011
     * @return  返回H，表示湿度
     */
    public static char getGroupInfo(String deviceId)
    {
        String key = deviceId.split(SuinengConst.METRIC_SPILT)[1];
        return key.charAt(key.length() -1);
    }
}
