# Exercício de Extensão — Biblioteca: Empréstimos e Avaliações

## Objetivo

Vocês irão estender a aplicação atual da biblioteca, que já possui o cadastro de **autores** e **livros**, para suportar dois novos fluxos:

1. **Avaliação de livros**
2. **Empréstimo e devolução de livros**

O objetivo do exercício é fazer com que a aplicação consiga registrar esses dados, aplicar as regras de negócio corretamente e expor funcionalidades que permitam consultar essas informações.

## Nota Importante

Vocês **podem implementar este exercício com a linguagem e o framework com os quais estiverem mais familiarizados**, desde que **todos os requisitos descritos neste enunciado sejam atendidos**.

Isso significa que não é obrigatório usar Java ou Spring, desde que a solução final cumpra o comportamento esperado.

---

## Contexto da aplicação existente

A aplicação atual já permite:

- cadastrar autores;
- listar autores;
- buscar autores por identificador;
- atualizar autores;
- remover autores;
- cadastrar livros;
- consultar livros;
- atualizar livros;
- remover livros;
- relacionar livros com autores.

O novo exercício deve partir desse contexto. Ou seja, os novos módulos devem conversar com o modelo atual de livros já existente.

---

## Escopo do exercício

Vocês deverão implementar **dois módulos novos**:

1. **Módulo de Avaliações**
2. **Módulo de Empréstimos**

Os dois módulos devem considerar o livro como elemento central do domínio.

---

## Módulo 1 — Avaliações de Livros

### Descrição funcional

Cada livro poderá receber avaliações feitas pelos usuários do sistema, mas com uma regra importante: a avaliação só pode existir se houver um **empréstimo previamente realizado** para aquele livro.

Isso significa que a avaliação não deve estar vinculada apenas ao livro. Ela deve estar vinculada ao **empréstimo** que permitiu ao usuário ler aquele livro.

Uma avaliação deve registrar:

- qual foi o empréstimo que originou aquela avaliação;
- qual é o livro avaliado;
- qual é o membro que está avaliando;
- uma nota;
- um comentário textual.

Um livro pode possuir várias avaliações ao longo do tempo, feitas por membros diferentes. Porém, **o mesmo membro só pode avaliar um mesmo livro uma única vez**.

### O que deve existir nesse módulo

#### Dados mínimos da avaliação

Cada avaliação deve possuir, no mínimo, os seguintes campos:

| Campo | Tipo sugerido | Obrigatório | Observações |
|-------|---------------|-------------|-------------|
| `id` | numérico | sim | identificador único da avaliação |
| `loanId` | numérico | sim | referência ao empréstimo que originou a avaliação |
| `bookId` | numérico | sim | referência ao livro avaliado |
| `memberId` | numérico | sim | referência ao membro que fez a avaliação |
| `rating` | inteiro | sim | valor entre 1 e 5 |
| `content` | texto | não | comentário descritivo da avaliação |

### Regras obrigatórias

1. Uma avaliação **só pode ser criada a partir de um empréstimo existente**.
2. O empréstimo informado na avaliação deve estar associado a um **livro existente** e a um **membro existente**.
3. Uma avaliação não pode ser criada para um empréstimo inexistente.
4. A nota da avaliação deve aceitar **somente valores inteiros entre 1 e 5**.
5. Caso a nota esteja fora do intervalo permitido, a aplicação deve retornar erro apropriado.
6. O mesmo membro **não pode avaliar o mesmo livro mais de uma vez**, mesmo que tenha realizado mais de um empréstimo desse livro.
7. Deve ser possível consultar todas as avaliações de um determinado livro.
8. Deve ser possível remover uma avaliação cadastrada.

### Funcionalidades mínimas esperadas

O sistema deve permitir:

- cadastrar uma avaliação com base em um empréstimo;
- buscar uma avaliação específica;
- excluir uma avaliação;
- listar todas as avaliações de um livro.

### Endpoints sugeridos

Se a implementação for REST, a estrutura abaixo é a referência esperada:

| Método | Rota | Descrição | Status esperado |
|--------|------|-----------|-----------------|
| `POST` | `/api/reviews` | criar uma nova avaliação | `201 Created` |
| `GET` | `/api/reviews/{id}` | buscar uma avaliação por ID | `200 OK` |
| `DELETE` | `/api/reviews/{id}` | remover uma avaliação | `204 No Content` |
| `GET` | `/api/books/{id}/reviews` | listar avaliações de um livro | `200 OK` |

### Estruturas de entrada e saída esperadas

#### Exemplo de requisição para criar avaliação

