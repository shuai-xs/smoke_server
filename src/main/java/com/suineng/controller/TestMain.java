package com.suineng.controller;

import java.util.Calendar;
import java.util.TimeZone;

public class TestMain {

    public static void main(String[] args)
    {
        Calendar calendar = Calendar.getInstance();
        //calendar.setTimeZone(TimeZone.getTimeZone("GMT+:08:00"));
        int value = calendar.get(Calendar.HOUR_OF_DAY);
        System.out.println(value);
    }
}
