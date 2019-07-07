package com.suineng.controller;


import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
import com.suineng.bean.Alert;
import com.suineng.service.AlertService;


@Controller
public class AlertController
{
    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);

    @Autowired
    private AlertService alertService;

    @RequestMapping(value = "/alert/test", method = RequestMethod.GET)
    public void test(HttpServletRequest request, HttpServletResponse response)
    {
        this.updateResponse(response, "this is test");
    }

    /**
     * 查询告警列表
     * @param
     * {
     *     "startTime":xx,//-1表示默认查询
     *     "endTime":xx,
     *     "status":xx,//-1表示查询所有状态
     *     "deviceType":"",//null或空字符串表示查询所有
     *     "zoneId":"",//-1表示查询所有
     *     "curIndex":"",//从哪条数据查起
     *     "pageSize":""//每页大小
     *
     * }
     */
    @RequestMapping(value="/v1/alert/list", method= RequestMethod.GET)
    @ResponseBody
    public String alertList(@RequestParam(required = false, value = "startTime") Long startTime,
                                 @RequestParam(required = false, value = "endTime") Long endTime,
                                 @RequestParam(required = false, value = "status") Integer status,
                                 @RequestParam(required = false, value = "moduleId") String mouduleId,
                                 @RequestParam(required = false, value = "zoneId") Integer zoneId,
                                 @RequestParam(required = false, value = "curIndex") Integer curIndex,
                                 @RequestParam(required = false, value = "pageSize") Integer pageSize)
    {
        if(curIndex == null || pageSize == null)
        {
            return null;
        }
        return alertService.getAlertList(startTime, endTime, status, mouduleId, zoneId, curIndex, pageSize);
    }

    /**
     * 本日，本周，本月，告警页面右上角，已处理告警，各个检测设备占比情况，预警处理占比
     */
    @RequestMapping(value = "/v1/alert/count", method = RequestMethod.GET)
    @ResponseBody
    public String alertCountInfo()
    {
        String result = this.alertService.getDefaultProcessedInfo();
        return result;
    }

    /**
     * 预警处理占比信息，告警页面左上角，累计告警信息
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "/v1/alert/totalInfo", method = RequestMethod.GET)
    @ResponseBody
    public String alertRateInfo(HttpServletRequest request, HttpServletResponse response)
    {
        return this.alertService.getAlertDescInfo();
    }

    /**
     * 首页左上角，历史告警分布图，显示最近6个月的告警个数 { "4":xx, "3":xx, "2":xx, "1":xx, "12":xx, "11":xx }
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "/v1/alert/6month/info", method = RequestMethod.GET)
    @ResponseBody
    public String getAlertlst6Month(HttpServletRequest request, HttpServletResponse response)
    {
        return this.alertService.getAlertlst6Month();
    }

    /**
     * 确认告警
     * 
     */
    @RequestMapping(value = "/v1/alert/confirm", method = RequestMethod.POST)
    @ResponseBody
    public String confirmAlert(@RequestBody String param)
    {
        try
        {
            JSONArray array = JSONArray.parseArray(param);
            int size = array.size();
            for(int i=0; i<size; i++)
            {
                JSONObject object = array.getJSONObject(i);
                Alert alert = new Alert();
                alert.setId(object.getString("alertId"));
                alert.setCreateTime(object.getLong("createTime"));
                this.alertService.confirmAlert(alert);
            }
        }catch (Exception e)
        {
            logger.error("", e);
        }
        /*Alert alert = new Alert();
        alert.setId(alertId);
        alert.setCreateTime(createTime);
        this.alertService.confirmAlert(alert);*/
        JSONObject object = new JSONObject();
        object.put("result",true);
        return object.toJSONString();
    }

    private void updateResponse(HttpServletResponse response, String result)
    {
        OutputStream outputStream = null;
        try
        {
            outputStream = response.getOutputStream();
            outputStream.write(result.getBytes());
        }
        catch (Exception e)
        {
            logger.error("response fail", e);
        }
        finally
        {
            if (null != outputStream)
            {
                try
                {
                    outputStream.close();
                }
                catch (Exception e)
                {
                    logger.error("close fail", e);
                }
            }
        }

    }

    private void updateResponse(HttpServletResponse response, int result)
    {
        String value = this.getResult(result);
        updateResponse(response, value);
    }

    private String getResult(int result)
    {
        JSONObject object = new JSONObject();
        if (1 == result)
        {
            object.put("result", "success");
        }
        else
        {
            object.put("result", "fail");
        }
        return object.toJSONString();
    }

    /**
     * 清楚告警
     * 告警的设备id
     * 告警产生时间，与id一起构成主键
     */
    @RequestMapping(value = "/v1/alert/clear", method = RequestMethod.POST)
    @ResponseBody
    public String clearAlert(@RequestBody String param)
    {

        try
        {
            JSONArray array = JSONArray.parseArray(param);
            int size = array.size();
            for(int i=0; i<size; i++)
            {
                JSONObject object = array.getJSONObject(i);
                Alert alert = new Alert();
                alert.setId(object.getString("alertId"));
                alert.setCreateTime(object.getLong("createTime"));
                this.alertService.cleanAlert(alert);
            }
        }catch (Exception e)
        {
            logger.error("", e);
        }
        JSONObject object = new JSONObject();
        object.put("result",true);
        return object.toJSONString();
    }
}
