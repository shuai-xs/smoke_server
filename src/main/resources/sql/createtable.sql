create database IF NOT EXISTS snwltest;
use snwltest;

CREATE TABLE IF NOT EXISTS `alert`(
   `id` VARCHAR(100) NOT NULL,
   `moduleId` VARCHAR(10),
   `zoneId` SMALLINT,
   `metricType` VARCHAR(10),
   `alertType` SMALLINT,
   `metricValue` FLOAT NOT NULL,
   `status` SMALLINT,
   `createTime` BIGINT,
   `confirmTime` BIGINT,
   PRIMARY KEY ( `id`,`createTime` )
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `user`(
   `userName` VARCHAR(100) NOT NULL,
   `passwd` VARCHAR(100) NOT NULL,
   PRIMARY KEY ( `userName`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tbl_position`(
   time BIGINT NOT NULL
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists tbl_device(
 id  varchar(12) primary key,
 module_id varchar(6),
 zone_id int,
 device_type varchar(6),
 controlType varchar(10),
 controlSwitch varchar(10),
 switchStatus varchar(10),
 highState DOUBLE,
 lowState DOUBLE,
 status boolean, 
 alarm boolean,  
 index(id, module_id, device_type, zone_id)  
 )ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
 create table if not exists tbl_metric(
 id  varchar(12), 
 time bigint,
 value_0 float,
 primary key(id,time)
 )ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
 