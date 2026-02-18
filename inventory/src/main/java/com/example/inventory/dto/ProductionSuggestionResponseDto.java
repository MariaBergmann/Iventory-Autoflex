package com.example.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductionSuggestionResponseDto {
  public List<ProductionSuggestionItemDto> items;
  public BigDecimal totalValue;
}
