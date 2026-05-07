package com.campushelp.life.comment.controller;

import com.campushelp.common.security.RequireRole;
import com.campushelp.common.security.RoleEnum;
import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.life.comment.dto.CommentCreateRequest;
import com.campushelp.life.comment.dto.CommentView;
import com.campushelp.life.comment.dto.MyCommentView;
import com.campushelp.life.client.dto.CommentDto;
import com.campushelp.life.comment.service.CommentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/api/v3/comments")
    public List<CommentView> list(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return commentService.listVisible(targetType, targetId, page, size);
    }

    @PostMapping("/api/v3/comments")
    public CommentView create(@Valid @RequestBody CommentCreateRequest req) {
        return commentService.create(SecurityContextUtils.requireUserId(), req);
    }

    @GetMapping("/api/v3/comments/mine")
    public List<MyCommentView> mine(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        long uid = SecurityContextUtils.requireUserId();
        List<CommentDto> list = commentService.listMineRaw(uid, page, size);
        return list.stream()
                .map(c -> new MyCommentView(c.getId(), c.getUserId(), c.getTargetType(), c.getTargetId(), c.getContent(), c.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @DeleteMapping("/api/v3/comments/{id}")
    public void delete(@PathVariable Long id) {
        long uid = SecurityContextUtils.requireUserId();
        boolean admin = SecurityContextUtils.hasRole("ADMIN");
        commentService.deleteByAuthorOrAdmin(uid, id, admin);
    }

    @RequireRole(RoleEnum.ADMIN)
    @PostMapping("/api/v3/admin/comments/{id}/hide")
    public void hide(@PathVariable Long id) {
        commentService.hideByAdmin(id);
    }
}
