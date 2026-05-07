package com.campushelp.life.notify.controller;

import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.life.notify.dto.MessageView;
import com.campushelp.life.notify.service.MessageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/api/v3/messages")
    public List<MessageView> inbox(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        long uid = SecurityContextUtils.requireUserId();
        return messageService.listInbox(uid, page, size);
    }

    @PostMapping("/api/v3/messages/{id}/read")
    public void read(@PathVariable Long id) {
        long uid = SecurityContextUtils.requireUserId();
        messageService.markRead(uid, id);
    }
}

