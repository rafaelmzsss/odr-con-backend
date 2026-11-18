-- Move um token ativo por reclamação para reclamacoes.token_acesso e remove a tabela separada.
ALTER TABLE reclamacoes ADD COLUMN token_acesso VARCHAR(255);

UPDATE reclamacoes r
SET token_acesso = (
    SELECT MIN(t.token)
    FROM tokens_acesso t
    WHERE t.reclamacao_id = r.id
      AND t.ativo = TRUE
      AND (t.expiracao IS NULL OR t.expiracao > CURRENT_TIMESTAMP)
);

ALTER TABLE reclamacoes
    ADD CONSTRAINT uk_reclamacoes_token_acesso UNIQUE (token_acesso);

DROP TABLE tokens_acesso;
