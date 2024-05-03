
package com.mytech.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.mytech.api.config.OAuth2.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class SpringApiReactProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringApiReactProjectApplication.class, args);
	}

}
