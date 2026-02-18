-- =====================================================================
-- INVENTORY / PRODUCTION PLANNING SCHEMA (PostgreSQL)
-- English naming for tables/columns (RNF007)
-- =====================================================================

-- Optional: keep everything in public schema
-- SET search_path TO public;

-- ---------------------------------------------------------------------
-- 1) Drop objects (safe re-run)
-- ---------------------------------------------------------------------
DROP VIEW IF EXISTS v_product_production_capacity;
DROP FUNCTION IF EXISTS fn_production_suggestion();

DROP TABLE IF EXISTS product_raw_materials;
DROP TABLE IF EXISTS raw_materials;
DROP TABLE IF EXISTS products;

-- ---------------------------------------------------------------------
-- 2) Core tables
-- ---------------------------------------------------------------------

-- Products table: code (id), name, value
CREATE TABLE products (
  id            BIGSERIAL PRIMARY KEY,
  code          VARCHAR(60) UNIQUE,
  name          VARCHAR(160) NOT NULL,
  value         NUMERIC(12,2) NOT NULL CHECK (value >= 0),
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_value_desc ON products (value DESC);
CREATE INDEX idx_products_name       ON products (name);

-- Raw materials table: code (id), name, stock quantity
CREATE TABLE raw_materials (
  id              BIGSERIAL PRIMARY KEY,
  code            VARCHAR(60) UNIQUE,
  name            VARCHAR(160) NOT NULL UNIQUE,
  stock_quantity  NUMERIC(18,3) NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_raw_materials_name ON raw_materials (name);

-- Association (BOM / recipe):
-- quantity_required = how much raw material is needed to produce 1 unit of product
CREATE TABLE product_raw_materials (
  product_id         BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  raw_material_id    BIGINT NOT NULL REFERENCES raw_materials(id) ON DELETE RESTRICT,
  quantity_required  NUMERIC(18,3) NOT NULL CHECK (quantity_required > 0),
  created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),

  PRIMARY KEY (product_id, raw_material_id)
);

CREATE INDEX idx_prm_product  ON product_raw_materials (product_id);
CREATE INDEX idx_prm_material ON product_raw_materials (raw_material_id);

-- ---------------------------------------------------------------------
-- 3) View: production capacity per product with current stock
--    capacity = min(stock_quantity / quantity_required) across its materials
--    If product has no materials -> capacity = 0 (by design)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_product_production_capacity AS
SELECT
  p.id AS product_id,
  p.name AS product_name,
  p.value AS product_value,
  COALESCE(
    FLOOR(
      MIN(rm.stock_quantity / prm.quantity_required)
    ),
    0
  )::BIGINT AS max_producible_units
FROM products p
LEFT JOIN product_raw_materials prm ON prm.product_id = p.id
LEFT JOIN raw_materials rm ON rm.id = prm.raw_material_id
GROUP BY p.id, p.name, p.value;

-- ---------------------------------------------------------------------
-- 4) Function: production suggestion prioritizing higher value
--    Greedy approach:
--      - Order products by value DESC
--      - For each product, compute max units based on remaining stock
--      - Reserve/consume stock virtually and move to next product
--
-- Returns:
--   product_id, product_name, product_value, suggested_units, suggested_value
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_production_suggestion()
RETURNS TABLE (
  product_id        BIGINT,
  product_name      VARCHAR,
  product_value     NUMERIC(12,2),
  suggested_units   BIGINT,
  suggested_value   NUMERIC(18,2)
)
LANGUAGE plpgsql
AS $$
DECLARE
  p RECORD;
  rm_row RECORD;
  units BIGINT;
  possible BIGINT;
BEGIN
  -- temp table with mutable stock (virtual consumption)
  CREATE TEMP TABLE tmp_stock (
    raw_material_id BIGINT PRIMARY KEY,
    remaining_qty   NUMERIC(18,3) NOT NULL
  ) ON COMMIT DROP;

  INSERT INTO tmp_stock(raw_material_id, remaining_qty)
  SELECT id, stock_quantity
  FROM raw_materials;

  -- iterate products by highest value first
  FOR p IN
    SELECT id, name, value
    FROM products
    ORDER BY value DESC, id ASC
  LOOP
    -- if product has no BOM lines, skip it (cannot produce anything)
    IF NOT EXISTS (SELECT 1 FROM product_raw_materials WHERE product_id = p.id) THEN
      CONTINUE;
    END IF;

    -- compute max units with remaining stock: min(remaining / required)
    units := NULL;

    FOR rm_row IN
      SELECT prm.raw_material_id, prm.quantity_required, ts.remaining_qty
      FROM product_raw_materials prm
      JOIN tmp_stock ts ON ts.raw_material_id = prm.raw_material_id
      WHERE prm.product_id = p.id
    LOOP
      possible := FLOOR(rm_row.remaining_qty / rm_row.quantity_required)::BIGINT;

      IF units IS NULL OR possible < units THEN
        units := possible;
      END IF;
    END LOOP;

    IF units IS NULL OR units <= 0 THEN
      CONTINUE;
    END IF;

    -- consume stock virtually
    UPDATE tmp_stock ts
    SET remaining_qty = ts.remaining_qty - (prm.quantity_required * units)
    FROM product_raw_materials prm
    WHERE prm.product_id = p.id
      AND prm.raw_material_id = ts.raw_material_id;

    -- output row
    product_id := p.id;
    product_name := p.name;
    product_value := p.value;
    suggested_units := units;
    suggested_value := (p.value * units)::NUMERIC(18,2);
    RETURN NEXT;
  END LOOP;

  RETURN;
END;
$$;

-- ---------------------------------------------------------------------
-- 5) Quick test queries (optional)
-- ---------------------------------------------------------------------
-- See capacities (without greedy allocation):
-- SELECT * FROM v_product_production_capacity ORDER BY product_value DESC;

-- Greedy production suggestion (with virtual allocation):
-- SELECT * FROM fn_production_suggestion();

-- Total suggested revenue:
-- SELECT COALESCE(SUM(suggested_value),0) AS total_value FROM fn_production_suggestion();
