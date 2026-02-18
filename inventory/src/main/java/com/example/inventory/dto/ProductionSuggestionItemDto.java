package com.example.inventory.dto;

import java.math.BigDecimal;

public class ProductionSuggestionItemDto {
  public Long productId;
  public String productCode;
  public String productName;
  public BigDecimal unitValue;
  public Integer quantityToProduce;
  public BigDecimal totalValue;
}
