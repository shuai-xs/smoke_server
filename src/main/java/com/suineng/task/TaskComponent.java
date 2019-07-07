package com.suineng.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskComponent {

    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Autowired
    private TaskScheduler taskScheduler;

    @PostConstruct
    public void init()
    {
        executor.scheduleAtFixedRate(taskScheduler, 10, 60, TimeUnit.SECONDS);
    }
}
