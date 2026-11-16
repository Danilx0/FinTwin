const sessaoDashboard = exigirSessao();
const coresCategoria = ['#3DF1FF', '#B7E7FC', '#FFB347', '#7DD3FC', '#94A3B8', '#A78BFA', '#FB7185'];

if (sessaoDashboard) {
  iniciarDashboard();
  document.getElementById('account-form').addEventListener('submit', vincularConta);
  document.getElementById('sync-all-accounts').addEventListener('click', sincronizarTodasContas);
}

async function iniciarDashboard() {
  document.getElementById('dashboard-date').textContent = new Intl.DateTimeFormat('pt-BR', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
  }).format(new Date());

  await Promise.allSettled([
    carregarTransacoesDashboard(),
    carregarContasDashboard(),
    carregarProjecoesDashboard()
  ]);
  carregarQuarentenasDashboard();
}

async function carregarTransacoesDashboard() {
  const lista = document.getElementById('dashboard-transactions');
  const categorias = document.getElementById('dashboard-categories');
  lista.replaceChildren();
  categorias.replaceChildren();
  try {
    const dados = await requisicaoApi(`/api/financas/transacoes/${sessaoDashboard.usuario.id}`);
    const agora = new Date();
    const transacoesDoMes = dados.filter(item => {
      const data = new Date(item.dataHora);
      return data.getMonth() === agora.getMonth() && data.getFullYear() === agora.getFullYear();
    });
    const somar = (itens, tipo) => itens
      .filter(item => item.tipoTransacao === tipo)
      .reduce((total, item) => total + Number(item.valor || 0), 0);
    document.getElementById('dashboard-income').textContent = formatarMoeda(somar(transacoesDoMes, 'RECEITA'));
    document.getElementById('dashboard-expenses').textContent = formatarMoeda(somar(transacoesDoMes, 'DESPESA'));

    const recentes = dados.slice().sort((a, b) => new Date(b.dataHora) - new Date(a.dataHora)).slice(0, 5);
    recentes.forEach(item => lista.append(criarLinhaTransacao(item)));
    if (!recentes.length) lista.textContent = 'Nenhuma transação cadastrada.';

    const despesasPorCategoria = new Map();
    transacoesDoMes.filter(item => item.tipoTransacao === 'DESPESA').forEach(item => {
      despesasPorCategoria.set(item.categoria, (despesasPorCategoria.get(item.categoria) || 0) + Number(item.valor || 0));
    });
    renderizarCategorias(despesasPorCategoria);
    document.getElementById('category-subtitle').textContent = agora.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
  } catch (erro) {
    lista.textContent = erro.message;
    categorias.textContent = 'Não foi possível carregar as categorias.';
  }
}

function criarLinhaTransacao(transacao) {
  const item = document.createElement('div');
  item.className = 'tx-item';
  const icone = document.createElement('div');
  icone.className = 'tx-item__icon';
  icone.textContent = transacao.tipoTransacao === 'RECEITA' ? '↗' : '↘';
  const info = document.createElement('div');
  info.className = 'tx-item__info';
  const nome = document.createElement('div');
  nome.className = 'tx-item__name';
  nome.textContent = transacao.descricao;
  const categoria = document.createElement('div');
  categoria.className = 'tx-item__cat';
  categoria.textContent = formatarEnum(transacao.categoria);
  info.append(nome, categoria);
  const lado = document.createElement('div');
  lado.className = 'tx-item__right';
  const valor = document.createElement('div');
  valor.className = `tx-item__amount ${transacao.tipoTransacao === 'RECEITA' ? 'tx-item__amount--credit' : 'tx-item__amount--debit'}`;
  valor.textContent = `${transacao.tipoTransacao === 'RECEITA' ? '+' : '−'}${formatarMoeda(Number(transacao.valor || 0))}`;
  const data = document.createElement('div');
  data.className = 'tx-item__date';
  data.textContent = new Date(transacao.dataHora).toLocaleDateString('pt-BR');
  lado.append(valor, data);
  item.append(icone, info, lado);
  return item;
}

