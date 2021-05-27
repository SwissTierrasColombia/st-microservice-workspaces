package com.ai.st.microservice.workspaces.clients;

import com.ai.st.microservice.common.dto.providers.*;
import com.ai.st.microservice.common.exceptions.BusinessException;

import com.ai.st.microservice.workspaces.dto.providers.WorkspacePetitionDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestPaginatedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceSupplyRevisionDto;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "st-microservice-providers", configuration = com.ai.st.microservice.common.clients.ProviderFeignClient.Configuration.class)
public interface ProviderFeignClient {

    @PostMapping(value = "/api/providers-supplies/v1/requests", consumes = APPLICATION_JSON_VALUE)
    MicroserviceRequestDto createRequest(
            @RequestBody MicroserviceCreateRequestDto request)
            throws BusinessException;

    @GetMapping("/api/providers-supplies/v1/users/{userCode}/providers")
    MicroserviceProviderDto findByUserCode(
            @PathVariable(name = "userCode") Long userCode);

    @GetMapping("/api/providers-supplies/v1/providers/{providerId}")
    MicroserviceProviderDto findById(
            @PathVariable(name = "providerId") Long providerId);

    @GetMapping("/api/providers-supplies/v1/providers/{providerId}/requests")
    List<MicroserviceRequestDto> getRequestsByProvider(
            @PathVariable(name = "providerId") Long providerId,
            @RequestParam(required = false, name = "state") Long requestStateId);

    @GetMapping("/api/providers-supplies/v1/providers/{providerId}/requests/closed")
    List<MicroserviceRequestDto> getRequestsByProviderClosed(
            @PathVariable(name = "providerId") Long providerId,
            @RequestParam(required = false, name = "user") Long userCode);

    @GetMapping("/api/providers-supplies/v1/requests/{requestId}")
    MicroserviceRequestDto findRequestById(
            @PathVariable(name = "requestId") Long requestId);

    @GetMapping("/api/providers-supplies/v1/providers/{providerId}/users")
    List<MicroserviceProviderUserDto> findUsersByProviderId(
            @PathVariable(name = "providerId") Long providerId)
            throws BusinessException;

    @PutMapping(value = "/api/providers-supplies/v1/requests/{requestId}/supplies/{supplyRequestedId}", consumes = APPLICATION_JSON_VALUE)
    MicroserviceRequestDto updateSupplyRequested(
            @PathVariable(name = "requestId") Long requestId,
            @PathVariable(name = "supplyRequestedId") Long supplyRequestedId,
            @RequestBody MicroserviceUpdateSupplyRequestedDto updateSupply);

    @PutMapping("/api/providers-supplies/v1/requests/{requestId}/delivered")
    MicroserviceRequestDto closeRequest(
            @PathVariable(name = "requestId") Long requestId,
            @RequestParam(name = "closed_by") Long userCode);

    @PostMapping(value = "/api/providers-supplies/v1/users", consumes = APPLICATION_JSON_VALUE)
    List<MicroserviceProviderUserDto> addUserToProvide(
            @RequestBody MicroserviceAddUserToProviderDto data);

    @GetMapping("/api/providers-supplies/v1/types-supplies/{typeSupplyId}")
    MicroserviceTypeSupplyDto findTypeSuppleById(
            @PathVariable(name = "typeSupplyId") Long typeSupplyId);

    @GetMapping("/api/providers-supplies/v1/providers/{providerId}/users")
    List<MicroserviceProviderUserDto> findUsersByProviderIdAndProfiles(
            @PathVariable(name = "providerId") Long providerId,
            @RequestParam(name = "profiles", required = false) List<Long> profiles) throws BusinessException;

    @GetMapping("/api/providers-supplies/v1/requests/emmiters")
    List<MicroserviceRequestDto> findRequestsByEmmiters(
            @RequestParam(name = "emmiter_code") Long emmiterCode,
            @RequestParam(name = "emmiter_type") String emmiterType);

    @PostMapping(value = "/api/providers-supplies/v1/administrators", consumes = APPLICATION_JSON_VALUE)
    List<MicroserviceProviderAdministratorDto> addAdministratorToProvide(
            @RequestBody MicroserviceAddAdministratorToProviderDto data);

    @GetMapping("/api/providers-supplies/v1/providers/{providerId}/administrators")
    List<MicroserviceProviderAdministratorDto> findAdministratorsByProviderId(
            @PathVariable(name = "providerId") Long providerId)
            throws BusinessException;

    @GetMapping("/api/providers-supplies/v1/administrators/{userCode}/roles")
    List<MicroserviceProviderRoleDto> findRolesByUser(
            @PathVariable(name = "userCode") Long userCode);

