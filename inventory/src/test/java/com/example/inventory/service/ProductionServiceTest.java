package com.example.inventory.service;

import com.example.inventory.dto.ProductionSuggestionResponseDto;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.ProductRawMaterial;
import com.example.inventory.entity.RawMaterial;
import com.example.inventory.repository.ProductRawMaterialRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.RawMaterialRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductionServiceTest {

  @Test
  void shouldPrioritizeHigherValueAndConsumeSharedStock() {
    // Products: A (100), B (60)
    Product a = new Product();
    a.id = 1L; a.code = "P1"; a.name = "A"; a.value = new BigDecimal("100.00");

    Product b = new Product();
    b.id = 2L; b.code = "P2"; b.name = "B"; b.value = new BigDecimal("60.00");

    // Raw material: Steel stock = 3
    RawMaterial steel = new RawMaterial();
    steel.id = 10L; steel.code = "RM1"; steel.name = "Steel"; steel.stockQuantity = new BigDecimal("3");

    // BOM:
    // A requires 2 steel
    ProductRawMaterial aSteel = new ProductRawMaterial();
    aSteel.id = 100L; aSteel.product = a; aSteel.rawMaterial = steel; aSteel.requiredQuantity = new BigDecimal("2");

    // B requires 1 steel
    ProductRawMaterial bSteel = new ProductRawMaterial();
    bSteel.id = 101L; bSteel.product = b; bSteel.rawMaterial = steel; bSteel.requiredQuantity = new BigDecimal("1");

    ProductRepository productRepo = mock(ProductRepository.class);
    RawMaterialRepository rawRepo = mock(RawMaterialRepository.class);
    ProductRawMaterialRepository linkRepo = mock(ProductRawMaterialRepository.class);

    when(productRepo.findAllByOrderByValueDescIdAsc()).thenReturn(List.of(a, b)); // already sorted
    when(rawRepo.findAll()).thenReturn(List.of(steel));
    when(linkRepo.findAll()).thenReturn(List.of(aSteel, bSteel));

    ProductionService service = new ProductionService(productRepo, rawRepo, linkRepo);

    ProductionSuggestionResponseDto resp = service.suggestProduction();

    assertNotNull(resp);
    assertNotNull(resp.items);
    assertEquals(2, resp.items.size());

    // Greedy result:
    // Stock 3 steel
    // A first: 3/2 = 1 unit (uses 2) remaining 1
    // B next: 1/1 = 1 unit
    assertEquals(1L, resp.items.get(0).productId);
    assertEquals(1, resp.items.get(0).quantityToProduce);
    assertEquals(new BigDecimal("100.00"), resp.items.get(0).unitValue);
    assertEquals(new BigDecimal("100.00"), resp.items.get(0).totalValue);

    assertEquals(2L, resp.items.get(1).productId);
    assertEquals(1, resp.items.get(1).quantityToProduce);
    assertEquals(new BigDecimal("60.00"), resp.items.get(1).unitValue);
    assertEquals(new BigDecimal("60.00"), resp.items.get(1).totalValue);

    assertEquals(new BigDecimal("160.00"), resp.totalValue);
  }

  @Test
  void shouldReturnEmptyWhenProductHasNoBom() {
    Product a = new Product();
    a.id = 1L; a.name = "A"; a.value = new BigDecimal("100.00");

    RawMaterial steel = new RawMaterial();
    steel.id = 10L; steel.name = "Steel"; steel.stockQuantity = new BigDecimal("100");

    ProductRepository productRepo = mock(ProductRepository.class);
    RawMaterialRepository rawRepo = mock(RawMaterialRepository.class);
    ProductRawMaterialRepository linkRepo = mock(ProductRawMaterialRepository.class);

    when(productRepo.findAllByOrderByValueDescIdAsc()).thenReturn(List.of(a));
    when(rawRepo.findAll()).thenReturn(List.of(steel));
    when(linkRepo.findAll()).thenReturn(List.of()); // no BOM lines

    ProductionService service = new ProductionService(productRepo, rawRepo, linkRepo);

    ProductionSuggestionResponseDto resp = service.suggestProduction();

    assertNotNull(resp);
    assertNotNull(resp.items);
    assertTrue(resp.items.isEmpty());
    assertEquals(BigDecimal.ZERO, resp.totalValue);
  }

  @Test
  void shouldUseMinRatioWhenProductNeedsMultipleMaterials() {
    Product a = new Product();
    a.id = 1L; a.name = "A"; a.value = new BigDecimal("50.00");

    RawMaterial steel = new RawMaterial();
    steel.id = 10L; steel.name = "Steel"; steel.stockQuantity = new BigDecimal("10");

    RawMaterial plastic = new RawMaterial();
    plastic.id = 11L; plastic.name = "Plastic"; plastic.stockQuantity = new BigDecimal("9");

    // A needs 2 steel and 5 plastic => possible steel: 10/2=5, plastic: 9/5=1 => min=1
    ProductRawMaterial aSteel = new ProductRawMaterial();
    aSteel.id = 100L; aSteel.product = a; aSteel.rawMaterial = steel; aSteel.requiredQuantity = new BigDecimal("2");

    ProductRawMaterial aPlastic = new ProductRawMaterial();
    aPlastic.id = 101L; aPlastic.product = a; aPlastic.rawMaterial = plastic; aPlastic.requiredQuantity = new BigDecimal("5");

    ProductRepository productRepo = mock(ProductRepository.class);
    RawMaterialRepository rawRepo = mock(RawMaterialRepository.class);
    ProductRawMaterialRepository linkRepo = mock(ProductRawMaterialRepository.class);

    when(productRepo.findAllByOrderByValueDescIdAsc()).thenReturn(List.of(a));
    when(rawRepo.findAll()).thenReturn(List.of(steel, plastic));
    when(linkRepo.findAll()).thenReturn(List.of(aSteel, aPlastic));

    ProductionService service = new ProductionService(productRepo, rawRepo, linkRepo);

    ProductionSuggestionResponseDto resp = service.suggestProduction();

    assertEquals(1, resp.items.size());
    assertEquals(1, resp.items.get(0).quantityToProduce);
    assertEquals(new BigDecimal("50.00"), resp.items.get(0).totalValue);
    assertEquals(new BigDecimal("50.00"), resp.totalValue);
  }
}
