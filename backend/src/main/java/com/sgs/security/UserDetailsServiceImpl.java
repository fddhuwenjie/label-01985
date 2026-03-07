package com.sgs.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sgs.entity.SysUser;
import com.sgs.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (sysUser.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        List<String> roles = sysUserMapper.selectRoleCodesByUserId(sysUser.getId());
        List<String> permissions = sysUserMapper.selectPermissionCodesByUserId(sysUser.getId());

        List<SimpleGrantedAuthority> authorities = Stream.concat(
                roles.stream().map(SimpleGrantedAuthority::new),
                permissions.stream().map(SimpleGrantedAuthority::new)
        ).collect(Collectors.toList());

        return new User(sysUser.getUsername(), sysUser.getPassword(), authorities);
    }
}
