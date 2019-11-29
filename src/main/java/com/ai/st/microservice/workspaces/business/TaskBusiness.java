package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.TaskFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMemberDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

@Component
public class TaskBusiness {

	@Autowired
	private TaskFeignClient taskClient;

	@Autowired
	private UserFeignClient userClient;

	public List<MicroserviceTaskDto> getPendingTasks(Long userCode) throws BusinessException {

		List<MicroserviceTaskDto> listTasksDto = new ArrayList<MicroserviceTaskDto>();

		try {
			List<MicroserviceTaskDto> listResponseTasks = taskClient.findByUserAndState(userCode, (long) 1);

			for (MicroserviceTaskDto taskDto : listResponseTasks) {

				List<MicroserviceTaskMemberDto> members = new ArrayList<MicroserviceTaskMemberDto>();
				for (MicroserviceTaskMemberDto member : taskDto.getMembers()) {
					try {
						MicroserviceUserDto userDto = userClient.findById(member.getMemberCode());
						member.setUser(userDto);
					} catch (Exception e) {
						member.setUser(null);
					}
					members.add(member);
				}
				taskDto.setMembers(members);
				listTasksDto.add(taskDto);
			}

		} catch (Exception e) {
			throw new BusinessException("No se ha podido consultar las tareas pendientes del usuario.");
		}

		return listTasksDto;
	}

}
