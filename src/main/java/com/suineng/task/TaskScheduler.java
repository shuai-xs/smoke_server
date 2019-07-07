package com.suineng.task;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.suineng.bean.Metric;
import com.suineng.cache.DeviceCache;
import com.suineng.service.DeviceService;
import com.suineng.service.MetricService;


public class TaskScheduler implements Runnable
{
//    private long lastTime = System.currentTimeMillis() - 3600 * 1000;

    private static final Logger logger = LoggerFactory.getLogger(TaskScheduler.class);

    private long lastTime = 1540036066081L;

    @Autowired
    private TsdbDao tsdbDao;

    @Autowired
    private DeviceCache deviceCache;
    
    @Autowired
    private MetricService metricService;

    @Autowired
    private DeviceService deviceService;

    private BlockingQueue<Runnable> workThreads = new LinkedBlockingDeque<Runnable>();// 当前工作线程

    ThreadPoolExecutor executor = new ThreadPoolExecutor(40, 40, 30, TimeUnit.SECONDS,
        workThreads);

    public TaskScheduler()
    {
        
    }

    public void taskSchedule()
    {
        try
        {
            if (tsdbDao == null)
            {
                logger.warn("tsdbDao is null");
                return;
            }

            List<String> fields = tsdbDao.getAllField();
            for (String field : fields)
            {
                deviceService.handlerRequest(field);
            }

            if (lastTime == 1540036066081L)
            {
                Long time = metricService.getPosition();
                if (time != null && time > 0)
                {
                    lastTime = time;
                }
            }

            long end = 1547120866081L;
//            long end = System.currentTimeMillis();
            // 取一分钟以内的数据
            List<Metric> metrics = tsdbDao.getMetric(fields, lastTime, end);

            logger.info("metrics size: " + metrics.size());
            List<MetricHandler> handlers = new ArrayList<MetricHandler>();

            for (int i = 0; i < (metrics.size() > 10000 ? 10000 : metrics.size()); ++i)
            {
                handlers.add(new MetricHandler(metricService, metrics.get(i), deviceCache));
            }
            executor.invokeAll(handlers);
            lastTime = end;

            metricService.insert(lastTime);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }

    }

    @Override
    public void run()
    {
        try
        {
            taskSchedule();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

//    public static void main(String[] args)
//    {
//        tsdbDao.cteate();

//        Metric metric = new Metric();
//        metric.setValue0(50.1f);
//        Class<?> classz = metric.getClass();
//        Method setValue;
//        try
//        {
//            setValue = classz.getMethod("setValue" + 1, float.class);
//            setValue.invoke(metric, metric.getValue0());
//            System.out.println(metric.getValue1());
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }

//        long end = 1547120866081L;
//        long start = 1540036066081L;
//
//        tsdbDao.getMetric(tsdbDao.getAllField(), start, end);
//        System.out.println(start);
//        System.out.println(end);
//    }
}
