package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.OperatorFeignClient;
import com.ai.st.microservice.workspaces.clients.ProviderFeignClient;
import com.ai.st.microservice.workspaces.clients.UserFeignClient;
import com.ai.st.microservice.workspaces.dto.CreateUserRoleAdministratorDto;
import com.ai.st.microservice.workspaces.dto.CreateUserRoleManagerDto;
import com.ai.st.microservice.workspaces.dto.CreateUserRoleOperatorDto;
import com.ai.st.microservice.workspaces.dto.CreateUserRoleProviderDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceChangePasswordDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceCreateUserDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceRoleDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUpdateUserDto;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceAddUserToManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerProfileDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerUserDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceOperatorDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceAddAdministratorToProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceAddUserToProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderAdministratorDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderProfileDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceProviderUserDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

@Component
public class AdministrationBusiness {

    private final Logger log = LoggerFactory.getLogger(AdministrationBusiness.class);

    @Autowired
    private UserFeignClient userClient;

    @Autowired
    private ProviderFeignClient providerClient;

    @Autowired
    private ManagerFeignClient managerClient;

    @Autowired
    private OperatorFeignClient operatorClient;

    @Autowired
    private NotificationBusiness notificationBusiness;

    @Autowired
    private ProviderBusiness providerBusiness;

    @Autowired
    private ManagerBusiness managerBusiness;

    @Autowired
    private OperatorBusiness operatorBusiness;

    public MicroserviceUserDto createUserFromAdministrator(String firstName, String lastName, String email,
                                                           String username, String password, CreateUserRoleProviderDto roleProvider,
                                                           CreateUserRoleAdministratorDto roleAdmin, CreateUserRoleManagerDto roleManager,
                                                           CreateUserRoleOperatorDto roleOperator) throws BusinessException {

        if (roleManager != null) {

            if (roleManager.getProfiles().size() == 0) {
                throw new BusinessException("Para asignar el rol de gestor se debe especificar al menos un perfil.");
            }

            List<Long> listRolesWrong = roleManager.getProfiles().stream()
                    .filter(p -> !p.equals(RoleBusiness.SUB_ROLE_DIRECTOR)).collect(Collectors.toList());

            if (listRolesWrong.size() > 0) {
                throw new BusinessException("No se puede asignar al usuario un perfil diferente al de Director.");
            }

        }

        if (roleProvider != null) {
            roleProvider.setProfiles(new ArrayList<>(Collections.singletonList(RoleBusiness.SUB_ROLE_DIRECTOR_PROVIDER)));
        }

        if (roleOperator != null) {

            if (!roleOperator.getRoleId().equals(RoleBusiness.ROLE_OPERATOR)) {
                throw new BusinessException("El rol no corresponde con un operador.");
            }

        }

        return this.createUser(firstName, lastName, email, username, password, roleProvider, roleAdmin, roleManager,
                roleOperator);
    }

    public MicroserviceUserDto createUserFromManager(String firstName, String lastName, String email, String username,
                                                     String password, CreateUserRoleManagerDto roleManager) throws BusinessException {

        if (roleManager.getProfiles().size() == 0) {
            throw new BusinessException("Para asignar el rol de gestor se debe especificar al menos un perfil.");
        }

        List<Long> listRolesWrong = roleManager.getProfiles().stream()
                .filter(p -> p.equals(RoleBusiness.SUB_ROLE_DIRECTOR)).collect(Collectors.toList());

        if (listRolesWrong.size() > 0) {
            throw new BusinessException("No se puede asignar al usuario un perfil Director.");
        }

        return this.createUser(firstName, lastName, email, username, password, null, null, roleManager, null);
    }

    public MicroserviceUserDto createUserFromProvider(String firstName, String lastName, String email, String username,
                                                      String password, CreateUserRoleProviderDto roleProvider) throws BusinessException {

        if (roleProvider.getProfiles().size() == 0) {
            throw new BusinessException("Se debe especificar al menos un perfil para el usuario.");
        }

        if (!roleProvider.getIsTechnical()) {
            Long profileDirector = roleProvider.getProfiles().stream()
                    .filter(profileId -> profileId.equals(RoleBusiness.SUB_ROLE_DIRECTOR_PROVIDER)).findAny()
                    .orElse(null);
            if (profileDirector != null) {
                throw new BusinessException("No se pueden crear usuarios administradores.");
            }
        }

        return this.createUser(firstName, lastName, email, username, password, roleProvider, null, null, null);
    }

