package com.mybatisplus.plugins.serachImage;
import com.google.gson.Gson;
import com.mybatisplus.utils.OK3HttpClient;
import com.mybatisplus.utils.Properties.properties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import love.forte.simboot.annotation.ContentTrim;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.Listener;
import love.forte.simboot.filter.MatchType;
import love.forte.simbot.ID;
import love.forte.simbot.component.mirai.message.MiraiForwardMessageBuilder;
import love.forte.simbot.event.ContinuousSessionContext;
import love.forte.simbot.event.GroupMessageEvent;
import love.forte.simbot.message.Image;
import love.forte.simbot.message.Message;
import love.forte.simbot.message.Messages;
import love.forte.simbot.message.MessagesBuilder;
import love.forte.simbot.resources.Resource;
;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class searchImage {
@Value("${picture_key}")
private String key;

    /**
     * 阻塞器
     * @param event
     * @param sessionContext
     * @return
     */
    @SneakyThrows
    @ContentTrim
    @Listener
    @Filter(value = "nana搜图",matchType = MatchType.TEXT_EQUALS)
    public void getSearch(GroupMessageEvent event, ContinuousSessionContext sessionContext) {
        MiraiForwardMessageBuilder miraiForwardMessageBuilder=new MiraiForwardMessageBuilder();
        final String qqId = String.valueOf(event.getAuthor().getId());
        final int time=30;
        ID id = event.getAuthor().getId();
        ID groupId = event.getGroup().getId();
        event.getSource().sendBlocking("发送图片");

        var params = new HashMap<String,Object>();
        params.put("db",999);
        params.put("output_type",2);
        params.put("testmode",1);
        params.put("numres",16);
        params.put("api_key",key);
        var url = "https://saucenao.com/search.php";
        var header = new HashMap<String, String>();
        header.put("cookie", "auth=0d70d389b73c3d57b8d33f10be7815b3290b846e;user=99199;token=6413deff682c7;cf_clearance=sj0ROrgulXlHquY48J62yLtXs7K8cIdsS4rvG0aQ4p0-1679023871-0-160");
        // header.put("cookie", "_ga=GA1.1.678918584.1673528436; cf_clearance=_ykkAq_tUmnqbJc5g9fmDAJaGb53VUGXI.0QG2VHAwM-1678499664-0-160; token=63c378ba6ab35; user=69680; auth=1f1c47f43b0ae73c920f85075aa23cdbd5efee86; _ga_LK5LRE77R3=GS1.1.1673773844.5.0.1673773844.0.0.0");
        header.put("Content-Type", "application/x-www-form-urlencoded");
        header.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");

        var messagesBuilder = new MessagesBuilder();
        try {
            sessionContext.waitingForNextMessage(qqId, GroupMessageEvent.Key, time, TimeUnit.SECONDS, (e, c) -> {
                if (!(c.getAuthor().getId().equals(id) && c.getGroup().getId().equals(groupId))) {
                    return false;
                }
                Messages messages = c.getMessageContent().getMessages();
                for (Message.Element<?> message : messages) {
                    if (message instanceof Image<?> image) {
                        c.getSource().sendBlocking("触发成功");
                        params.put("url", image.getResource().getName());
                        String httpImage = OK3HttpClient.httpGet(url, params, header);
                        log.info(httpImage);
                        data data = new Gson().fromJson(httpImage, data.class);

                        messagesBuilder.at(c.getAuthor().getId());
                        try {
                            data.getResults().forEach(a -> {
                                double similarity = 0;
                                try {
                                    similarity = Double.parseDouble(a.getHeader().getSimilarity().trim());
                                } catch (NumberFormatException ec) {
                                    log.error(ec.getMessage());
                                }
                                if (similarity >= 60) {
                                    messagesBuilder.text(MessageFormat.format("\n置信度: {0}\n", similarity)).text(MessageFormat.format("标题: {0}\n", a.getData().getTitle()));
                                    messagesBuilder.text(MessageFormat.format("PID: {0}\n", a.getData().getPixivId())).text(MessageFormat.format("作者: {0}\n", a.getData().getMemberName()));
                                    messagesBuilder.text(MessageFormat.format("作者ID: {0}\n", a.getData().getMemberId()));
                                    try {
                                        messagesBuilder.image(Resource.of(new URL(a.getHeader().getThumbnail().trim())));
                                    } catch (MalformedURLException ec) {
                                        log.error(MessageFormat.format("无缩略图异常: {0}", ec.getMessage()));
                                    }
                                    if (a.getData().getExtUrls() != null) {
                                        var imageUrl = String.valueOf(a.getData().getExtUrls());
                                        messagesBuilder.text(MessageFormat.format("图片链接: {0}\n", imageUrl));
                                    }
                                }
                            });
                            miraiForwardMessageBuilder.add(event.getAuthor().getId(), event.getAuthor().getNickname(), messagesBuilder.build());
                            c.getSource().sendBlocking(miraiForwardMessageBuilder.build());
                        } catch (Exception ec) {
                            log.error(ec.getMessage());
                            c.getSource().sendBlocking(MessageFormat.format("搜索超出每日限制: 100 -> {0}", ec.getMessage()));
                        }
                        return true;
                    }
                }
                c.getSource().sendBlocking("结束会话");
                return true;
            });
        } catch (Exception e) {
            log.error("会话超时退出: \n" + e.getMessage());
            event.getSource().sendBlocking("会话超时退出~\n异常信息: " + e.getMessage());
        }
    }
}
