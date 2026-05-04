package br.umc.demo.entity;

/**
 * Papéis disponíveis na aplicação.
 *
 * ADMIN → acesso total (criar, editar, deletar autores e livros)
 * USER  → acesso somente leitura (listar livros e autores)
 *
 * Conceito de aula: "autorização" é o processo de verificar SE um usuário
 * já autenticado tem PERMISSÃO para executar determinada ação.
 */
public enum Role {
    ADMIN,
    USER
}
