/**
 * 
 */
package com.tydic.activiti_component.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.tydic.activiti_component.entity.TaskAssignee;

/**
 * @author shujingling
 * @date 2019年4月15日
 * @version v1.0
 * @package com.tydic.activiti_component.dao
 * @description
 */
public interface TaskAssigneeDao {
	void save(TaskAssignee taskAssignee);
	
	public List<TaskAssignee> selectBySid(@Param("sid")String sid);

}
