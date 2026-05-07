package com.campushelp.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campushelp.common.security.JwtTokenProvider;
import com.campushelp.common.security.RoleEnum;
import com.campushelp.user.dto.LoginRequest;
import com.campushelp.user.dto.LoginResponse;
import com.campushelp.user.dto.RegisterRequest;
import com.campushelp.user.dto.UserProfileUpdateRequest;
import com.campushelp.user.dto.UserProfileVO;
import com.campushelp.user.entity.ChUser;
import com.campushelp.user.entity.ChRoleApplication;
import com.campushelp.user.entity.ChUserRole;
import com.campushelp.user.exception.BadRequestException;
import com.campushelp.user.exception.UserNotFoundException;
import com.campushelp.user.exception.UnauthorizedException;
import com.campushelp.user.mapper.ChRoleApplicationMapper;
import com.campushelp.user.mapper.ChUserMapper;
import com.campushelp.user.mapper.ChUserRoleMapper;
import com.campushelp.user.mapper.RoleQueryMapper;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * 注册与登录：密码仅保存 BCrypt 哈希；JWT 载荷含 userId 与角色编码列表。
 */
@Service
public class AuthService {

    private final ChUserMapper userMapper;
    private final ChUserRoleMapper userRoleMapper;
    private final ChRoleApplicationMapper roleApplicationMapper;
    private final RoleQueryMapper roleQueryMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${campus.jwt.expiration-ms:86400000}")
    private long expirationMs;

    /** 非空时注册商家必须携带相同邀请码（环境变量 CAMPUS_AUTH_MERCHANT_INVITE） */
    @Value("${campus.auth.merchant-register-invite:}")
    private String merchantRegisterInvite;

    public AuthService(ChUserMapper userMapper,
                       ChUserRoleMapper userRoleMapper,
                       ChRoleApplicationMapper roleApplicationMapper,
                       RoleQueryMapper roleQueryMapper,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleApplicationMapper = roleApplicationMapper;
        this.roleQueryMapper = roleQueryMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest req) {
        long cnt = userMapper.selectCount(new QueryWrapper<ChUser>().eq("phone", req.getPhone().trim()));
        if (cnt > 0) {
            throw new BadRequestException("该手机号已注册");
        }
        LocalDateTime now = LocalDateTime.now();
        ChUser u = new ChUser();
        u.setPhone(req.getPhone().trim());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setNickname(req.getNickname());
        u.setStatus(1);
        u.setCreatedAt(now);
        u.setUpdatedAt(now);
        userMapper.insert(u);

        RoleEnum requested = resolveRequestedRole(req.getRequestedRole());
        if (requested == RoleEnum.ADMIN) {
            throw new BadRequestException("不可自助注册管理员角色");
        }
        if (requested == RoleEnum.MERCHANT) {
            if (merchantRegisterInvite != null && !merchantRegisterInvite.isBlank()) {
                String code = req.getInviteCode() == null ? "" : req.getInviteCode().trim();
                if (!merchantRegisterInvite.equals(code)) {
                    throw new BadRequestException("商家注册邀请码无效");
                }
            }
        }
        // 默认授予 STUDENT，保证基本可登录能力；RIDER/MERCHANT 通过审核后再生效。
        bindRoleIfMissing(u.getId(), RoleEnum.STUDENT, now);
        if (requested == RoleEnum.RIDER || requested == RoleEnum.MERCHANT) {
            createRoleApplication(u.getId(), requested, req, now);
        }

        return buildTokenResponse(u.getId());
    }

    private void bindRoleIfMissing(Long userId, RoleEnum role, LocalDateTime now) {
        Long roleId = roleQueryMapper.findRoleIdByCode(role.getCode());
        if (roleId == null) {
            throw new BadRequestException("角色不存在: " + role.getCode());
        }
        long exists = userRoleMapper.selectCount(new QueryWrapper<ChUserRole>()
                .eq("user_id", userId)
                .eq("role_id", roleId));
        if (exists > 0) {
            return;
        }
        ChUserRole ur = new ChUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        ur.setCreatedAt(now);
        userRoleMapper.insert(ur);
    }

    private void createRoleApplication(Long userId, RoleEnum role, RegisterRequest req, LocalDateTime now) {
        long pending = roleApplicationMapper.selectCount(new QueryWrapper<ChRoleApplication>()
                .eq("user_id", userId)
                .eq("role_code", role.getCode())
                .eq("status", "PENDING"));
        if (pending > 0) {
            return;
        }
        if (req.getQualificationImageUrls() == null || req.getQualificationImageUrls().isEmpty()) {
            throw new BadRequestException("申请 " + role.getCode() + " 需上传资质图片");
        }
        ChRoleApplication app = new ChRoleApplication();
        app.setUserId(userId);
        app.setRoleCode(role.getCode());
        app.setDocType(req.getQualificationDocType());
        app.setDocNo(req.getQualificationDocNo());
        app.setDocImagesJson(JSONUtil.toJsonStr(req.getQualificationImageUrls()));
        app.setStatus("PENDING");
        app.setSubmitRemark(req.getQualificationRemark());
        app.setCreatedAt(now);
        app.setUpdatedAt(now);
        roleApplicationMapper.insert(app);
    }

