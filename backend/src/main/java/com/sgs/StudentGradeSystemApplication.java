package com.sgs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sgs.mapper")
public class StudentGradeSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudentGradeSystemApplication.class, args);
    }
}
