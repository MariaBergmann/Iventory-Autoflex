package com.example.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "raw_materials")
public class RawMaterial {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(length = 60, unique = true)
  public String code;

  @Column(nullable = false, length = 160, unique = true)
  public String name;

  @Column(name = "stock_quantity", nullable = false, precision = 18, scale = 3)
  public BigDecimal stockQuantity;
}
