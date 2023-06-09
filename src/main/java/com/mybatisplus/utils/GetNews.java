package com.mybatisplus.utils;


import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import love.forte.simbot.component.mirai.message.MiraiForwardMessageBuilder;
import love.forte.simbot.event.GroupMessageEvent;
import love.forte.simbot.message.MessagesBuilder;
import love.forte.simbot.resources.Resource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

@Service
public class GetNews {
            //因为信息过长所以需要以聊天列表的形式发送 经过测试图片和文字在一起容易被风控所以分开发送
            public   ArrayList<String> EveryDayNews() throws IOException {
                ArrayList<String> strings = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readValue(new URL("https://www.zhihu.com/api/v4/columns/c_1261258401923026944/items"), JsonNode.class);
                String contentHtml = jsonNode.get("data").get(0).get("content").asText();
                //   System.out.println(contentHtml);
                Document parse = Jsoup.parse(contentHtml);
                StringBuilder result = new StringBuilder();
                Elements allElements = parse.getAllElements();
                for (Element element : allElements) {
                    if("img".equals(element.tagName())){
                        String url = element.attr("src");
                        strings.add(url);
                    }else if("p".equals(element.tagName())) {
                        String content = element.text().trim();
                        strings.add(content);
                    }
                }


                return strings;
            }

    public  MiraiForwardMessageBuilder EveryDayNews(GroupMessageEvent event) throws IOException {
        MiraiForwardMessageBuilder miraiForwardMessageBuilder=new MiraiForwardMessageBuilder();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readValue(new URL("https://www.zhihu.com/api/v4/columns/c_1261258401923026944/items"), JsonNode.class);
        String contentHtml = jsonNode.get("data").get(0).get("content").asText();

        Document parse = Jsoup.parse(contentHtml);
        Elements allElements = parse.getAllElements();
        for (Element element : allElements) {
            if("img".equals(element.tagName())){
                var result = new MessagesBuilder();
                String url = element.attr("src");
                if (!StrUtil.isEmpty(url)){
                    result.image(Resource.of(new URL(url)));
                    miraiForwardMessageBuilder.add(event.getBot().getId(),event.getBot().getUsername(), result.build());
                }
            }else if("p".equals(element.tagName())) {
                var result = new MessagesBuilder();

                    String content = element.text().trim();
                if (!StrUtil.isEmpty(content)) {
                    result.append(content).append("\n");
                    miraiForwardMessageBuilder.add(event.getBot().getId(), event.getBot().getUsername(), result.build());
                }
            }
        }
        return  miraiForwardMessageBuilder;
    }


    public  MiraiForwardMessageBuilder EveryDayNews_safe(GroupMessageEvent event) throws IOException {
        MiraiForwardMessageBuilder miraiForwardMessageBuilder=new MiraiForwardMessageBuilder();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readValue(new URL("https://www.zhihu.com/api/v4/columns/c_1261258401923026944/items"), JsonNode.class);
        String contentHtml = jsonNode.get("data").get(0).get("content").asText();

        Document parse = Jsoup.parse(contentHtml);
        Elements allElements = parse.getAllElements();
        for (Element element : allElements) {
            if("img".equals(element.tagName())){
//                var result = new MessagesBuilder();
//                String url = element.attr("src");
//                if (!StrUtil.isEmpty(url)){
//                    result.image(Resource.of(new URL(url)));
//                    miraiForwardMessageBuilder.add(event.getBot().getId(),event.getBot().getUsername(), result.build());
//                }
            }else if("p".equals(element.tagName())) {
                var result = new MessagesBuilder();

                String content = element.text().trim();
                if (!StrUtil.isEmpty(content)) {
                    result.append(content).append("\n");
                    miraiForwardMessageBuilder.add(event.getBot().getId(), event.getBot().getUsername(), result.build());
                }
            }
        }
        return  miraiForwardMessageBuilder;
    }
}
