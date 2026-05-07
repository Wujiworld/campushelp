package com.campushelp.life.notify.service;

import com.campushelp.life.client.OrderNotifyClient;
import com.campushelp.life.notify.dto.MessageView;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final OrderNotifyClient orderNotifyClient;

    public MessageService(OrderNotifyClient orderNotifyClient) {
        this.orderNotifyClient = orderNotifyClient;
    }

    public List<MessageView> listInbox(long userId, int page, int size) {
        return orderNotifyClient.listInbox(userId, page, size);
    }

    public void markRead(long userId, long messageId) {
        orderNotifyClient.markRead(userId, messageId);
    }
}

