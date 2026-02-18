package com.example.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(length = 60, unique = true)
  public String code;

  @Column(nullable = false, length = 160)
  public String name;

  @Column(nullable = false, precision = 12, scale = 2)
  public BigDecimal value;
}
