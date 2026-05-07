package com.campushelp.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campushelp.order.entity.ChSettlementLedger;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChSettlementLedgerMapper extends BaseMapper<ChSettlementLedger> {

    @Select("""
            SELECT COALESCE(SUM(amount_cent), 0)
            FROM ch_settlement_ledger
            WHERE user_id = #{userId}
              AND user_role = #{role}
              AND status = 'AVAILABLE'
            """)
    Integer sumAvailable(@Param("userId") long userId, @Param("role") String role);
}
