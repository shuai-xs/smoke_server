package com.suineng.dao;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.suineng.bean.Metric;

@Mapper
public interface MetricMapper
{
    int insert(Metric metric);

    List<Metric> getMetric(@Param("ids") List<String> ids, @Param("from") long from,
                           @Param("to") long to);

    List<Metric> getMetricLimit(@Param("ids") List<String> ids, @Param("from") long from,
                           @Param("to") long to, @Param("pageIndex") long pageIndex,
                           @Param("pageSize") long pageSize);
}