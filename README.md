# PrédioSimples 🏢

Sistema de gerenciamento para pequenos condomínios  ou micro-comunidades, desenvolvido para solucionar problemas de comunicação, governança e transparência alinhados ao ODS 11 - Cidades e Comunidades Sustentáveis.
---

## 🛠️ Arquitetura da Solução (3-Tier)
O projeto está estruturado em uma arquitetura limpa em camadas:
* **Cliente (Frontend):** Aplicativo móvel multiplataforma desenvolvido em **Flutter**.
* **Servidor (Backend):** API REST desenvolvida em **Java (Spring Boot)** com **Spring Security** e **Spring Data JPA**.
* **Banco de Dados:** **PostgreSQL** para persistência relacional segura.
---

## 🗄️ Modelagem do Banco de Dados (Core)
O banco de dados relacional é composto pelas seguintes tabelas principais:
1. **`tb_users`:** Gerenciamento de usuários, perfis (`MORADOR`, `SINDICO`, `SUPORTE`), senhas criptografadas via **BCrypt** e controle de liberação (`status_aprovacao`).
2. **`tb_ocorrencias`:** Rastreio de chamados e manutenções do condomínio (com status *Aberto*, *Em Andamento* e *Resolvido*).
3. **`tb_user_logs`:** Tabela de auditoria imutável para garantir segurança e integridade dos dados.
---

## 🔒 Segurança e Diretrizes OWASP
* **Autenticação:** Obrigatoriedade de HTTPS e senhas protegidas estritamente com **BCrypt** (proibido texto limpo, MD5 ou SHA-1).
* **Prevenção de Injeção:** Uso de ORM (Hibernate) com consultas parametrizadas para barrar SQL Injection.
* **Controle de Acesso:** Implementação de RBAC (Role-Based Access Control) nas rotas protegidas da API.
---

## 📱 Prototipagem e Telas Principais
O fluxo do MVP compreende três telas principais validadas via testes práticos de usabilidade:
1. **Tela de Login / Autenticação:** Entrada segura com validação de credenciais.
2. **Tela Inicial (Painel do Morador / Ações Rápidas):** Mural de avisos oficiais e atalhos rápidos (*Emitir Boleto*, *Nova Ocorrência*, *Reservar Área*).
3. **Tela de Acompanhamento:** Histórico e status em tempo real das ocorrências do morador.
