package com.campushelp.common.event;

/**
 * 发布业务域事件。实现建议保证：在事务内调用时，事件在提交后再真正发送。
 */
public interface DomainEventPublisher {
    void publishAfterCommit(DomainEvent event);
}

