package cn;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import java.lang.annotation.*;

@SpringBootApplication
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@MapperScan("cn.**.mapper")
@EnableAsync
@EnableWebSocket
public @interface MyAppConfig {
}
