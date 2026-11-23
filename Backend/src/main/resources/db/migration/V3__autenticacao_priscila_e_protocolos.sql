ALTER TABLE usuarios
    ADD COLUMN perfil VARCHAR(20) NOT NULL DEFAULT 'pessoa';

ALTER TABLE reclamacoes ADD COLUMN protocolo VARCHAR(40);
ALTER TABLE reclamacoes ADD COLUMN conversation_id VARCHAR(36);
ALTER TABLE reclamacoes ADD COLUMN empresa VARCHAR(255);
ALTER TABLE reclamacoes ADD COLUMN solucao_solicitada TEXT;

ALTER TABLE reclamacoes
    ADD CONSTRAINT uk_reclamacoes_protocolo UNIQUE (protocolo);

ALTER TABLE reclamacoes
    ADD CONSTRAINT uk_reclamacoes_conversation_id UNIQUE (conversation_id);

CREATE TABLE conversas_priscila (
    id                  VARCHAR(36) PRIMARY KEY,
    usuario_id          BIGINT NOT NULL REFERENCES usuarios (id),
    historico           TEXT NOT NULL,
    status              VARCHAR(30) NOT NULL,
    empresa             VARCHAR(255),
    categoria           VARCHAR(100),
    descricao           TEXT,
    solucao_solicitada  TEXT,
    criado_em           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_conversas_priscila_usuario ON conversas_priscila (usuario_id);
