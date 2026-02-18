package com.example.inventory.repository;

import com.example.inventory.entity.ProductRawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRawMaterialRepository extends JpaRepository<ProductRawMaterial, Long> {
  List<ProductRawMaterial> findByProductId(Long productId);
}
