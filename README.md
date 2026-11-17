# FinTwin

Aplicativo web de gestão financeira pessoal cujo destaque é o **Gêmeo Digital**: uma projeção do patrimônio do usuário em até 35 anos. Junto com ela, o FinTwin reúne contas, transações categorizadas automaticamente, simulação de crises, uma trava contra compras por impulso e exportação de relatórios.

Projeto acadêmico de Engenharia de Software da Universidade Católica do Salvador (UCSAL).

---

## Tecnologias

| Camada | O que é usado |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4 (Web, Data JPA, Security, Validation, Actuator) |
| Telas | Thymeleaf, HTML, CSS e JavaScript puro (sem framework de front-end) |
| Banco de dados | PostgreSQL (hospedado na Neon) e H2 em memória nos testes |
| Autenticação | JWT (token de 15 minutos) e senhas com BCrypt |
| Dois fatores | TOTP, compatível com Google Authenticator e similares, com QR Code gerado pelo ZXing |
| Build | Maven (wrapper incluído no projeto) |

---

## Funcionalidades

### Acesso e segurança
- **Cadastro e login**: e-mail e senha, com o perfil Individual ou Familiar escolhido no cadastro.
- **Verificação em duas etapas (2FA)**: opcional, ativada no perfil lendo um QR Code com um aplicativo autenticador. Com ela ativa, o login pede o código de 6 dígitos.
- **Recuperação de senha**: gera um código de redefinição. Como o projeto não tem envio de e-mail, o código aparece na própria tela (modo de demonstração).
- **Sessão**: o token de acesso vale 15 minutos a partir do login. Depois disso, ou ao fechar a aba, é preciso entrar de novo.
- **Direito ao esquecimento (LGPD)**: o usuário pode pedir a exclusão da conta no perfil. O acesso é bloqueado na hora, e uma rotina que roda a cada 15 minutos apaga todos os dados dele em até 24 horas.

### Perfil
- Edição de nome, telefone e fuso horário.
- Foto de perfil em JPG ou PNG de até 5 MB, recortada em quadrado e reduzida pelo servidor.
- Ativação e desativação do 2FA e o pedido de exclusão da conta.

### Dashboard
- Indicadores do mês: saldo total das contas, receitas, despesas e quantidade de contas.
- **Contas**: o usuário cadastra as contas (corrente, poupança ou investimento) e informa o saldo atual. O saldo pode ser atualizado a qualquer momento, e a tela mostra quando ele foi informado. Só a conta corrente aceita saldo negativo. Ao remover uma conta, as transações dela são mantidas.
- Resumo das transações recentes, das despesas por categoria, da última projeção do Gêmeo Digital e dos gastos em quarentena.

### Transações
- Lançamento de receitas e despesas.
- **Categorização automática**: ao lançar, o sistema sugere a categoria pela descrição (por exemplo, "mercado" vira Alimentação e "uber" vira Transporte). Categorias: Alimentação, Moradia, Transporte, Saúde, Educação, Lazer, Vestuário, Investimento, Salário e Serviços.
- A categoria sugerida pode ser corrigida. Cada correção fica registrada, e o sistema calcula a taxa de acerto da sugestão automática.
- Listagem com filtros por período (este mês, últimos 90 dias ou tudo), por tipo e por busca na descrição.

### Gêmeo Digital
- Projeção do patrimônio ano a ano, de 1 a 35 anos, a partir do patrimônio inicial, do aporte mensal e da taxa de rendimento anual, com juros compostos mensais.
- Gráfico da evolução do patrimônio comparada ao dinheiro investido, com o patrimônio final, o total investido e o ganho com rendimentos.
- Cada projeção fica salva como um cenário para comparação e pode ser excluída.

### Teste de Estresse
- Simula uma crise a partir da renda, das despesas e da reserva de emergência do usuário, com a queda da renda (%) e a duração da crise (em meses).
- Mostra o déficit mensal, o impacto no patrimônio e por quantos meses a reserva aguenta, com um gráfico mês a mês do que fica coberto e do que fica sem cobertura.

### Trava de Impulso
- Um gasto não planejado (item, valor e motivo) entra em **quarentena de 24 horas** antes de virar despesa, com uma contagem regressiva na tela.
- Passado o prazo, o usuário decide: **liberar** (a despesa é registrada nas transações) ou **cancelar** (nada é gasto).
- A tela mostra o total em espera e o histórico das decisões.

### Relatórios
- Exportação das transações em **CSV** (separado por ponto e vírgula, abre direto no Excel), com até **5.000 registros** por arquivo.
- Resumo antes de exportar: quantidade de transações, período coberto e saldo líquido.
- Cada exportação fica registrada no histórico do usuário.

### Família (prévia)
- Página demonstrativa de como seria a gestão de um grupo familiar: membros, meta compartilhada, aprovação de gastos e permissões, com visões de Responsável e de Membro.
- É apenas visual: usa dados de exemplo e não grava nada no banco.

---

## Estrutura do Projeto