    @GetMapping("/api/providers-supplies/v1/administrators/{userCode}/providers")
    MicroserviceProviderDto findProviderByAdministrator(
            @PathVariable(name = "userCode") Long userCode);

    @DeleteMapping(value = "/api/providers-supplies/v1/users", consumes = APPLICATION_JSON_VALUE)
    List<MicroserviceProviderUserDto> removeUserToProvider(
            @RequestBody MicroserviceAddUserToProviderDto data)
            throws BusinessException;

    @PostMapping(value = "/api/providers-supplies/v1/providers/{providerId}/profiles", consumes = APPLICATION_JSON_VALUE)
    MicroserviceProviderProfileDto createProfile(
            @PathVariable(name = "providerId") Long providerId,
            @RequestBody MicroserviceCreateProviderProfileDto createProviderProfileDto)
            throws BusinessException;

    @GetMapping("/api/providers-supplies/v1/providers/{providerId}/profiles")
    List<MicroserviceProviderProfileDto> getProfilesByProvider(
            @PathVariable(name = "providerId") Long providerId)
            throws BusinessException;

    @PutMapping(value = "/api/providers-supplies/v1/providers/{providerId}/profiles/{profileId}", consumes = APPLICATION_JSON_VALUE)
    MicroserviceProviderProfileDto updateProfile(
            @PathVariable(name = "providerId") Long providerId,
            @PathVariable(name = "profileId") Long profileId,
            @RequestBody MicroserviceCreateProviderProfileDto createProviderProfileDto)
            throws BusinessException;

    @DeleteMapping(value = "/api/providers-supplies/v1/providers/{providerId}/profiles/{profileId}", consumes = APPLICATION_JSON_VALUE)
    void deleteProfile(
            @PathVariable(name = "providerId") Long providerId,
            @PathVariable(name = "profileId") Long profileId)
            throws BusinessException;

    @PostMapping(value = "/api/providers-supplies/v1/providers/{providerId}/type-supplies", consumes = APPLICATION_JSON_VALUE)
    MicroserviceTypeSupplyDto createTypeSupplies(
            @PathVariable(name = "providerId") Long providerId,
            @RequestBody MicroserviceCreateTypeSupplyDto createTypeSupplyDto)
            throws BusinessException;

    @GetMapping("/api/providers-supplies/v1/providers/{providerId}/types-supplies")
    List<MicroserviceTypeSupplyDto> getTypesSuppliesByProvider(
            @PathVariable(name = "providerId") Long providerId)
            throws BusinessException;

    @PutMapping(value = "/api/providers-supplies/v1/providers/{providerId}/type-supplies/{typeSupplyId}", consumes = APPLICATION_JSON_VALUE)
    MicroserviceTypeSupplyDto updateTypeSupplies(
            @PathVariable(name = "providerId") Long providerId,
            @PathVariable(name = "typeSupplyId") Long typeSupplyId,
            @RequestBody MicroserviceCreateTypeSupplyDto data)
            throws BusinessException;

    @DeleteMapping(value = "/api/providers-supplies/v1/providers/{providerId}/type-supplies/{typeSupplyId}", consumes = APPLICATION_JSON_VALUE)
    void deleteTypeSupply(
            @PathVariable(name = "providerId") Long providerId,
            @PathVariable(name = "typeSupplyId") Long typeSupplyId)
            throws BusinessException;

    @GetMapping("/api/providers-supplies/v1/users/{userCode}/profiles")
    List<MicroserviceProviderProfileDto> findProfilesByUser(
            @PathVariable(name = "userCode") Long userCode);

    @GetMapping("/api/providers-supplies/v1/requests/search-manager-municipality")
    MicroserviceRequestPaginatedDto getRequestsByManagerAndMunicipality(
            @RequestParam(name = "manager") Long managerCode,
            @RequestParam(name = "municipality") String municipalityCode,
            @RequestParam(name = "page") Integer page)
            throws BusinessException;

    @GetMapping("/api/providers-supplies/v1/requests/search-manager-provider")
    MicroserviceRequestPaginatedDto getRequestsByManagerAndProvider(
            @RequestParam(name = "manager") Long managerCode,
            @RequestParam(name = "provider") Long providerId,
            @RequestParam(name = "page") Integer page)
            throws BusinessException;

    @GetMapping("/api/providers-supplies/v1/requests/search-manager-package")
    List<MicroserviceRequestDto> getRequestsByManagerAndPackage(
            @RequestParam(name = "manager") Long managerCode,
            @RequestParam(name = "package_label") String packageLabel)
            throws BusinessException;

