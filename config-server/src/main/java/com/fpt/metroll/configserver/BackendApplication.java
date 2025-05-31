package com.fpt.metroll.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

import java.time.ZoneOffset;
import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = {"com.fpt.metroll"})
@EnableConfigServer
public class BackendApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.of("+7")));
        SpringApplication.run(BackendApplication.class, args);
    }
}

