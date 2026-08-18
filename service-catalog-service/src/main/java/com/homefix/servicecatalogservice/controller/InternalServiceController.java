package com.homefix.servicecatalogservice.controller;

import com.homefix.servicecatalogservice.dto.ServiceItemResponse;
import com.homefix.servicecatalogservice.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalServiceController {

    private final CatalogService catalogService;

    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceItemResponse> getServiceById(@PathVariable Long id) {
        ServiceItemResponse response = catalogService.getServiceById(id);
        return ResponseEntity.ok(response);
    }
}
