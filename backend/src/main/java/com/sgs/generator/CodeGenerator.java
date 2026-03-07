package com.sgs.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Collections;

/**
 * MyBatis Plus 代码生成器
 * 运行此类可自动生成 Entity、Mapper、Service、Controller
 */
public class CodeGenerator {

    public static void main(String[] args) {
        FastAutoGenerator.create(
                "jdbc:mysql://localhost:3306/student_grade_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai",
                "root", "root")
            .globalConfig(builder -> builder
                .author("SGS")
                .outputDir(System.getProperty("user.dir") + "/src/main/java")
                .enableSwagger()
                .disableOpenDir())
            .packageConfig(builder -> builder
                .parent("com.sgs")
                .entity("entity")
                .mapper("mapper")
                .service("service")
                .controller("controller")
                .pathInfo(Collections.singletonMap(OutputFile.xml,
                    System.getProperty("user.dir") + "/src/main/resources/mapper")))
            .strategyConfig(builder -> builder
                .addInclude("sys_user", "sys_role", "sys_permission", "sys_user_role",
                    "sys_role_permission", "student", "teacher", "course", "score", "class_info")
                .entityBuilder()
                    .enableLombok()
                    .enableTableFieldAnnotation()
                    .logicDeleteColumnName("deleted")
                .mapperBuilder()
                    .enableBaseResultMap()
                    .enableBaseColumnList()
                .controllerBuilder()
                    .enableRestStyle())
            .templateEngine(new VelocityTemplateEngine())
            .execute();
    }
}
