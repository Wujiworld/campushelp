package com.campushelp.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campushelp.order.entity.ChActivity;
import com.campushelp.order.entity.ChComment;
import com.campushelp.order.entity.ChTicketType;
import com.campushelp.order.mapper.ChActivityMapper;
import com.campushelp.order.mapper.ChCommentMapper;
import com.campushelp.order.mapper.ChTicketTypeMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class InternalLifeActivityCommentController {
    private final ChActivityMapper activityMapper;
    private final ChTicketTypeMapper ticketTypeMapper;
    private final ChCommentMapper commentMapper;

    public InternalLifeActivityCommentController(ChActivityMapper activityMapper,
                                                 ChTicketTypeMapper ticketTypeMapper,
                                                 ChCommentMapper commentMapper) {
        this.activityMapper = activityMapper;
        this.ticketTypeMapper = ticketTypeMapper;
        this.commentMapper = commentMapper;
    }

    @GetMapping("/api/v3/internal/life/activities")
    public List<ChActivity> listActivities(@RequestParam(value = "campusId", required = false) Long campusId,
                                           @RequestParam(value = "status", required = false) String status,
                                           @RequestParam(value = "limit", required = false) Integer limit) {
        QueryWrapper<ChActivity> q = new QueryWrapper<>();
        if (campusId != null) q.eq("campus_id", campusId);
        if (status != null && !status.isBlank()) q.eq("status", status.trim().toUpperCase());
        q.orderByAsc("start_time");
        q.last("LIMIT " + Math.max(1, Math.min(limit == null ? 200 : limit, 500)));
        return activityMapper.selectList(q);
    }

    @GetMapping("/api/v3/internal/life/activities/{id}")
    public ChActivity getActivity(@PathVariable("id") Long id) {
        return activityMapper.selectById(id);
    }

    @GetMapping("/api/v3/internal/life/activities/batch")
    public List<ChActivity> batchActivities(@RequestParam("ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return activityMapper.selectBatchIds(ids);
    }

    @PostMapping("/api/v3/internal/life/activities")
    public ChActivity createActivity(@RequestParam("campusId") Long campusId,
                                     @RequestParam("title") String title,
                                     @RequestParam(value = "description", required = false) String description,
                                     @RequestParam(value = "place", required = false) String place,
                                     @RequestParam("startTime") String startTime,
                                     @RequestParam("endTime") String endTime,
                                     @RequestParam("status") String status,
                                     @RequestParam("createdBy") Long createdBy) {
        LocalDateTime now = LocalDateTime.now();
        ChActivity a = new ChActivity();
        a.setCampusId(campusId);
        a.setTitle(title);
        a.setDescription(description);
        a.setPlace(place);
        a.setStartTime(LocalDateTime.parse(startTime));
        a.setEndTime(LocalDateTime.parse(endTime));
        a.setStatus(status);
        a.setLikeCount(0);
        a.setCreatedBy(createdBy);
        a.setCreatedAt(now);
        a.setUpdatedAt(now);
        activityMapper.insert(a);
        return a;
    }

    @GetMapping("/api/v3/internal/life/activities/{id}/tickets")
    public List<ChTicketType> listTickets(@PathVariable("id") Long activityId,
                                          @RequestParam(value = "status", required = false) String status) {
        QueryWrapper<ChTicketType> q = new QueryWrapper<ChTicketType>().eq("activity_id", activityId).orderByAsc("id");
        if (status != null && !status.isBlank()) q.eq("status", status.trim().toUpperCase());
        return ticketTypeMapper.selectList(q);
    }

    @GetMapping("/api/v3/internal/life/tickets/{id}")
    public ChTicketType getTicket(@PathVariable("id") Long id) {
        return ticketTypeMapper.selectById(id);
    }

    @GetMapping("/api/v3/internal/life/tickets")
    public List<ChTicketType> listTicketsByStatus(@RequestParam(value = "status", required = false) String status,
                                                   @RequestParam(value = "limit", required = false) Integer limit) {
        QueryWrapper<ChTicketType> q = new QueryWrapper<>();
        if (status != null && !status.isBlank()) q.eq("status", status.trim().toUpperCase());
        q.orderByDesc("created_at").last("LIMIT " + Math.max(1, Math.min(limit == null ? 200 : limit, 1000)));
        return ticketTypeMapper.selectList(q);
    }

    @PostMapping("/api/v3/internal/life/tickets/{id}/release")
    public Boolean releaseTicketStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity) {
        return ticketTypeMapper.releaseStock(id, Math.max(1, quantity == null ? 1 : quantity)) > 0;
    }

    @PostMapping("/api/v3/internal/life/activities/{id}/tickets")
    public ChTicketType createTicketType(@PathVariable("id") Long activityId,
                                         @RequestParam("name") String name,
                                         @RequestParam("priceCent") Integer priceCent,
                                         @RequestParam("stockTotal") Integer stockTotal,
                                         @RequestParam("perUserLimit") Integer perUserLimit,
                                         @RequestParam("saleStartTime") String saleStartTime,
                                         @RequestParam("saleEndTime") String saleEndTime,
                                         @RequestParam("status") String status) {
        LocalDateTime now = LocalDateTime.now();
        ChTicketType t = new ChTicketType();
        t.setActivityId(activityId);
        t.setName(name);
        t.setPriceCent(priceCent);
        t.setStockTotal(stockTotal);
        t.setStockSold(0);
        t.setPerUserLimit(perUserLimit);
        t.setSaleStartTime(LocalDateTime.parse(saleStartTime));
        t.setSaleEndTime(LocalDateTime.parse(saleEndTime));
        t.setStatus(status);
        t.setCreatedAt(now);
        t.setUpdatedAt(now);
        ticketTypeMapper.insert(t);
        return t;
    }

    @GetMapping("/api/v3/internal/life/comments")
    public List<ChComment> listComments(@RequestParam("targetType") String targetType,
                                        @RequestParam("targetId") Long targetId,
                                        @RequestParam(value = "status", required = false) String status,
                                        @RequestParam("page") int page,
                                        @RequestParam("size") int size) {
        if (targetId != null && targetId <= 0) {
            QueryWrapper<ChComment> q = new QueryWrapper<ChComment>()
                    .orderByDesc("created_at")
                    .last("LIMIT " + page + "," + size);
            if (status != null && !status.isBlank()) q.eq("status", status.trim().toUpperCase());
            return commentMapper.selectList(q);
        }
        QueryWrapper<ChComment> q = new QueryWrapper<ChComment>()
                .eq("target_type", targetType)
                .eq("target_id", targetId)
                .orderByDesc("created_at")
                .last("LIMIT " + page + "," + size);
        if (status != null && !status.isBlank()) q.eq("status", status.trim().toUpperCase());
        return commentMapper.selectList(q);
    }

    @GetMapping("/api/v3/internal/life/comments/mine")
    public List<ChComment> listMyComments(@RequestParam("userId") Long userId,
                                          @RequestParam("page") int page,
                                          @RequestParam("size") int size) {
        return commentMapper.selectList(new QueryWrapper<ChComment>()
                .eq("user_id", userId)
                .orderByDesc("created_at")
                .last("LIMIT " + page + "," + size));
    }

    @GetMapping("/api/v3/internal/life/comments/{id}")
    public ChComment getComment(@PathVariable("id") Long id) {
        return commentMapper.selectById(id);
    }

    @PostMapping("/api/v3/internal/life/comments")
    public ChComment createComment(@RequestParam("userId") Long userId,
                                   @RequestParam("targetType") String targetType,
                                   @RequestParam("targetId") Long targetId,
                                   @RequestParam("content") String content,
                                   @RequestParam("status") String status) {
        LocalDateTime now = LocalDateTime.now();
        ChComment c = new ChComment();
        c.setUserId(userId);
        c.setTargetType(targetType);
        c.setTargetId(targetId);
        c.setContent(content);
        c.setStatus(status);
        c.setCreatedAt(now);
        c.setUpdatedAt(now);
        commentMapper.insert(c);
        return c;
    }

    @PostMapping("/api/v3/internal/life/comments/{id}/status")
    public Boolean updateCommentStatus(@PathVariable("id") Long id,
                                       @RequestParam("status") String status) {
        return commentMapper.update(null, new UpdateWrapper<ChComment>()
                .eq("id", id)
                .set("status", status)
                .set("updated_at", LocalDateTime.now())) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/v3/internal/life/comments/{id}/delete")
    public Boolean deleteComment(@PathVariable("id") Long id) {
        return commentMapper.deleteById(id) > 0;
    }
}
