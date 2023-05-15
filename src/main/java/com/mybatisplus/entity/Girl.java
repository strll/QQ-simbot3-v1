package com.mybatisplus.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import love.forte.simbot.event.FriendMessageEvent;
import love.forte.simbot.event.MessageEvent;

import java.util.HashMap;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Girl {
    private String id;
    private HashMap<FriendMessageEvent,String> hashMap;
}
