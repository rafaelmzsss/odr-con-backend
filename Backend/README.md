# ODR — Backend

API Java 21 com Spring Boot, PostgreSQL, Flyway e Spring AI. O endpoint de conversa é `POST /api/chat`; a geração de respostas usa Ollama Cloud pela API compatível com OpenAI.

## Executar localmente (Windows)

Pré-requisitos: Docker Desktop com Compose e uma chave de API do Ollama Cloud. Crie a chave na sua conta Ollama e defina-a apenas no terminal da sessão atual:

```powershell
$env:OLLAMA_API_KEY = "sua-chave"
docker compose up --build
```

O Compose inicia PostgreSQL e API. O modelo é executado pelo Ollama Cloud, então não é necessário baixar nem iniciar um container Ollama local. A API fica em `http://localhost:8080`; saúde: `GET http://localhost:8080/actuator/health`.

Para parar, use `Ctrl+C` e `docker compose down`. O banco continua no volume local. `docker compose down -v` remove permanentemente os dados locais do banco.

### Testar a conversa

Em outro PowerShell:

```powershell
$body = @{ sessionId = "teste-001"; message = "Estou tendo dificuldade para resolver um desacordo no trabalho." } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/chat -ContentType "application/json" -Body $body
```

A resposta contém `sessionId`, `reply`, `severidade`, `statusCaso`, `dadosMascarados` e `fallbackAtivado`. Reutilize o mesmo `sessionId` para manter o contexto. Se `fallbackAtivado` for `true`, confira se a chave está válida e se sua conta Ollama tem saldo/uso disponível.

## Hospedar no Render

O serviço Docker da API usa `Backend/` como diretório raiz e o `Dockerfile` existente. Crie um PostgreSQL no Render e configure no Web Service da API:

| Variável | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `jdbc:postgresql://HOST:5432/NOME_DO_BANCO` |
| `DB_USER` | usuário do PostgreSQL |
| `DB_PASSWORD` | senha do PostgreSQL |
| `OLLAMA_API_KEY` | chave criada na sua conta Ollama |
| `AI_MODEL` | `gpt-oss:20b` |
| `AI_READ_TIMEOUT` | `60s` |
| `AI_MAX_RETRIES` | `1` |
| `CORS_ALLOWED_ORIGINS` | origem exata do frontend, se já hospedado |

Não é necessário criar um segundo serviço Ollama no Render. `OLLAMA_CLOUD_BASE_URL` tem como padrão `https://ollama.com/v1`; só configure-a se precisar mudar o endpoint. Depois de salvar as variáveis, faça o deploy. O Flyway aplica as migrações na inicialização. Health check: `/actuator/health`.

Teste `GET https://SEU-DOMINIO/actuator/health` e envie o JSON do teste acima para `POST https://SEU-DOMINIO/api/chat`.

## Configuração e comportamento

- `OLLAMA_API_KEY` é segredo: configure-o no Render ou como variável de ambiente local. Nunca o inclua no Git nem no frontend.
- `AI_MODEL` define o modelo no Ollama Cloud. `AI_TEMPERATURE`, `AI_READ_TIMEOUT` e `AI_MAX_RETRIES` ajustam a geração e as tentativas.
- As mensagens são enviadas ao Ollama Cloud para gerar respostas. O backend mascara padrões de CPF, e-mail e números financeiros, mas isso não remove todos os dados pessoais ou confidenciais; não envie relatos sensíveis sem a autorização adequada.
- A memória da conversa fica no processo, não no PostgreSQL. Ela se perde quando o serviço reinicia e não é compartilhada entre múltiplas instâncias.
- A classificação de severidade é uma heurística de apoio, não uma decisão final. O chat não cria nem encaminha reclamações.
- Se o provedor estiver indisponível ou a chave falhar, a API retorna uma resposta de fallback.

## Banco de dados

O schema é gerenciado pelo Flyway em `src/main/resources/db/migration`. Para alterar o banco, crie uma nova migração versionada; não edite uma migração já aplicada. O Hibernate valida as entidades com `ddl-auto: validate`.
