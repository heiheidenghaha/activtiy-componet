/**
 * 
 */
package com.tydic.activiti_component.entity;

import com.sun.tracing.dtrace.ProviderAttributes;

/**
 * @author shujingling
 * @date 2019年3月26日
 * @version v1.0
 * @package com.tydic.activiti_component.entity
 * @description
 */
public class ProcessDefinitionVo {
	private String name;
	private String id;
	private String key;
	private String resourceName;
	private String deploymentId;
	private String diagramResourceName;
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the resourceName
	 */
	public String getResourceName() {
		return resourceName;
	}
	/**
	 * @param resourceName the resourceName to set
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	/**
	 * @return the deploymentId
	 */
	public String getDeploymentId() {
		return deploymentId;
	}
	/**
	 * @param deploymentId the deploymentId to set
	 */
	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}
	/**
	 * @return the diagramResourceName
	 */
	public String getDiagramResourceName() {
		return diagramResourceName;
	}
	/**
	 * @param diagramResourceName the diagramResourceName to set
	 */
	public void setDiagramResourceName(String diagramResourceName) {
		this.diagramResourceName = diagramResourceName;
	}
	

}
