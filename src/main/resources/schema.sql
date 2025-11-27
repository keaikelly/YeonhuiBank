ALTER TABLE account
    ADD CONSTRAINT chk_account_num_format
        CHECK (account_num REGEXP '^[0-9-]+$');
