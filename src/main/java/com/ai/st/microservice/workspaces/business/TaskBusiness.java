package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.TaskFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCreateTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCreateTaskMetadataDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceCreateTaskStepDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMemberDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMetadataDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskMetadataPropertyDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component
public class TaskBusiness {

	public static final Long TASK_CATEGORY_INTEGRATION = (long) 1;

	public static final Long TASK_TYPE_STEP_ONCE = (long) 1;
	public static final Long TASK_TYPE_STEP_ALWAYS = (long) 2;

	public static final Long TASK_STATE_ASSIGNED = (long) 1;
	public static final Long TASK_STATE_CLOSED = (long) 2;
	public static final Long TASK_STATE_CANCELLED = (long) 3;
	public static final Long TASK_STATE_STARTED = (long) 4;

	@Autowired
	private TaskFeignClient taskClient;

	@Autowired
	private UserFeignClient userClient;

	public List<MicroserviceTaskDto> getPendingTasks(Long userCode) throws BusinessException {

		List<MicroserviceTaskDto> listTasksDto = new ArrayList<MicroserviceTaskDto>();

		try {

			List<Long> taskStates = new ArrayList<>();
			taskStates.add(TaskBusiness.TASK_STATE_ASSIGNED);
			taskStates.add(TaskBusiness.TASK_STATE_STARTED);

			List<MicroserviceTaskDto> listResponseTasks = taskClient.findByUserAndState(userCode, taskStates);

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

				JsonObject objectMetadata = new JsonObject();

				for (MicroserviceTaskMetadataDto metadataDto : taskDto.getMetadata()) {

					JsonObject objectProperties = new JsonObject();
					for (MicroserviceTaskMetadataPropertyDto propertyDto : metadataDto.getProperties()) {
						objectProperties.addProperty(propertyDto.getKey(), propertyDto.getValue());
					}

					objectMetadata.add(metadataDto.getKey(), objectProperties);

				}

				Gson gson = new Gson();
				@SuppressWarnings("unchecked")
				Map<String, Object> mapData = gson.fromJson(objectMetadata.toString(), Map.class);
				taskDto.setData(mapData);

				listTasksDto.add(taskDto);
			}

		} catch (Exception e) {
			throw new BusinessException("No se ha podido consultar las tareas pendientes del usuario.");
		}

		return listTasksDto;
	}

	public MicroserviceTaskDto createTask(List<Long> categories, String deadline, String description, String name,
			List<Long> users, List<MicroserviceCreateTaskMetadataDto> metadata,
			List<MicroserviceCreateTaskStepDto> steps) throws BusinessException {

		MicroserviceTaskDto taskDto = null;

		try {

			MicroserviceCreateTaskDto createTask = new MicroserviceCreateTaskDto();
			createTask.setCategories(categories);
			createTask.setDeadline(deadline);
			createTask.setDescription(description);
			createTask.setMetadata(metadata);
			createTask.setName(name);
			createTask.setUsers(users);
			createTask.setSteps(steps);

			taskDto = taskClient.createTask(createTask);

		} catch (Exception e) {
			throw new BusinessException("No se ha podido crear la tarea.");
		}

		return taskDto;
	}

	public MicroserviceTaskDto startTask(Long taskId, Long userId) throws BusinessException {

		MicroserviceTaskDto taskDto = null;
		
		

		return taskDto;
	}

}
