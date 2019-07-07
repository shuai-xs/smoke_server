package com.suineng.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PositionMapper
{
    public void insert(Long time);

    public Long getPosition();
}
