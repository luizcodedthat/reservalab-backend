# ReservaLab — referência para o app mobile

Este diretório descreve o contrato da API que o app mobile deve seguir.
Nada aqui exige banco, provedor de IA ou servidor de produção.

- [`openapi.json`](openapi.json): contrato OpenAPI 3.1 completo (endpoints, DTOs, enums). Serve para gerar
  o cliente HTTP (ex.: `openapi-generator`, `orval`, `openapi-typescript`) ou montar mocks.
- Este README: regras que o OpenAPI não expressa bem (autenticação, permissões, erros, paginação).

## Rodando o backend sem nenhuma conexão real

```bash
SPRING_PROFILES_ACTIVE=mock ./mvnw spring-boot:run
```

O perfil `mock` usa:

| Recurso | No perfil `mock` |
|---|---|
| Banco | H2 em memória, populado pelas mesmas migrations Flyway de produção (reinicia limpo a cada execução) |
| IA (chat, sugestão de laboratório, prioridade de chamado) | `MockLlmAdapter`: respostas determinísticas marcadas com `[mock]` |
| JWT | segredo local fixo, sem variáveis de ambiente |

- API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Contrato ao vivo: `http://localhost:8080/v3/api-docs`

No emulador Android, use `http://10.0.2.2:8080` para acessar o `localhost` da máquina.

### Usuários de exemplo (senha de todos: `L!123456`)

| Email | Perfil |
|---|---|
| `joao.silva@uni.edu` | STUDENT |
| `maria.souza@uni.edu` | STUDENT |
| `carlos.lima@uni.edu` | PROFESSOR |
| `paula.mendes@uni.edu` | SECRETARY |
| `lgsl3@discente.ifpe.edu.br` | TECHNICIAN |
| `inativo@uni.edu` | STUDENT inativo (login retorna 403) |

## Autenticação

1. `POST /api/auth/login` com `{ "email", "password" }` → `{ "token", "user" }`.
2. Guarde o `token` em armazenamento seguro (Keychain / EncryptedSharedPreferences / `expo-secure-store`).
3. Envie `Authorization: Bearer <token>` em todas as outras chamadas.
4. Ao abrir o app, `GET /api/auth/me` valida o token salvo e devolve o usuário atual.

O token expira em 24h e **não há refresh token**. Qualquer `401` significa "volte para o login".

`POST /api/auth/register` cria sempre um usuário `STUDENT` e já devolve `{ token, user }`.

| Situação no login | Status |
|---|---|
| Sucesso | 200 |
| Email inexistente ou senha errada | 401 (mesma resposta nos dois casos) |
| Conta desativada | 403 |

## Identidade: o servidor usa o token

O usuário é sempre o dono do token. O app **não** envia "quem sou eu" no corpo:

- `POST /api/reservations`: `requestedByUserId` é opcional. Só SECRETARY/TECHNICIAN podem informar outro usuário.
- `POST /api/tickets`: o criador é o usuário do token; a prioridade inicial é calculada pela IA.
- `POST /api/tickets/{id}/comments`, `POST /api/laboratories/{id}/comments`, votos: o autor é o usuário do token.

## Permissões por perfil

`STAFF` = `SECRETARY` ou `TECHNICIAN`.

| Ação | Quem pode |
|---|---|
| Ver laboratórios, reservas, chamados | qualquer autenticado |
| Criar/editar/ativar/desativar/excluir laboratório | STAFF |
| Criar reserva própria | qualquer autenticado |
| Editar (só `PENDING`) ou cancelar reserva | dono ou STAFF |
| Cancelamento em lote (`DELETE /api/reservations?...`) | STAFF, com ao menos um filtro |
| Abrir chamado | qualquer autenticado |
| Editar título/descrição/laboratório do chamado | criador ou STAFF |
| Alterar status, prioridade, responsável, resolução | STAFF |
| Excluir chamado | STAFF |
| Remover comentário (chamado ou laboratório) | autor ou STAFF |
| Listar/buscar/editar usuários | STAFF (desativar e excluir: só SECRETARY) |

