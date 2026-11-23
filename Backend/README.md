# ODR — Backend

API Java 21 com Spring Boot, PostgreSQL, Flyway e Spring AI. A geração de respostas usa a integração nativa do Spring AI com Gemini API. O endpoint de conversa é `POST /api/chat`.

## Configurar a IA

1. Crie uma chave no [Google AI Studio](https://aistudio.google.com/api-keys).
2. Configure a chave como variável de ambiente `GEMINI_API_KEY` no ambiente em que o backend roda. Não coloque a chave no código, no frontend ou no Git.
3. O modelo padrão é `gemini-3.1-flash-lite`, configurado pela integração Google GenAI do Spring AI.

O plano gratuito tem limites de uso e a disponibilidade da cota/modelo pode variar por projeto. Ao atingir o limite, o backend responde com fallback. A documentação atual informa que conteúdo do plano gratuito pode ser usado para melhorar produtos do Google. Este projeto mascara alguns padrões de CPF, e-mail e números financeiros, mas não remove todos os dados pessoais. Não use o plano gratuito com relatos sensíveis ou dados reais sem avaliar essa condição.

## Executar localmente (Windows)

Pré-requisitos: Java 21, Docker Desktop com Compose e uma chave Gemini configurada em `GEMINI_API_KEY` no PowerShell:

```powershell
$env:GEMINI_API_KEY = "sua-chave"
docker compose up --build
```

O Compose inicia PostgreSQL e API. A API fica em `http://localhost:8080`; saúde: `GET http://localhost:8080/actuator/health`.

### Testar a conversa

Em outro PowerShell:

```powershell
$body = @{ sessionId = "teste-001"; message = "Estou tendo dificuldade para resolver um desacordo no trabalho." } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/chat -ContentType "application/json" -Body $body
```

A resposta contém `sessionId`, `reply`, `severidade`, `statusCaso`, `dadosMascarados` e `fallbackAtivado`. Se `fallbackAtivado` for `true`, confira a variável `GEMINI_API_KEY`, o modelo e a cota do projeto no Google AI Studio.

## Hospedar no Render

O Web Service Docker usa `Backend/` como diretório raiz e o `Dockerfile` existente. Configure no Environment do serviço:

| Variável | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `jdbc:postgresql://HOST:5432/NOME_DO_BANCO` |
| `DB_USER` | usuário do PostgreSQL |
| `DB_PASSWORD` | senha do PostgreSQL |
| `GEMINI_API_KEY` | chave criada no Google AI Studio |
| `AI_MODEL` | `gemini-3.1-flash-lite` |
| `CORS_ALLOWED_ORIGINS` | origem exata do frontend, se já hospedado |

Salve as variáveis e faça o deploy. O Flyway aplica as migrações na inicialização. Health check: `/actuator/health`.

## Configuração e comportamento

- `GEMINI_API_KEY` é segredo: configure-a no Render ou no ambiente local. Nunca a inclua no Git nem no frontend.
- `AI_MODEL` escolhe o modelo Gemini; `AI_TEMPERATURE` ajusta a geração e `AI_MAX_RETRIES` controla tentativas.
- A memória da conversa fica no processo, não no PostgreSQL. Ela se perde quando o serviço reinicia e não é compartilhada entre múltiplas instâncias.
- A classificação de severidade é uma heurística de apoio, não uma decisão final. O chat não cria nem encaminha reclamações.
- Se o provedor estiver indisponível, a chave falhar ou a cota gratuita acabar, a API retorna uma resposta de fallback.

## Banco de dados

O schema é gerenciado pelo Flyway em `src/main/resources/db/migration`. Para alterar o banco, crie uma nova migração versionada; não edite uma migração já aplicada. O Hibernate valida as entidades com `ddl-auto: validate`.
