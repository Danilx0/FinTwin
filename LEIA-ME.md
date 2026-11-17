# FinTwin — Leia-Me de Contexto do Projeto

## O que é o FinTwin

FinTwin é um aplicativo de gestão financeira pessoal e familiar cujo diferencial é o **Gêmeo Digital (Digital Twin)**: uma projeção preditiva do patrimônio líquido do usuário em horizontes de até 35 anos. O sistema combina integração bancária via Open Finance, categorização automática de despesas por IA e ferramentas de economia comportamental para transformar dados financeiros em decisões conscientes.

Projeto acadêmico desenvolvido na Universidade Católica do Salvador (UCSAL) — Engenharia de Software.

---

## Perfis de Usuário

| Perfil | Descrição |
|---|---|
| Usuário Individual | Pessoa física que gerencia finanças próprias |
| Usuário Familiar | Gerencia finanças de um grupo familiar |
| Consultor | Profissional que assessora clientes dentro da plataforma |
| Administrador do Sistema | Gerencia a operação e configurações globais |

---

## Funcionalidades Principais

### Acesso e Segurança
- **UC01 – Cadastro**: criação de conta e perfil de gestão
- **UC02 – Login**: autenticação com credenciais
- **UC03 – 2FA (TOTP)**: validação obrigatória em menos de 300 ms
- **UC04 – Recuperar Senha**: envio seguro para e-mail cadastrado
- **UC05 – Gerenciar Perfil**: atualização de dados cadastrais e foto de perfil

### Core Business
- **UC06 – Sincronizar Contas Bancárias**: consolidação via Open Finance a cada 6 horas
- **UC07 – Categorizar Transações com IA**: classificação automática de despesas com meta de 95% de precisão após 100 interações de remapeamento
- **UC08 – Gêmeo Digital (Projeção)**: projeção gráfica do patrimônio líquido em até 35 anos
- **UC09 – Teste de Estresse**: simulação de cenários de crise (ex.: redução de 50% da renda)
- **UC10 – Trava de Impulso**: "Modo Quarentena" com espera obrigatória de 24 h para gastos manuais
- **UC11 – Exportar Relatórios**: download de relatórios consolidados com até 5.000 registros

---

## Arquitetura — Módulos Principais

### Módulo de Acesso e Segurança
Centraliza autenticação e conformidade com a LGPD. Sessões expiram após **15 minutos**. Funcionalidade de "Esquecimento" (exclusão de dados) executada em até **24 horas**.

### Módulo Core e Inteligência
Executa algoritmos de categorização de despesas e projeção preditiva (Gêmeo Digital). Tempo máximo de processamento complexo: **4 segundos**.

### Módulo de Gestão e Dashboard
Entrega a interface responsiva (320 px a 3440 px), painéis customizáveis, suporte a temas Claro/Escuro e acessibilidade **WCAG 2.1 AA**.

---

## Integrações Externas

- **Open Finance**: sincronização de dados bancários de múltiplas instituições
- **Serviço de E-mail**: envio de recuperação de senha e notificações
- **Autenticador TOTP**: geração e validação de códigos 2FA

---

## Requisitos Não-Funcionais Relevantes

| Categoria | Detalhe |
|---|---|
| Segurança | 2FA obrigatório; criptografia de dados; conformidade LGPD |
| Desempenho | Validação 2FA < 300 ms; processamento IA < 4 s |
| Disponibilidade | Sincronização bancária a cada 6 h |
| Acessibilidade | WCAG 2.1 AA; responsividade 320 px – 3440 px |
| Privacidade | Exclusão de dados ("Esquecimento") em até 24 h |

---

## Como Rodar a Aplicação

### Pré-requisitos
- **Java 21** (JDK)
- Acesso a um banco **PostgreSQL**. Em produção o banco fica na **Neon** (Postgres gerenciado, região AWS São Paulo).
- O Maven não precisa ser instalado: o projeto usa o wrapper (`mvnw` / `mvnw.cmd`) que fica na pasta `demo/`.

### Variáveis de ambiente do banco

