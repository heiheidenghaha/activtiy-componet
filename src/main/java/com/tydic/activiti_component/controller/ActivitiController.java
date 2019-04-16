/**
 * 
 */
package com.tydic.activiti_component.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;


import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.CommentEntityImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.tydic.activiti_component.entity.CommentVo;
import com.tydic.activiti_component.entity.ProcessDefinitionVo;
import com.tydic.activiti_component.entity.ResponseVO;
import com.tydic.activiti_component.entity.TUser;
import com.tydic.activiti_component.entity.TaskVo;



/**
 * @author shujingling
 * @date 2019年3月18日
 * @version v1.0
 * @package com.tydic.activiti_component.controller
 * @description
 */
@RestController
@RequestMapping("activiti")
public class ActivitiController {
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
    private ProcessEngineConfiguration processEngineConfiguration;
	@Autowired
	private IdentityService identityService;
	@Autowired
	private HistoryService historyService;

	

	
	/**
	 * 查看流程图
	 * @param response
	 * @param deploymentId
	 * @param resourceName	
	 * @author shujingling
	 * @date 2019年3月19日
	 */
	@GetMapping("imageView")
	public void imageView(HttpServletResponse response,String deploymentId){
		List<String> names = repositoryService.getDeploymentResourceNames(deploymentId);
		String imageName = null;
		for (String name : names) {
			if (name.indexOf(".png") >= 0) {
				imageName = name;
			}
		}
		if (imageName != null) {
			InputStream inputStream = repositoryService.getResourceAsStream(deploymentId, imageName);
			try {
				OutputStream outputStream = response.getOutputStream();
				byte[] bs = new byte[1024];
				int len = -1;
				while ((len = inputStream.read(bs)) != -1) {
					outputStream.write(bs, 0, len);
				}
				outputStream.close();
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * 查询最新版本流程定义
	 * @return	List<ProcessDefinition>
	 * @author shujingling
	 * @date 2019年3月26日
	 */
	@GetMapping("latestProcessDefinition")
	public ResponseVO<?> queryLatestProcessDefinition(){
		Map<String, ProcessDefinition> map = new HashMap<String, ProcessDefinition>();
		List<ProcessDefinition> processDefinitions =repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().asc().list();
		for (ProcessDefinition processDefinition : processDefinitions) {
			map.put(processDefinition.getKey(), processDefinition);
		}
		
		Set<String> set = map.keySet();
		List<ProcessDefinitionVo> list = new ArrayList<ProcessDefinitionVo>();
		for (String key : set) {
			ProcessDefinition pDefinition = map.get(key);
			ProcessDefinitionVo processDefinitionEntity = new ProcessDefinitionVo();
			processDefinitionEntity.setDeploymentId(pDefinition.getDeploymentId());
			processDefinitionEntity.setDiagramResourceName(pDefinition.getDiagramResourceName());
			processDefinitionEntity.setId(pDefinition.getId());
			processDefinitionEntity.setKey(pDefinition.getKey());
			processDefinitionEntity.setName(pDefinition.getResourceName());
			processDefinitionEntity.setResourceName(pDefinition.getResourceName());
			list.add(processDefinitionEntity);
		}
		return ResponseVO.success(list);
	}
	
	
	/**
	 * 
	 * 	高亮显示活动节点
	 * @author shujingling
	 * @date 2019年3月19日
	 */
	@GetMapping("activImageView")
	public void activImageView(HttpServletResponse response,String taskId){
		
		//获取任务
		Task task =taskService.createTaskQuery().taskId(taskId).singleResult();
		//根据任务获取流程实例id
		String processInstanceId = task.getProcessInstanceId();
		//获取流程实例
		ProcessInstance  processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		

        //得到流程执行对象
        List<Execution> executions = runtimeService.createExecutionQuery()
                .processInstanceId(processInstance.getId()).list();
        //得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<String>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }
        //生成高亮图片
       BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
       ProcessDiagramGenerator processDiagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
       InputStream in = processDiagramGenerator.generateDiagram(bpmnModel, "png", activityIds,Collections.<String>emptyList(), "宋体", "宋体","宋体", null,1.0);
        try {
			OutputStream outputStream = response.getOutputStream();
			byte[] bs = new byte[1024];
			int len = -1;
			while ((len=in.read(bs))!=-1) {
				outputStream.write(bs,0,len);
			}
			outputStream.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}       
		
	}
	
	/**
	 * 根据用户名查询代办任务
	 * @param userId
	 * @return	
	 * @author shujingling
	 * @date 2019年3月25日
	 */
	@PostMapping("queryTasksByUserId")
	public ResponseVO<?> queryTasksByUserId(String userId){
		//查询用户所属组
        GroupQuery qroupQuery = identityService.createGroupQuery();
        List<Group> groups = qroupQuery.groupMember(userId).list();
        List<String> candidateGroups = new ArrayList<String>();
        for (Group group : groups) {
		  candidateGroups.add(group.getId());
        }
        List<TaskVo> userTasks = new ArrayList<TaskVo>();
        if(candidateGroups.size() == 0){
        	return ResponseVO.success(userTasks);
        }
        //创建一个任务查询对象
        TaskQuery taskQuery = taskService.createTaskQuery();
        
        //办理人的任务列表  
        List<Task> list = taskQuery.taskCandidateGroupIn(candidateGroups).list();
       
        for (Task task : list) {
        	ProcessInstance processInstance  = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
			String processName = repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult().getName();
        	TaskVo userTask = new TaskVo();
			userTask.setProcessName(processName);
			userTask.setTaskId(task.getId());
			userTask.setTaskName(task.getName());
			userTask.setProcessInstanceId(task.getProcessInstanceId());
			userTasks.add(userTask);
		}		
        return ResponseVO.success(userTasks);      
	}
	
	/**
	 * 查询任务可审批用户
	 * @return	List<User>
	 * @author shujingling
	 * @date 2019年3月25日
	 */
	@PostMapping("queryTaskUser")
	public ResponseVO<?> queryTaskUser(String taskId){
		Set<User> users = new HashSet<User>();
		//查询参与候选人
		List<IdentityLink> identityLinkList = taskService.getIdentityLinksForTask(taskId);
		List<TUser> tusers = new ArrayList<TUser>();
		if(CollectionUtils.isEmpty(identityLinkList)){
			ResponseVO.success(tusers);
		}
		
	    for (IdentityLink identityLink : identityLinkList) {
			if(identityLink.getType().equals("candidate")){
				List<User> userlist =identityService.createUserQuery().memberOfGroup(identityLink.getGroupId()).list();
				if(!CollectionUtils.isEmpty(identityLinkList)){
					users.addAll(userlist);
				}	
			}
			if(identityLink.getType().equals("participant")){
				User user = identityService.createUserQuery().userId(identityLink.getUserId()).singleResult();
				users.add(user);
			}
		}
	   
	    for (User user : users) {
			TUser tUser = new TUser();
			tUser.setUserId(user.getId());
			tUser.setUserName((user.getFirstName()==null? "" : user.getFirstName())+(user.getLastName()==null ? "" : user.getLastName()));
			tusers.add(tUser);
		}
	    return ResponseVO.success(tusers);
	}
	
	/**
	 * 启动流程
	 * @param processDefiKey
	 * @param businesskey
	 * @return	
	 * @author shujingling
	 * @date 2019年3月25日
	 */
	@PostMapping("startProcess")
	public ResponseVO<?> startProcess(String processDefiKey,String businesskey){
	  ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefiKey, businesskey);
	  return ResponseVO.success(processInstance.getProcessInstanceId());
	}
	
	/**
	 * 审批业务
	 * @param taskId 业务id
	 * @param comment 评论
	 * @param userId 用户id
	 * @param businessStatus 业务状态
	 * @return	
	 * @author shujingling
	 * @date 2019年3月26日
	 */
	@PostMapping("completeTask")
	public ResponseVO<?> completeTask(@RequestBody String parms){
		JSONObject jsonObject = JSONObject.parseObject(parms);
		
		Map<String, Object> variables = new HashMap<String, Object>();
		//设置业务状态
		if(!StringUtils.isEmpty(jsonObject.getString("businessStatus"))){
			variables.put("status", jsonObject.getString("businessStatus"));
		}
		//设置流程走向
		if(!StringUtils.isEmpty(jsonObject.getString("message"))){
			variables.put("message", jsonObject.getString("message"));
		}
		Task task = taskService.createTaskQuery().taskId(jsonObject.getString("taskId")).singleResult();
		if(null != jsonObject.getString("comment")){
			Authentication.setAuthenticatedUserId(jsonObject.getString("userId"));
			taskService.addComment(jsonObject.getString("taskId"), task.getProcessInstanceId(), jsonObject.getString("comment"));
		}
		taskService.claim(jsonObject.getString("taskId"), jsonObject.getString("userId"));
		if(variables.size() > 0){
			taskService.complete(jsonObject.getString("taskId"), variables);
		}else{
			taskService.complete(jsonObject.getString("taskId"));
		}
	
        return ResponseVO.success("success");
	}
	
	/**
	 * 查询历史批注
	 * @param taskId
	 * @return	
	 * @author shujingling
	 * @date 2019年3月25日
	 */
	@PostMapping("historyComments")
	public ResponseVO<?> historyComments(String taskId,String processInstanceId){
		List<Comment> comments = new ArrayList<Comment>();
		List<CommentVo> commentEntities = new ArrayList<CommentVo>();
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if(processInstanceId == null){
			processInstanceId= task.getProcessInstanceId();
		}
		
		//历史任务
		List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).list();
		if(CollectionUtils.isEmpty(historicTaskInstances)){
			return ResponseVO.success(commentEntities);
		}
		
		//分别查询备注
		for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
			String hTaskId = historicTaskInstance.getId();
			List<Comment> hComments = taskService.getTaskComments(hTaskId);
			comments.addAll(hComments);
		}
		
		//转成实体
		for (Comment comment : comments) {
			if(comment !=null){
				CommentVo commentEntity = new CommentVo();
				commentEntity.setFullMessage(((CommentEntityImpl)comment).getMessage());
				commentEntity.setId(comment.getId());
				commentEntity.setTaskId(comment.getTaskId());
				String taskName = historyService.createHistoricTaskInstanceQuery().taskId(comment.getTaskId()).singleResult().getName();
				commentEntity.setTaskName(taskName);
				commentEntity.setTime(comment.getTime());
				commentEntity.setProcessInstanceId(comment.getProcessInstanceId());
				User user = identityService.createUserQuery().userId(comment.getUserId()).singleResult();
				commentEntity.setUserName((user.getFirstName()==null? "" : user.getFirstName())+(user.getLastName()==null ? "" : user.getLastName()));
				commentEntity.setUserId(comment.getUserId());
				commentEntities.add(commentEntity);
			}
			
		}
		return ResponseVO.success(commentEntities);
	}
	
