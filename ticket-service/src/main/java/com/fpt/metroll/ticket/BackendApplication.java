package com.fpt.metroll.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.ZoneOffset;
import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = {"com.fpt.metroll"})
@EnableFeignClients(basePackages = {"com.fpt.metroll"})
@EnableScheduling
public class BackendApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.of("+7")));
        SpringApplication.run(BackendApplication.class, args);
    }

}