```json
{
  "loanId": 1,
  "rating": 5,
  "content": "Leitura muito boa e com conteúdo prático."
}
```

#### Exemplo de resposta de criação

```json
{
  "id": 1,
  "loanId": 1,
  "bookId": 1,
  "memberId": 1,
  "rating": 5,
  "content": "Leitura muito boa e com conteúdo prático."
}
```

#### Exemplo de listagem das avaliações de um livro

```json
[
  {
    "id": 1,
    "loanId": 1,
    "bookId": 1,
    "memberId": 1,
    "rating": 5,
    "content": "Leitura muito boa e com conteúdo prático."
  },
  {
    "id": 2,
    "loanId": 3,
    "bookId": 1,
    "memberId": 2,
    "rating": 4,
    "content": "Bom livro para revisar conceitos."
  }
]
```

### Observações de modelagem

- Um livro pode ter **muitas avaliações**.
- Um membro pode ter **muitos empréstimos**.
- Um empréstimo pode originar **no máximo uma avaliação**.
- Uma avaliação pertence a **um único empréstimo**, e por consequência fica associada a um único livro e a um único membro.
- A aplicação deve garantir também a regra de unicidade lógica: **um mesmo membro só pode avaliar um mesmo livro uma vez**.
- Se quiserem enriquecer a solução, vocês podem incluir dados adicionais, como data da avaliação, mas isso é opcional.

---

## Módulo 2 — Empréstimos de Livros

### Descrição funcional

Agora a biblioteca deve controlar o empréstimo de livros para usuários cadastrados.

Para isso, vocês deverão criar um fluxo em que:

1. um membro da biblioteca é cadastrado;
2. um livro é emprestado para esse membro;
3. o sistema registra a data do empréstimo;
4. o sistema registra a data prevista de devolução;
5. posteriormente, o livro pode ser devolvido;
6. o sistema deve impedir que o mesmo livro seja emprestado para duas pessoas ao mesmo tempo.

### Entidades conceituais envolvidas

Este módulo deve, no mínimo, trabalhar com duas estruturas:

#### Membro da biblioteca

Representa a pessoa que pode pegar livros emprestados.

Campos mínimos esperados:

| Campo | Tipo sugerido | Obrigatório | Observações |
|-------|---------------|-------------|-------------|
| `id` | numérico | sim | identificador único |
| `name` | texto | sim | nome do membro |
| `email` | texto | sim | deve ser único |

#### Empréstimo

Representa o ato de emprestar um livro para um membro.

Campos mínimos esperados:

| Campo | Tipo sugerido | Obrigatório | Observações |
|-------|---------------|-------------|-------------|
| `id` | numérico | sim | identificador único |
| `bookId` | numérico | sim | livro emprestado |
| `memberId` | numérico | sim | membro que pegou o livro |
| `loanDate` | data | sim | gerada pelo sistema no momento do empréstimo |
| `dueDate` | data | sim | data prevista para devolução |
| `returnedAt` | data | não | preenchida somente quando houver devolução |

### Regras obrigatórias do fluxo de empréstimo

1. O empréstimo só pode ser criado se o **livro existir**.
2. O empréstimo só pode ser criado se o **membro existir**.
3. Um livro **não pode possuir dois empréstimos ativos ao mesmo tempo**.
4. Considera-se empréstimo ativo quando o campo de devolução ainda não foi preenchido.
5. A data do empréstimo deve ser gerada automaticamente pela aplicação no momento do cadastro.
6. A data prevista de devolução deve ser informada na criação do empréstimo.
7. A devolução deve atualizar o empréstimo existente, preenchendo a data em que o livro foi devolvido.
8. Um empréstimo já devolvido não deve ser devolvido novamente.
9. Deve ser possível consultar:
   - todos os empréstimos de um membro;
   - o histórico de empréstimos de um livro.

### Funcionalidades mínimas esperadas

O sistema deve permitir:

- cadastrar membros;
- listar membros;
- buscar um membro por identificador;
- remover membro;
- criar empréstimo;
- registrar devolução;
- listar empréstimos de um membro;
- listar histórico de empréstimos de um livro.

### Endpoints sugeridos

Se a implementação for REST, a estrutura abaixo é a referência esperada:

| Método | Rota | Descrição | Status esperado |
|--------|------|-----------|-----------------|
| `POST` | `/api/members` | cadastrar membro | `201 Created` |
| `GET` | `/api/members` | listar membros | `200 OK` |
| `GET` | `/api/members/{id}` | buscar membro por ID | `200 OK` |
| `DELETE` | `/api/members/{id}` | remover membro | `204 No Content` |
| `GET` | `/api/members/{id}/loans` | listar empréstimos do membro | `200 OK` |
| `POST` | `/api/loans` | registrar empréstimo | `201 Created` |
| `PATCH` | `/api/loans/{id}/return` | registrar devolução | `200 OK` |
| `GET` | `/api/loans/book/{bookId}` | listar histórico de empréstimos do livro | `200 OK` |

