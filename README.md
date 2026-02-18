
---

# 📦 Inventory & Production Planning API

Esta é uma solução robusta de **Back-End** desenvolvida para gerenciar o ciclo de vida de produtos, matérias-primas e o planejamento inteligente de produção. O sistema vai além do CRUD básico, oferecendo um algoritmo de sugestão de produção baseado na disponibilidade real de estoque.

---

## 🚀 Sobre o Projeto

Projeto criado para a empresa PROJEDATA, estruturado sob os princípios de **Clean Architecture** e as melhores práticas de **APIs RESTful**. A separação de responsabilidades garante uma manutenção simplificada e facilidade de evolução.

### Diferencial Técnico: Sugestão de Produção (RF004)

A lógica de negócio de planejamento utiliza um **Algoritmo Guloso (Greedy)** implementado na camada de serviço. Ele prioriza a produção de itens de maior valor agregado, realizando o cálculo de consumo virtual de estoque em tempo real.

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Descrição |
| --- | --- |
| **Java 17+** | Linguagem base para o desenvolvimento. |
| **Spring Boot 3** | Framework para agilidade e configuração da aplicação. |
| **Spring Data JPA** | Abstração de persistência de dados. |
| **PostgreSQL** | Banco de dados relacional robusto. |
| **Flyway** | Versionamento e automação de migrações de banco. |
| **JUnit 5 & Mockito** | Ecossistema completo para testes automatizados. |
| **Maven** | Gerenciador de dependências e build. |

---

## 📐 Arquitetura do Sistema

A aplicação segue o padrão de camadas para garantir desacoplamento:

* **Controller:** Porta de entrada, lida com requisições HTTP e códigos de status.
* **Service:** O "cérebro" da aplicação, onde residem as regras de negócio.
* **Repository:** Interface de comunicação direta com o banco via JPA.
* **Entity:** Mapeamento objeto-relacional (ORM) das tabelas.
* **DTO (Data Transfer Object):** Segurança e performance no tráfego de dados.

---

## 📋 Funcionalidades Principais

### **Gestão de Inventário**

* **Produtos & Matérias-Primas:** Gerenciamento completo (CRUD).
* **BOM (Bill of Materials):** Associação dinâmica entre produtos e seus componentes necessários.

### **Inteligência de Produção**

* Cálculo automático de capacidade produtiva com base no estoque.
* Cálculo de valor financeiro total da produção sugerida.
* **Priorização:** Algoritmo que maximiza o retorno financeiro respeitando a escassez de matéria-prima.

---

## 🚦 Começando

### Pré-requisitos

* JDK 17 ou superior.
* Maven 3.8+.
* Instância ativa do PostgreSQL.

### Configuração do Banco de Dados

1. Crie o banco de dados: `CREATE DATABASE inventory;`
2. Configure as credenciais no arquivo `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/inventory
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

```



### Execução

No terminal, utilize o Maven Wrapper:

**Windows:**

```bash
./mvnw.cmd spring-boot:run

```

**Linux/Mac:**

```bash
./mvnw spring-boot:run

```

A API estará disponível em: `http://localhost:8080`

---

## 📡 Endpoints da API

| Recurso | Método | Endpoint | Descrição |
| --- | --- | --- | --- |
| **Produtos** | `GET` | `/api/products` | Lista todos os produtos |
| **Matérias-Primas** | `POST` | `/api/raw-materials` | Cadastra novo insumo |
| **Associações** | `POST` | `/api/links` | Vincula insumo ao produto |
| **Planejamento** | `GET` | `/api/production/suggestion` | Retorna o plano de produção |

---

## 🧪 Qualidade de Software (Testes)

O projeto conta com uma suíte de testes unitários focada na integridade das regras de negócio.

```bash
./mvnw test

```

**Cenários cobertos:**

* Validação da prioridade por maior valor de venda.
* Consumo virtual de estoque (prevenção de "over-booking" de insumos).
* Cálculo do "gargalo" (mínimo de matéria-prima necessário).

---

## 📝 Respostas HTTP Padronizadas

A API comunica-se através de códigos de status semânticos:

* `200/201`: Sucesso e Criação.
* `204`: Remoção bem-sucedida.
* `400/404`: Erros de requisição ou recurso não encontrado.
* `409`: Conflitos de regra de negócio ou duplicidade.

---

**Desenvolvido como projeto de excelência técnica para controle industrial.**

---

Gostaria que eu gerasse o arquivo `README.md` formatado pronto para você baixar ou que eu detalhe como documentar o algoritmo guloso em uma seção técnica separada?
