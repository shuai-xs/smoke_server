package com.suineng.mttq;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

public class ServerMQTTUtil {
    //tcp://MQTT安装的服务器地址:MQTT定义的端口号
    public String HOST = "tcp://192.168.1.107:61613";
    //定义MQTT的ID，可以在MQTT服务配置中指定
    private String clientid = "SNCS_001";

    private MqttClient client;
    private MqttTopic mqttTopic;
    public String userName = "admin";
    public String passWord = "password";

    /**
     * 构造函数
     * @throws MqttException
     */
    public ServerMQTTUtil(String topic) throws MqttException {
        // MemoryPersistence设置clientid的保存形式，默认为以内存保存
        client = new MqttClient(HOST, clientid, new MemoryPersistence());
        connect(topic);
    }

    /**
     *  用来连接服务器
     */
    private void connect(String topic) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setUserName(userName);
        options.setPassword(passWord.toCharArray());
        // 设置超时时间
        options.setConnectionTimeout(10);
        // 设置会话心跳时间
        options.setKeepAliveInterval(20);
        try {
//          client.setCallback(new PushCallback());
            client.connect(options);
            mqttTopic = client.getTopic(topic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送消息并获取回执
    public void publish(String topic,MqttMessage message)
            {
                try {
                    client.publish(topic,message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
}
