package com.suineng.bean;
import java.text.DateFormat;
import java.text.DecimalFormat;

public class Metric
{
    private static final DecimalFormat df3 = new DecimalFormat("###.00");
    // 详细设备id
    private String id;

    long time;

    float value0;


    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time = time;
    }

    public float getValue0()
    {
        return value0;
    }

    public void setValue0(float value0)
    {
        this.value0 = value0 == 0f ? 0f : Float.valueOf(df3.format(value0));
    }
    @Override
    public String toString()
    {
        DateFormat format1 = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return "Metric [id=" + id + ", time=" + time + ", value0=" + format1.format(value0) + "]";
    }

}
