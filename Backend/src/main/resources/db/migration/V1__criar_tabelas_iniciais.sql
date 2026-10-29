-- Schema inicial (PostgreSQL). Baseado no schema.sql / odr.obs originais (SQLite).
-- Mantido em SQL padrão (IDENTITY, TIMESTAMP, BOOLEAN) para rodar também no H2 dos testes.

CREATE TABLE usuarios (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    senha      VARCHAR(255) NOT NULL,
    criado_em  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE colaboradores (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    chave_acesso  VARCHAR(255) NOT NULL UNIQUE,
    ativo         BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reclamacoes (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id      BIGINT REFERENCES usuarios (id),
    tipo_registro   VARCHAR(100) NOT NULL,
    texto_original  TEXT NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
    criado_em       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tratamentos_ia (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reclamacao_id   BIGINT NOT NULL REFERENCES reclamacoes (id),
    texto_original  TEXT NOT NULL,
    texto_tratado   TEXT NOT NULL,
    resultado       TEXT NOT NULL,
    criado_em       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tokens_acesso (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reclamacao_id  BIGINT NOT NULL REFERENCES reclamacoes (id),
    token          VARCHAR(255) NOT NULL UNIQUE,
    criado_em      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiracao      TIMESTAMP,
    ativo          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_reclamacoes_usuario ON reclamacoes (usuario_id);
CREATE INDEX idx_reclamacoes_status ON reclamacoes (status);
CREATE INDEX idx_tratamentos_ia_reclamacao ON tratamentos_ia (reclamacao_id);
CREATE INDEX idx_tokens_acesso_reclamacao ON tokens_acesso (reclamacao_id);