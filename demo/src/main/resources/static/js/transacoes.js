const sessaoTransacoes = exigirSessao();
const formularioTransacoes = document.getElementById('transacao-form');
const feedbackTransacoes = document.getElementById('transacao-feedback');
const listaTransacoes = document.getElementById('transacoes-lista');
const campoBusca = document.getElementById('tx-busca');
const campoCategoria = document.getElementById('categoria');

const coresCategoriaTransacao = {
  ALIMENTACAO: '#B7E7FC', MORADIA: '#3DF1FF', TRANSPORTE: '#FFB347', SAUDE: '#FB7185', EDUCACAO: '#7DD3FC',
  LAZER: '#A78BFA', VESTUARIO: '#F0ABFC', INVESTIMENTO: '#34D399', SALARIO: '#3DF1FF', SERVICOS: '#94A3B8', OUTROS: '#64748B'
};
const rotulosPeriodo = { mes: 'Este mês', 90: 'Últimos 90 dias', tudo: 'Todo o histórico' };

let todasTransacoes = [];
let filtroPeriodo = 'mes';
let filtroTipo = 'TODOS';
let idsRecentes = new Set();

if (sessaoTransacoes) {
  carregarTransacoes();
  formularioTransacoes.addEventListener('submit', salvarTransacao);
  campoBusca.addEventListener('input', renderizarTransacoes);
  document.querySelectorAll('[data-periodo]').forEach(botao => botao.addEventListener('click', () => {
    filtroPeriodo = botao.dataset.periodo;
    marcarAtivo('[data-periodo]', botao);
    renderizarTransacoes();
  }));
  document.querySelectorAll('[data-tipo]').forEach(botao => botao.addEventListener('click', () => {
    filtroTipo = botao.dataset.tipo;
    marcarAtivo('[data-tipo]', botao);
    renderizarTransacoes();
  }));
  formularioTransacoes.querySelectorAll('input[name="tipoTransacao"]').forEach(opcao => opcao.addEventListener('change', () => {
    if (opcao.value === 'RECEITA' && campoCategoria.value === 'ALIMENTACAO') campoCategoria.value = 'SALARIO';
    if (opcao.value === 'DESPESA' && campoCategoria.value === 'SALARIO') campoCategoria.value = 'ALIMENTACAO';
  }));
}

function marcarAtivo(seletor, ativo) {
  document.querySelectorAll(seletor).forEach(botao => botao.setAttribute('aria-pressed', String(botao === ativo)));
}

async function salvarTransacao(evento) {
  evento.preventDefault();
  if (!formularioTransacoes.reportValidity()) return;
  const botao = formularioTransacoes.querySelector('button[type="submit"]');
  botao.disabled = true;
  mostrarFeedback(feedbackTransacoes, 'Salvando transação...');
  const valores = Object.fromEntries(new FormData(formularioTransacoes).entries());
  valores.valor = Number(valores.valor);
  if (!valores.dataHora) delete valores.dataHora;

  try {
    const criada = await requisicaoApi(`/api/financas/transacoes/${sessaoTransacoes.usuario.id}`, {
      method: 'POST',
      body: JSON.stringify(valores)
    });
    idsRecentes = new Set(criada?.id != null ? [criada.id] : []);
    mostrarFeedback(feedbackTransacoes, 'Transação salva.');
    formularioTransacoes.reset();
    await carregarTransacoes();
  } catch (erro) {
    mostrarFeedback(feedbackTransacoes, erro.message, true);
  } finally {
    botao.disabled = false;
  }
}

async function carregarTransacoes() {
  try {
    const dados = await requisicaoApi(`/api/financas/transacoes/${sessaoTransacoes.usuario.id}`);
    todasTransacoes = dados.slice().sort((a, b) => new Date(b.dataHora) - new Date(a.dataHora));
    renderizarTransacoes();
  } catch (erro) {
    document.getElementById('tx-contagem').textContent = 'Não foi possível carregar as transações';
    mostrarFeedback(feedbackTransacoes, erro.message, true);
  }
}

function filtrarTransacoes() {
  const agora = new Date();
  const busca = campoBusca.value.trim().toLocaleLowerCase('pt-BR');
  return todasTransacoes.filter(item => {
    const data = new Date(item.dataHora);
    if (filtroPeriodo === 'mes' && (data.getMonth() !== agora.getMonth() || data.getFullYear() !== agora.getFullYear())) return false;
    if (filtroPeriodo === '90' && agora - data > 90 * 86400000) return false;
    if (filtroTipo !== 'TODOS' && item.tipoTransacao !== filtroTipo) return false;
    if (busca && !String(item.descricao || '').toLocaleLowerCase('pt-BR').includes(busca)) return false;
    return true;
  });
}

