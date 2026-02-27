package com.pathum.productapi.service;

import com.pathum.productapi.dto.request.ProductRequestDto;
import com.pathum.productapi.dto.response.ProductResponseDto;
import java.util.List;

public interface ProductService {

    ProductResponseDto createProduct(ProductRequestDto requestDto);

    ProductResponseDto getProductById(Long id);

    List<ProductResponseDto> getAllProducts();

    List<ProductResponseDto> searchProductsByName(String name);

    ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto);

    void deleteProduct(Long id);
}