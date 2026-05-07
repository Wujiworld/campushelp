# 异常处理统一规范

## 📋 背景

之前各模块的异常处理存在以下问题：
1. **相同异常类名，不同HTTP状态码**（如 `BadRequestException` 在 order 模块返回 422，在 user 模块返回 409）
2. **错误消息格式不一致**（有的包含字段名，有的不包含）
3. **重复定义异常类**（每个模块都定义自己的 `BadRequestException`）
4. **缺少统一的异常体系**

## ✅ 解决方案

在 `campus-help-common` 模块中建立了统一的异常体系：

### 1. 标准异常类（位于 `com.campushelp.common.exception`）

| 异常类 | HTTP状态码 | ResultCode | 使用场景 |
|--------|-----------|------------|---------|
| `NotFoundException` | 404 | NOT_FOUND | 资源不存在（订单、用户、商品等） |
| `UnauthorizedException` | 401 | UNAUTHORIZED | 未登录、无权操作 |
| `ValidationException` | 422 | BIZ_RULE | 业务规则校验失败（库存不足、状态不允许等） |
| `ConflictException` | 409 | CONFLICT | 业务冲突（手机号已注册、重复提交等） |
| `BusinessException` | 动态 | 自定义 | 通用业务异常基类 |

### 2. 统一异常处理器

- **`UnifiedExceptionHandler`**: 处理 common.exception 下的所有标准异常
- **`GlobalRestExceptionHandler`**: 兜底处理所有未捕获的异常（返回 500）
- **`AccessDeniedRestAdvice`**: 处理 Spring Security 的权限异常

## 🚀 迁移指南

### Step 1: 停止使用模块内的旧异常类

❌ **不再使用**:
```java
// order 模块
throw new com.campushelp.order.exception.BadRequestException("库存不足");
throw new com.campushelp.order.exception.OrderNotFoundException("订单不存在");

// user 模块
throw new com.campushelp.user.exception.BadRequestException("手机号已注册");
throw new com.campushelp.user.exception.UserNotFoundException("用户不存在");
```

✅ **改为使用**:
```java
import com.campushelp.common.exception.ValidationException;
import com.campushelp.common.exception.NotFoundException;

throw new ValidationException("库存不足");
throw new NotFoundException("订单", orderId);  // 自动格式化为 "订单 不存在: 123"
throw new ConflictException("手机号已注册");
```

### Step 2: 删除或废弃旧的异常类

可以保留旧异常类以兼容现有代码，但标记为 `@Deprecated`：

```java
@Deprecated
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
```

### Step 3: 简化或删除模块级的 ExceptionHandler

如果模块的 ExceptionHandler 只是处理自己的异常类，可以删除它，因为 `UnifiedExceptionHandler` 已经统一处理。

**可以删除的情况**:
- 只处理模块自定义异常
- 没有特殊的业务逻辑

**需要保留的情况**:
- 有特殊的异常处理逻辑
- 需要记录额外的日志
- 需要触发特定的副作用

## 📝 使用示例

### 示例 1: 订单服务

```java
@Service
public class OrderService {
    
    public ChOrder getByIdOrThrow(Long id) {
        ChOrder o = orderMapper.selectById(id);
        if (o == null) {
            // ❌ 旧写法
            // throw new OrderNotFoundException("订单不存在: " + id);
            
            // ✅ 新写法
            throw new NotFoundException("订单", id);
        }
        return o;
    }
    
    public void deductStock(Long skuId, int quantity) {
        // ❌ 旧写法
        // throw new BadRequestException("库存不足");
        
        // ✅ 新写法
        throw new ValidationException("库存不足");
    }
}
```

### 示例 2: 用户服务

```java
@Service
public class AuthService {
    
    public void register(String phone) {
        if (userMapper.existsByPhone(phone)) {
            // ❌ 旧写法
            // throw new BadRequestException("手机号已注册");
            
            // ✅ 新写法
            throw new ConflictException("手机号已注册");
        }
    }
    
    public UserProfile getProfile(Long userId) {
        ChUser user = userMapper.selectById(userId);
        if (user == null) {
            // ❌ 旧写法
            // throw new UserNotFoundException("用户不存在");
            
            // ✅ 新写法
            throw new NotFoundException("用户", userId);
        }
        return convert(user);
    }
}
```

### 示例 3: 自定义业务异常（可选）

如果某个模块有特殊的业务异常需求，可以继承 `BusinessException`：

```java
package com.campushelp.order.exception;

import com.campushelp.common.exception.BusinessException;
import com.campushelp.common.api.ResultCode;

/**
 * 订单支付超时异常
 */
public class OrderTimeoutException extends BusinessException {
    
    public OrderTimeoutException(Long orderId) {
        super(ResultCode.BIZ_RULE, "订单 " + orderId + " 已超时，请重新下单");
    }
}
```

这样仍然会被 `UnifiedExceptionHandler` 统一处理。

## 🎯 统一后的优势

1. **一致性**: 所有模块使用相同的异常类和HTTP状态码映射
2. **可维护性**: 修改异常处理逻辑只需改一处（`UnifiedExceptionHandler`）
3. **可扩展性**: 新增异常类型只需在 common 中定义
4. **清晰语义**: 异常类名直接表达业务含义
5. **减少重复**: 不需要在每个模块重复定义相同的异常类

## ⚠️ 注意事项

1. **参数校验异常** (`MethodArgumentNotValidException`) 仍由 `UnifiedExceptionHandler` 统一处理，格式为 `"{field}: {message}"`
2. **Spring Security 异常** (`AccessDeniedException`) 仍由 `AccessDeniedRestAdvice` 处理
3. **未捕获的异常** 会由 `GlobalRestExceptionHandler` 兜底，返回 500 并记录日志
4. **模块特异性** 可以通过继承 `BusinessException` 实现，同时享受统一处理的好处

## 🔄 迁移优先级

1. **高优先级**: 新建代码直接使用新异常类
2. **中优先级**: 重构核心业务逻辑的异常抛出
3. **低优先级**: 逐步替换边缘业务的异常

不需要一次性全部迁移，可以渐进式进行。
