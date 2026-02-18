package com.example.inventory.repository;

import com.example.inventory.entity.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long> {
  Optional<RawMaterial> findByCode(String code);
  Optional<RawMaterial> findByName(String name);
}