O app pode esconder botões com base em `user.role`, mas a regra vale no servidor: uma ação proibida retorna `403`.

## Formato dos erros

Todo erro tem corpo JSON. Trate pelo `status` HTTP.

```json
{ "timestamp": "2026-11-26T22:22:36.26", "status": 404, "error": "Not Found", "message": "Chamado não encontrado: id=9999" }
```

| Status | Significado | O que o app faz |
|---|---|---|
| 400 | Requisição inválida | mostra `message` |
| 400 (validação) | Campos inválidos: `{ ..., "error": "Erro de validação", "violations": [{ "field", "message" }] }` | marca os campos do formulário |
| 401 | Token ausente, inválido ou expirado | limpa o token e volta ao login |
| 403 | Sem permissão | mostra `message` |
| 404 | Recurso não existe | mostra `message` / volta à lista |
| 409 | Conflito de horário (`conflicts: [{ date, requestedStart, requestedEnd, conflictingReservationId }]`) ou registro alterado por outra pessoa | destaca os blocos em conflito / recarrega |
| 422 | Nenhum laboratório livre para a sugestão da IA | mostra `message` |
| 429 | Limite do provedor de IA; respeite o header `Retry-After` (segundos) | desabilita o envio temporariamente |
| 503 | IA indisponível | mostra aviso e permite tentar de novo |

## Paginação

Listagens retornam o `Page` do Spring. Use apenas estes campos (os demais podem mudar):

```json
{ "content": [ ... ], "number": 0, "size": 20, "totalElements": 5, "totalPages": 1, "first": true, "last": false }
```

Parâmetros: `?page=0&size=20&sort=campo,asc|desc` (página começa em 0).

## Datas e horas

| Tipo | Formato | Exemplo |
|---|---|---|
| Data | `yyyy-MM-dd` | `2026-12-01` |
| Hora | `HH:mm[:ss]` | `14:00` |
| Data e hora | ISO-8601 **sem fuso**, horário de Brasília | `2026-11-26T22:22:36.113711` |

## Reservas

Uma reserva é um dia com um ou mais blocos de horário (podem ser descontínuos):

```json
POST /api/reservations
{
  "laboratoryId": 1,
  "reservationDate": "2026-12-01",
  "purpose": "Aula de redes",
  "notes": "opcional",
  "timeBlocks": [
    { "startTime": "08:00", "endTime": "09:40", "blockOrder": 1 },
    { "startTime": "10:00", "endTime": "11:40", "blockOrder": 2 }
  ]
}
```

- Novas reservas já nascem `APPROVED`.
- Status: `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`, `COMPLETED`. Cancelar é possível em `PENDING`/`APPROVED`.
- `GET /api/reservations/search` usa a data de hoje quando `dateFrom`/`dateTo` não são enviados.

## Chamados

- Status: `OPEN`, `IN_PROGRESS`, `PENDING`, `RESOLVED`, `CLOSED`. `resolvedAt` é preenchido ao passar para `RESOLVED`/`CLOSED`.
- Prioridade: `LOW`, `MEDIUM`, `HIGH`, `URGENT`. Se a IA falhar, o chamado é criado como `MEDIUM`.
- `PUT /api/tickets/{id}` é atualização parcial: envie só o que muda.

## IA

- `POST /api/ai/chat` com `{ "message", "history": [{ "role": "user"|"assistant", "content" }] }` → `{ "reply" }`.
  O servidor não guarda a conversa: o app mantém o histórico e reenvia (só os últimos 10 itens são usados).
- `POST /api/ai/labs/suggest` com `{ "userPrompt", "date", "startTime", "endTime" }` → `{ "labId", "labName", "reason" }`.

Chamadas de IA podem levar vários segundos em produção. Mostre um indicador de carregamento e use timeout alto (~120 s).

## Atualizando o contrato

Depois de mudar controllers ou DTOs, regenere o `openapi.json` com o backend rodando no perfil `mock`:

```bash
curl -s http://localhost:8080/v3/api-docs > docs/mobile/openapi.json
```