function renderizarCategorias(categorias) {
  const elemento = document.getElementById('dashboard-categories');
  const total = [...categorias.values()].reduce((soma, valor) => soma + valor, 0);
  if (!total) {
    elemento.textContent = 'Sem despesas registradas neste mês.';
    return;
  }

  [...categorias.entries()].sort((a, b) => b[1] - a[1]).slice(0, 5).forEach(([categoria, valor], indice) => {
    const item = document.createElement('div');
    item.className = 'cat-item';
    const linha = document.createElement('div');
    linha.className = 'cat-item__row';
    const nome = document.createElement('div');
    nome.className = 'cat-item__name';
    const ponto = document.createElement('span');
    ponto.className = 'cat-item__dot';
    ponto.style.background = coresCategoria[indice % coresCategoria.length];
    nome.append(ponto, document.createTextNode(formatarEnum(categoria)));
    const percentual = Math.round(valor / total * 100);
    const valorElemento = document.createElement('div');
    valorElemento.className = 'cat-item__pct';
    valorElemento.textContent = `${percentual}%`;
    linha.append(nome, valorElemento);
    const progresso = document.createElement('div');
    progresso.className = 'progress-wrap';
    const barra = document.createElement('div');
    barra.className = 'progress-bar';
    barra.style.width = `${percentual}%`;
    barra.style.background = coresCategoria[indice % coresCategoria.length];
    progresso.append(barra);
    item.append(linha, progresso);
    elemento.append(item);
  });
}

async function carregarContasDashboard() {
  const lista = document.getElementById('dashboard-accounts');
  lista.replaceChildren();
  try {
    const contas = await requisicaoApi(`/api/open-finance/contas/${sessaoDashboard.usuario.id}`);
    const total = contas.reduce((soma, conta) => soma + Number(conta.saldo || 0), 0);
    document.getElementById('dashboard-balance').textContent = formatarMoeda(total);
    document.getElementById('dashboard-account-count').textContent = String(contas.length);
    document.getElementById('accounts-subtitle').textContent = `${contas.length} conta(s) · saldo cadastrado`;
    contas.forEach(conta => lista.append(criarLinhaConta(conta)));
    if (!contas.length) lista.textContent = 'Nenhuma conta bancária vinculada.';
  } catch (erro) {
    lista.textContent = erro.message;
    document.getElementById('dashboard-balance').textContent = '—';
    document.getElementById('dashboard-account-count').textContent = '—';
  }
}

function criarLinhaConta(conta) {
  const item = document.createElement('div');
  item.className = 'conta-item';
  const detalhes = document.createElement('div');
  const banco = document.createElement('div');
  banco.className = 'conta-item__bank';
  banco.textContent = `🏦 ${conta.instituicaoFinanceira}`;
  const identificacao = document.createElement('div');
  identificacao.className = 'conta-item__agency';
  identificacao.textContent = `${formatarEnum(conta.tipoConta)}${conta.numeroConta ? ` ·· ${conta.numeroConta.slice(-4)}` : ''}`;
  detalhes.append(banco, identificacao);
  const saldo = document.createElement('div');
  saldo.className = 'conta-item__balance';
  saldo.textContent = formatarMoeda(Number(conta.saldo || 0));
  const atualizacao = document.createElement('span');
  atualizacao.className = 'conta-item__sync';
  atualizacao.textContent = conta.dataUltimaSincronizacao
    ? `Atualizada ${new Date(conta.dataUltimaSincronizacao).toLocaleString('pt-BR')}`
    : 'Aguardando primeira sincronização';
  const acao = document.createElement('button');
  acao.type = 'button';
  acao.className = 'btn-sm';
  acao.textContent = 'Sincronizar';
  acao.addEventListener('click', () => sincronizarConta(conta.id));
  const coluna = document.createElement('div');
  coluna.append(saldo, atualizacao, acao);
  item.append(detalhes, coluna);
  return item;
}

