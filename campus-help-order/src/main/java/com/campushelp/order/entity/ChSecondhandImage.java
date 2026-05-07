package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_secondhand_image")
public class ChSecondhandImage {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long itemId;
    private String url;
    private Integer sortNo;
    private LocalDateTime createdAt;
}
