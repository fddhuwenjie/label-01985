package com.sgs.controller;

import com.sgs.common.Result;
import com.sgs.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.security.Principal;
import java.util.Map;

@Api(tags = "认证管理")
@RestController
@RequestMapping(value = "/api/auth", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.getUsername(), request.getPassword());
    }

    @ApiOperation("注册")
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request.getUsername(), request.getPassword(),
                request.getRealName(), request.getRoleId());
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<?> logout(Principal principal) {
        return authService.logout(principal.getName());
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
        private String realName;
        private Long roleId;
    }
}