    public MicroserviceUserDto createUser(String firstName, String lastName, String email, String username,
                                          String password, CreateUserRoleProviderDto roleProvider, CreateUserRoleAdministratorDto roleAdmin,
                                          CreateUserRoleManagerDto roleManager, CreateUserRoleOperatorDto roleOperator) throws BusinessException {

        MicroserviceUserDto userResponseDto;

        MicroserviceCreateUserDto createUserDto = new MicroserviceCreateUserDto();
        createUserDto.setEmail(email);
        createUserDto.setFirstName(firstName);
        createUserDto.setLastName(lastName);
        createUserDto.setPassword(password);
        createUserDto.setUsername(username);

        List<Long> roles = new ArrayList<>();

        String entityName = "";

        if (roleProvider != null) {

            if (roleProvider.getProfiles().size() == 0) {
                throw new BusinessException("Para asignar el rol de proveedor se debe especificar al menos un perfil.");
            }

            if (roleProvider.getRoleId() != null && roleProvider.getRoleId() > 0) {
                roles.add(roleProvider.getRoleId());
            }

            MicroserviceProviderDto providerDto = providerBusiness.getProviderById(roleProvider.getProviderId());
            entityName = (providerDto != null) ? providerDto.getName() : "";
        }

        if (roleAdmin != null) {
            roles.add(roleAdmin.getRoleId());
            entityName = "ADMINISTRADOR";
        }

        if (roleManager != null) {

            if (roleManager.getProfiles().size() == 0) {
                throw new BusinessException("Para asignar el rol de gestor se debe especificar al menos un perfil.");
            }

            if (roleManager.getRoleId() != null && roleManager.getRoleId() > 0) {
                roles.add(roleManager.getRoleId());
            }

            MicroserviceManagerDto managerDto = managerBusiness.getManagerById(roleManager.getManagerId());
            entityName = (managerDto != null) ? managerDto.getName() : "";
        }

        if (roleOperator != null) {

            if (roleOperator.getRoleId() != null && roleOperator.getRoleId() > 0) {
                roles.add(roleOperator.getRoleId());
            }

            MicroserviceOperatorDto operatorDto = operatorBusiness.getOperatorById(roleOperator.getOperatorId());
            entityName = (operatorDto != null) ? operatorDto.getName() : "";
        }

        createUserDto.setRoles(roles);

        if (roles.size() > 0) {

            try {
                userResponseDto = userClient.createUser(createUserDto);

                if (roleProvider != null) {

                    if (roleProvider.getIsTechnical()) {

                        try {
                            for (Long profileId : roleProvider.getProfiles()) {
                                MicroserviceAddUserToProviderDto addUser = new MicroserviceAddUserToProviderDto();
                                addUser.setUserCode(userResponseDto.getId());
                                addUser.setProfileId(profileId);
                                addUser.setProviderId(roleProvider.getProviderId());

                                providerClient.addUserToProvide(addUser);
                            }
                        } catch (Exception e) {
                            log.error("Error asignando perfil proveedor al usuario: " + e.getMessage());
                        }

                    } else {

                        try {
                            for (Long profileId : roleProvider.getProfiles()) {
                                MicroserviceAddAdministratorToProviderDto addUser = new MicroserviceAddAdministratorToProviderDto();
                                addUser.setUserCode(userResponseDto.getId());
                                addUser.setRoleId(profileId);
                                addUser.setProviderId(roleProvider.getProviderId());

                                providerClient.addAdministratorToProvide(addUser);
                            }
                        } catch (Exception e) {
                            log.error("Error asignando role proveedor al usuario: " + e.getMessage());
                        }

                    }

                }

                if (roleManager != null) {
                    try {

                        for (Long profileId : roleManager.getProfiles()) {

                            MicroserviceAddUserToManagerDto addUser = new MicroserviceAddUserToManagerDto();
                            addUser.setUserCode(userResponseDto.getId());
                            addUser.setProfileId(profileId);
                            addUser.setManagerId(roleManager.getManagerId());

                            managerClient.addUserToManager(addUser);
                        }

                    } catch (Exception e) {
                        log.error("Error adding profile to manager: " + e.getMessage());
                    }
                }

                if (roleOperator != null) {
                    operatorBusiness.addUserToOperator(roleOperator.getOperatorId(), userResponseDto.getId());
                }

            } catch (BusinessException e) {
                throw new BusinessException(e.getMessage());
            }

        } else {
            throw new BusinessException("El usuario necesita tener al menos un rol.");
        }

        // send notification
        try {
            notificationBusiness.sendNotificationCreationUser(email, password, entityName, username,
                    userResponseDto.getId());
        } catch (Exception e) {
            log.error(String.format("Error enviando notificación de la creación del usuario: %s", e.getMessage()));
        }

        return userResponseDto;
    }

