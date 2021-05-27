package com.ai.st.microservice.workspaces.clients;

import com.ai.st.microservice.common.dto.supplies.MicroserviceCreateSupplyDto;
import com.ai.st.microservice.common.dto.supplies.MicroserviceUpdateSupplyDto;
import com.ai.st.microservice.common.exceptions.BusinessException;

import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceDataPaginatedDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "st-microservice-supplies", configuration = com.ai.st.microservice.common.clients.SupplyFeignClient.Configuration.class)
public interface SupplyFeignClient {

    @PostMapping(value = "/api/supplies/v1/supplies", consumes = APPLICATION_JSON_VALUE)
    MicroserviceSupplyDto createSupply(
            @RequestBody MicroserviceCreateSupplyDto createSupply);

    @GetMapping("/api/supplies/v1/supplies/municipality/{municipalityId}")
    List<MicroserviceSupplyDto> getSuppliesByMunicipalityCode(
            @PathVariable(name = "municipalityId") String municipalityId,
            @RequestParam(name = "states", required = false) List<Long> states);

    @GetMapping("/api/supplies/v1/supplies/municipality/{municipalityId}")
    MicroserviceDataPaginatedDto getSuppliesByMunicipalityCodeByFilters(
            @PathVariable(name = "municipalityId") String municipalityId,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "manager", required = false) Long managerCode,
            @RequestParam(name = "requests", required = false) List<Long> requests,
            @RequestParam(name = "states", required = false) List<Long> states);

    @GetMapping("/api/supplies/v1/supplies/{supplyId}")
    MicroserviceSupplyDto findSupplyById(
            @PathVariable(name = "supplyId") Long supplyId);

    @DeleteMapping("/api/supplies/v1/supplies/{supplyId}")
    void deleteSupplyById(
            @PathVariable(name = "supplyId") Long supplyId);

    @PutMapping(value = "/api/supplies/v1/supplies/{supplyId}", consumes = APPLICATION_JSON_VALUE)
    MicroserviceSupplyDto updateSupply(
            @PathVariable(name = "supplyId") Long supplyId,
            @RequestBody MicroserviceUpdateSupplyDto updateSupply)
            throws BusinessException;

}
