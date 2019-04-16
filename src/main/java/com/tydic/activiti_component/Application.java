/**
 * 
 */
package com.tydic.activiti_component;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;



/**
 * @author shujingling
 * @date 2019年3月6日
 * @version v1.0
 * @package com.tydic.activiti_component
 * @description
 */
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@MapperScan("com.tydic.activiti_component.dao")
public class Application  extends SpringBootServletInitializer{
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		// 注意这里要指向原先用main方法执行的Application启动类
		return builder.sources(Application.class);
	}

}
