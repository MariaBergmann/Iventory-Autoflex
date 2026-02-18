INVENTORY & PRODUCTION PLANNING API

Descrição

Aplicação Back-End desenvolvida com Spring Boot para controle de
produtos, matérias-primas e planejamento de produção com base no estoque
disponível.

O projeto foi desenvolvido como trabalho acadêmico, mas estruturado
seguindo boas práticas profissionais de arquitetura REST e separação de
camadas.

Tecnologias Utilizadas

-   Java 17+
-   Spring Boot
-   Spring Data JPA
-   PostgreSQL
-   Flyway (controle de versão do banco)
-   Maven
-   JUnit 5 + Mockito (testes unitários)

Arquitetura

A aplicação segue arquitetura em camadas:

Controller → Service → Repository → Database

-   Entity: Representa as tabelas do banco de dados
-   DTO: Objetos utilizados para comunicação da API (entrada e saída)
-   Repository: Camada de acesso a dados (JPA)
-   Service: Regras de negócio
-   Controller: Endpoints REST

A regra de negócio de sugestão de produção (RF004) foi implementada em
Java, mantendo a lógica independente do banco de dados.

Requisitos Funcionais Implementados

RF001 – CRUD de Produtos - Criar produto - Atualizar produto - Deletar
produto - Listar produtos

RF002 – CRUD de Matérias-Primas - Criar matéria-prima - Atualizar
matéria-prima - Deletar matéria-prima - Listar matérias-primas

RF003 – Associação Produto ↔ Matéria-Prima (BOM) - Criar associação -
Remover associação - Listar associações

RF004 – Sugestão de Produção - Retorna quais produtos podem ser
produzidos - Calcula quantidade possível de cada produto - Calcula valor
total da produção - Prioriza produtos de maior valor (algoritmo
guloso/greedy)

Banco de Dados

Banco utilizado: PostgreSQL

Tabelas: - products - raw_materials - product_raw_materials

O versionamento do banco é feito com Flyway (migrations automáticas).

Pré-Requisitos

Antes de executar o projeto, é necessário instalar:

-   Java 17 ou superior
-   Maven (ou utilizar Maven Wrapper do projeto)
-   PostgreSQL 14 ou superior

Configuração do Banco

Criar banco:

CREATE DATABASE inventory;

Configuração padrão (application.properties):

spring.datasource.url=jdbc:postgresql://localhost:5432/inventory
spring.datasource.username=postgres spring.datasource.password=postgres

Execução do Projeto

Windows: ..cmd spring-boot:run

Linux / Mac: ./mvnw spring-boot:run

A aplicação iniciará em: http://localhost:8080

Principais Endpoints

Produtos: GET /api/products POST /api/products PUT /api/products/{id}
DELETE /api/products/{id}

Matérias-Primas: GET /api/raw-materials POST /api/raw-materials PUT
/api/raw-materials/{id} DELETE /api/raw-materials/{id}

Associações: GET /api/links POST /api/links DELETE /api/links/{id}

Sugestão de Produção: GET /api/production/suggestion

Testes

Para rodar os testes unitários:

Windows: ..cmd test

Linux / Mac: ./mvnw test

Os testes cobrem: - Prioridade por maior valor - Consumo virtual de
estoque - Cálculo do mínimo entre matérias-primas - Cálculo do valor
total

Códigos HTTP Utilizados

-   200 OK
-   201 Created
-   204 No Content
-   400 Bad Request
-   404 Not Found
-   409 Conflict

Autor

Projeto desenvolvido como trabalho acadêmico com foco em boas práticas
profissionais de desenvolvimento Back-End.
