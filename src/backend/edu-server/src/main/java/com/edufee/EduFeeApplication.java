package com.edufee;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * EduFeeMS 教育培训机构教务收费管理系统 - 启动类
 *
 * 系统概述:
 * EduFeeMS (Education Fee Management System) 是一套面向教育培训机构的教务收费管理系统，
 * 涵盖学员管理、课程管理、班级管理、教师管理、收费管理、考勤管理和报表统计等核心业务模块。
 *
 * 技术栈: Spring Boot 2.7.x + MyBatis-Plus + MySQL + JWT + Spring Security
 * 系统架构: Maven多模块架构，按业务域拆分
 *
 * @author EduFeeMS Team
 */
@Slf4j
@SpringBootApplication
@EnableTransactionManagement
public class EduFeeApplication {

    public static void main(String[] args) {
        log.info("========================================");
        log.info("  EduFeeMS 教育培训机构教务收费管理系统");
        log.info("  Starting application...");
        log.info("========================================");

        SpringApplication.run(EduFeeApplication.class, args);

        log.info("========================================");
        log.info("  EduFeeMS 启动成功!");
        log.info("  API文档: http://localhost:8080/doc.html");
        log.info("========================================");
    }

}
