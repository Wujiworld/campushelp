package com.campushelp.life.client;

import com.campushelp.life.client.dto.AgentItemDto;
import com.campushelp.life.client.dto.ActivityDto;
import com.campushelp.life.client.dto.CommentDto;
import com.campushelp.life.client.dto.OrderItemDto;
import com.campushelp.life.client.dto.SecondhandImageDto;
import com.campushelp.life.client.dto.SecondhandItemDto;
import com.campushelp.life.client.dto.TicketTypeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "campus-help-order")
public interface LifeCatalogClient {
    @GetMapping("/api/v3/internal/life/activities")
    List<ActivityDto> listActivities(@RequestParam(value = "campusId", required = false) Long campusId,
                                     @RequestParam(value = "status", required = false) String status,
                                     @RequestParam(value = "limit", required = false) Integer limit);

    @GetMapping("/api/v3/internal/life/activities/{id}")
    ActivityDto getActivity(@PathVariable("id") Long id);

    @GetMapping("/api/v3/internal/life/activities/batch")
    List<ActivityDto> batchActivities(@RequestParam("ids") List<Long> ids);

    @PostMapping("/api/v3/internal/life/activities")
    ActivityDto createActivity(@RequestParam("campusId") Long campusId,
                               @RequestParam("title") String title,
                               @RequestParam(value = "description", required = false) String description,
                               @RequestParam(value = "place", required = false) String place,
                               @RequestParam("startTime") String startTime,
                               @RequestParam("endTime") String endTime,
                               @RequestParam("status") String status,
                               @RequestParam("createdBy") Long createdBy);

    @GetMapping("/api/v3/internal/life/activities/{id}/tickets")
    List<TicketTypeDto> listTickets(@PathVariable("id") Long activityId,
                                    @RequestParam(value = "status", required = false) String status);

    @GetMapping("/api/v3/internal/life/tickets/{id}")
    TicketTypeDto getTicket(@PathVariable("id") Long ticketTypeId);

    @GetMapping("/api/v3/internal/life/tickets")
    List<TicketTypeDto> listTicketsByStatus(@RequestParam(value = "status", required = false) String status,
                                            @RequestParam(value = "limit", required = false) Integer limit);

    @PostMapping("/api/v3/internal/life/tickets/{id}/release")
    Boolean releaseTicketStock(@PathVariable("id") Long ticketTypeId,
                               @RequestParam("quantity") Integer quantity);

    @PostMapping("/api/v3/internal/life/activities/{id}/tickets")
    TicketTypeDto createTicketType(@PathVariable("id") Long activityId,
                                   @RequestParam("name") String name,
                                   @RequestParam("priceCent") Integer priceCent,
                                   @RequestParam("stockTotal") Integer stockTotal,
                                   @RequestParam("perUserLimit") Integer perUserLimit,
                                   @RequestParam("saleStartTime") String saleStartTime,
                                   @RequestParam("saleEndTime") String saleEndTime,
                                   @RequestParam("status") String status);

    @GetMapping("/api/v3/internal/life/comments")
    List<CommentDto> listComments(@RequestParam("targetType") String targetType,
                                  @RequestParam("targetId") Long targetId,
                                  @RequestParam(value = "status", required = false) String status,
                                  @RequestParam("page") int page,
                                  @RequestParam("size") int size);

    @GetMapping("/api/v3/internal/life/comments/mine")
    List<CommentDto> listMyComments(@RequestParam("userId") Long userId,
                                    @RequestParam("page") int page,
                                    @RequestParam("size") int size);

    @GetMapping("/api/v3/internal/life/comments/{id}")
    CommentDto getComment(@PathVariable("id") Long id);

    @PostMapping("/api/v3/internal/life/comments")
    CommentDto createComment(@RequestParam("userId") Long userId,
                             @RequestParam("targetType") String targetType,
                             @RequestParam("targetId") Long targetId,
                             @RequestParam("content") String content,
                             @RequestParam("status") String status);

