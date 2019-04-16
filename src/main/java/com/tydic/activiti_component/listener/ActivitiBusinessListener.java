/**
 * 
 */
package com.tydic.activiti_component.listener;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tydic.activiti_component.utils.HttpClientUtils;



/**
 * @author shujingling
 * @date 2019年3月7日
 * @version v1.0
 * @package com.tydic.activiti_component.activiti
 * @description
 */
@Service
public  class ActivitiBusinessListener implements JavaDelegate,TaskListener {

	private static final long serialVersionUID = 1L;

	/**
     * 日志记录
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ActivitiBusinessListener.class);
	
	@Autowired 
	private RuntimeService runtimeService;
	@Value("${business.businessUrl}")
	private String businessUrl;
	private static String completeEvent = "complete";
	
	/**
	 * @param execution	
	 * @author shujingling
	 * @date 2019年3月7日
	 */
	public void execute(DelegateExecution execution) {
		
	}

	/**
	 * @param delegateTask	
	 * @author shujingling
	 * @date 2019年3月7日
	 */
	public void notify(DelegateTask delegateTask) {
		System.out.println(delegateTask.getEventName());
		if(completeEvent.equals(delegateTask.getEventName())){
			String processInstanceId= delegateTask.getProcessInstanceId();
			ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
			String businesskey = processInstance.getBusinessKey();
			Map<String, Object> variables =processInstance.getProcessVariables();
			Map<String, Object> map = new HashMap<>();
			if(variables !=null && variables.containsKey("status")){
				map.put("status", variables.get("status"));
			}
			map.put("processInstanceId", processInstanceId);
			map.put("businesskey", businesskey);
			ObjectMapper mapper = new ObjectMapper();
			String reqData = "";
			try {
				reqData = mapper.writeValueAsString(map);
			} catch (JsonProcessingException e) {
				LOGGER.error("业务请求参数错误");
			}
			if(businessUrl !=null){
				HttpClientUtils.post( businessUrl, reqData);
			}
			
		}
		
		
	}
	
}
