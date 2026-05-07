package com.campushelp.common.api;

/**
 * 业务错误码（V3）：HTTP 状态与 body.code 配合，便于前端与监控按码分流。
 * <p>
 * 约定：0 成功；4xxxx 客户端/业务可预期错误；5xxxx 服务端异常。
 */
public enum ResultCode {

    SUCCESS("0", "成功"),

    PARAM_INVALID("40001", "参数校验失败"),
    UNAUTHORIZED("40101", "未登录或 Token 无效"),
    FORBIDDEN("40301", "无访问权限"),
    NOT_FOUND("40401", "资源不存在"),
    CONFLICT("40901", "业务状态冲突"),
    BIZ_RULE("42201", "业务规则不满足"),

    INTERNAL_ERROR("50000", "系统繁忙，请稍后重试");

    private final String code;
    private final String defaultMessage;

    ResultCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