	/**
	 * 查询审批流程状态
	 * @param processInstanceId
	 * @return	
	 * @author shujingling
	 * @date 2019年3月26日
	 */
	@PostMapping("workFLowStatus")
	public ResponseVO<?> queryWorkFLowStatus(String processInstanceId){
		String status = null;
		List<HistoricVariableInstance> hInstances =historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();
		for (HistoricVariableInstance historicVariableInstance : hInstances) {
			if(historicVariableInstance.getVariableName().equals("status")){
				status = historicVariableInstance.getValue().toString();
			}
		}
		return ResponseVO.success(status);
				
	}
	
	/**
	 * 查询任务节点名称（返回渲染按钮）
	 * @param taskId
	 * @return	
	 * @author shujingling
	 * @date 2019年3月27日
	 */
	@GetMapping("taskOutFlow")
	public ResponseVO<?> queryTaskOutFlow(String taskId){
		List<String> list = new ArrayList<>();
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
		String excId = task.getExecutionId();
		ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(excId).singleResult();
		FlowElement flowElement = bpmnModel.getFlowElement(execution.getActivityId());
		if(flowElement instanceof UserTask){
			UserTask userTask=(UserTask) flowElement;
			List<SequenceFlow> sequenceFlows = userTask.getOutgoingFlows();
			if(!CollectionUtils.isEmpty(sequenceFlows)){
				for (SequenceFlow sequenceFlow : sequenceFlows) {
					if(!StringUtils.isEmpty(sequenceFlow.getName())){
						list.add(sequenceFlow.getName());
					}else{
						list.add("默认提交");
					}
					
				}
			}
			
		}
		return ResponseVO.success(list);
	}
	
