package co.kr.leddata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 스케줄러 활성화
public class LeddataApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeddataApplication.class, args);
    }

}