    public MicroserviceUserDto changeUserPassword(Long userId, String password) throws BusinessException {

        MicroserviceUserDto userDto;

        try {

            MicroserviceChangePasswordDto requestChangePassword = new MicroserviceChangePasswordDto();
            requestChangePassword.setPassword(password);

            userDto = userClient.changeUserPassword(userId, requestChangePassword);

        } catch (BusinessException e) {
            throw new BusinessException(e.getMessage());
        }

        return userDto;
    }

    public MicroserviceUserDto updateUserFromSuperAdmin(Long userId, String firstName, String lastName)
            throws BusinessException {

        MicroserviceUserDto userDto;
        try {
            userDto = userClient.findById(userId);
        } catch (Exception e) {
            log.error("Error consultando el usuario para la edición: " + e.getMessage());
            throw new BusinessException("No se ha encontrado el usuario");
        }

        MicroserviceRoleDto roleDto = userDto.getRoles().stream()
                .filter(r -> r.getId().equals(RoleBusiness.ROLE_ADMINISTRATOR)).findAny().orElse(null);

        if (!(roleDto instanceof MicroserviceRoleDto)) {
            throw new BusinessException("No cuenta con los permisos necesarios para editar el usuario");
        }

        return this.updateUser(userId, firstName, lastName);
    }

    public MicroserviceUserDto updateUserFromAdministrator(Long userId, String firstName, String lastName)
            throws BusinessException {

        MicroserviceUserDto userDto;
        try {
            userDto = userClient.findById(userId);
        } catch (Exception e) {
            log.error("Error consultando el usuario para la edición: " + e.getMessage());
            throw new BusinessException("No se ha encontrado el usuario");
        }

        MicroserviceRoleDto roleManagerDto = userDto.getRoles().stream()
                .filter(r -> r.getId().equals(RoleBusiness.ROLE_MANAGER)).findAny().orElse(null);

        MicroserviceRoleDto roleOperatorDto = userDto.getRoles().stream()
                .filter(r -> r.getId().equals(RoleBusiness.ROLE_OPERATOR)).findAny().orElse(null);

        MicroserviceRoleDto roleProviderDto = userDto.getRoles().stream()
                .filter(r -> r.getId().equals(RoleBusiness.ROLE_SUPPLY_SUPPLIER)).findAny().orElse(null);

        if (!(roleManagerDto instanceof MicroserviceRoleDto) && !(roleOperatorDto instanceof MicroserviceRoleDto)
                && !(roleProviderDto instanceof MicroserviceRoleDto)) {
            throw new BusinessException("No cuenta con los permisos necesarios para editar el usuario");
        }

        if (roleManagerDto != null) {

            List<MicroserviceManagerProfileDto> profiles = new ArrayList<>();

            try {
                profiles = managerClient.findProfilesByUser(userId);
            } catch (Exception e) {
                log.error("Error consultando los perfiles de un usuario gestor: " + e.getMessage());
            }

            MicroserviceManagerProfileDto profileDto = profiles.stream()
                    .filter(p -> p.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR)).findAny().orElse(null);

            if (!(profileDto instanceof MicroserviceManagerProfileDto)) {
                throw new BusinessException("No se puede editar usuarios gestores que no cuentan con el rol Director");
            }

        }

        if (roleProviderDto instanceof MicroserviceRoleDto) {

            List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> roles = new ArrayList<>();

            try {
                roles = providerClient.findRolesByUser(userId);
            } catch (Exception e) {
                log.error("Error consultando los roles de un usuario proveedor: " + e.getMessage());
            }

            com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto roleDto = roles.stream()
                    .filter(r -> r.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR_PROVIDER)).findAny().orElse(null);

