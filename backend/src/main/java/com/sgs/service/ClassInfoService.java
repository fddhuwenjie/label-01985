package com.sgs.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgs.entity.ClassInfo;
import com.sgs.mapper.ClassInfoMapper;
import org.springframework.stereotype.Service;

@Service
public class ClassInfoService extends ServiceImpl<ClassInfoMapper, ClassInfo> {
}
