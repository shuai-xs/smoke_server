package com.suineng.stream;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suineng.bean.Metric;
import com.suineng.cache.DeviceCache;
import com.suineng.service.DeviceService;
import com.suineng.service.MetricService;
import com.suineng.task.AlertCompute;
import com.suineng.task.MetricHandler;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ReadData {

    @Value("${model.read:true}")
    public boolean read = true;


    @Value("${model.host:3600}")
    public String HOST = "tcp://192.168.1.103:61613";

    @Value("${model.userName:admin}")
    public String userName = "admin";

    @Value("${model.passWord:passWord}")
    public String passWord = "password";

    //服务器内置主题，用来监测当前服务器上连接的客户端数量（$SYS/broker/clients/connected）
    @Value("${model.topic:UPDATA}")
    public String TOPIC1 = "UPDATA";

    private BlockingQueue<Runnable> workThreads = new LinkedBlockingDeque<Runnable>();// 当前工作线程

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 5, TimeUnit.SECONDS,
            workThreads);
    @Autowired
    private DeviceCache deviceCache;

    @Autowired
    private MetricService metricService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private AlertCompute alertCompute;

    private MqttClient client;

    private MqttConnectOptions options;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadData.class);

    private long parseTime(String timeStr)
    {
        try
        {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = null;
            date = formatter.parse(timeStr);
            long time = date.getTime();
            time = time - (time%(3600*1000));
            return time;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return -1;
    }
    @PostConstruct
    public void init()
    {
        if(!read)
        {
            return;
        }
        try
        {
            // host为主机名，clientid即连接MQTT的客户端ID，一般以唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(HOST, "1", new MemoryPersistence());
            // MQTT的连接设置
            options = new MqttConnectOptions();
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(false);
            // 设置连接的用户名
            options.setUserName(userName);
            // 设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            // 设置回调
            client.setCallback(new MqttCallback(){

                @Override
                public void connectionLost(Throwable cause) {
                    try
                    {
                        //10秒之后开始重连
                        Thread.sleep(10*1000);
                        client.connect(options);
                        //订阅消息
                        int[] Qos  = {1};
                        String[] topic1 = {TOPIC1};
                        client.subscribe(topic1, Qos);
                    }catch (Exception e)
                    {
                        LOGGER.error("reconnect fail", e);
                    }

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("deliveryComplete---------" + token.isComplete());
                }
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    List<MetricHandler> handlers = new ArrayList<MetricHandler>();
                    try {
                        String str  = message.toString();
                        System.out.println(str);
                        JSONArray array = JSONArray.parseArray(str);
                        Iterator<Object> ite = array.iterator();
                        while(ite.hasNext())
                        {
                            JSONObject object = (JSONObject)ite.next();
                            long time = parseTime(object.getString("TIME"));
                            JSONObject metricObj = object.getJSONObject("body").getJSONObject("A").getJSONObject("REG_VAL");
                            Set<String> keySet = metricObj.keySet();
                            for(String key : keySet)
                            {
                                deviceService.handlerRequest(key);
                                float value = metricObj.getFloatValue(key);
                                Metric metric = new Metric();
                                metric.setId(key);
                                metric.setValue0(value);
                                metric.setTime(time);
                                alertCompute.computeData(metric.getId(), metric.getTime(), metric.getValue0());
                                new MetricHandler(metricService, metric, deviceCache).call();
                            }
                            //executor.invokeAll(handlers);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            client.connect(options);
            //订阅消息
            int[] Qos  = {1};
            String[] topic1 = {TOPIC1};
            client.subscribe(topic1, Qos);
        }catch (Exception e)
        {
            LOGGER.error("read data init fail", e);
        }
    }

    public static void main(String[] args)
    {
        try
        {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = null;
            date = formatter.parse("20190512114530");
            System.out.println(date.getTime());
            System.out.println(System.currentTimeMillis());
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
