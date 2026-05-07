package com.campushelp.life.social.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeStateResponse {
    private boolean liked;
    private int likeCount;
}
