package com.suineng.bean;


import java.util.ArrayList;
import java.util.List;


public class MetricData
{
    String id;

    List<Point> data;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public List<Point> getData()
    {
        return data;
    }

    public void setData(List<Point> data)
    {
        this.data = data;
    }

    public void addData(Point point)
    {
        if (data == null)
        {
            data = new ArrayList<Point>();
        }
        data.add(point);
    }
}
