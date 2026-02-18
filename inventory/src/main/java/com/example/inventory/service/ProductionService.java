package com.example.inventory.service;

import com.example.inventory.dto.*;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.RawMaterial;
import com.example.inventory.entity.ProductRawMaterial;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.RawMaterialRepository;
import com.example.inventory.repository.ProductRawMaterialRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductionService {

  private final ProductRepository productRepo;
  private final RawMaterialRepository rawRepo;
  private final ProductRawMaterialRepository linkRepo;

  public ProductionService(ProductRepository productRepo,
                           RawMaterialRepository rawRepo,
                           ProductRawMaterialRepository linkRepo) {
    this.productRepo = productRepo;
    this.rawRepo = rawRepo;
    this.linkRepo = linkRepo;
  }

  public ProductionSuggestionResponseDto suggestProduction() {

    // 1) Products ordered by highest value first
    List<Product> productsSorted = productRepo.findAllByOrderByValueDescIdAsc()
        .stream()
        .filter(p -> p.value != null)
        .toList();

    // 2) Virtual stock (BigDecimal)
    Map<Long, BigDecimal> virtualStock = new HashMap<>();
    for (RawMaterial rm : rawRepo.findAll()) {
      virtualStock.put(rm.id, rm.stockQuantity == null ? BigDecimal.ZERO : rm.stockQuantity);
    }

    // 3) Group links by product
    Map<Long, List<ProductRawMaterial>> linksByProduct = linkRepo.findAll()
        .stream()
        .collect(Collectors.groupingBy(l -> l.product.id));

    List<ProductionSuggestionItemDto> items = new ArrayList<>();

    for (Product product : productsSorted) {
      List<ProductRawMaterial> reqs = linksByProduct.getOrDefault(product.id, List.of());

      if (reqs.isEmpty()) continue;

      long maxUnits = Long.MAX_VALUE;

      for (ProductRawMaterial req : reqs) {
        if (req.requiredQuantity == null || req.requiredQuantity.compareTo(BigDecimal.ZERO) <= 0) {
          maxUnits = 0;
          break;
        }

        BigDecimal available = virtualStock.getOrDefault(req.rawMaterial.id, BigDecimal.ZERO);

        long possible = available
            .divide(req.requiredQuantity, 0, RoundingMode.FLOOR)
            .longValue();

        maxUnits = Math.min(maxUnits, possible);
      }

      if (maxUnits <= 0 || maxUnits == Long.MAX_VALUE) continue;

      // consume virtual stock
      for (ProductRawMaterial req : reqs) {
        BigDecimal available = virtualStock.getOrDefault(req.rawMaterial.id, BigDecimal.ZERO);
        BigDecimal used = req.requiredQuantity.multiply(BigDecimal.valueOf(maxUnits));
        virtualStock.put(req.rawMaterial.id, available.subtract(used));
      }

      // response item
      ProductionSuggestionItemDto item = new ProductionSuggestionItemDto();
      item.productId = product.id;
      item.productCode = product.code;
      item.productName = product.name;
      item.unitValue = product.value;
      item.quantityToProduce = (int) maxUnits;
      item.totalValue = product.value.multiply(BigDecimal.valueOf(maxUnits));

      items.add(item);
    }

    BigDecimal total = items.stream()
        .map(i -> i.totalValue)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    ProductionSuggestionResponseDto resp = new ProductionSuggestionResponseDto();
    resp.items = items;
    resp.totalValue = total;

    return resp;
  }
}
