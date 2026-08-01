# SH BFF Gateway

Gateway/BFF em Java com Spring Boot para autenticação e controle de acesso por perfil, com emissão de JWT para o front-end.

## Fluxo implementado

1. Front envia `username` e `password` para `POST /api/auth/login`.
2. Gateway valida credenciais no `AuthenticationManager`.
3. Gateway emite JWT contendo `sub` (usuário), `roles` (perfis), contexto do tenant,
   `iss` e `aud`.
4. Gateway devolve token para o front-end.
5. Front usa `Authorization: Bearer <token>` para consumir rotas protegidas.

## Endpoints

### Login

`POST /api/auth/login`

Payload:

```json
{
  "username": "prefeitura",
  "password": "prefeitura123"
}
```

Resposta (exemplo):

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "profile": "ROLE_PREFEITURA"
}
```

### Rota protegida de exemplo

`GET /api/gateway/me`

Header:

`Authorization: Bearer <jwt>`

## Usuários de demonstração

- `admin / admin123` → `ROLE_ADMIN`
- `empresa / empresa123` → `ROLE_EMPRESA`
- `prefeitura / prefeitura123` → `ROLE_PREFEITURA`

## Executar

Configure `JWT_SECRET` fora do repositório. `JWT_ISSUER` e `JWT_AUDIENCE` podem ser
sobrescritos por ambiente; os valores padrão são `payroll-auth` e
`payroll-publisher`, respectivamente.

```bash
mvn spring-boot:run
```

## Testes

```bash
mvn test
```

> **Importante**: para produção, mova usuários para banco/IdP e armazene segredo JWT em secret manager.
