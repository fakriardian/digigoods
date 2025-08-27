package com.example.digigoods.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.digigoods.model.Discount;
import com.example.digigoods.model.DiscountType;
import com.example.digigoods.repository.DiscountRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
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
 * Integration tests for DiscountController.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class DiscountControllerIntegrationTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Autowired
  private DiscountRepository discountRepository;

  private Discount discount1;
  private Discount discount2;

  @BeforeEach
  void setUp() {
    // Set up MockMvc
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    // Clear and set up test data
    discountRepository.deleteAll();

    // Create test discounts
    discount1 = new Discount();
    discount1.setCode("TEST20");
    discount1.setPercentage(new BigDecimal("20.00"));
    discount1.setType(DiscountType.GENERAL);
    discount1.setValidFrom(LocalDate.now().minusDays(1));
    discount1.setValidUntil(LocalDate.now().plusDays(30));
    discount1.setRemainingUses(10);
    discount1.setApplicableProducts(new HashSet<>());
    discount1 = discountRepository.save(discount1);

    discount2 = new Discount();
    discount2.setCode("PRODUCT15");
    discount2.setPercentage(new BigDecimal("15.00"));
    discount2.setType(DiscountType.PRODUCT_SPECIFIC);
    discount2.setValidFrom(LocalDate.now().minusDays(5));
    discount2.setValidUntil(LocalDate.now().plusDays(60));
    discount2.setRemainingUses(5);
    discount2.setApplicableProducts(new HashSet<>());
    discount2 = discountRepository.save(discount2);
  }

  @Test
  @DisplayName("Given discounts in database, when getting all discounts, then return list")
  void givenDiscountsInDatabase_whenGettingAllDiscounts_thenReturnDiscountsList() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/discounts")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(discount1.getId()))
        .andExpect(jsonPath("$[0].code").value("TEST20"))
        .andExpect(jsonPath("$[0].percentage").value(20.00))
        .andExpect(jsonPath("$[0].type").value("GENERAL"))
        .andExpect(jsonPath("$[0].remainingUses").value(10))
        .andExpect(jsonPath("$[1].id").value(discount2.getId()))
        .andExpect(jsonPath("$[1].code").value("PRODUCT15"))
        .andExpect(jsonPath("$[1].percentage").value(15.00))
        .andExpect(jsonPath("$[1].type").value("PRODUCT_SPECIFIC"))
        .andExpect(jsonPath("$[1].remainingUses").value(5));
  }

  @Test
  @DisplayName("Given no discounts in database, when getting all discounts, then return empty list")
  void givenNoDiscountsInDatabase_whenGettingAllDiscounts_thenReturnEmptyList() throws Exception {
    // Arrange
    discountRepository.deleteAll();

    // Act & Assert
    mockMvc.perform(get("/discounts")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @DisplayName("Given discounts endpoint, when making GET request, then return valid response")
  void givenDiscountsEndpoint_whenMakingGetRequest_thenReturnValidResponse() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/discounts")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }
}
