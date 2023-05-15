package com.mybatisplus.controller;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mybatisplus.entity.Girl;
import com.mybatisplus.entity.Message;
import com.mybatisplus.utils.GetChatGpt;
import com.mybatisplus.utils.MyRedis;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.Listener;
import love.forte.simboot.filter.MatchType;
import love.forte.simbot.event.ContinuousSessionContext;
import love.forte.simbot.event.FriendMessageEvent;
import love.forte.simbot.event.GroupMessageEvent;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.message.Messages;
import love.forte.simbot.message.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Controller
public class Privite_Talk {
    @Autowired
    private GetChatGpt getChatGpt;
    private HashMap<FriendMessageEvent,String> hashMap;
    @Autowired
    private MyRedis myRedis;



    @Listener
    public void openAi(FriendMessageEvent event, ContinuousSessionContext sessionContext) throws Exception {

            String next = new Scanner(event.getMessageContent().getPlainText()).next();
            if (next.equals("设置生理期")){
                final int time = 30;
                String accountCode = event.getId().toString();  //获取发送人的QQ号
                String groupid =event.getId().toString();
                event.replyBlocking("请输入您上一次生理期的时间 格式为 xxxx年xx月xx日 如2023年5月15日");
                sessionContext.waitingForNextMessage(accountCode , GroupMessageEvent.Key, time, TimeUnit.SECONDS, (e, c) ->{
                    if (!(c.getAuthor().getId().toString().equals(accountCode) && c.getGroup().getId().toString().equals(groupid))) {
                        return false;
                    }
                    if (e instanceof TimeoutException) {
                        c.replyAsync("超时啦");
                    }
                    Messages messages1 = c.getMessageContent().getMessages();
                    for (love.forte.simbot.message.Message.Element<?> element :messages1) {
                        if(element instanceof Text text){
                            String text1 = text.getText();
                            Girl girl = new Girl();
                            girl.setId(accountCode);
                            HashMap<FriendMessageEvent, String> messageEventStringHashMap = new HashMap<>();
                            messageEventStringHashMap.put((FriendMessageEvent) messageEventStringHashMap,text1);
                            girl.setHashMap((HashMap<FriendMessageEvent, String>) messageEventStringHashMap);
                            JSON json = JSONUtil.parse(girl);
                            String jsonString = json.toString();
                            myRedis.set("girl",jsonString);
                        }
                    }
                    return true;
                });
            }else {
                event.replyBlocking((getChatGpt.Get(next)));
            }
    }
    @Scheduled(cron="0 0 8 * * * ")
    public void historyTody() {
        List<String> girl = myRedis.listgetAll("girl");
        for (String s : girl) {
            // 将 JSON 字符串转换为 JSON 对象
            JSONObject jsonObject = JSONUtil.parseObj(s);
            // 将 JSON 对象转换为 Java 对象
            Girl thisgirl = JSONUtil.toBean(jsonObject, Girl.class);
            HashMap<FriendMessageEvent, String> hashMap = thisgirl.getHashMap();
            for (FriendMessageEvent friendMessageEvent : hashMap.keySet()) {
                String s1 = hashMap.get(friendMessageEvent);
                  if(is(s1))  {
                     friendMessageEvent.replyBlocking("今天可能是您的生理期(生理期按照30天为一个周期进行计算)");
            }
        }
    }

    }
    private Boolean is(String s) {

        // 已知的上一次生理期开始时间（字符串形式）
        String lastMenstrualStartDateStr =s;
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        // 定义日期格式模式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年M月d日");
        // 格式化当前日期为字符串
        String todayStr = currentDate.format(formatter);
        LocalDate lastMenstrualStartDate = LocalDate.parse(lastMenstrualStartDateStr, formatter);
        // 解析今天的日期字符串为 LocalDate 对象
        LocalDate today = LocalDate.parse(todayStr, formatter);
        // 判断今天是否是生理期开始时间
        if (today.equals(lastMenstrualStartDate)) {
            return  true;
        } else {
            return false;
        }
    }
    }

