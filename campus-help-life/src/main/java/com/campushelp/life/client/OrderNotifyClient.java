package com.campushelp.life.client;

import com.campushelp.life.notify.dto.MessageView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "campus-help-order")
public interface OrderNotifyClient {

    @GetMapping("/api/v3/internal/life/messages/inbox")
    List<MessageView> listInbox(@RequestParam("userId") long userId,
                                @RequestParam("page") int page,
                                @RequestParam("size") int size);

    @PostMapping("/api/v3/internal/life/messages/read")
    void markRead(@RequestParam("userId") long userId, @RequestParam("messageId") long messageId);

    @PostMapping("/api/v3/internal/life/messages/upsert")
    void upsertEventMessage(@RequestParam("eventId") String eventId,
                            @RequestParam("type") String type,
                            @RequestParam(value = "bizId", required = false) String bizId,
                            @RequestParam("title") String title,
                            @RequestParam("content") String content,
                            @RequestParam(value = "payloadJson", required = false) String payloadJson,
                            @RequestParam(value = "recipients", required = false) List<Long> recipients);
}
