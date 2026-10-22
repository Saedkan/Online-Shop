CREATE TABLE shop_order (
    id UUID PRIMARY KEY,
    business_key TEXT NOT NULL UNIQUE,
    status TEXT NOT NULL,
    title TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT shop_order_status_known
        CHECK (
            status IN (
                'CART',
                'PAID',
                'SHIPPED',
                'DELIVERED',
                'RETURNED'
            )
        )
);