    private static RoleEnum resolveRequestedRole(String raw) {
        if (raw == null || raw.isBlank()) {
            return RoleEnum.STUDENT;
        }
        String t = raw.trim().toUpperCase();
        for (RoleEnum r : RoleEnum.values()) {
            if (r.getCode().equals(t)) {
                return r;
            }
        }
        throw new BadRequestException("不支持的 requestedRole，可选: STUDENT, RIDER, MERCHANT");
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest req) {
        ChUser u = userMapper.selectOne(new QueryWrapper<ChUser>().eq("phone", req.getPhone().trim()));
        if (u == null || u.getStatus() == null || u.getStatus() != 1) {
            throw new UnauthorizedException("账号或密码错误");
        }
        if (u.getPasswordHash() == null || !passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new UnauthorizedException("账号或密码错误");
        }
        u.setLastLoginAt(LocalDateTime.now());
        u.setUpdatedAt(u.getLastLoginAt());
        userMapper.updateById(u);
        return buildTokenResponse(u.getId());
    }

    private LoginResponse buildTokenResponse(Long userId) {
        List<String> codes = roleQueryMapper.listRoleCodesByUserId(userId);
        String[] roles = codes == null ? new String[0] : codes.toArray(new String[0]);
        String token = jwtTokenProvider.createToken(userId, roles);
        return new LoginResponse(token, userId, expirationMs, roles);
    }

    /**
     * 当前用户资料（需已登录）。
     */
    public UserProfileVO getProfile(Long userId) {
        ChUser u = userMapper.selectById(userId);
        if (u == null) {
            throw new UserNotFoundException("用户不存在");
        }
        List<String> roles = roleQueryMapper.listRoleCodesByUserId(userId);
        return new UserProfileVO(
                u.getId(),
                u.getPhone(),
                u.getNickname(),
                roles,
                u.getAvatarUrl(),
                u.getCampusId(),
                u.getCreatedAt());
    }

    @Transactional(rollbackFor = Exception.class)
    public UserProfileVO updateProfile(Long userId, UserProfileUpdateRequest req) {
        ChUser u = userMapper.selectById(userId);
        if (u == null) {
            throw new UserNotFoundException("用户不存在");
        }
        if (req.getNickname() != null) {
            u.setNickname(req.getNickname().trim().isEmpty() ? null : req.getNickname().trim());
        }
        if (req.getAvatarUrl() != null) {
            String a = req.getAvatarUrl().trim();
            u.setAvatarUrl(a.isEmpty() ? null : a);
        }
        if (req.getCampusId() != null) {
            u.setCampusId(req.getCampusId());
        }
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
        return getProfile(userId);
    }

    public List<ChRoleApplication> listRoleApplications(String status, int limit) {
        QueryWrapper<ChRoleApplication> q = new QueryWrapper<>();
        if (status != null && !status.isBlank()) {
            q.eq("status", status.trim().toUpperCase(Locale.ROOT));
        }
        int safe = Math.min(Math.max(limit, 1), 200);
        q.orderByDesc("created_at").last("LIMIT " + safe);
        return roleApplicationMapper.selectList(q);
    }

    @Transactional(rollbackFor = Exception.class)
    public ChRoleApplication approveRoleApplication(Long appId, Long adminUserId, String auditRemark) {
        ChRoleApplication app = roleApplicationMapper.selectById(appId);
        if (app == null) {
            throw new BadRequestException("申请不存在");
        }
        if (!"PENDING".equals(app.getStatus())) {
            throw new BadRequestException("仅待审核申请可通过");
        }
        app.setStatus("APPROVED");
        app.setAuditRemark(auditRemark);
        app.setAuditedBy(adminUserId);
        app.setAuditedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());
        roleApplicationMapper.updateById(app);
        bindRoleIfMissing(app.getUserId(), RoleEnum.valueOf(app.getRoleCode()), LocalDateTime.now());
        return app;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChRoleApplication rejectRoleApplication(Long appId, Long adminUserId, String auditRemark) {
        ChRoleApplication app = roleApplicationMapper.selectById(appId);
        if (app == null) {
            throw new BadRequestException("申请不存在");
        }
        if (!"PENDING".equals(app.getStatus())) {
            throw new BadRequestException("仅待审核申请可驳回");
        }
        app.setStatus("REJECTED");
        app.setAuditRemark(auditRemark);
        app.setAuditedBy(adminUserId);
        app.setAuditedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());
        roleApplicationMapper.updateById(app);
        return app;
    }

    public List<ChUser> listUsers(Integer status, int limit) {
        QueryWrapper<ChUser> q = new QueryWrapper<>();
        if (status != null) {
            q.eq("status", status);
        }
        int safe = Math.min(Math.max(limit, 1), 200);
        q.orderByDesc("created_at").last("LIMIT " + safe);
        return userMapper.selectList(q);
    }

    @Transactional(rollbackFor = Exception.class)
    public ChUser updateUserStatus(Long userId, int status) {
        ChUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }
}