A aplicação lê a conexão com o banco de três variáveis de ambiente (veja `demo/src/main/resources/application.properties`):

| Variável | Obrigatória | Exemplo | Padrão, se não for definida |
|---|---|---|---|
| `DB_URL` | Sim, para usar a Neon | `jdbc:postgresql://ep-xxxx.sa-east-1.aws.neon.tech/neondb?sslmode=require` | `jdbc:postgresql://localhost:5432/fintwin` |
| `DB_USER` | Sim, para usar a Neon | `neondb_owner` | `fintwin_app` |
| `DB_PASSWORD` | Sempre | (senha do banco) | — |

Como montar os valores a partir da URL de conexão da Neon (`postgresql://USUARIO:SENHA@SERVIDOR/BANCO?sslmode=require`):
- `DB_URL`: começa com `jdbc:postgresql://`, seguido de `SERVIDOR/BANCO?sslmode=require`. Usuário e senha **não** entram na URL.
- `DB_USER`: o `USUARIO` da URL.
- `DB_PASSWORD`: a `SENHA` da URL.
- Use a URL de conexão **direta**, com *Connection pooling* desligado (o servidor não tem `-pooler` no nome).

> As senhas nunca devem ser escritas no código nem em arquivos versionados. O arquivo `.env` já está no `.gitignore`.

### Tabelas do banco

A aplicação usa um banco que **já existe**, com as tabelas criadas (o da Neon e uma cópia no PostgreSQL local). Ela não cria nem altera tabelas: ao iniciar, o Hibernate só **confere** se as tabelas batem com as entidades Java (`ddl-auto=validate`) e para com erro se faltar alguma tabela ou coluna. Qualquer mudança de estrutura deve ser feita direto no banco (por exemplo, no SQL Editor da Neon) e refletida na entidade correspondente em `demo/src/main/java/FinTwin/EGR/demo/model/`.

### Executando

**Pelo Eclipse:** importe a pasta `demo/` como projeto Maven. Em *Run Configurations → Environment*, cadastre `DB_URL`, `DB_USER` e `DB_PASSWORD` e rode a classe principal como *Spring Boot App* / *Java Application*. Ao trocar as variáveis, pare a aplicação (*Terminate*) e rode de novo: o reinício automático do DevTools não relê as variáveis.

**Pelo terminal** (dentro de `demo/`):

```powershell
# Windows (PowerShell)
$env:DB_URL="jdbc:postgresql://SERVIDOR/neondb?sslmode=require"
$env:DB_USER="neondb_owner"
$env:DB_PASSWORD="sua-senha"
.\mvnw.cmd spring-boot:run
```

```bash
# Linux / macOS
export DB_URL="jdbc:postgresql://SERVIDOR/neondb?sslmode=require"
export DB_USER="neondb_owner"
export DB_PASSWORD="sua-senha"
./mvnw spring-boot:run
```

A aplicação sobe em **http://localhost:8080**. Para conferir se está no ar, use `http://localhost:8080/actuator/health`, que deve responder `{"status":"UP"}`.

### Testes

```bash
./mvnw test
```

Os testes usam um banco **H2 em memória** (configurado em `demo/src/test/resources/application.properties`) e não precisam das variáveis acima nem acessam a Neon.

---

## Estrutura de Modelagem Produzida

- Diagramas de Casos de Uso (Acesso e Core Business)
- Diagrama de Contexto e Fronteiras de Módulos
- Diagrama de Classes (Visão Macro — herança, sobrecarga, polimorfismo)
- Diagramas de Atividades (Visão Geral, Cadastro, Core Business)
- Diagramas de Sequência (Visão Geral, Cadastro, Integrações e Projeção do Gêmeo Digital)

---

## Referências

- Documento base: *Documentação de Arquitetura e Modelagem de Casos de Uso — FinTwin* (Agosto 2026)
- Autor: Leandro Garcia Magalhães Cerqueira
- Orientador: Elton Figueiredo da Silva
- Instituição: UCSAL — Universidade Católica do Salvador
