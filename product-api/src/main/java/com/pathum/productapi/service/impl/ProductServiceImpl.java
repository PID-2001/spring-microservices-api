package com.pathum.productapi.service.impl;

import com.pathum.productapi.dto.request.ProductRequestDto;
import com.pathum.productapi.dto.response.ProductResponseDto;
import com.pathum.productapi.entity.Product;
import com.pathum.productapi.exception.ProductNotFoundException;
import com.pathum.productapi.mapper.ProductMapper;
import com.pathum.productapi.repository.ProductRepository;
import com.pathum.productapi.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        log.info("Creating new product with name: {}", requestDto.getName());

        if (productRepository.existsByName(requestDto.getName())) {
            throw new IllegalArgumentException(
                    "Product with name '" + requestDto.getName() + "' already exists"
            );
        }

        Product product = productMapper.toEntity(requestDto);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with id: {}", savedProduct.getId());
        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        log.info("Fetching product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with id: " + id
                ));

        return productMapper.toResponseDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        log.info("Fetching all products");

        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> searchProductsByName(String name) {
        log.info("Searching products with name containing: {}", name);

        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(productMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto) {
        log.info("Updating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with id: " + id
                ));

        productMapper.updateEntityFromDto(requestDto, product);
        Product updatedProduct = productRepository.save(product);

        log.info("Product updated successfully with id: {}", id);
        return productMapper.toResponseDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with id: {}", id);
    }
}