            if (roleDto == null) {
                throw new BusinessException(
                        "No se puede editar usuarios proveedores que no cuentan con el rol Director");
            }

        }

        return this.updateUser(userId, firstName, lastName);
    }

    public MicroserviceUserDto updateUserFromManager(Long userId, String firstName, String lastName, Long managerCode)
            throws BusinessException {

        MicroserviceUserDto userDto;
        try {
            userDto = userClient.findById(userId);
        } catch (Exception e) {
            log.error("Error consultando el usuario para la edición: " + e.getMessage());
            throw new BusinessException("No se ha encontrado el usuario");
        }

        MicroserviceRoleDto roleManagerDto = userDto.getRoles().stream()
                .filter(r -> r.getId().equals(RoleBusiness.ROLE_MANAGER)).findAny().orElse(null);

        if (!(roleManagerDto instanceof MicroserviceRoleDto)) {
            throw new BusinessException("No se puede editar usuarios que no son gestores");
        }

        MicroserviceManagerDto managerDto;
        try {

            managerDto = managerClient.findByUserCode(userId);

        } catch (Exception e) {
            log.error("Error consultando gestor: " + e.getMessage());
            throw new BusinessException("No se ha podido modificar el usuario.");
        }

        if (!managerDto.getId().equals(managerCode)) {
            throw new BusinessException("El usuario que se desea editar no pertenece al gestor.");
        }

        return this.updateUser(userId, firstName, lastName);
    }

    public MicroserviceUserDto updateUserFromProvider(Long userId, String firstName, String lastName, Long providerCode)
            throws BusinessException {

        MicroserviceUserDto userDto;
        try {
            userDto = userClient.findById(userId);
        } catch (Exception e) {
            log.error("Error consultando el usuario para la edición: " + e.getMessage());
            throw new BusinessException("No se ha encontrado el usuario");
        }

        MicroserviceRoleDto roleProviderDto = userDto.getRoles().stream()
                .filter(r -> r.getId().equals(RoleBusiness.ROLE_SUPPLY_SUPPLIER)).findAny().orElse(null);

        if (!(roleProviderDto instanceof MicroserviceRoleDto)) {
            throw new BusinessException("No se puede editar usuarios que no son proveedores");
        }

        MicroserviceProviderDto providerDtoByUser = null;
        try {

            providerDtoByUser = providerClient.findByUserCode(userId);

        } catch (Exception e) {
            log.error("Error consultando proveedor por usuario: " + e.getMessage());
        }

        MicroserviceProviderDto providerDtoByAdmin = null;
        try {

            providerDtoByAdmin = providerClient.findProviderByAdministrator(userId);

        } catch (Exception e) {
            log.error("Error consultando proveedor por administrador: " + e.getMessage());
        }

        if (providerDtoByAdmin == null && providerDtoByUser == null) {
            throw new BusinessException("No se ha encontrado el usuario.");
        }

        MicroserviceProviderDto providerDto = (providerDtoByAdmin != null) ? providerDtoByAdmin : providerDtoByUser;

        if (!providerDto.getId().equals(providerCode)) {
            throw new BusinessException("El usuario que se desea editar no pertenece al proveedor.");
        }

        return this.updateUser(userId, firstName, lastName);
    }

    public MicroserviceUserDto updateUser(Long userId, String firstName, String lastName) throws BusinessException {

        MicroserviceUserDto userDto;

        try {

            MicroserviceUpdateUserDto updateUser = new MicroserviceUpdateUserDto();
            updateUser.setFirstName(firstName);
            updateUser.setLastName(lastName);

            userDto = userClient.updateUser(userId, updateUser);

        } catch (BusinessException e) {
            throw new BusinessException(e.getMessage());
        }

        return userDto;
    }

    public MicroserviceUserDto changeStatusUserFromSuperAdmin(Long userId, Boolean status) throws BusinessException {

        MicroserviceUserDto userDto;
        try {
            userDto = userClient.findById(userId);
        } catch (Exception e) {
            log.error("Error consultando el usuario para la edición: " + e.getMessage());
            throw new BusinessException("No se ha encontrado el usuario");
        }

        MicroserviceRoleDto roleDto = userDto.getRoles().stream()
                .filter(r -> r.getId().equals(RoleBusiness.ROLE_ADMINISTRATOR)).findAny().orElse(null);
        if (!(roleDto instanceof MicroserviceRoleDto)) {
            throw new BusinessException("No cuenta con los permisos necesarios para editar el usuario");
        }

        return this.changeStatusUser(userId, status);
    }

    public MicroserviceUserDto changeStatusUserFromAdministrator(Long userId, Boolean status) throws BusinessException {

        MicroserviceUserDto userDto;
        try {
            userDto = userClient.findById(userId);
        } catch (Exception e) {
            log.error("Error consultando el usuario para la edición: " + e.getMessage());
            throw new BusinessException("No se ha encontrado el usuario");
        }

        MicroserviceRoleDto roleManagerDto = userDto.getRoles().stream()
                .filter(r -> r.getId().equals(RoleBusiness.ROLE_MANAGER)).findAny().orElse(null);

        MicroserviceRoleDto roleOperatorDto = userDto.getRoles().stream()
                .filter(r -> r.getId().equals(RoleBusiness.ROLE_OPERATOR)).findAny().orElse(null);

        MicroserviceRoleDto roleProviderDto = userDto.getRoles().stream()
                .filter(r -> r.getId().equals(RoleBusiness.ROLE_SUPPLY_SUPPLIER)).findAny().orElse(null);

        if (!(roleManagerDto instanceof MicroserviceRoleDto) && !(roleOperatorDto instanceof MicroserviceRoleDto)
                && !(roleProviderDto instanceof MicroserviceRoleDto)) {
            throw new BusinessException("No cuenta con los permisos necesarios para editar el usuario");
        }

        if (roleManagerDto != null) {

            List<MicroserviceManagerProfileDto> profiles = new ArrayList<>();

            try {
                profiles = managerClient.findProfilesByUser(userId);
            } catch (Exception e) {
                log.error("Error consultando los perfiles de un usuario gestor: " + e.getMessage());
            }

            MicroserviceManagerProfileDto profileDto = profiles.stream()
                    .filter(p -> p.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR)).findAny().orElse(null);

            if (!(profileDto instanceof MicroserviceManagerProfileDto)) {
                throw new BusinessException("No se puede editar usuarios gestores que no cuentan con el rol Director");
            }

        }

        if (roleProviderDto instanceof MicroserviceRoleDto) {

            List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> roles = new ArrayList<>();

            try {
                roles = providerClient.findRolesByUser(userId);
            } catch (Exception e) {
                log.error("Error consultando los roles de un usuario proveedor: " + e.getMessage());
            }

            com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto roleDto = roles.stream()
                    .filter(r -> r.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR_PROVIDER)).findAny().orElse(null);

            if (roleDto == null) {
                throw new BusinessException(
                        "No se puede editar usuarios proveedores que no cuentan con el rol Director");
            }

        }

        return this.changeStatusUser(userId, status);
    }

    public MicroserviceUserDto changeStatusUserFromManager(Long userId, Boolean status, Long managerCode)
            throws BusinessException {

        MicroserviceUserDto userDto;
        try {
            userDto = userClient.findById(userId);
        } catch (Exception e) {
            log.error("Error consultando el usuario para la edición: " + e.getMessage());
            throw new BusinessException("No se ha encontrado el usuario");
        }

        MicroserviceRoleDto roleManagerDto = userDto.getRoles().stream()
                .filter(r -> r.getId().equals(RoleBusiness.ROLE_MANAGER)).findAny().orElse(null);

        if (!(roleManagerDto instanceof MicroserviceRoleDto)) {
            throw new BusinessException("No se puede editar usuarios que no son gestores");
        }

        MicroserviceManagerDto managerDto;
        try {

            managerDto = managerClient.findByUserCode(userId);

        } catch (Exception e) {
            log.error("Error consultando gestor: " + e.getMessage());
            throw new BusinessException("No se ha podido modificar el usuario.");
        }

        if (!managerDto.getId().equals(managerCode)) {
            throw new BusinessException("El usuario que se desea editar no pertenece al gestor.");
        }

        return this.changeStatusUser(userId, status);
    }

    public MicroserviceUserDto changeStatusUserFromProvider(Long userId, Boolean status, Long providerCode)
            throws BusinessException {

        MicroserviceUserDto userDto;
        try {
            userDto = userClient.findById(userId);
        } catch (Exception e) {
            log.error("Error consultando el usuario para la edición: " + e.getMessage());
            throw new BusinessException("No se ha encontrado el usuario");
        }

        MicroserviceRoleDto roleProviderDto = userDto.getRoles().stream()
                .filter(r -> r.getId().equals(RoleBusiness.ROLE_SUPPLY_SUPPLIER)).findAny().orElse(null);

        if (!(roleProviderDto instanceof MicroserviceRoleDto)) {
            throw new BusinessException("No se puede editar usuarios que no son proveedores");
        }

        MicroserviceProviderDto providerDtoByUser = null;
        try {

            providerDtoByUser = providerClient.findByUserCode(userId);

        } catch (Exception e) {
            log.error("Error consultando proveedor por usuario: " + e.getMessage());
        }

        MicroserviceProviderDto providerDtoByAdmin = null;
        try {

            providerDtoByAdmin = providerClient.findProviderByAdministrator(userId);

        } catch (Exception e) {
            log.error("Error consultando proveedor por administrador: " + e.getMessage());
        }

        if (providerDtoByAdmin == null && providerDtoByUser == null) {
            throw new BusinessException("No se ha encontrado el usuario.");
        }

        MicroserviceProviderDto providerDto = (providerDtoByAdmin != null) ? providerDtoByAdmin : providerDtoByUser;

        if (!providerDto.getId().equals(providerCode)) {
            throw new BusinessException("El usuario que se desea editar no pertenece al proveedor.");
        }

        return this.changeStatusUser(userId, status);
    }

    public MicroserviceUserDto changeStatusUser(Long userId, Boolean status) throws BusinessException {

        MicroserviceUserDto userDto;

        try {

            if (status) {
                userDto = userClient.enableUser(userId);
            } else {
                userDto = userClient.disableUser(userId);
            }

            MicroserviceRoleDto roleManagerDto = userDto.getRoles().stream()
                    .filter(r -> r.getId().equals(RoleBusiness.ROLE_MANAGER)).findAny().orElse(null);

            MicroserviceRoleDto roleProviderDto = userDto.getRoles().stream()
                    .filter(r -> r.getId().equals(RoleBusiness.ROLE_SUPPLY_SUPPLIER)).findAny().orElse(null);

            if (roleManagerDto instanceof MicroserviceRoleDto) {
                List<MicroserviceManagerProfileDto> profiles = managerClient.findProfilesByUser(userDto.getId());
                userDto.setProfilesManager(profiles);
            } else if (roleProviderDto instanceof MicroserviceRoleDto) {

                List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> roles = providerClient
                        .findRolesByUser(userDto.getId());
                userDto.setRolesProvider(roles);

                List<MicroserviceProviderProfileDto> profiles = providerClient.findProfilesByUser(userDto.getId());
                userDto.setProfilesProvider(profiles);

            }

        } catch (BusinessException e) {
            throw new BusinessException(e.getMessage());
        }

        return userDto;
    }

    public List<MicroserviceUserDto> getUsersFromSuperAdmin() throws BusinessException {

        List<Long> roles = new ArrayList<>(Collections.singletonList(RoleBusiness.ROLE_ADMINISTRATOR));

        return this.getUsers(roles);
    }

    public List<MicroserviceUserDto> getUsersFromAdministrator() throws BusinessException {

        List<Long> roles = new ArrayList<>(Arrays.asList(RoleBusiness.ROLE_MANAGER, RoleBusiness.ROLE_SUPPLY_SUPPLIER,
                RoleBusiness.ROLE_OPERATOR));

        List<MicroserviceUserDto> users = this.getUsers(roles);

        List<MicroserviceUserDto> listUsersResponse = new ArrayList<>();

        for (MicroserviceUserDto userDto : users) {

            MicroserviceRoleDto roleManager = userDto.getRoles().stream()
                    .filter(r -> r.getId().equals(RoleBusiness.ROLE_MANAGER)).findAny().orElse(null);

            MicroserviceRoleDto roleProvider = userDto.getRoles().stream()
                    .filter(r -> r.getId().equals(RoleBusiness.ROLE_SUPPLY_SUPPLIER)).findAny().orElse(null);

            if (roleManager instanceof MicroserviceRoleDto) {

                List<MicroserviceManagerProfileDto> profiles = managerClient.findProfilesByUser(userDto.getId());

                MicroserviceManagerDto managerDto = managerClient.findByUserCode(userDto.getId());

                userDto.setProfilesManager(profiles);
                userDto.setEntity(managerDto);
                listUsersResponse.add(userDto);

            } else if (roleProvider instanceof MicroserviceRoleDto) {

                try {
                    List<com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto> profiles = providerClient
                            .findRolesByUser(userDto.getId());
                    userDto.setRolesProvider(profiles);

                    MicroserviceProviderDto providerDto = providerClient.findProviderByAdministrator(userDto.getId());
                    userDto.setEntity(providerDto);
                } catch (Exception e) {
                    log.error("Error consultando el proveedor de insumo por el código de usuario administrador: " + e.getMessage());
                }

                try {
                    List<MicroserviceProviderProfileDto> profiles = providerClient.findProfilesByUser(userDto.getId());
                    userDto.setProfilesProvider(profiles);

                    MicroserviceProviderDto providerDto = providerClient.findByUserCode(userDto.getId());
                    userDto.setEntity(providerDto);

                } catch (Exception e) {
                    log.error("Error consultando el proveedor de insumo por el código de usuario: " + e.getMessage());
                }

                listUsersResponse.add(userDto);

            } else {

                MicroserviceOperatorDto operatorDto = operatorClient.findByUserCode(userDto.getId());
                userDto.setEntity(operatorDto);

                listUsersResponse.add(userDto);
            }

        }

        return listUsersResponse;
    }

    public List<MicroserviceUserDto> getUsersFromManager(Long managerCode) throws BusinessException {

        List<MicroserviceManagerUserDto> usersManagerDto = new ArrayList<>();

        try {
            usersManagerDto = managerClient.findUsersByManager(managerCode, null);
        } catch (Exception e) {
            log.error("Error consultando usuarios de un gestor: " + e.getMessage());
        }

        List<MicroserviceUserDto> users = new ArrayList<>();

        for (MicroserviceManagerUserDto userManagerDto : usersManagerDto) {
            try {
                MicroserviceUserDto userDto = userClient.findById(userManagerDto.getUserCode());
                userDto.setProfilesManager(userManagerDto.getProfiles());
                users.add(userDto);
            } catch (Exception e) {
                log.error("Error consultando usuario: " + e.getMessage());
            }
        }

        return users;
    }

    public List<MicroserviceUserDto> getUsersFromProvider(Long providerCode) throws BusinessException {

        List<MicroserviceProviderUserDto> usersProviderDto = new ArrayList<>();

        try {
            usersProviderDto = providerClient.findUsersByProviderId(providerCode);
        } catch (Exception e) {
            log.error("Error consultando usuarios de un proveedor: " + e.getMessage());
        }

        List<MicroserviceUserDto> users = new ArrayList<>();

        for (MicroserviceProviderUserDto userProviderDto : usersProviderDto) {
            try {
                MicroserviceUserDto userDto = userClient.findById(userProviderDto.getUserCode());
                userDto.setProfilesProvider(userProviderDto.getProfiles());
                users.add(userDto);
            } catch (Exception e) {
                log.error("Error consultando usuario: " + e.getMessage());
            }
        }

        List<MicroserviceProviderAdministratorDto> adminsProviderDto = new ArrayList<>();

        try {
            adminsProviderDto = providerClient.findAdministratorsByProviderId(providerCode);
        } catch (Exception e) {
            log.error("Error consultando usuarios (administradores) de un proveedor: " + e.getMessage());
        }

        for (MicroserviceProviderAdministratorDto userProviderDto : adminsProviderDto) {

            com.ai.st.microservice.workspaces.dto.providers.MicroserviceRoleDto roleDirector = userProviderDto
                    .getRoles().stream().filter(r -> r.getId().equals(RoleBusiness.SUB_ROLE_DIRECTOR_PROVIDER))
                    .findAny().orElse(null);
            if (roleDirector == null) {
                try {
                    MicroserviceUserDto userDto = userClient.findById(userProviderDto.getUserCode());
                    userDto.setRolesProvider(userProviderDto.getRoles());
                    users.add(userDto);
                } catch (Exception e) {
                    log.error("Error consultando usuario: " + e.getMessage());
                }
            }

        }

        return users;
    }

    public List<MicroserviceUserDto> getUsers(List<Long> roles) throws BusinessException {
        List<MicroserviceUserDto> users;
        try {
            users = userClient.findUsersByRoles(roles);
        } catch (Exception e) {
            throw new BusinessException("Error consultando los usuarios: " + e.getMessage());
        }
        return users;
    }

    public MicroserviceUserDto addProfileToUserFromManager(Long userId, Long profileId, Long managerCode)
            throws BusinessException {

        MicroserviceUserDto userDto;

        MicroserviceManagerDto managerDto;
        try {
            managerDto = managerClient.findByUserCode(userId);
        } catch (Exception e) {
            log.error("Error consultando gestor por usuario: " + e.getMessage());
            throw new BusinessException("No se ha encontrado el gestor");
        }

        if (!managerDto.getId().equals(managerCode)) {
            throw new BusinessException("El usuario no pertenece al gestor.");
        }

        try {

            MicroserviceAddUserToManagerDto addUser = new MicroserviceAddUserToManagerDto();
            addUser.setUserCode(userId);
            addUser.setProfileId(profileId);
            addUser.setManagerId(managerCode);

            MicroserviceManagerUserDto managerUser = managerClient.addUserToManager(addUser);

            userDto = userClient.findById(userId);
            userDto.setProfilesManager(managerUser.getProfiles());

        } catch (Exception e) {
            log.error("Error agregando perfil a un usuario gestor: " + e.getMessage());
            throw new BusinessException("No se ha podido agregar el perfil al usuario.");
        }

        return userDto;
    }

    public MicroserviceUserDto addProfileToUserFromProvider(Long userId, Long profileId, Long providerCode)
            throws BusinessException {

        MicroserviceUserDto userDto;
        MicroserviceProviderDto providerDto;

        try {
            providerDto = providerClient.findByUserCode(userId);
        } catch (Exception e) {
            log.error("Error consultando proveedor por usuario: " + e.getMessage());
            throw new BusinessException("No se ha encontrado el proveedor");
        }

        if (!providerDto.getId().equals(providerCode)) {
            throw new BusinessException("El usuario no pertenece al proveedor.");
        }

        try {

            MicroserviceAddUserToProviderDto addUser = new MicroserviceAddUserToProviderDto();
            addUser.setUserCode(userId);
            addUser.setProfileId(profileId);
            addUser.setProviderId(providerCode);

            List<MicroserviceProviderUserDto> usersProvider = providerClient.addUserToProvide(addUser);

            userDto = userClient.findById(userId);

            usersProvider.stream().filter(u -> u.getUserCode().equals(userId))
                    .findAny().ifPresent(userFound -> userDto.setProfilesProvider(userFound.getProfiles()));

        } catch (Exception e) {
            log.error("Error agregando perfil a un usuario proveedor: " + e.getMessage());
            throw new BusinessException("No se ha podido agregar el perfil al usuario.");
        }

        return userDto;
    }

    public MicroserviceUserDto removeProfileToUserFromManager(Long userId, Long profileId, Long managerCode)
            throws BusinessException {

        MicroserviceUserDto userDto;
        MicroserviceManagerDto managerDto;

        try {
            managerDto = managerClient.findByUserCode(userId);
        } catch (Exception e) {
            log.error("Error consultando gestor por usuario: " + e.getMessage());
            throw new BusinessException("No se ha encontrado el gestor");
        }

        if (!managerDto.getId().equals(managerCode)) {
            throw new BusinessException("El usuario no pertenece al gestor.");
        }

        try {

            MicroserviceAddUserToManagerDto removeUser = new MicroserviceAddUserToManagerDto();
            removeUser.setUserCode(userId);
            removeUser.setProfileId(profileId);
            removeUser.setManagerId(managerCode);

            MicroserviceManagerUserDto managerUser = managerClient.removeUserToManager(removeUser);

            userDto = userClient.findById(userId);
            userDto.setProfilesManager(managerUser.getProfiles());

        } catch (BusinessException e) {
            log.error("Error quitando perfil a un usuario gestor: " + e.getMessage());
            throw new BusinessException(e.getMessage());
        }

        return userDto;
    }

    public MicroserviceUserDto removeProfileToUserFromProvider(Long userId, Long profileId, Long providerCode)
            throws BusinessException {

        MicroserviceUserDto userDto;
        MicroserviceProviderDto providerDto;

        try {
            providerDto = providerClient.findByUserCode(userId);
        } catch (Exception e) {
            log.error("Error consultando proveedor por usuario: " + e.getMessage());
            throw new BusinessException("No se ha encontrado el proveedor");
        }

        if (!providerDto.getId().equals(providerCode)) {
            throw new BusinessException("El usuario no pertenece al proveedor.");
        }

        try {

            MicroserviceAddUserToProviderDto removeUser = new MicroserviceAddUserToProviderDto();
            removeUser.setUserCode(userId);
            removeUser.setProfileId(profileId);
            removeUser.setProviderId(providerCode);

            List<MicroserviceProviderUserDto> usersProvider = providerClient.removeUserToProvider(removeUser);

            userDto = userClient.findById(userId);

            usersProvider.stream().filter(u -> u.getUserCode().equals(userId))
                    .findAny().ifPresent(userFound -> userDto.setProfilesProvider(userFound.getProfiles()));

        } catch (BusinessException e) {
            log.error("Error quitando el perfil a un usuario proveedor: " + e.getMessage());
            throw new BusinessException(e.getMessage());
        }

        return userDto;
    }

}
