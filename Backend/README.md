# ODR — Backend

API Java 21 com Spring Boot, PostgreSQL, Flyway e Spring AI/Ollama. A conversa com a IA fica disponível em `POST /api/chat`.

## Executar localmente (Windows)

Pré-requisitos: Docker Desktop com Compose e pelo menos alguns GB de memória livre para o modelo `llama3.2:3b`. Na primeira execução, o Ollama baixa o modelo (aprox. 2 GB); as próximas execuções reutilizam o volume `ollama-models`.

Na raiz do repositório:

```powershell
docker compose up --build
```

O Compose inicia PostgreSQL, Ollama, baixa o modelo e inicia a API. Acompanhe os logs com `docker compose logs -f api`. A API fica em `http://localhost:8080`; saúde: `GET http://localhost:8080/actuator/health`.

Para parar, use `Ctrl+C` e `docker compose down`. Os dados do banco e o modelo continuam nos volumes. Para apagá-los também, `docker compose down -v` remove permanentemente os dados locais.

### Testar a conversa

Em outro PowerShell:

```powershell
$body = @{ sessionId = "teste-001"; message = "Estou tendo dificuldade para resolver um desacordo no trabalho." } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/chat -ContentType "application/json" -Body $body
```

A resposta contém `sessionId`, `reply`, `severidade`, `statusCaso`, `dadosMascarados` e `fallbackAtivado`. Reutilize o mesmo `sessionId` para manter o contexto da conversa. Se Ollama estiver indisponível ou demorar demais, a API responde com `fallbackAtivado: true`.

Valide também a API e o banco:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Para executar a aplicação pela IDE/Maven, inicie somente o banco e o Ollama com `docker compose up -d db ollama`, baixe o modelo com `docker compose run --rm ollama-model`, depois execute `Backend\mvnw.cmd spring-boot:run` dentro de `Backend`. Se usar Ollama instalado diretamente no Windows, configure `OLLAMA_BASE_URL=http://localhost:11434` e baixe o modelo com `ollama pull llama3.2:3b`.

## Hospedar

O Dockerfile em `Backend/` empacota apenas a API. Em Render, Railway, Fly.io ou serviço equivalente, crie um serviço Docker apontando o diretório raiz de build para `Backend`. Crie um PostgreSQL gerenciado e informe as variáveis abaixo no painel da plataforma (não grave segredos no Git):

| Variável | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `jdbc:postgresql://HOST:5432/NOME_DO_BANCO` |
| `DB_USER` | usuário do banco |
| `DB_PASSWORD` | senha do banco |
| `CORS_ALLOWED_ORIGINS` | URL(s) exata(s) do frontend, separadas por vírgula |
| `OLLAMA_BASE_URL` | URL alcançável pela API para o servidor Ollama |
| `OLLAMA_MODEL` | modelo que já esteja disponível no servidor Ollama, por exemplo `llama3.2:3b` |
| `AI_READ_TIMEOUT` | `120s` (ou maior se o modelo for lento) |

O Ollama do Compose é para desenvolvimento. Para produção, hospede Ollama como serviço separado com CPU/GPU, memória e disco persistente adequados, baixe nele o modelo escolhido e use sua URL privada em `OLLAMA_BASE_URL`. Garanta conectividade de rede entre a API e Ollama; o endereço `localhost` dentro do container da API aponta para a própria API. O banco gerenciado deve aceitar conexões da plataforma. Health check: `/actuator/health`.

Após publicar, teste `GET https://SEU-DOMINIO/actuator/health` e envie o mesmo JSON de conversa para `POST https://SEU-DOMINIO/api/chat`. A primeira resposta pode levar mais tempo enquanto o modelo aquece. O Flyway aplica as migrações na inicialização.

## Configuração e comportamento

- `OLLAMA_BASE_URL`, `OLLAMA_MODEL`, `OLLAMA_TEMPERATURE`, `AI_CONNECT_TIMEOUT` e `AI_READ_TIMEOUT` ajustam a integração.
- O endpoint de chat é público para permitir uso pelo frontend. Limita mensagens a 4.000 caracteres e mascara padrões de CPF, e-mail e números financeiros antes de enviar o texto ao modelo.
- A memória de conversa é mantida em memória no processo. Ela não é persistida no PostgreSQL e é perdida quando o serviço reinicia; em múltiplas instâncias, cada instância terá seu próprio histórico.
- A classificação de severidade é uma heurística de apoio, não uma decisão final. O chat não cria nem encaminha reclamações; a API responde isso explicitamente.
- O fallback mantém a API disponível quando Ollama falhar, mas não substitui a resposta gerada pelo modelo.

## Banco de dados

O schema é gerenciado pelo Flyway em `src/main/resources/db/migration`. Para alterar o banco, crie uma nova migração versionada; não edite uma migração já aplicada. O Hibernate valida as entidades com `ddl-auto: validate`.
