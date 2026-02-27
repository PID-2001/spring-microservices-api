package com.pathum.orderapi.client;

import com.pathum.orderapi.dto.response.ProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-api")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductResponseDto getProductById(@PathVariable Long id);
}