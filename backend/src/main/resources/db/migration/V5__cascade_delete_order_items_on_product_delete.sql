ALTER TABLE order_items
    DROP FOREIGN KEY fk_order_item_product;

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_item_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE;
