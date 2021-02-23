package com.ai.st.microservice.workspaces.controllers.v1;

import com.ai.st.microservice.workspaces.business.*;
import com.ai.st.microservice.workspaces.exceptions.InputValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.AddProfileToUserDto;
import com.ai.st.microservice.workspaces.dto.BasicResponseDto;
import com.ai.st.microservice.workspaces.dto.ChangePasswordDto;
import com.ai.st.microservice.workspaces.dto.CreateUserDto;
import com.ai.st.microservice.workspaces.dto.UpdateUserDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Manage Users-Roles", tags = {"Administration"})
@RestController
@RequestMapping("api/workspaces/v1/administration")
public class AdministrationV1Controller {

    private final Logger log = LoggerFactory.getLogger(AdministrationV1Controller.class);

    @Autowired
    private AdministrationBusiness administrationBusiness;

    @Autowired
    private UserFeignClient userClient;

    @Autowired
    private ProviderFeignClient providerClient;

    @Autowired
    private ManagerFeignClient managerClient;

    @Autowired
    private UserBusiness userBusiness;

    @Autowired
    private ManagerBusiness managerBusiness;

    @Autowired
    private ProviderBusiness providerBusiness;

    @RequestMapping(value = "/users", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create user")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Create user", response = MicroserviceUserDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> createUser(@RequestBody CreateUserDto requestCreateUser,
                                             @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (userBusiness.isSuperAdministrator(userDtoSession)) {
                responseDto = administrationBusiness.createUser(requestCreateUser.getFirstName(),
                        requestCreateUser.getLastName(), requestCreateUser.getEmail(), requestCreateUser.getUsername(),
                        requestCreateUser.getPassword(), null, requestCreateUser.getRoleAdministrator(), null, null);
            }

            if (userBusiness.isAdministrator(userDtoSession)) {

                if (requestCreateUser.getRoleProvider() != null) {
                    requestCreateUser.getRoleProvider().setFromAdministrator(true);
                }

                responseDto = administrationBusiness.createUserFromAdministrator(requestCreateUser.getFirstName(),
                        requestCreateUser.getLastName(), requestCreateUser.getEmail(), requestCreateUser.getUsername(),
                        requestCreateUser.getPassword(), requestCreateUser.getRoleProvider(),
                        requestCreateUser.getRoleAdministrator(), requestCreateUser.getRoleManager(),
                        requestCreateUser.getRoleOperator());
            }

            if (userBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }
                if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para crear usuarios.");
                }

                requestCreateUser.getRoleManager().setManagerId(managerDto.getId());

                responseDto = administrationBusiness.createUserFromManager(requestCreateUser.getFirstName(),
                        requestCreateUser.getLastName(), requestCreateUser.getEmail(), requestCreateUser.getUsername(),
                        requestCreateUser.getPassword(), requestCreateUser.getRoleManager());

            }

            if (userBusiness.isProvider(userDtoSession)) {

                requestCreateUser.getRoleProvider().setFromAdministrator(false);

                MicroserviceProviderDto providerDto = providerBusiness.getProviderByUserAdministrator(userDtoSession.getId());
                if (providerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
                }
                if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para crear usuarios.");
                }

                requestCreateUser.getRoleProvider().setProviderId(providerDto.getId());