    @GetMapping("/api/providers-supplies/v1/requests/search-package")
    List<MicroserviceRequestDto> getRequestsByPackage(
            @RequestParam(name = "package_label") String packageLabel)
            throws BusinessException;

    @GetMapping("/api/providers-supplies/v1/providers/{providerId}/supplies-requested")
    List<MicroserviceSupplyRequestedDto> getSuppliesRequestedToReview(
            @PathVariable(name = "providerId") Long providerId,
            @RequestParam(name = "states") List<Long> states)
            throws BusinessException;

    @GetMapping("/api/providers-supplies/v1/supplies-requested/{supplyRequestedId}")
    MicroserviceSupplyRequestedDto getSupplyRequested(
            @PathVariable(name = "supplyRequestedId") Long supplyRequestedId);

    @PostMapping(value = "/api/providers-supplies/v1/supplies-requested/{supplyRequestedId}/revision", consumes = APPLICATION_JSON_VALUE)
    MicroserviceSupplyRevisionDto createSupplyRevision(
            @PathVariable(name = "supplyRequestedId") Long supplyRequestedId,
            @RequestBody MicroserviceCreateSupplyRevisionDto createRevisionDto);

    @DeleteMapping(value = "/api/providers-supplies/v1/supplies-requested/{supplyRequestedId}/revision/{supplyRevisionId}", consumes = APPLICATION_JSON_VALUE)
    void deleteSupplyRevision(
            @PathVariable(name = "supplyRequestedId") Long supplyRequestedId,
            @PathVariable(name = "supplyRevisionId") Long supplyRevisionId);

    @GetMapping("/api/providers-supplies/v1/supplies-requested/{supplyRequestedId}/revision")
    MicroserviceSupplyRevisionDto getSupplyRevisionFromSupplyRequested(
            @PathVariable(name = "supplyRequestedId") Long supplyRequestedId);

    @PutMapping(value = "/api/providers-supplies/v1/supplies-requested/{supplyRequestedId}/revision/{revisionId}", consumes = APPLICATION_JSON_VALUE)
    MicroserviceSupplyRevisionDto updateSupplyRevision(
            @PathVariable(name = "supplyRequestedId") Long supplyRequestedId,
            @PathVariable(name = "revisionId") Long revisionId,
            @RequestBody MicroserviceUpdateSupplyRevisionDto createRevisionDto);

    // Petitions Module
    @PostMapping(value = "/api/providers-supplies/v1/providers/{providerId}/petitions", consumes = APPLICATION_JSON_VALUE)
    WorkspacePetitionDto createPetition(
            @PathVariable(name = "providerId") Long providerId,
            @RequestBody MicroserviceCreatePetitionDto createPetitionDto);

    @GetMapping("/api/providers-supplies/v1/providers/{providerId}/petitions-manager/{managerId}")
    List<WorkspacePetitionDto> getPetitionsForManager(
            @PathVariable(name = "providerId") Long providerId,
            @PathVariable(name = "managerId") Long managerId);

    @GetMapping("/api/providers-supplies/v1/providers/petitions-manager/{managerId}")
    List<WorkspacePetitionDto> getPetitionsByManager(
            @PathVariable(name = "managerId") Long managerId);

    @GetMapping("/api/providers-supplies/v1/providers/{providerId}/petitions")
    List<WorkspacePetitionDto> getPetitionsForProvider(
            @PathVariable(name = "providerId") Long providerId,
            @RequestParam(name = "states") List<Long> states);

    @PutMapping(value = "/api/providers-supplies/v1/providers/{providerId}/petitions/{petitionId}", consumes = APPLICATION_JSON_VALUE)
    WorkspacePetitionDto updatePetition(
            @PathVariable(name = "providerId") Long providerId,
            @PathVariable(name = "petitionId") Long petitionId,
            @RequestBody MicroserviceUpdatePetitionDto updatePetitionDto)
            throws BusinessException;

    // Supplies Module

    @PutMapping(value = "/api/providers-supplies/v1/types-supplies/{typeSupplyId}/enable", consumes = APPLICATION_JSON_VALUE)
    MicroserviceTypeSupplyDto enableTypeSupply(
            @PathVariable(name = "typeSupplyId") Long typeSupplyId)
            throws BusinessException;

    @PutMapping(value = "/api/providers-supplies/v1/types-supplies/{typeSupplyId}/disable", consumes = APPLICATION_JSON_VALUE)
    MicroserviceTypeSupplyDto disableTypeSupply(
            @PathVariable(name = "typeSupplyId") Long typeSupplyId)
            throws BusinessException;

}
