package com.example.inventory.controller;

import com.example.inventory.dto.ProductRawMaterialDto;
import com.example.inventory.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/links")
public class LinkController {

  private final InventoryService service;

  public LinkController(InventoryService service) {
    this.service = service;
  }

  @GetMapping
  public List<ProductRawMaterialDto> list() {
    return service.listLinks();
  }

  @PostMapping
  public ProductRawMaterialDto create(@RequestBody ProductRawMaterialDto link) {
    return service.createLink(link);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.deleteLink(id);
  }
}
