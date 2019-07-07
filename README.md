1. 设备管理
2. 指标管理
3. 数据导入

数据库mysql

数据库表设计：

#设备表
create table if not exists tbl_device(
 id  varchar(12) primary key,    # "DLFJ_042"
 module_id varchar(6),           # "DL","RQ","ZH"
 zone_id small int,              # 4
 device_type varchar(6),           # FJ
 op_state varchar(3),            # "1.1.1" 以.隔开的状态字符串，第一位代表远程控制(0)/就地控制(1), 第二位代表开(0)/关状态(1), 第三位代表高(0)/低(1)/不涉及(2)
 status bool,                    # 设备是否正常上报数据
 alarm bool,                     # 告警(true)/正常(false) 
 index("id", "module_id", "device_id", "zone_id")  
 );


# 指标表 
create table if not exists tbl_metric_201903(
 id  varchar(12) primary key,    # "DLH_042"
 time int,
 value_0 float,
 value_1 float, 
 value_2 float,
 value_3 float,
 value_4 float,
 value_5 float,
 value_6 float,
 value_7 float,
 value_8 float,
 value_9 float,
 value_10 float,
 value_11 float,
 value_12 float, 
 value_13 float,
 value_14 float,
 value_15 float,
 value_16 float,
 value_17 float,
 value_18 float,
 value_19 float,
 value_20 float,
 value_21 float, 
 value_22 float,
 value_23 float,
 value_24 float,
 value_25 float,
 value_26 float,
 value_27 float,
 value_28 float,
 value_29 float,
 value_30 float,
 value_31 float, 
 value_32 float,
 value_33 float,
 value_34 float,
 value_35 float,
 value_36 float,
 value_37 float,
 value_38 float,
 value_39 float,
 value_40 float,
 value_41 float, 
 value_42 float,
 value_43 float,
 value_44 float,
 value_45 float,
 value_46 float,
 value_47 float,
 value_48 float,
 value_49 float,
 value_50 float,
 value_51 float, 
 value_52 float,
 value_53 float,
 value_54 float,
 value_55 float,
 value_56 float,
 value_57 float,
 value_58 float,
 value_59 float,
 primary key(id,time)
 );

 
 