# Biblioteca UMC — API de Acervo

API RESTful para gerenciamento do acervo de livros da biblioteca da UMC.

## Tecnologias

- Java 17
- Spring Boot 4
- Spring Security (JWT, Argon2)
- Spring Data JPA
- H2 (banco em memória)

## Como executar

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

O console do banco H2 fica disponível em `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:librarydb`, usuário: `sa`, senha: em branco).

---

## Segurança

### Autenticação vs Autorização

| Conceito | O que é | Como funciona aqui |
|---|---|---|
| **Autenticação** | Verificar *quem* é o usuário | Login com email/senha → gera token JWT |
| **Autorização** | Verificar *o que* o usuário pode fazer | Token carrega a role → Spring Security verifica permissões por rota |

### Por que Argon2 para senhas?

Senhas **nunca** são armazenadas em texto puro. São transformadas em hash irreversível usando **Argon2**:

- **Não use MD5 ou SHA-256 para senhas**: são hashes rápidos. Uma GPU moderna calcula bilhões de SHA-256 por segundo, tornando ataques de força bruta viáveis.
- **Argon2 é memory-hard**: exige muita memória para ser calculado. Ataques com GPU ou ASIC ficam proibitivamente caros.
- **Venceu o Password Hashing Competition (2015)** — recomendado pelo OWASP.

### Roles

| Role | Permissões |
|---|---|
| `USER` | Leitura (listar livros e autores) |
| `ADMIN` | Leitura + escrita (criar, editar, deletar) |

---

## Usuário Admin para Aula

> **ATENÇÃO**: Use apenas em desenvolvimento. Nunca em produção.

```
Email: admin@biblioteca.com
Senha: Admin@123
```

---

## Fluxo de Autenticação

### 1. Registrar usuário

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Ana Paula",
    "email": "ana@email.com",
    "phone": "11999991234",
    "password": "Minha@Senha123"
  }'
```

Resposta `201 Created`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "ana@email.com",
  "role": "USER"
}
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ana@email.com",
    "password": "Minha@Senha123"
  }'
```

### 3. Usar o token

Inclua o token em todas as requisições protegidas:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 4. Exemplo: listar livros (USER ou ADMIN)

```bash
curl http://localhost:8080/api/books \
  -H "Authorization: Bearer <token>"
```

### 5. Exemplo: criar livro (somente ADMIN)

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Authorization: Bearer <token-admin>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Clean Code",
    "authorIds": [1],
    "publisher": "Prentice Hall",
    "isbn": "9780132350884",
    "summary": "Guia de boas práticas de programação."
  }'
```

---

## Endpoints

### Públicos (sem autenticação)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/auth/register` | Registra novo usuário |
| `POST` | `/api/auth/login` | Login e geração de token JWT |

### Requerem autenticação (ADMIN ou USER)

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/books` | Listar todos os livros |
| `GET` | `/api/books/{id}` | Ver detalhes de um livro |
| `GET` | `/api/authors` | Listar todos os autores |
| `GET` | `/api/authors/{id}` | Buscar autor por ID |
| `GET` | `/api/authors/{id}/books` | Listar livros de um autor |

### Requerem ADMIN

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/authors` | Cadastrar autor |
| `PUT` | `/api/authors/{id}` | Atualizar autor |
| `DELETE` | `/api/authors/{id}` | Remover autor |
| `POST` | `/api/books` | Cadastrar livro |
| `PUT` | `/api/books/{id}` | Alterar livro |
| `DELETE` | `/api/books/{id}` | Remover livro |

### Respostas de erro

| Código | Significado |
|---|---|
| `401 Unauthorized` | Token ausente, inválido ou expirado |
| `403 Forbidden` | Token válido, mas role insuficiente |
| `409 Conflict` | Email já cadastrado |

---

## Autores

#### Cadastrar autor (ADMIN)

```
POST /api/authors
Authorization: Bearer <token-admin>
```

```json
{ "name": "Robert C. Martin" }
```

Resposta `201 Created`:
```json
{ "id": 1, "name": "Robert C. Martin" }
```

#### Listar todos os autores

```
GET /api/authors
Authorization: Bearer <token>
```

Resposta `200 OK`:
```json
[
  { "id": 1, "name": "Robert C. Martin" },
  { "id": 2, "name": "Martin Fowler" }
]
```

#### Buscar autor por ID

```
GET /api/authors/{id}
Authorization: Bearer <token>
```

#### Atualizar autor (ADMIN)

```
PUT /api/authors/{id}
Authorization: Bearer <token-admin>
```

#### Remover autor (ADMIN)

```
DELETE /api/authors/{id}
Authorization: Bearer <token-admin>
```

Resposta `204 No Content`.

#### Listar livros de um autor

```
GET /api/authors/{id}/books
Authorization: Bearer <token>
```

---

## Livros

#### Listar todos os livros

```
GET /api/books
Authorization: Bearer <token>
```

Resposta `200 OK`:
```json
[
  {
    "id": 1,
    "title": "Clean Code",
    "authors": [{ "id": 1, "name": "Robert C. Martin" }],
    "publisher": "Prentice Hall",
    "isbn": "9780132350884",
    "summary": "Guia de boas práticas de programação."
  }
]
```

#### Cadastrar livro (ADMIN)

```
POST /api/books
Authorization: Bearer <token-admin>
```

```json
{
  "title": "Clean Code",
  "authorIds": [1],
  "publisher": "Prentice Hall",
  "isbn": "9780132350884",
  "summary": "Guia de boas práticas de programação para escrever código limpo e de fácil manutenção."
}
```

Resposta `201 Created`.

#### Ver detalhes de um livro

```
GET /api/books/{id}
Authorization: Bearer <token>
```

#### Alterar livro (ADMIN)

```
PUT /api/books/{id}
Authorization: Bearer <token-admin>
```

#### Remover livro (ADMIN)

```
DELETE /api/books/{id}
Authorization: Bearer <token-admin>
```

Resposta `204 No Content`.
