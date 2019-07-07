package com.suineng.common;

public class AlertConst {

    public static final String REMOTE = "remote";

    public static final String LOCAL = "local";

    public static final String AUTO = "auto";

    public static final String MANAUL = "manual";

    public static final String OPEN = "open";

    public static final String CLOSE = "close";

    public static final String HIGH = "high";

    public static final String LOW = "low";

    public static final String METRICNAMTE = "metricName";

    public static final String START_MONTH = "startMonth";

    public static final String END_MONTH = "endMonth";

    public static final String HIGH_THRESHOLD = "highThreshold";

    public static final String LOW_THRESHOLD = "lowThreshold";

    //表示告警状态
    public static final String STATUS_0 = "待确认";

    //表示告警已确认
    public static final String STATUS_1 = "已确认";

    //表示告警清楚
    public static final String STATUS_2 = "已处理";

    //告警信息列表，编号
    public static final String ID = "id";

    //告警信息列表，告警位置
    public static final String ADDRESS = "address";

    //告警名称
    public static final String ALERT_NAME = "alertName";

    public static final String ALERT_INFO = "alertInfo";

    //告警值
    public static final String ALERT_VALUE = "alertValue";

    public static final String ALERT_STATUS = "alertStatus";

    public static final String ALERT_TIME = "createTime";

    public static final String CONFIRM_TIME = "confirmTime";

    //2表示已处理、1表示已确认、0表示待确认
    public static final int STATUS_PROCESSED = 2;

    public static final int STATUS_CONFIRMED = 1;

    public static final int STATUS_NOT_CONFIRM = 0;

    ////指标类型，0表示氧气浓度，1表示液位，2表示温度，3表示湿度
    //    private int metricType;
    //氧气浓度
    public static final String OXGEN = "O";

    //液位
    public static final String LEVEL = "Y";

    //温度
    public static final String TEMPERATURE = "T";

    //湿度
    public static final String HUMIDITY = "H";

}
