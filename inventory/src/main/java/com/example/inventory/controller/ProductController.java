package com.example.inventory.controller;

import com.example.inventory.dto.ProductDto;
import com.example.inventory.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final InventoryService service;

  public ProductController(InventoryService service) {
    this.service = service;
  }

  @GetMapping
  public List<ProductDto> list() {
    return service.listProducts();
  }

  @PostMapping
  public ProductDto create(@RequestBody ProductDto p) {
    return service.createProduct(p);
  }

  @PutMapping("/{id}")
  public ProductDto update(@PathVariable Long id, @RequestBody ProductDto p) {
    return service.updateProduct(id, p);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.deleteProduct(id);
  }
}
