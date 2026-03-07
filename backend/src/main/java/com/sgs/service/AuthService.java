package com.sgs.service;

import com.sgs.common.BusinessException;
import com.sgs.common.Result;
import com.sgs.entity.SysUser;
import com.sgs.entity.SysUserRole;
import com.sgs.mapper.SysUserMapper;
import com.sgs.mapper.SysUserRoleMapper;
import com.sgs.security.JwtUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    public Result<Map<String, Object>> login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SysUser user = sysUserMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username));

        String token = jwtUtils.generateToken(username, user.getId());
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(user.getId());

        // 缓存到Redis
        redisTemplate.opsForValue().set("token:" + username, token, 24, TimeUnit.HOURS);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", username);
        data.put("realName", user.getRealName());
        data.put("roles", roles);
        return Result.success(data);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<?> register(String username, String password, String realName, Long roleId) {
        Long count = sysUserMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setStatus(1);
        sysUserMapper.insert(user);

        if (roleId != null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(roleId);
            sysUserRoleMapper.insert(userRole);
        }

        return Result.success("注册成功");
    }

    public Result<?> logout(String username) {
        redisTemplate.delete("token:" + username);
        return Result.success("退出成功");
    }
}
