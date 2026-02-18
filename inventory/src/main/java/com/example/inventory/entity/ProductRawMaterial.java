package com.example.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
  name = "product_raw_materials",
  uniqueConstraints = @UniqueConstraint(name = "uq_product_material", columnNames = {"product_id", "raw_material_id"})
)
public class ProductRawMaterial {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  public Product product;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "raw_material_id", nullable = false)
  public RawMaterial rawMaterial;

  @Column(name = "required_quantity", nullable = false, precision = 18, scale = 3)
  public BigDecimal requiredQuantity;
}
