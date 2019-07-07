package com.suineng.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.suineng.bean.Alert;

@Mapper
public interface AlertMapper {

    /**
     *
     * @param curIndex 当前offset位置
     * @param pageSize 查询数据个数
     * @return
     */
    public List<Alert> queryAlert(@Param("curIndex")int curIndex, @Param("pageSize")int pageSize,
                                  @Param("startTime") long startTime, @Param("endTime") long endTime,
                                  @Param("status") int status );

    /**
     *
     * @param curIndex 当前offset位置
     * @param pageSize 查询数据个数
     * @return
     */
    public List<Alert> queryAlertAll(@Param("curIndex")int curIndex, @Param("pageSize")int pageSize,
                                  @Param("startTime") long startTime, @Param("endTime") long endTime);


    public List<Alert> queryAlertWithZone(@Param("curIndex")int curIndex, @Param("pageSize")int pageSize,
                                            @Param("startTime") long startTime, @Param("endTime") long endTime,
                                            @Param("deviceType") String deviceType, @Param("zoneId") int zoneId,
                                            @Param("status") int status);

    public List<Alert> queryAlertWithZoneAll(@Param("curIndex")int curIndex, @Param("pageSize")int pageSize,
                                          @Param("startTime") long startTime, @Param("endTime") long endTime,
                                          @Param("deviceType") String deviceType, @Param("zoneId") int zoneId);

    public List<Alert> queryAlertWithDevice(@Param("curIndex")int curIndex, @Param("pageSize")int pageSize,
                                            @Param("startTime") long startTime, @Param("endTime") long endTime,
                                            @Param("deviceType") String deviceType, @Param("status") int status);

    public List<Alert> queryAlertWithDeviceAll(@Param("curIndex")int curIndex, @Param("pageSize")int pageSize,
                                            @Param("startTime") long startTime, @Param("endTime") long endTime,
                                            @Param("deviceType") String deviceType);

    //设备id， 报警信息，告警值(指标值)，状态，告警时间，确认时间
    public int insertAlert(Alert alert);

    public void updateAlert(Alert alert);

    public int countAlertNum(Alert alert);
    /**
     * 计算当前告警总数
     * @return
     */
    public int countAlert(@Param("startTime") long startTime, @Param("endTime") long endTime, @Param("status")int status);

    public int countAlertWithMetricType(@Param("startTime") long startTime, @Param("endTime") long endTime, @Param("status")int status, @Param("metricType")int metricType);

    public int countAlertAll(@Param("startTime") long startTime, @Param("endTime") long endTime);

    public int countWithDevice(@Param("startTime") long startTime, @Param("endTime") long endTime,
                               @Param("deviceType") String deviceType,
                                @Param("status") int status);

    public int countWithDeviceAll(@Param("startTime") long startTime, @Param("endTime") long endTime,
                               @Param("deviceType") String deviceType);

    public int countWithZone(@Param("startTime") long startTime, @Param("endTime") long endTime,
                             @Param("deviceType") String deviceType, @Param("zoneId") int zoneId,
                             @Param("status") int status);

    public int countWithZoneAll(@Param("startTime") long startTime, @Param("endTime") long endTime,
                             @Param("deviceType") String deviceType, @Param("zoneId") int zoneId);

    List<Alert> getAlert(@Param("alert") Alert alert, @Param("curIndex") int curIndex, @Param("pageSize") int pageSize);
}
