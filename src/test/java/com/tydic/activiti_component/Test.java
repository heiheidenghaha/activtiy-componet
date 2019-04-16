/**
 * 
 */
package com.tydic.activiti_component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tydic.activiti_component.dao.TaskAssigneeDao;
import com.tydic.activiti_component.entity.TaskAssignee;


/**
 * @author shujingling
 * @date 2019年3月6日
 * @version v1.0
 * @package com.tydic.activiti_component
 * @description
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=Application.class)
public class Test {
	
	@Autowired
	private TaskAssigneeDao taskAssigneeDao;
	 
	/**
	 * 部署
	 * 
	 * @author shujingling
	 * @date 2019年3月7日
	 */
	@org.junit.Test
	public void deploy() {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		processEngine.getRepositoryService().createDeployment().addClasspathResource("processes/MyProcessTest.bpmn")
				.addClasspathResource("processes/MyProcessTest.png").name("多用户").deploy();

	}
	
	@org.junit.Test
    public  void startProcess(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //指定执行我们刚才部署的工作流程
        String processDefiKey="process3";
        String businesskey ="123";
      
        //取运行时服务
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //取得流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processDefiKey, businesskey);//通过流程定义的key 来执行流程
        System.out.println("流程实例id:"+pi.getId());//流程实例id
        System.out.println("流程定义id:"+pi.getProcessDefinitionId());//输出流程定义的id
        
        List<Task> taskList=processEngine.getTaskService().createTaskQuery().processInstanceId(pi.getId()).list();
        for(Task task:taskList){
        	ExecutionEntity execution = (ExecutionEntity)processEngine.getRuntimeService().createExecutionQuery().executionId(task.getExecutionId()).singleResult();
        	String activitiId = execution.getActivityId();
        	List<TaskAssignee> list = taskAssigneeDao.selectBySid(activitiId);
        	for (TaskAssignee taskAssignee : list) {
        		processEngine.getTaskService().addCandidateGroup(task.getId(), taskAssignee.getRolegroup());
			}
        }
    }
	
	
	 //查询任务
	@org.junit.Test
    public void queryTask(){
        //任务的办理人
        String userId="qa1";
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //查询用户所属组
        IdentityService identityService = processEngine.getIdentityService();
        GroupQuery qroupQuery = identityService.createGroupQuery();
        List<Group> groups = qroupQuery.groupMember(userId).list();
        List<String> candidateGroups = new ArrayList<String>();
        for (Group group : groups) {
		  candidateGroups.add(group.getId());
        }
        
        //取得任务服务
        TaskService taskService = processEngine.getTaskService();
        //创建一个任务查询对象
        TaskQuery taskQuery = taskService.createTaskQuery();
        
        //办理人的任务列表
        List<Task> list = taskQuery.taskCandidateGroupIn(candidateGroups).list();
              
        
        if(null == list || list.size() ==0){
        	System.out.println("没有待办任务");
        }
        //遍历任务列表
        if(list!=null&&list.size()>0){
            for(Task task:list){
                System.out.println("任务的办理人："+task.getAssignee());
                System.out.println("任务的id："+task.getId());
                System.out.println("任务的名称："+task.getName());
            }
        }
    }
	
	@org.junit.Test
	public void compileTask() {
		String taskId = "357505";
		
		// taskId：任务id
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		Task taskI = processEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
		//processEngine.getTaskService().addComment("102503", "100001", "请假同意");
		Map<String, Object> map = new HashMap<>();
		map.put("message", "100");
		
		processEngine.getTaskService().complete(taskId, map);
		List<Task> taskList=processEngine.getTaskService().createTaskQuery().processInstanceId(taskI.getProcessInstanceId()).list();
		 for(Task task:taskList){
	        	ExecutionEntity execution = (ExecutionEntity)processEngine.getRuntimeService().createExecutionQuery().executionId(task.getExecutionId()).singleResult();
	        	String activitiId = execution.getActivityId();
	        	List<TaskAssignee> list = taskAssigneeDao.selectBySid(activitiId);
	        	for (TaskAssignee taskAssignee : list) {
	        		processEngine.getTaskService().addCandidateGroup(task.getId(), taskAssignee.getRolegroup());
				}
	        }
		
		System.out.println("当前任务执行完毕");
	}
	
	@org.junit.Test
	public void rejectTask(){
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		TaskService taskService = processEngine.getTaskService();
		
	}
	
	/**
	 * 用户组
	 * 	
	 * @author shujingling
	 * @date 2019年3月7日
	 */
	@org.junit.Test
	public void  groupTest(){
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService = processEngine.getIdentityService();
		Group group = identityService.newGroup("xxx12");
		group.setName("xxx12Name");
		group.setType("type1");
		identityService.saveGroup(group);
		
	}
	
	/**
	 * 	创建用户并所属组
	 * @author shujingling
	 * @date 2019年3月7日
	 */
	@org.junit.Test
	public void userTest(){
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService = processEngine.getIdentityService();
		User user = identityService.newUser("qa2");
		user.setFirstName("A2");
		user.setLastName("B2");
			
		identityService.saveUser(user);
		identityService.setUserInfo("qa2", "aa2", "bb2");
		identityService.createMembership("qa2", "xxx12");
		
	}
	
	/**
	 *查询组下面的用户
	 * 	
	 * @author shujingling
	 * @date 2019年3月7日
	 */
	@org.junit.Test
	public void groupUser(){
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService = processEngine.getIdentityService();
		 List<Group> group = identityService.createGroupQuery().groupId("xxx12").list();
		 for (Group group2 : group) {
			System.out.println("组名称："+group2.getName());
			System.out.println("组id："+group2.getId());
		}
	}
	
	@org.junit.Test
	public void testBpmn(){
		String taskId="305012";
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		TaskService taskService = processEngine.getTaskService();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
	    BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
		String excId = task.getExecutionId();
		ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(excId).singleResult();
		FlowElement arts = bpmnModel.getFlowElement(execution.getActivityId());
		if(arts instanceof UserTask){
			UserTask userTask=(UserTask) arts;
			List<SequenceFlow> list = userTask.getOutgoingFlows();
			for (SequenceFlow sequenceFlow : list) {
				System.out.println(sequenceFlow.getName());
			}
		}
		
	}
	
	/**
	 * 获取流程定义的id
	 * 	
	 * @author shujingling
	 * @date 2019年4月15日
	 */
	@org.junit.Test
	public void getDeploymentActivitiIdList(){
		String processDefinitionId ="process3:1:300004";
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	
		RepositoryService repositoryService = processEngine.getRepositoryService();

	    BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
	    Collection<FlowElement> flowElements =bpmnModel.getMainProcess().getFlowElements();
	  
	    for (FlowElement flowElement : flowElements) {
			if(flowElement instanceof UserTask){
				System.out.println(flowElement.getId() +" 名称"+ flowElement.getName());
			}
		}
	   
	}
	
	@org.junit.Test
	public void setGroups(){
		
		String sid = "sid-D5613A6E-B139-4836-9FEA-07CAC56670CC";
		String ActivitiName="开始";
		
		TaskAssignee taskAssignee = new TaskAssignee();
		taskAssignee.setActivitiname(ActivitiName);
		taskAssignee.setSid(sid);
		taskAssignee.setRolegroup("xxx12");
		taskAssignee.setAssigneeType("3");
		taskAssigneeDao.save(taskAssignee);
		
		sid = "sid-2650B5D6-02CC-45D8-AEFF-8D1E43E89AF9";
		ActivitiName="同意一";
		taskAssignee.setActivitiname(ActivitiName);
		taskAssignee.setSid(sid);
		taskAssigneeDao.save(taskAssignee);
		sid = "sid-C7B60F80-7BC3-4134-B24F-B01E8A2EA1E3";
		ActivitiName="同意二";
		taskAssignee.setActivitiname(ActivitiName);
		taskAssignee.setSid(sid);
		taskAssigneeDao.save(taskAssignee);
		
	}
	
}
