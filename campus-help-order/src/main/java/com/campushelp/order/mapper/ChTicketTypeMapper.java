package com.campushelp.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campushelp.order.entity.ChTicketType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ChTicketTypeMapper extends BaseMapper<ChTicketType> {

    @Update("UPDATE ch_ticket_type SET stock_sold = stock_sold + #{qty}, updated_at = NOW(3) "
            + "WHERE id = #{id} AND status = 'ON' AND stock_sold + #{qty} <= stock_total "
            + "AND sale_start_time <= NOW(3) AND sale_end_time >= NOW(3)")
    int tryReserveStock(@Param("id") Long id, @Param("qty") int qty);

    @Update("UPDATE ch_ticket_type SET stock_sold = stock_sold - #{qty}, updated_at = NOW(3) "
            + "WHERE id = #{id} AND stock_sold >= #{qty}")
    int releaseStock(@Param("id") Long id, @Param("qty") int qty);
}
