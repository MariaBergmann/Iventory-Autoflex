package com.example.inventory.service;

import com.example.inventory.dto.*;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.RawMaterial;
import com.example.inventory.entity.ProductRawMaterial;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.RawMaterialRepository;
import com.example.inventory.repository.ProductRawMaterialRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryService {

  private final ProductRepository productRepo;
  private final RawMaterialRepository rawRepo;
  private final ProductRawMaterialRepository linkRepo;

  public InventoryService(ProductRepository productRepo,
                          RawMaterialRepository rawRepo,
                          ProductRawMaterialRepository linkRepo) {
    this.productRepo = productRepo;
    this.rawRepo = rawRepo;
    this.linkRepo = linkRepo;
  }

  // ---------------- PRODUCTS (RF001)
  public List<ProductDto> listProducts() {
    return productRepo.findAll().stream().map(this::toDto).toList();
  }

  public ProductDto createProduct(ProductDto p) {
    validateProduct(p);

    if (p.code != null && !p.code.isBlank()) {
      productRepo.findByCode(p.code).ifPresent(x -> {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Product code already exists");
      });
    }

    Product e = new Product();
    e.code = blankToNull(p.code);
    e.name = p.name.trim();
    e.value = p.value;

    e = productRepo.save(e);
    return toDto(e);
  }

  public ProductDto updateProduct(Long id, ProductDto p) {
    Product e = productRepo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

    validateProduct(p);

    // code uniqueness
    String newCode = blankToNull(p.code);
    if (newCode != null) {
      productRepo.findByCode(newCode).ifPresent(other -> {
        if (!other.id.equals(id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Product code already exists");
      });
    }

    e.code = newCode;
    e.name = p.name.trim();
    e.value = p.value;

    return toDto(productRepo.save(e));
  }

  public void deleteProduct(Long id) {
    if (!productRepo.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
    }
    productRepo.deleteById(id);
  }

  // ---------------- RAW MATERIALS (RF002)
  public List<RawMaterialDto> listRawMaterials() {
    return rawRepo.findAll().stream().map(this::toDto).toList();
  }

  public RawMaterialDto createRawMaterial(RawMaterialDto r) {
    validateRawMaterial(r);

    if (r.name != null) {
      rawRepo.findByName(r.name.trim()).ifPresent(x -> {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Raw material name already exists");
      });
    }

    if (r.code != null && !r.code.isBlank()) {
      rawRepo.findByCode(r.code).ifPresent(x -> {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Raw material code already exists");
      });
    }

    RawMaterial e = new RawMaterial();
    e.code = blankToNull(r.code);
    e.name = r.name.trim();
    e.stockQuantity = BigDecimal.valueOf(r.stockQuantity == null ? 0 : r.stockQuantity);

    e = rawRepo.save(e);
    return toDto(e);
  }

  public RawMaterialDto updateRawMaterial(Long id, RawMaterialDto r) {
    RawMaterial e = rawRepo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Raw material not found"));

    validateRawMaterial(r);

    String newName = r.name.trim();
    rawRepo.findByName(newName).ifPresent(other -> {
      if (!other.id.equals(id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Raw material name already exists");
    });

    String newCode = blankToNull(r.code);
    if (newCode != null) {
      rawRepo.findByCode(newCode).ifPresent(other -> {
        if (!other.id.equals(id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Raw material code already exists");
      });
    }

    e.code = newCode;
    e.name = newName;
    e.stockQuantity = BigDecimal.valueOf(r.stockQuantity == null ? 0 : r.stockQuantity);

    return toDto(rawRepo.save(e));
  }

  public void deleteRawMaterial(Long id) {
    if (!rawRepo.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Raw material not found");
    }
    rawRepo.deleteById(id);
  }

  // ---------------- LINKS (RF003)  /api/links
  public List<ProductRawMaterialDto> listLinks() {
    return linkRepo.findAll().stream().map(this::toDto).toList();
  }

  public ProductRawMaterialDto createLink(ProductRawMaterialDto link) {
    if (link.productId == null || link.rawMaterialId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId and rawMaterialId are required");
    }
    if (link.requiredQuantity == null || link.requiredQuantity <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requiredQuantity must be > 0");
    }

    Product product = productRepo.findById(link.productId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

    RawMaterial material = rawRepo.findById(link.rawMaterialId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Raw material not found"));

    // uniqueness (product_id, raw_material_id)
    linkRepo.findByProductId(link.productId).forEach(existing -> {
      if (existing.rawMaterial.id.equals(link.rawMaterialId)) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Link already exists for this product/material");
      }
    });

    ProductRawMaterial e = new ProductRawMaterial();
    e.product = product;
    e.rawMaterial = material;
    e.requiredQuantity = BigDecimal.valueOf(link.requiredQuantity);

    e = linkRepo.save(e);
    return toDto(e);
  }

  public void deleteLink(Long id) {
    if (!linkRepo.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found");
    }
    linkRepo.deleteById(id);
  }

  // ---------------- mapping helpers (keep your DTOs)
  private ProductDto toDto(Product e) {
    ProductDto d = new ProductDto();
    d.id = e.id;
    d.code = e.code;
    d.name = e.name;
    d.value = e.value;
    return d;
  }

  private RawMaterialDto toDto(RawMaterial e) {
    RawMaterialDto d = new RawMaterialDto();
    d.id = e.id;
    d.code = e.code;
    d.name = e.name;
    // keeping your DTO as Integer stockQuantity
    d.stockQuantity = e.stockQuantity == null ? 0 : e.stockQuantity.intValue();
    return d;
  }

  private ProductRawMaterialDto toDto(ProductRawMaterial e) {
    ProductRawMaterialDto d = new ProductRawMaterialDto();
    d.id = e.id;
    d.productId = e.product.id;
    d.rawMaterialId = e.rawMaterial.id;
    d.requiredQuantity = e.requiredQuantity == null ? 0 : e.requiredQuantity.intValue();
    return d;
  }

  private void validateProduct(ProductDto p) {
    if (p == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required");
    if (p.name == null || p.name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
    if (p.value == null || p.value.compareTo(BigDecimal.ZERO) < 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value must be >= 0");
    }
  }

  private void validateRawMaterial(RawMaterialDto r) {
    if (r == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required");
    if (r.name == null || r.name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
    if (r.stockQuantity != null && r.stockQuantity < 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "stockQuantity must be >= 0");
    }
  }

  private String blankToNull(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }
}
