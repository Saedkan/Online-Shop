-- Online Shop: one table, written by hand.
-- "order" is a reserved word in SQL, so the table is called shop_order.
CREATE TABLE shop_order (
  id            uuid PRIMARY KEY,
  business_key  text NOT NULL UNIQUE,
  status        text NOT NULL,
  title         text NOT NULL,
  created_at    timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT shop_order_status_known
    CHECK (status IN ('CART', 'PAID', 'SHIPPED', 'DELIVERED', 'RETURNED'))
);
