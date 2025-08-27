package com.example.digigoods.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.digigoods.model.Product;
import com.example.digigoods.repository.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration tests for ProductController.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerIntegrationTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Autowired
  private ProductRepository productRepository;

  private Product product1;
  private Product product2;

  @BeforeEach
  void setUp() {
    // Set up MockMvc
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    // Clear and set up test data
    productRepository.deleteAll();

    // Create test products
    product1 = new Product();
    product1.setName("Test Product 1");
    product1.setPrice(new BigDecimal("100.00"));
    product1.setStock(10);
    product1 = productRepository.save(product1);

    product2 = new Product();
    product2.setName("Test Product 2");
    product2.setPrice(new BigDecimal("50.00"));
    product2.setStock(5);
    product2 = productRepository.save(product2);
  }

  @Test
  @DisplayName("Given products in database, when getting all products, then return products list")
  void givenProductsInDatabase_whenGettingAllProducts_thenReturnProductsList() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/products")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(product1.getId()))
        .andExpect(jsonPath("$[0].name").value("Test Product 1"))
        .andExpect(jsonPath("$[0].price").value(100.00))
        .andExpect(jsonPath("$[0].stock").value(10))
        .andExpect(jsonPath("$[1].id").value(product2.getId()))
        .andExpect(jsonPath("$[1].name").value("Test Product 2"))
        .andExpect(jsonPath("$[1].price").value(50.00))
        .andExpect(jsonPath("$[1].stock").value(5));
  }

  @Test
  @DisplayName("Given no products in database, when getting all products, then return empty list")
  void givenNoProductsInDatabase_whenGettingAllProducts_thenReturnEmptyList() throws Exception {
    // Arrange
    productRepository.deleteAll();

    // Act & Assert
    mockMvc.perform(get("/products")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
