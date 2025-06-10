package cn;

import org.springframework.boot.SpringApplication;

@MyAppConfig
public class CallApplication {

	public static void main(String[] args) {
		SpringApplication.run(CallApplication.class, args);
		System.out.println("启动成功");
	}

}
