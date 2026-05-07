package com.campushelp.life.social.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campushelp.life.social.entity.ChUserFollow;
import com.campushelp.life.social.mapper.ChUserFollowMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FollowServiceTest {

    @Test
    void toggle_shouldCreateFollow_whenNotExists() {
        ChUserFollowMapper mapper = mock(ChUserFollowMapper.class);
        when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        FollowService service = new FollowService(mapper, null);
        boolean following = service.toggle(1001L, 2002L);

        Assertions.assertTrue(following);
        ArgumentCaptor<ChUserFollow> captor = ArgumentCaptor.forClass(ChUserFollow.class);
        verify(mapper).insert(captor.capture());
        Assertions.assertEquals(1001L, captor.getValue().getFollowerUserId());
        Assertions.assertEquals(2002L, captor.getValue().getFolloweeUserId());
    }

    @Test
    void toggle_shouldUnfollow_whenExists() {
        ChUserFollowMapper mapper = mock(ChUserFollowMapper.class);
        ChUserFollow old = new ChUserFollow();
        old.setId(999L);
        old.setFollowerUserId(1001L);
        old.setFolloweeUserId(2002L);
        when(mapper.selectOne(any(QueryWrapper.class))).thenReturn(old);

        FollowService service = new FollowService(mapper, null);
        boolean following = service.toggle(1001L, 2002L);

        Assertions.assertFalse(following);
        verify(mapper).deleteById(999L);
        verify(mapper, never()).insert(any(ChUserFollow.class));
    }
}