function renderizarTransacoes() {
  const filtradas = filtrarTransacoes();
  atualizarResumo(filtradas);
  listaTransacoes.replaceChildren();

  const contagem = document.getElementById('tx-contagem');
  contagem.textContent = `${filtradas.length} ${filtradas.length === 1 ? 'lançamento' : 'lançamentos'} · ${rotulosPeriodo[filtroPeriodo].toLocaleLowerCase('pt-BR')}`;

  if (!filtradas.length) {
    const linha = document.createElement('tr');
    const celula = document.createElement('td');
    celula.colSpan = 4;
    const vazio = criarElemento('div', 'empty-state');
    if (todasTransacoes.length) {
      vazio.append(criarElemento('strong', null, 'Nada encontrado com esses filtros'), 'Amplie o período ou limpe a busca.');
    } else {
      vazio.append(criarElemento('strong', null, 'Nenhuma transação ainda'), 'Registre seu primeiro lançamento no formulário ao lado.');
    }
    celula.append(vazio);
    linha.append(celula);
    listaTransacoes.append(linha);
    return;
  }

  filtradas.slice(0, 200).forEach(transacao => listaTransacoes.append(criarLinha(transacao)));
  idsRecentes = new Set();
}

function criarLinha(transacao) {
  const receita = transacao.tipoTransacao === 'RECEITA';
  const data = new Date(transacao.dataHora);
  const dataTexto = data.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' });
  const linha = document.createElement('tr');
  if (idsRecentes.has(transacao.id)) linha.className = 'tx-row--nova';

  const descricao = criarElemento('div', 'tx-desc');
  const icone = criarElemento('span', `tx-desc__icon ${receita ? 'tx-desc__icon--receita' : 'tx-desc__icon--despesa'}`, receita ? '↗' : '↘');
  icone.setAttribute('aria-hidden', 'true');
  const texto = criarElemento('div', 'tx-desc__text');
  texto.append(
    criarElemento('div', 'tx-desc__name', transacao.descricao),
    criarElemento('div', 'tx-desc__sub', `${formatarEnum(transacao.categoria)} · ${dataTexto}`)
  );
  descricao.append(icone, texto);

  const tag = criarElemento('span', 'tag');
  const ponto = criarElemento('span', 'tag__dot');
  ponto.style.background = coresCategoriaTransacao[transacao.categoria] || '#64748B';
  tag.append(ponto, formatarEnum(transacao.categoria));

  const valor = criarElemento('span', `tx-value ${receita ? 'tx-value--receita' : 'tx-value--despesa'}`, `${receita ? '+' : '−'}${formatarMoeda(transacao.valor)}`);

  const celulas = [
    [descricao, ''],
    [tag, ''],
    [criarElemento('span', 'tx-date', dataTexto), ''],
    [valor, 'is-numeric']
  ];
  celulas.forEach(([conteudo, classe]) => {
    const celula = criarElemento('td', classe);
    celula.append(conteudo);
    linha.append(celula);
  });
  return linha;
}

function atualizarResumo(filtradas) {
  const receitas = filtradas.filter(item => item.tipoTransacao === 'RECEITA');
  const despesas = filtradas.filter(item => item.tipoTransacao === 'DESPESA');
  const soma = itens => itens.reduce((total, item) => total + Number(item.valor || 0), 0);
  const totalReceitas = soma(receitas);
  const totalDespesas = soma(despesas);
  const saldo = totalReceitas - totalDespesas;
  const plural = quantidade => `${quantidade} ${quantidade === 1 ? 'lançamento' : 'lançamentos'}`;

  document.getElementById('tx-total-receitas').textContent = formatarMoeda(totalReceitas);
  document.getElementById('tx-total-despesas').textContent = formatarMoeda(totalDespesas);
  document.getElementById('tx-qtd-receitas').textContent = plural(receitas.length);
  document.getElementById('tx-qtd-despesas').textContent = plural(despesas.length);
  const saldoElemento = document.getElementById('tx-saldo');
  saldoElemento.textContent = `${saldo < 0 ? '−' : ''}${formatarMoeda(Math.abs(saldo))}`;
  saldoElemento.className = `stat__value ${saldo < 0 ? 'stat__value--negative' : 'stat__value--positive'}`;
  document.getElementById('tx-periodo-nota').textContent = rotulosPeriodo[filtroPeriodo];
}