### Estruturas de entrada e saída esperadas

#### Exemplo de cadastro de membro

```json
{
  "name": "Ana Silva",
  "email": "ana@umc.br"
}
```

#### Exemplo de resposta de membro criado

```json
{
  "id": 1,
  "name": "Ana Silva",
  "email": "ana@umc.br"
}
```

#### Exemplo de criação de empréstimo

```json
{
  "bookId": 1,
  "memberId": 1,
  "dueDate": "2026-04-01"
}
```

#### Exemplo de resposta de empréstimo criado

```json
{
  "id": 1,
  "bookId": 1,
  "bookTitle": "Clean Code",
  "memberId": 1,
  "memberName": "Ana Silva",
  "loanDate": "2026-03-18",
  "dueDate": "2026-04-01",
  "returnedAt": null
}
```

#### Exemplo de tentativa inválida de empréstimo

Se um livro já estiver emprestado e ainda não tiver sido devolvido, uma nova tentativa de empréstimo para esse mesmo livro deve falhar.

Exemplo:

```json
{
  "message": "Livro já está emprestado"
}
```

Nesse caso, o comportamento esperado é retornar um erro de conflito, como `409 Conflict`.

#### Exemplo de devolução

Ao registrar a devolução, o sistema deve atualizar o empréstimo:

```json
{
  "id": 1,
  "bookId": 1,
  "bookTitle": "Clean Code",
  "memberId": 1,
  "memberName": "Ana Silva",
  "loanDate": "2026-03-18",
  "dueDate": "2026-04-01",
  "returnedAt": "2026-03-25"
}
```

---

## Requisitos gerais de comportamento

Independentemente da linguagem ou framework escolhidos, a solução deve demonstrar claramente os seguintes comportamentos:

1. Os dados de avaliações devem estar corretamente associados aos livros.
2. Os dados de avaliações devem estar corretamente associados também aos empréstimos que originaram essas avaliações.
3. Os dados de empréstimos devem estar corretamente associados aos livros e aos membros.
4. O sistema deve impedir estados inválidos, especialmente:
   - avaliação para empréstimo inexistente;
   - avaliação criada sem vínculo com empréstimo;
   - avaliação com nota fora da faixa permitida;
   - segunda avaliação do mesmo membro para o mesmo livro;
   - empréstimo para livro inexistente;
   - empréstimo para membro inexistente;
   - empréstimo duplicado de um livro que ainda está em posse de outro membro;
   - devolução duplicada do mesmo empréstimo.
5. As respostas da aplicação devem ser coerentes com o resultado da operação, tanto em casos de sucesso quanto de erro.

---

## O que será observado na correção

Os seguintes pontos serão considerados:

- clareza da modelagem das entidades ou estruturas de dados;
- organização do código;
- atendimento aos requisitos funcionais;
- implementação correta das regras de negócio;
- clareza dos contratos de entrada e saída;
- tratamento adequado de erros;
- capacidade de demonstrar o fluxo completo funcionando.

---

## Fluxos mínimos que devem funcionar

Ao final, a aplicação deve ser capaz de executar pelo menos os seguintes cenários:

### Cenário 1 — Avaliação

1. Buscar ou cadastrar um livro existente.
2. Cadastrar um membro.
3. Registrar um empréstimo desse livro para esse membro.
4. Criar uma avaliação vinculada a esse empréstimo.
5. Consultar a avaliação criada.
6. Listar todas as avaliações do livro.
7. Tentar criar uma segunda avaliação para o mesmo livro pelo mesmo membro e validar que a operação falha.
8. Excluir a avaliação.

### Cenário 2 — Empréstimo

1. Cadastrar um membro.
2. Registrar o empréstimo de um livro para esse membro.
3. Impedir um segundo empréstimo do mesmo livro enquanto ele estiver em aberto.
4. Registrar a devolução do livro.
5. Permitir que o mesmo livro seja emprestado novamente após a devolução.
6. Consultar o histórico de empréstimos do livro.

---

## Forma de validação sugerida

Vocês podem demonstrar a solução com qualquer ferramenta de teste de API ou interface, por exemplo:

- Postman;
- Insomnia;
- cURL;
- Swagger;
- frontend próprio;
- testes automatizados.

O importante é conseguir comprovar que os requisitos foram atendidos.
