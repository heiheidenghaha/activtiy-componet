/**
 * 
 */
package com.tydic.activiti_component.configuration;



import javax.annotation.PostConstruct;


import org.activiti.engine.ProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * @author shujingling
 * @date 2019年3月18日
 * @version v1.0
 * @package com.tydic.activiti_component.configuration
 * @description
 */
@Configuration
public class ActivitiConfiguration  {
	
	@Autowired
	private ProcessEngineConfiguration processEngineConfiguration;

	// 解决中文乱码
	@PostConstruct
	public void initProcessEngineConfiguration() {
		processEngineConfiguration.setActivityFontName("宋体");
		processEngineConfiguration.setAnnotationFontName("宋体");
		processEngineConfiguration.setLabelFontName("宋体");
	}

}
