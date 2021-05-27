package com.ai.st.microservice.workspaces.controllers.v1;

import com.ai.st.microservice.common.business.AdministrationBusiness;
import com.ai.st.microservice.common.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.common.exceptions.*;

import com.ai.st.microservice.workspaces.business.TaskBusiness;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.CancelTaskDto;
import com.ai.st.microservice.workspaces.dto.tasks.MicroserviceTaskDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;

@Api(value = "Manage Tasks", tags = {"Tasks"})
@RestController
@RequestMapping("api/workspaces/v1/tasks")
public class TaskV1Controller {

    private final Logger log = LoggerFactory.getLogger(TaskV1Controller.class);

    private final TaskBusiness taskBusiness;
    private final AdministrationBusiness administrationBusiness;

    public TaskV1Controller(TaskBusiness taskBusiness, AdministrationBusiness administrationBusiness) {
        this.taskBusiness = taskBusiness;
        this.administrationBusiness = administrationBusiness;
    }

    @GetMapping(value = "/pending", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get pending tasks")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get pending tasks", response = MicroserviceTaskDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> getPendingTasks(@RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        List<MicroserviceTaskDto> listTasks = new ArrayList<>();
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            listTasks = taskBusiness.getPendingTasks(userDtoSession.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error TaskV1Controller@createRequest#getPendingTasks ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (BusinessException e) {
            log.error("Error TaskV1Controller@createRequest#getPendingTasks ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error TaskV1Controller@createRequest#getPendingTasks ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return (responseDto != null) ? new ResponseEntity<>(responseDto, httpStatus)
                : new ResponseEntity<>(listTasks, httpStatus);
    }

    @PutMapping(value = "/{taskId}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Start task")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Task started", response = MicroserviceTaskDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> startTask(@RequestHeader("authorization") String headerAuthorization,
                                       @PathVariable Long taskId) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            responseDto = taskBusiness.startTask(taskId, userDtoSession.getId());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error TaskV1Controller@startTask#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (BusinessException e) {
            log.error("Error TaskV1Controller@startTask#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error TaskV1Controller@startTask#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PutMapping(value = "/{taskId}/finish", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Finish task")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Task finished", response = MicroserviceTaskDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> finishTask(@RequestHeader("authorization") String headerAuthorization,
                                        @PathVariable Long taskId) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            responseDto = taskBusiness.finishTask(taskId, userDtoSession);
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error TaskV1Controller@finishTask#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (BusinessException e) {
            log.error("Error TaskV1Controller@finishTask#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error TaskV1Controller@finishTask#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PutMapping(value = "/{taskId}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Cancel task")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Task cancelled", response = MicroserviceTaskDto.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<?> cancelTask(@RequestHeader("authorization") String headerAuthorization,
                                        @PathVariable Long taskId,
                                        @RequestBody CancelTaskDto cancelTaskRequest) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = administrationBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            String reason = cancelTaskRequest.getReason();
            if (reason == null || reason.isEmpty()) {
                throw new InputValidationException("Se debe justificar porque se cancelará la tarea.");
            }

            responseDto = taskBusiness.cancelTask(taskId, cancelTaskRequest.getReason(), userDtoSession);
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error TaskV1Controller@cancelTask#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (InputValidationException e) {
            log.error("Error TaskV1Controller@cancelTask#Validation ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (BusinessException e) {
            log.error("Error TaskV1Controller@cancelTask#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error TaskV1Controller@cancelTask#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

}
