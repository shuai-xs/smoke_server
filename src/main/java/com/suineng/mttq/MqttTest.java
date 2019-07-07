package com.suineng.mttq;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttTest {

    public static void  publish() throws  Exception{
        //设置推送消息体
        String sendTime = String.valueOf(System.currentTimeMillis());
        String str="[{\"PHONE\":\"123\",\"IDX\":\"576934d7b1004b459a357c0964f6321a\",\"FUNC\":6,\"body\":{\"A\":{\"REG_VAL\":{\"RQFJG_011\":0}}}}]";
//        String str="hello11";
        System.out.println(str);
        //将信息写入消息体
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(0);
        mqttMessage.setRetained(true);
        mqttMessage.setPayload(str.getBytes("UTF-8"));
            String topic = "DOWNDATA/123";
            ServerMQTTUtil serverMQTTUtil = new ServerMQTTUtil(topic);
            serverMQTTUtil.publish(topic,mqttMessage);
            System.out.println(mqttMessage.isRetained() + "------ratained状态");
//        }
    }

    /**
     * 每三秒钟返回服务器上的客户端连接数
     */
    public static void runTask() {
        final long timeInterval = 200;// 两秒运行一次
        Runnable runnable = new Runnable() {
            public void run() {
                while (true) {
                    // ------- code for task to run
                    try {       //你要运行的程序
                        ClientSearch clientSearch = new ClientSearch();
                        try {
                            clientSearch.start();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        Thread.sleep(1000); //给一秒时间接收服务器消息
//                        Integer num = Integer.valueOf(clientSearch.resc());
                        System.out.println("当前客户端连接数："+clientSearch.resc());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // ------- ends here
                    try {
                        Thread.sleep(timeInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public static void main (String[] args){


//        runTask();  //这个是订阅消息（获取）
        try {
            publish(); //这个是发布主题（控制）
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
