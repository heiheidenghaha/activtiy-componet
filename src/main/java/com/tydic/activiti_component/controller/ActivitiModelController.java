/**
 * 
 */
package com.tydic.activiti_component.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.validation.ProcessValidator;
import org.activiti.validation.ProcessValidatorFactory;
import org.activiti.validation.ValidationError;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tydic.activiti_component.entity.ResponseVO;

/**
 * @author shujingling
 * @date 2019年3月18日
 * @version v1.0
 * @package com.tydic.activiti_component.controller
 * @description
 */
@RestController
@RequestMapping("model")
public class ActivitiModelController {
	
	@Autowired
	ProcessEngine processEngine;
	@Autowired
	ObjectMapper objectMapper;

	@RequestMapping("create")
	public void createModel(HttpServletRequest request, HttpServletResponse response) {
		try {
			String modelName = "modelName";
			String modelKey = "modelKey";
			String description = "description";

			ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

			RepositoryService repositoryService = processEngine.getRepositoryService();

			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode editorNode = objectMapper.createObjectNode();
			editorNode.put("id", "canvas");
			editorNode.put("resourceId", "canvas");
			ObjectNode stencilSetNode = objectMapper.createObjectNode();
			stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
			editorNode.put("stencilset", stencilSetNode);
			Model modelData = repositoryService.newModel();

			ObjectNode modelObjectNode = objectMapper.createObjectNode();
			modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, modelName);
			modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
			modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
			modelData.setMetaInfo(modelObjectNode.toString());
			modelData.setName(modelName);
			modelData.setKey(modelKey);

			// 保存模型
			repositoryService.saveModel(modelData);
			repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
			response.sendRedirect(request.getContextPath() + "/modeler.html?modelId=" + modelData.getId());
		} catch (Exception e) {
		}
	}
	
	//修改模型
	@GetMapping("update/{id}")
	public void updateModel(HttpServletResponse response,HttpServletRequest request,@PathVariable("id") String id){
		try {
			response.sendRedirect(request.getContextPath() + "/modeler.html?modelId=" + id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取所有模型
	 * 
	 * @return
	 */
	@GetMapping("modelList")
	public ResponseVO<Model> modelList() {
		RepositoryService repositoryService = processEngine.getRepositoryService();
		List<Model> models = repositoryService.createModelQuery().list();
		ResponseVO<Model> responseVO = ResponseVO.success(models);
		return responseVO;
	}

	/**
	 * 删除模型
	 * 
	 * @param id
	 * @return
	 */
	@DeleteMapping("{id}")
	public ResponseVO<String> deleteModel(@PathVariable("id") String id) {
		RepositoryService repositoryService = processEngine.getRepositoryService();
		repositoryService.deleteModel(id);
		ResponseVO<String> responseVO = ResponseVO.success("success");
		return responseVO;
	}

	/**
	 * 发布模型为流程定义
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@PostMapping("{id}/deployment")
	public ResponseVO<String> deploy(@PathVariable("id") String id) throws Exception {
	
		// 获取模型
		RepositoryService repositoryService = processEngine.getRepositoryService();
		Model modelData = repositoryService.getModel(id);
		byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

		if (bytes == null) {
			return ResponseVO.success("模型数据为空，请先设计流程并成功保存，再进行发布。");
		}

		JsonNode modelNode = new ObjectMapper().readTree(bytes);

		BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
		if (model.getProcesses().size() == 0) {
			return ResponseVO.success("数据模型不符要求，请至少设计一条主线流程。");
		}
		
		ProcessValidatorFactory processValidatorFactory = new ProcessValidatorFactory();
		ProcessValidator processValidator = processValidatorFactory.createDefaultProcessValidator();
		
		List<ValidationError> vErrors = processValidator.validate(model);
		if(vErrors.size() > 0){
			return ResponseVO.success("流程设计有误");
		}
				
		byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

		// 发布流程
		String processName = modelData.getName() + ".bpmn20.xml";
		Deployment deployment = repositoryService.createDeployment().name(modelData.getName())
				.addString(processName, new String(bpmnBytes, "UTF-8")).deploy();
		modelData.setDeploymentId(deployment.getId());
		repositoryService.saveModel(modelData);

		return ResponseVO.success("success");
	}
	

}
