package com.suineng.mttq;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class ClientSearch {

    public static String str="";
    public static final String HOST = "tcp://192.168.1.103s:61613";
    //服务器内置主题，用来监测当前服务器上连接的客户端数量（$SYS/broker/clients/connected）
    public static final String TOPIC1 = "UPDATA";
    private static final String clientid = "1";
    private MqttClient client;
    private MqttConnectOptions options;
    private String userName = "admin";
    private String passWord = "password";
    @SuppressWarnings("unused")
    private ScheduledExecutorService scheduler;

    public void start() throws MqttException {
        // host为主机名，clientid即连接MQTT的客户端ID，一般以唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
        client = new MqttClient(HOST, clientid, new MemoryPersistence());
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
            public void connectionLost(Throwable cause) {
                // 连接丢失后，一般在这里面进行重连
//                System.out.println("连接断开，可以做重连");
            }

            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("deliveryComplete---------" + token.isComplete());
            }
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                try {
                    str = message.toString();
                  System.out.println(" 从服务器收到的消息为:"+message.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
//      MqttTopic topic = client.getTopic(TOPIC1);
//      setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
//      options.setWill(topic, "close".getBytes(), 2, true);  //遗嘱
        client.connect(options);
        //订阅消息
        int[] Qos  = {1};
        String[] topic1 = {TOPIC1};
        client.subscribe(topic1, Qos);
    }
    @SuppressWarnings("static-access")
    //用来给调用处返回消息内容
    public String resc() {
        return this.str;
    }
}
