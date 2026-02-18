package com.example.inventory.controller;

import com.example.inventory.dto.RawMaterialDto;
import com.example.inventory.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/raw-materials")
public class RawMaterialController {

  private final InventoryService service;

  public RawMaterialController(InventoryService service) {
    this.service = service;
  }

  @GetMapping
  public List<RawMaterialDto> list() {
    return service.listRawMaterials();
  }

  @PostMapping
  public RawMaterialDto create(@RequestBody RawMaterialDto r) {
    return service.createRawMaterial(r);
  }

  @PutMapping("/{id}")
  public RawMaterialDto update(@PathVariable Long id, @RequestBody RawMaterialDto r) {
    return service.updateRawMaterial(id, r);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.deleteRawMaterial(id);
  }
}