```
FinTwin/
└── demo/                                   projeto Spring Boot
    ├── pom.xml
    ├── mvnw / mvnw.cmd                     Maven wrapper
    └── src/
        ├── main/java/FinTwin/EGR/demo/
        │   ├── controller/                 páginas e API REST (/api/...)
        │   ├── service/                    regras de negócio
        │   ├── model/                      entidades (tabelas do banco)
        │   ├── repository/                 acesso ao banco (Spring Data JPA)
        │   ├── dto/                        dados de entrada e saída da API
        │   ├── security/                   JWT, 2FA (TOTP) e configuração de segurança
        │   └── exception/                  tratamento de erros da API
        ├── main/resources/
        │   ├── templates/                  telas (Thymeleaf)
        │   ├── static/css, static/js       estilos e scripts de cada tela
        │   └── application.properties      configuração
        └── test/                           testes automatizados (JUnit + H2)
```

---

## Como Rodar Localmente

### 1. Pré-requisitos
- **Java 21** (JDK) instalado.
- Acesso a um banco **PostgreSQL** com as tabelas do FinTwin: o banco da Neon ou uma cópia no PostgreSQL local.
- Não é preciso instalar o Maven: o projeto usa o wrapper (`mvnw` / `mvnw.cmd`) da pasta `demo/`.

### 2. Variáveis de ambiente

A aplicação lê a conexão com o banco e o segredo do login de variáveis de ambiente (veja `demo/src/main/resources/application.properties`):

Para usar o PostgreSQL local, basta definir `DB_PASSWORD` e `JWT_SECRET`: a URL e o usuário padrão já apontam para `localhost:5432/fintwin` com o usuário `fintwin_app`.

O `JWT_SECRET` assina os tokens de login: quem conhece o valor consegue criar um token válido, então ele deve ser longo e aleatório. Para gerar um no PowerShell:

```powershell
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 62) + -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 62)
```

Trocar o `JWT_SECRET` desconecta todos os usuários logados.

> Senhas e segredos nunca devem ser escritos no código nem em arquivos versionados. O arquivo `.env` já está no `.gitignore`.

### 3. Executar

**Pelo Eclipse**
1. *File → Import → Maven → Existing Maven Projects* e selecione a pasta `demo/`.
2. Em *Run → Run Configurations*, crie uma configuração para a classe `FinTwin.EGR.demo.DemoApplication`.
3. Na aba *Environment*, cadastre `DB_URL`, `DB_USER`, `DB_PASSWORD` e `JWT_SECRET` e clique em *Apply*.
4. Clique em *Run*.

Ao trocar alguma variável, pare a aplicação (*Terminate*) e rode de novo: o reinício automático do DevTools não relê as variáveis.

**Pelo terminal** (dentro da pasta `demo/`)

```powershell
# Windows (PowerShell)
$env:DB_URL="jdbc:postgresql://SERVIDOR/neondb?sslmode=require"
$env:DB_USER="neondb_owner"
$env:DB_PASSWORD="sua-senha"
$env:JWT_SECRET="segredo-aleatorio-com-pelo-menos-64-caracteres"
.\mvnw.cmd spring-boot:run
```

```bash
# Linux / macOS
export DB_URL="jdbc:postgresql://SERVIDOR/neondb?sslmode=require"
export DB_USER="neondb_owner"
export DB_PASSWORD="sua-senha"
export JWT_SECRET="segredo-aleatorio-com-pelo-menos-64-caracteres"
./mvnw spring-boot:run
```

### 4. Acessar
- Aplicação: **http://localhost:8080**. Crie uma conta na tela inicial e faça login.
- Para conferir se está no ar: **http://localhost:8080/actuator/health**, que deve responder `{"status":"UP"}`.

### 5. Testes

```bash
./mvnw test
```

Os testes usam um banco **H2 em memória** (configurado em `demo/src/test/resources/application.properties`) e não precisam das variáveis acima nem acessam a Neon.

### Problemas comuns

| Mensagem ao iniciar | Causa |
|---|---|
| `Could not resolve placeholder 'JWT_SECRET'` | A variável `JWT_SECRET` não foi definida. |
| `JWT_SECRET precisa ter pelo menos 64 caracteres` | O segredo é curto demais. |
| `password authentication failed` |  `DB_PASSWORD` incorretos. |
| `Schema-validation: missing table` ou `missing column` | O banco não tem as tabelas que as entidades Java esperam. |

### Sobre o banco

A aplicação usa um banco que **já existe**, com as tabelas criadas. Ela não cria nem altera tabelas: ao iniciar, o Hibernate só **confere** se as tabelas batem com as entidades Java (`ddl-auto=validate`). Uma mudança de estrutura deve ser feita direto no banco (por exemplo, no SQL Editor da Neon) e refletida na entidade correspondente em `demo/src/main/java/FinTwin/EGR/demo/model/`.

---

## Créditos

- Documento base: *Documentação de Arquitetura e Modelagem de Casos de Uso — FinTwin* (agosto de 2026)
- Autor: Leandro Garcia Magalhães Cerqueira
- Orientador: Elton Figueiredo da Silva
- Instituição: UCSAL — Universidade Católica do Salvador