async function carregarProjecoesDashboard() {
  const subtitulo = document.getElementById('projection-subtitle');
  try {
    const projecoes = await requisicaoApi(`/api/inteligencia/gemeo-digital/${sessaoDashboard.usuario.id}`);
    const ultima = projecoes.slice().sort((a, b) => new Date(b.dataCriacao) - new Date(a.dataCriacao))[0];
    if (!ultima || !ultima.pontosProjecao?.length) {
      subtitulo.textContent = 'Nenhuma projeção salva';
      return;
    }
    subtitulo.textContent = `${ultima.horizonteAnos} anos · ${ultima.nomeCenario}`;
    desenharProjecao(ultima);
  } catch (erro) {
    subtitulo.textContent = erro.message;
  }
}

function desenharProjecao(projecao) {
  const pontos = [
    { ano: 0, patrimonioProjetado: Number(projecao.patrimonioInicial) },
    ...projecao.pontosProjecao.map(ponto => ({ ano: ponto.ano, patrimonioProjetado: Number(ponto.patrimonioProjetado) }))
  ];
  const minimo = Math.min(...pontos.map(ponto => ponto.patrimonioProjetado));
  const maximo = Math.max(...pontos.map(ponto => ponto.patrimonioProjetado));
  const intervalo = maximo - minimo || 1;
  const coordenadas = pontos.map((ponto, indice) => ({
    x: indice / Math.max(pontos.length - 1, 1) * 700,
    y: 175 - (ponto.patrimonioProjetado - minimo) / intervalo * 150
  }));
  const caminho = coordenadas.map((ponto, indice) => `${indice ? 'L' : 'M'}${ponto.x.toFixed(1)} ${ponto.y.toFixed(1)}`).join(' ');
  document.getElementById('projection-line').setAttribute('d', caminho);
  document.getElementById('projection-area').setAttribute('d', `${caminho} L700 190 L0 190 Z`);
  document.getElementById('projection-start-label').textContent = String(new Date().getFullYear());
  document.getElementById('projection-end-label').textContent = formatarMoeda(pontos.at(-1).patrimonioProjetado);
}

async function vincularConta(evento) {
  evento.preventDefault();
  const feedback = document.getElementById('account-feedback');
  const valores = Object.fromEntries(new FormData(evento.currentTarget).entries());
  mostrarFeedback(feedback, 'Vinculando conta...');
  try {
    await requisicaoApi(`/api/open-finance/contas/${sessaoDashboard.usuario.id}`, {
      method: 'POST',
      body: JSON.stringify(valores)
    });
    evento.currentTarget.reset();
    mostrarFeedback(feedback, 'Conta vinculada. O saldo permanece zerado até haver integração com a instituição financeira.');
    await carregarContasDashboard();
  } catch (erro) {
    mostrarFeedback(feedback, erro.message, true);
  }
}

async function sincronizarConta(contaId) {
  try {
    await requisicaoApi(`/api/open-finance/sincronizar/${contaId}`, { method: 'POST' });
    await carregarContasDashboard();
  } catch (erro) {
    mostrarFeedback(document.getElementById('account-feedback'), erro.message, true);
  }
}

async function sincronizarTodasContas() {
  try {
    await requisicaoApi(`/api/open-finance/sincronizar-todas/${sessaoDashboard.usuario.id}`, { method: 'POST' });
    await carregarContasDashboard();
    mostrarFeedback(document.getElementById('account-feedback'), 'Horários de sincronização atualizados. Os saldos não são consultados sem integração bancária.');
  } catch (erro) {
    mostrarFeedback(document.getElementById('account-feedback'), erro.message, true);
  }
}

function carregarQuarentenasDashboard() {
  let quarentenas = [];
  try {
    quarentenas = JSON.parse(sessionStorage.getItem(`fintwin.quarentenas.${sessaoDashboard.usuario.id}`) || '[]');
  } catch {
    quarentenas = [];
  }
  const agora = Date.now();
  const emEspera = quarentenas.filter(item => item.liberacao > agora);
  document.getElementById('dashboard-impulso-total').textContent = formatarMoeda(emEspera.reduce((soma, item) => soma + Number(item.valor || 0), 0));
  document.getElementById('dashboard-impulso-espera').textContent = String(emEspera.length);
  document.getElementById('dashboard-impulso-liberados').textContent = String(quarentenas.length - emEspera.length);
}
