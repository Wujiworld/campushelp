package com.campushelp.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色查询（联表）：用于登录成功后把角色编码写入 JWT。
 */
@Mapper
public interface RoleQueryMapper {

    @Select("SELECT r.code FROM ch_role r "
            + "INNER JOIN ch_user_role ur ON r.id = ur.role_id "
            + "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> listRoleCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT id FROM ch_role WHERE code = #{code} AND status = 1 LIMIT 1")
    Long findRoleIdByCode(@Param("code") String code);
}