	/**
	 * 删除流程定义
	 * @return	
	 * @author shujingling
	 * @date 2019年4月10日
	 */
	@DeleteMapping("deleteProcessDef")
	public ResponseVO<?> deleteProcessDef(String deploymentId) {
		//删除流程定义，但不删除正在运行的流程实例
		repositoryService.deleteDeployment(deploymentId, false);
		return ResponseVO.success("success");
	}
	
	/**
	 * 查询历史流程
	 * @param params
	 * @return	
	 * @author shujingling
	 * @date 2019年4月11日
	 */
	@PostMapping("historyProcessInstance")
	public ResponseVO<?> historyProcessInstance(@RequestBody String params){
		JSONObject jsonObject = JSONObject.parseObject(params);
		List<String> list = (List)jsonObject.getJSONArray("processInstanceIds");
		int page = jsonObject.getIntValue("page");
		int size = jsonObject.getIntValue("size");
		Set<String> processInstanceIds = new HashSet<>(list); 
		List<HistoricProcessInstance> historicProcessInstances =historyService.createHistoricProcessInstanceQuery()
				.processInstanceIds(processInstanceIds)
				.orderByProcessInstanceId()
				.desc()
				.listPage(page, size);
		return ResponseVO.success(historicProcessInstances);
	}
	
}
