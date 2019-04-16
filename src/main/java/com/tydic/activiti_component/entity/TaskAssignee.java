/**
 * 
 */
package com.tydic.activiti_component.entity;

/**
 * @author shujingling
 * @date 2019年4月15日
 * @version v1.0
 * @package com.tydic.activiti_component.entity
 * @description
 */
public class TaskAssignee {
	private Integer id;
	private String sid ;
	private String assignee; // 办理人
	private String rolegroup;//候选组
	private String assigneeType;//办理人类型
	private String assigneename;//候选人
	private String activitiname;//节点名称
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return the sid
	 */
	public String getSid() {
		return sid;
	}
	/**
	 * @param sid the sid to set
	 */
	public void setSid(String sid) {
		this.sid = sid;
	}
	/**
	 * @return the assignee
	 */
	public String getAssignee() {
		return assignee;
	}
	/**
	 * @param assignee the assignee to set
	 */
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}
	/**
	 * @return the rolegroup
	 */
	public String getRolegroup() {
		return rolegroup;
	}
	/**
	 * @param rolegroup the rolegroup to set
	 */
	public void setRolegroup(String rolegroup) {
		this.rolegroup = rolegroup;
	}
	/**
	 * @return the assigneeType
	 */
	public String getAssigneeType() {
		return assigneeType;
	}
	/**
	 * @param assigneeType the assigneeType to set
	 */
	public void setAssigneeType(String assigneeType) {
		this.assigneeType = assigneeType;
	}
	/**
	 * @return the assigneename
	 */
	public String getAssigneename() {
		return assigneename;
	}
	/**
	 * @param assigneename the assigneename to set
	 */
	public void setAssigneename(String assigneename) {
		this.assigneename = assigneename;
	}
	/**
	 * @return the activitiname
	 */
	public String getActivitiname() {
		return activitiname;
	}
	/**
	 * @param activitiname the activitiname to set
	 */
	public void setActivitiname(String activitiname) {
		this.activitiname = activitiname;
	}
}
