package com.suineng.controller;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.suineng.bean.Metric;
import com.suineng.cache.DeviceCache;
import com.suineng.common.SuinengConst;
import com.suineng.common.WetherUtil;
import com.suineng.service.MetricService;


@Controller
public class MetricController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricController.class);

    @Autowired
    private MetricService metricQueryHandler;

    @Autowired
    private DeviceCache deviceCache;
    
    @Autowired
    private HttpServletResponse response;

    @RequestMapping(value="/v1/wether", method= RequestMethod.GET)
    @ResponseBody
    public String getWether(@RequestParam(value =  "city")String city)
    {
        return WetherUtil.excute(city);
    }
    
    /**
     * get all the device, or get device by module and zone zone_id
     * 
     * @return List<Device>
     */
    @RequestMapping(value = "/v1/metricdata", method = RequestMethod.GET)
    @ResponseBody
    public List<Metric> getMetricData(@RequestParam(value = "moduleId") String moduleId,
                                          @RequestParam(value = "zoneId") String zoneId,
                                          @RequestParam(required = false, value = "startTime") Long startTime,
                                          @RequestParam(required = false, value = "endTime") Long endTime,
                                          @RequestParam(required = false, value = "pageIndex") int pageIndex,
                                          @RequestParam(required = false, value = "pageSize") int pageSize)
    {
        return metricQueryHandler.getMetricData(moduleId, zoneId, startTime, endTime, pageIndex, pageSize);
    }

    /**
     * get all the device, or get device by module and zone zone_id
     * 
     * @return List<Device>
     */
    @RequestMapping(value = "/v1/exportdata", method = RequestMethod.GET)
    @ResponseBody
    public void exportMetricData(@RequestParam(required = false, value = "ids") String ids,
                                 @RequestParam(required = false, value = "from") Long from,
                                 @RequestParam(required = false, value = "to") Long to,
                                 @RequestParam(required = false, value = "moduleId") String moduleId,
                                 @RequestParam(required = false, value = "zoneId") String zoneId)
    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("1", "id");
        map.put("2", "timestamp");
        map.put("3", "value");
        if(moduleId == null)
        {
            moduleId = "RQ";
            zoneId = "1";
        }
        List<String> idList = deviceCache.getDeviceIdList(moduleId, zoneId);
        for (int i = 0; i < idList.size(); ++i)
        {
            map.put(String.valueOf(i + 1), idList.get(i));
        }

        System.out.println(idList);
        List<Metric> metrics = metricQueryHandler.getMetric(idList, from == null ? 0 : from,
            to == null ? 0 : to);
        List<String> fields = new ArrayList<String>();
        fields.add("getId");
        fields.add("getTime");
        fields.add("getValue0");

        exportFile(response, map, metrics, fields);
    }

    /**
     * @param response
     * @param map
     *            对应的列标题
     * @param
     *
     * @param
     *
     * @throws IOException
     */
    private void exportFile(HttpServletResponse response, LinkedHashMap<String, String> map,
                                  List<Metric> metrics, List<String> fields)
    {
        BufferedWriter csvFileOutputStream = null;
        try
        {
            // 写入临时文件
            File tempFile = File.createTempFile("export", ".csv");
            // UTF-8使正确读取分隔符","
            csvFileOutputStream = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"), 1024);
            // 写入文件头部
            for (Iterator<Entry<String, String>> propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext();)
            {
                Entry<String, String> propertyEntry = (Entry<String, String>)propertyIterator.next();
                csvFileOutputStream.write((String)propertyEntry.getValue() != null ? new String(
                    ((String)propertyEntry.getValue()).getBytes("UTF-8"), "UTF-8") : "");
                if (propertyIterator.hasNext())
                {
                    csvFileOutputStream.write(",");
                }
            }
            csvFileOutputStream.write("\r\n");
            LOGGER.info("metrics size is {}", metrics.size());
            // 写入文件内容,
            for (Metric metric : metrics)
            {
                // RealTimeSO2 t = (BankWageMonth) exportData.get(j);
                Class<? extends Metric> clazz = metric.getClass();
                String[] contents = new String[fields.size()];
                for (int i = 0; fields != null && i < fields.size(); i++ )
                {
                    Object obj = null;
                    try
                    {
                        Method method = clazz.getMethod(fields.get(i));
                        obj = method.invoke(metric);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        LOGGER.error("export error", e);
                    }
                    String str = String.valueOf(obj);
                    if (str == null || str.equals("null"))
                    {
                        str = "";
                    }
                    contents[i] = str;
                }

                for (int n = 0; n < contents.length; n++ )
                {
                    // 将生成的单元格添加到工作表中
                    csvFileOutputStream.write(contents[n]);
                    csvFileOutputStream.write(",");
                }
                csvFileOutputStream.write("\r\n");
            }

            csvFileOutputStream.flush();

            // 写入csv结束，写出流
            OutputStream out = response.getOutputStream();
            byte[] b = new byte[10240];
            java.io.File fileLoad = new java.io.File(tempFile.getCanonicalPath());
            response.reset();
            response.setContentType("application/csv");
            String trueCSVName = "export.csv";
            response.setHeader("Content-Disposition",
                "attachment;  filename=" + new String(trueCSVName.getBytes("UTF-8"), "UTF-8"));
            long fileLength = fileLoad.length();
            String length1 = String.valueOf(fileLength);
            response.setHeader("Content_Length", length1);
            java.io.FileInputStream in = new java.io.FileInputStream(fileLoad);
            int n;
            while ((n = in.read(b)) != -1)
            {
                out.write(b, 0, n); // 每次写入out1024字节
            }
            in.close();
            out.close();

        }
        catch (IOException e)
        {
            e.printStackTrace();
            LOGGER.error("export error", e);
        }
        finally
        {
            if (csvFileOutputStream != null)
            {
                try
                {
                    csvFileOutputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将第一个字母转换为大写字母并和get拼合成方法
     * 
     * @param origin
     * @return
     */
    private static String toUpperCaseFirstOne(String origin)
    {
        StringBuffer sb = new StringBuffer(origin);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.insert(0, "get");
        return sb.toString();
    }

    /**
     * 首页功能，获取当月每个舱的平均温度
     * 
     * @return
     */
    @RequestMapping(value = "/v1/cur/temp", method = RequestMethod.GET)
    @ResponseBody
    public String getCurTempInfo()
    {
        return metricQueryHandler.getCurMonthTempInfo();
    }

    /**
     * 获取最近6天濕度
     * 
     * @return
     */
    @RequestMapping(value = "/v1/cur/humidity", method = RequestMethod.GET)
    @ResponseBody
    public String get6daysHumidityInfo()
    {
        return metricQueryHandler.get6daysHumidityInfo();
    }

    /**
     * 获取当前某个舱，某个区的指标详细
     * 
     * @return
     */
    @RequestMapping(value = "/v1/cur/metricInfo", method = RequestMethod.GET)
    @ResponseBody
    public String getCurMetricInfo(@RequestParam(value = "moduleId") String moduleId,
                                   @RequestParam(value = "zoneId") String zoneId,
                                   @RequestParam(value="startTime", required = false) Long startTime)
    {
        if(null == startTime)
        {
            long end = System.currentTimeMillis();
            // 一小时前的时间，指标每小时上报一次
            long start = end - SuinengConst.PERIOD;
            return metricQueryHandler.getCurMetricInfo(moduleId, zoneId, start, end);
        }
        else
        {
            startTime = startTime - startTime%(3600*1000);
            long endTime = startTime + 3600*1000 -1;
            return metricQueryHandler.getCurMetricInfo(moduleId, zoneId, startTime, endTime);
        }
    }

    /**
     * 获取某个时间点的某个舱，某个区的指标详细
     *
     * @return
     */
    @RequestMapping(value = "/v1/metricInfo", method = RequestMethod.GET)
    @ResponseBody
    public String getMetricInfo(@RequestParam(value = "moduleId") String moduleId,
                                   @RequestParam(value = "zoneId") String zoneId,
                                   @RequestParam(value = "startTime") long startTime)
    {
        startTime = startTime - startTime%(3600*1000);
        long endTime = startTime + 3600*1000 -1;
        return metricQueryHandler.getCurMetricInfo(moduleId, zoneId, startTime, endTime);
    }


    /**
     * 获取当前某个舱，某个区的指标详细
     * @return
     */
    @RequestMapping(value = "/v1/avg/metricInfo", method = RequestMethod.GET)
    @ResponseBody
    public String getAvgMetricInfo(@RequestParam(value = "moduleId") String moduleId,
                                   @RequestParam(value = "zoneId") String zoneId,
                                   @RequestParam(value = "type" ,required = false) String type,
                                   @RequestParam(value = "startTime" ,required = false) Long startTime,
                                   @RequestParam(value = "endTime",required = false) Long endTime)
    {
        if("month".equals(type))
        {
            long time = System.currentTimeMillis();
            startTime = getMonthStart(0, time);
            long numValue = (time - startTime) / (3600*24*1000) +1;
            numValue = numValue > 31 ? 31 : numValue;
            return metricQueryHandler.getAvgMetric(moduleId, zoneId, startTime, time, (int)numValue, "day");
        }
        if("day".equals(type))
        {
            long time = System.currentTimeMillis();
            startTime = getDayStart(time);
            long numValue = (time - startTime) / (3600*1000) + 1;
            numValue = numValue > 24 ? 24 : numValue;
            return metricQueryHandler.getAvgMetric(moduleId, zoneId, startTime, time, (int)numValue, "hour");
        }
        if("week".equals(type))
        {
            long time = System.currentTimeMillis();
            startTime = getWeekStart(time);
            long numValue = (time - startTime) / (3600*24*1000) +1;
            numValue = numValue > 7 ? 7 : numValue;
            return metricQueryHandler.getAvgMetric(moduleId, zoneId, startTime, time, (int)numValue, "day");
        }
        Calendar calendar = Calendar.getInstance();
        int value = calendar.get(Calendar.HOUR_OF_DAY) + 1;
        startTime = startTime + ((25-value) * 3600 * 1000);
        endTime = endTime + ((24-value) * 3600 * 1000)+1;
        long numValue = (endTime - startTime) / (3600*24*1000) + 1;
        numValue = numValue > 30 ? 30 : numValue;
        return metricQueryHandler.getAvgMetric(moduleId, zoneId, startTime, endTime, (int)numValue, "day");
    }

    private long getWeekStart(long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        if(Calendar.SUNDAY == calendar.get(Calendar.DAY_OF_WEEK))
        {
            calendar.add(Calendar.DAY_OF_WEEK, -6);
        }
        else
        {
            //国外每周第一天从周天开始
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMinimum(Calendar.DAY_OF_WEEK)+1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getWeekEnd(long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTimeInMillis();
    }


    private long getMonthStart(int offset, long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getMonthEnd(int offset, long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.MONTH, offset);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTimeInMillis();
    }

    private long getDayStart(long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }
}