                responseDto = administrationBusiness.createUserFromProvider(requestCreateUser.getFirstName(),
                        requestCreateUser.getLastName(), requestCreateUser.getEmail(), requestCreateUser.getUsername(),
                        requestCreateUser.getPassword(), requestCreateUser.getRoleProvider());

            }

            httpStatus = HttpStatus.CREATED;

        } catch (BusinessException e) {
            log.error("Error AdministrationV1Controller@createUser#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error AdministrationV1Controller@createUser#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "/users/reset-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Change password user")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Create user", response = MicroserviceUserDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> resetUserPassword(@RequestBody ChangePasswordDto requestChangePassword,
                                                    @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            responseDto = administrationBusiness.changeUserPassword(userDtoSession.getId(),
                    requestChangePassword.getPassword());
            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error AdministrationV1Controller@resetUserPassword#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (BusinessException e) {
            log.error("Error AdministrationV1Controller@resetUserPassword#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error AdministrationV1Controller@resetUserPassword#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "/users/{userId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update user")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Update user", response = MicroserviceUserDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> updateUser(@PathVariable Long userId, @RequestBody UpdateUserDto requestUpdateUser,
                                             @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (userBusiness.isSuperAdministrator(userDtoSession)) {
                responseDto = administrationBusiness.updateUserFromSuperAdmin(userId, requestUpdateUser.getFirstName(),
                        requestUpdateUser.getLastName());
            }

            if (userBusiness.isAdministrator(userDtoSession)) {
                responseDto = administrationBusiness.updateUserFromAdministrator(userId,
                        requestUpdateUser.getFirstName(), requestUpdateUser.getLastName());
            }

            if (userBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }
                if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para editar usuarios.");
                }

                responseDto = administrationBusiness.updateUserFromManager(userId, requestUpdateUser.getFirstName(),
                        requestUpdateUser.getLastName(), managerDto.getId());

            }

            if (userBusiness.isProvider(userDtoSession)) {

                // get provider
                MicroserviceProviderDto providerDto = providerBusiness.getProviderByUserAdministrator(userDtoSession.getId());
                if (providerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
                }
                if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para editar usuarios.");
                }

                responseDto = administrationBusiness.updateUserFromProvider(userId, requestUpdateUser.getFirstName(),
                        requestUpdateUser.getLastName(), providerDto.getId());

            }

            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error AdministrationV1Controller@updateUser#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (InputValidationException e) {
            log.error("Error AdministrationV1Controller@updateUser#Validation ---> " + e.getMessage());
            httpStatus = HttpStatus.UNAUTHORIZED;
            responseDto = new BasicResponseDto(e.getMessage(), 1);
        } catch (BusinessException e) {
            log.error("Error AdministrationV1Controller@updateUser#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error AdministrationV1Controller@updateUser#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "/users/{userId}/disable", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Disable user")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "User disabled", response = MicroserviceUserDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> disableUser(@PathVariable Long userId,
                                              @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (userBusiness.isSuperAdministrator(userDtoSession)) {
                responseDto = administrationBusiness.changeStatusUserFromSuperAdmin(userId, false);
            }

            if (userBusiness.isAdministrator(userDtoSession)) {
                responseDto = administrationBusiness.changeStatusUserFromAdministrator(userId, false);
            }

            if (userBusiness.isManager(userDtoSession)) {

                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }
                if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para deshabilitar el soporte.");
                }

                responseDto = administrationBusiness.changeStatusUserFromManager(userId, false, managerDto.getId());
            }

            if (userBusiness.isProvider(userDtoSession)) {

                // get provider
                MicroserviceProviderDto providerDto = providerBusiness.getProviderByUserAdministrator(userDtoSession.getId());
                if (providerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
                }
                if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para deshabilitar usuarios.");
                }

                responseDto = administrationBusiness.changeStatusUserFromProvider(userId, false, providerDto.getId());

            }

            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error AdministrationV1Controller@disableUser#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (InputValidationException e) {
            log.error("Error AdministrationV1Controller@disableUser#Validation ---> " + e.getMessage());
            httpStatus = HttpStatus.UNAUTHORIZED;
            responseDto = new BasicResponseDto(e.getMessage(), 1);
        } catch (BusinessException e) {
            log.error("Error AdministrationV1Controller@disableUser#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error AdministrationV1Controller@disableUser#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "/users/{userId}/enable", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Enable user")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "User enabled", response = MicroserviceUserDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> enableUser(@PathVariable Long userId,
                                             @RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (userBusiness.isSuperAdministrator(userDtoSession)) {
                responseDto = administrationBusiness.changeStatusUserFromSuperAdmin(userId, true);
            }

            if (userBusiness.isAdministrator(userDtoSession)) {
                responseDto = administrationBusiness.changeStatusUserFromAdministrator(userId, true);
            }

            if (userBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }
                if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para habilitar usuarios.");
                }

                responseDto = administrationBusiness.changeStatusUserFromManager(userId, true, managerDto.getId());
            }

            if (userBusiness.isProvider(userDtoSession)) {

                // get provider
                MicroserviceProviderDto providerDto = providerBusiness.getProviderByUserAdministrator(userDtoSession.getId());
                if (providerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
                }
                if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para habilitar usuarios.");
                }

                responseDto = administrationBusiness.changeStatusUserFromProvider(userId, true, providerDto.getId());

            }

            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error AdministrationV1Controller@enableUser#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (InputValidationException e) {
            log.error("Error AdministrationV1Controller@enableUser#Validation ---> " + e.getMessage());
            httpStatus = HttpStatus.UNAUTHORIZED;
            responseDto = new BasicResponseDto(e.getMessage(), 1);
        } catch (BusinessException e) {
            log.error("Error AdministrationV1Controller@enableUser#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error AdministrationV1Controller@enableUser#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get users")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Get users", response = MicroserviceUserDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> getUsers(@RequestHeader("authorization") String headerAuthorization) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (userBusiness.isSuperAdministrator(userDtoSession)) {
                responseDto = administrationBusiness.getUsersFromSuperAdmin();
            }

            if (userBusiness.isAdministrator(userDtoSession)) {
                responseDto = administrationBusiness.getUsersFromAdministrator();
            }

            if (userBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }
                if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para consultar usuarios.");
                }

                responseDto = administrationBusiness.getUsersFromManager(managerDto.getId());

            }

            if (userBusiness.isProvider(userDtoSession)) {

                // get provider
                MicroserviceProviderDto providerDto = providerBusiness.getProviderByUserAdministrator(userDtoSession.getId());
                if (providerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
                }
                if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para consultar usuarios.");
                }

                responseDto = administrationBusiness.getUsersFromProvider(providerDto.getId());

            }

            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error AdministrationV1Controller@getUsers#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (InputValidationException e) {
            log.error("Error AdministrationV1Controller@getUsers#Validation ---> " + e.getMessage());
            httpStatus = HttpStatus.UNAUTHORIZED;
            responseDto = new BasicResponseDto(e.getMessage(), 1);
        } catch (BusinessException e) {
            log.error("Error AdministrationV1Controller@getUsers#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error AdministrationV1Controller@getUsers#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "/users/{userId}/profiles", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add profile to user")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Profile Added", response = MicroserviceUserDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> addProfileToUser(@RequestHeader("authorization") String headerAuthorization,
                                                   @RequestBody AddProfileToUserDto addProfileUser, @PathVariable Long userId) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (userBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }
                if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para agregar perfiles a los usuarios.");
                }

                responseDto = administrationBusiness.addProfileToUserFromManager(userId, addProfileUser.getProfileId(),
                        managerDto.getId());

            }

            if (userBusiness.isProvider(userDtoSession)) {

                // get provider
                MicroserviceProviderDto providerDto = providerBusiness.getProviderByUserAdministrator(userDtoSession.getId());
                if (providerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
                }
                if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para agregar perfiles a los usuarios.");
                }

                responseDto = administrationBusiness.addProfileToUserFromProvider(userId, addProfileUser.getProfileId(),
                        providerDto.getId());

            }

            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error AdministrationV1Controller@addProfileToUser#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (InputValidationException e) {
            log.error("Error AdministrationV1Controller@addProfileToUser#Validation ---> " + e.getMessage());
            httpStatus = HttpStatus.UNAUTHORIZED;
            responseDto = new BasicResponseDto(e.getMessage(), 1);
        } catch (BusinessException e) {
            log.error("Error AdministrationV1Controller@addProfileToUser#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error AdministrationV1Controller@addProfileToUser#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @RequestMapping(value = "/users/{userId}/profiles", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Remove profile to user")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Profile Added", response = MicroserviceUserDto.class),
            @ApiResponse(code = 500, message = "Error Server", response = String.class)})
    @ResponseBody
    public ResponseEntity<Object> removeProfileToUser(@RequestHeader("authorization") String headerAuthorization,
                                                      @RequestBody AddProfileToUserDto addProfileUser, @PathVariable Long userId) {

        HttpStatus httpStatus;
        Object responseDto = null;

        try {

            // user session
            MicroserviceUserDto userDtoSession = userBusiness.getUserByToken(headerAuthorization);
            if (userDtoSession == null) {
                throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el usuario");
            }

            if (userBusiness.isManager(userDtoSession)) {

                // get manager
                MicroserviceManagerDto managerDto = managerBusiness.getManagerByUserCode(userDtoSession.getId());
                if (managerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el gestor.");
                }
                if (!managerBusiness.userManagerIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para remover perfiles a los usuarios.");
                }

                responseDto = administrationBusiness.removeProfileToUserFromManager(userId,
                        addProfileUser.getProfileId(), managerDto.getId());

            }

            if (userBusiness.isProvider(userDtoSession)) {

                // get provider
                MicroserviceProviderDto providerDto = providerBusiness.getProviderByUserAdministrator(userDtoSession.getId());
                if (providerDto == null) {
                    throw new DisconnectedMicroserviceException("Ha ocurrido un error consultando el proveedor.");
                }
                if (!providerBusiness.userProviderIsDirector(userDtoSession.getId())) {
                    throw new InputValidationException("El usuario no tiene permisos para remover perfiles a los usuarios.");
                }

                responseDto = administrationBusiness.removeProfileToUserFromProvider(userId,
                        addProfileUser.getProfileId(), providerDto.getId());

            }

            httpStatus = HttpStatus.OK;

        } catch (DisconnectedMicroserviceException e) {
            log.error("Error AdministrationV1Controller@removeProfileToUser#Microservice ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 4);
        } catch (InputValidationException e) {
            log.error("Error AdministrationV1Controller@removeProfileToUser#Validation ---> " + e.getMessage());
            httpStatus = HttpStatus.UNAUTHORIZED;
            responseDto = new BasicResponseDto(e.getMessage(), 1);
        } catch (BusinessException e) {
            log.error("Error AdministrationV1Controller@removeProfileToUser#Business ---> " + e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            responseDto = new BasicResponseDto(e.getMessage(), 2);
        } catch (Exception e) {
            log.error("Error AdministrationV1Controller@removeProfileToUser#General ---> " + e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseDto = new BasicResponseDto(e.getMessage(), 3);
        }

        return new ResponseEntity<>(responseDto, httpStatus);
    }

}
