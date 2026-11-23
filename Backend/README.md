# ODR-CON — Backend

API Java 21, Spring Boot 4.0.8, Spring AI 2.0.2, PostgreSQL e Flyway. O backend usa a integração Google GenAI do Spring AI para a Priscila.

## Contratos disponíveis

- `POST /api/auth/register`: cria conta `pessoa` ou `empresa`, salva senha com BCrypt e devolve `accessToken` e `user`.
- `POST /api/auth/login`: valida e-mail, senha e perfil no banco e devolve um JWT Bearer.
- `POST /api/auth/password/forgot`: devolve sempre uma resposta genérica `not_configured`, sem consultar/revelar se o e-mail existe. O envio de e-mail e a redefinição com token ainda precisam ser implementados quando houver um provedor SMTP definido.
- `POST /api/priscila/messages`: requer JWT de pessoa; salva a conversa no banco e devolve `collecting` ou `ready_for_review` com o resumo.
- `POST /api/reports`: requer JWT de pessoa; valida que o resumo confirmado corresponde à conversa, salva em `reclamacoes` e devolve protocolo `ODR-ANO-NÚMERO`. Repetir o mesmo `conversationId` devolve o mesmo protocolo.
- `POST /api/usuarios` e `POST /api/chat` continuam como rotas legadas. Cadastro legado cria perfil pessoa. Rotas administrativas antigas de reclamações e colaboradores exigem `ROLE_ADMIN`; ainda não há fluxo de provisionamento de conta admin.

Os perfis `pessoa` e `empresa` são gravados pelo backend. O token assinado pelo servidor define a identidade e a autoridade; o papel informado no navegador não concede privilégios por si só. O cadastro empresa cria uma conta de perfil, mas o cadastro de organizações/equipe e o painel de colaboradores ainda não estão implementados.

## Executar localmente (Windows)

Pré-requisitos: Java 21, Docker Desktop e uma chave Gemini configurada no PowerShell. O Compose sobe PostgreSQL e API.

```powershell
$env:GEMINI_API_KEY = "sua-chave"
docker compose up --build
```

API: `http://localhost:8080`. Health check: `GET /actuator/health`. Em desenvolvimento, CORS aceita `http://localhost:5173`. Para testar a conversa, use o JWT retornado por `/api/auth/register` ou `/api/auth/login` no cabeçalho `Authorization: Bearer ...` ao chamar Priscila e `/api/reports`.

## Variáveis do Web Service no Render

| Variável | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | URL JDBC do PostgreSQL do Render, começando por `jdbc:postgresql://` |
| `DB_USER` | usuário do PostgreSQL |
| `DB_PASSWORD` | senha do PostgreSQL |
| `GEMINI_API_KEY` | chave do Google AI Studio; segredo, somente no backend |
| `JWT_SECRET` | segredo aleatório com pelo menos 32 bytes; não use o valor de exemplo |
| `CORS_ALLOWED_ORIGINS` | URL HTTPS exata do Static Site, sem barra final |
| `AI_MODEL` | opcional; padrão `gemini-3.1-flash-lite` |
| `JWT_TTL_SECONDS` | opcional; padrão `3600` |

Não use `*` em CORS e não inclua o domínio do frontend em `VITE_*` junto com nenhuma chave secreta. O frontend deve usar `VITE_API_BASE_URL=https://SEU-BACKEND.onrender.com` (sem `/api`); configure essa variável no serviço Static Site. Para o backend local, o valor padrão de CORS já permite `http://localhost:5173`.

O Flyway aplica a migração V3 ao iniciar. Depois de configurar as variáveis, faça o deploy do Web Service. Se o Render estiver ligado à branch `develop`, integre nela as alterações antes de esperar o deploy.

## Testes de API

Cadastro:

```json
POST /api/auth/register
{"email":"pessoa@exemplo.com","password":"senha-forte","role":"pessoa","fullName":"Nome Sobrenome"}
```

Login:

```json
POST /api/auth/login
{"email":"pessoa@exemplo.com","password":"senha-forte","role":"pessoa"}
```

Mensagem Priscila (enviar o JWT no cabeçalho):

```json
POST /api/priscila/messages
{"conversationId":null,"message":"Comprei um produto e ele apresentou defeito."}
```

Registro do resumo confirmado (usar o `conversationId` e o `summary` devolvidos em `ready_for_review`):

```json
POST /api/reports
{"conversationId":"UUID_DA_CONVERSA","summary":{"company":"Loja Horizonte","category":"Produto","description":"Produto apresentou defeito.","requestedSolution":"Substituição do produto"}}
```

O endpoint de recuperação responde `202` com `status: not_configured`. Para ativar recuperação real, ainda é necessário configurar provedor de e-mail, criar tokens de uso único com expiração e persistir somente o hash do token.

## Segurança e privacidade

- Senhas são salvas com BCrypt; a API nunca as devolve.
- JWT é assinado com HS256 e expira em `JWT_TTL_SECONDS`; rotas protegidas exigem `Authorization: Bearer <token>`.
- Preflight CORS aceita `Authorization`, `Content-Type` e `Accept`; cookies não são usados.
- Relatos e conversas são persistidos no PostgreSQL. O backend mascara CPF, e-mail e números longos antes de enviar o texto da Priscila ao Gemini, mas isso não elimina todos os dados pessoais.
- A cota gratuita Gemini tem limites e os termos do plano gratuito informam que conteúdo pode ser usado para melhorar produtos. Não envie relatos reais/sensíveis sem avaliar essa condição.

## Migrations e compatibilidade

V1 e V2 são migrations existentes e não devem ser alteradas. A V3 adiciona perfil, conversas, resumo e protocolo às tabelas existentes. O Hibernate valida o schema com `ddl-auto: validate`.

O código-fonte do frontend não está neste workspace, então a integração do navegador e o domínio real do Static Site ainda precisam ser conferidos no repositório/frontend publicado.
