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