    @PostMapping("/api/v3/internal/life/comments/{id}/status")
    Boolean updateCommentStatus(@PathVariable("id") Long id,
                                @RequestParam("status") String status);

    @PostMapping("/api/v3/internal/life/comments/{id}/delete")
    Boolean deleteComment(@PathVariable("id") Long id);


    @GetMapping("/api/v3/internal/life/agent/items")
    List<AgentItemDto> listAgentItems(@RequestParam(value = "campusId", required = false) Long campusId,
                                      @RequestParam(value = "status", required = false) String status);

    @GetMapping("/api/v3/internal/life/agent/items/{id}")
    AgentItemDto getAgentItem(@PathVariable("id") Long id);

    @PostMapping("/api/v3/internal/life/agent/items")
    AgentItemDto createAgentItem(@RequestParam("sellerUserId") Long sellerUserId,
                                 @RequestParam("campusId") Long campusId,
                                 @RequestParam("title") String title,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam("priceCent") Integer priceCent);

    @PostMapping("/api/v3/internal/life/agent/items/{id}/offline")
    Boolean offlineAgentItem(@PathVariable("id") Long id);

    @PostMapping("/api/v3/internal/life/agent/items/{id}/restore")
    Boolean restoreAgentItem(@PathVariable("id") Long id);

    @GetMapping("/api/v3/internal/life/orders/{orderId}/agent-line")
    OrderItemDto getAgentOrderLine(@PathVariable("orderId") Long orderId);

    @GetMapping("/api/v3/internal/life/secondhand/items")
    List<SecondhandItemDto> listSecondhandItems(@RequestParam(value = "campusId", required = false) Long campusId,
                                                @RequestParam(value = "status", required = false) String status,
                                                @RequestParam(value = "keyword", required = false) String keyword,
                                                @RequestParam(value = "minPriceCent", required = false) Integer minPriceCent,
                                                @RequestParam(value = "maxPriceCent", required = false) Integer maxPriceCent,
                                                @RequestParam(value = "sort", required = false) String sort);

    @GetMapping("/api/v3/internal/life/secondhand/items/{id}")
    SecondhandItemDto getSecondhandItem(@PathVariable("id") Long id);

    @GetMapping("/api/v3/internal/life/secondhand/items/{id}/images")
    List<SecondhandImageDto> listSecondhandImages(@PathVariable("id") Long itemId);

    @GetMapping("/api/v3/internal/life/secondhand/seller/{sellerUserId}/items")
    List<SecondhandItemDto> listSecondhandBySeller(@PathVariable("sellerUserId") Long sellerUserId);

    @PostMapping("/api/v3/internal/life/secondhand/items")
    SecondhandItemDto createSecondhandItem(@RequestParam("sellerUserId") Long sellerUserId,
                                           @RequestParam("campusId") Long campusId,
                                           @RequestParam("title") String title,
                                           @RequestParam(value = "description", required = false) String description,
                                           @RequestParam("priceCent") Integer priceCent,
                                           @RequestParam("negotiable") Integer negotiable);

    @PostMapping("/api/v3/internal/life/secondhand/items/{id}/images")
    SecondhandImageDto addSecondhandImage(@PathVariable("id") Long itemId,
                                          @RequestParam("url") String url,
                                          @RequestParam("sortNo") Integer sortNo);

    @PostMapping("/api/v3/internal/life/secondhand/items/{id}/offline")
    Boolean offlineSecondhand(@PathVariable("id") Long itemId);

    @PostMapping("/api/v3/internal/life/secondhand/items/{id}/view")
    Boolean incSecondhandView(@PathVariable("id") Long itemId);

    @PostMapping("/api/v3/internal/life/secondhand/items/{id}/status")
    Boolean updateSecondhandStatus(@PathVariable("id") Long itemId,
                                   @RequestParam("fromStatus") String fromStatus,
                                   @RequestParam("toStatus") String toStatus);
}
