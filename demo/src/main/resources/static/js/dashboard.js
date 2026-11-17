const sessaoDashboard = exigirSessao();
const coresCategoria = ['#3DF1FF', '#B7E7FC', '#FFB347', '#7DD3FC', '#94A3B8', '#A78BFA', '#FB7185'];

if (sessaoDashboard) {
  iniciarDashboard();
  document.getElementById('account-form').addEventListener('submit', vincularConta);
}

async function iniciarDashboard() {
  document.getElementById('dashboard-date').textContent = new Intl.DateTimeFormat('pt-BR', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
  }).format(new Date());

  await Promise.allSettled([
    carregarTransacoesDashboard(),
    carregarContasDashboard(),
    carregarProjecoesDashboard(),
    carregarQuarentenasDashboard()
  ]);
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
    document.getElementById('accounts-subtitle').textContent = contas.length
      ? `${contas.length} conta(s) · saldos informados por você`
      : 'Saldos informados por você';
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
  detalhes.className = 'conta-item__detalhes';
  const banco = document.createElement('div');
  banco.className = 'conta-item__bank';
  banco.textContent = `🏦 ${conta.instituicaoFinanceira}`;
  const identificacao = document.createElement('div');
  identificacao.className = 'conta-item__agency';
  identificacao.textContent = `${formatarEnum(conta.tipoConta)}${conta.numeroConta ? ` ·· ${conta.numeroConta.slice(-4)}` : ''}`;
  detalhes.append(banco, identificacao);
  const valor = Number(conta.saldo || 0);
  const saldo = document.createElement('div');
  saldo.className = `conta-item__balance${valor < 0 ? ' conta-item__balance--negativo' : ''}`;
  saldo.textContent = formatarMoeda(valor);
  const atualizacao = document.createElement('span');
  atualizacao.className = 'conta-item__sync';
  atualizacao.textContent = conta.saldoAtualizadoEm
    ? `Saldo de ${new Date(conta.saldoAtualizadoEm).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })}`
    : 'Saldo ainda não informado';
  const acao = document.createElement('button');
  acao.type = 'button';
  acao.className = 'btn-sm conta-item__acao';
  acao.textContent = 'Atualizar saldo';
  acao.setAttribute('aria-label', `Atualizar saldo de ${conta.instituicaoFinanceira}`);
  acao.dataset.contaId = conta.id;
  acao.addEventListener('click', () => abrirEdicaoSaldo(item, conta));
  const remover = document.createElement('button');
  remover.type = 'button';
  remover.className = 'conta-item__remover';
  remover.title = 'Remover conta';
  remover.setAttribute('aria-label', `Remover a conta ${conta.instituicaoFinanceira}`);
  remover.innerHTML = '<svg width="15" height="15" fill="none" viewBox="0 0 16 16" aria-hidden="true">'
    + '<path d="M2.5 4h11M6.5 4V2.75h3V4M4 4l.6 9.25h6.8L12 4M6.75 6.75v4M9.25 6.75v4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>';
  remover.addEventListener('click', () => pedirConfirmacaoRemocao(item, conta));
  const botoes = document.createElement('div');
  botoes.className = 'conta-item__acoes';
  botoes.append(acao, remover);
  const coluna = document.createElement('div');
  coluna.className = 'conta-item__valores';
  coluna.append(saldo, atualizacao, botoes);
  item.append(detalhes, coluna);
  return item;
}

function pedirConfirmacaoRemocao(item, conta) {
  document.querySelectorAll('#dashboard-accounts .conta-item__confirmar').forEach(caixa => {
    caixa.closest('.conta-item')?.classList.remove('conta-item--confirmando');
    caixa.remove();
  });

  const caixa = criarElemento('div', 'conta-item__confirmar');
  caixa.setAttribute('role', 'group');
  caixa.setAttribute('aria-label', `Confirmar remoção de ${conta.instituicaoFinanceira}`);
  const texto = criarElemento('span', 'conta-item__confirmar-texto',
    'Remover esta conta? As transações já registradas continuam salvas.');
  const confirmar = criarElemento('button', 'btn-sm btn-sm--perigo', 'Remover');
  confirmar.type = 'button';
  const cancelar = criarElemento('button', 'btn-sm conta-item__cancelar', 'Cancelar');
  cancelar.type = 'button';
  const botoes = criarElemento('div', 'conta-item__botoes');
  botoes.append(confirmar, cancelar);
  caixa.append(texto, botoes);

  const fechar = () => {
    caixa.remove();
    item.classList.remove('conta-item--confirmando');
    item.querySelector('.conta-item__remover')?.focus();
  };
  cancelar.addEventListener('click', fechar);
  caixa.addEventListener('keydown', evento => {
    if (evento.key === 'Escape') fechar();
  });
  confirmar.addEventListener('click', async () => {
    confirmar.disabled = true;
    cancelar.disabled = true;
    const posicao = [...document.querySelectorAll('#dashboard-accounts .conta-item')].indexOf(item);
    try {
      await requisicaoApi(`/api/open-finance/contas/${conta.id}`, { method: 'DELETE' });
      await carregarContasDashboard();
      mostrarFeedback(document.getElementById('account-feedback'), `Conta ${conta.instituicaoFinanceira} removida.`);
      const restantes = document.querySelectorAll('#dashboard-accounts .conta-item__acao');
      (restantes[Math.min(posicao, restantes.length - 1)] || document.getElementById('bank-name')).focus();
    } catch (erro) {
      confirmar.disabled = false;
      cancelar.disabled = false;
      mostrarFeedback(document.getElementById('account-feedback'), erro.message, true);
    }
  });

  item.classList.add('conta-item--confirmando');
  item.append(caixa);
  cancelar.focus();
}

function abrirEdicaoSaldo(item, conta) {
  const coluna = item.querySelector('.conta-item__valores');
  const formulario = document.createElement('form');
  formulario.className = 'conta-item__edicao';
  formulario.noValidate = true;

  const campo = document.createElement('div');
  campo.className = 'field__prefix';
  const prefixo = criarElemento('span', null, 'R$');
  const entrada = document.createElement('input');
  entrada.type = 'number';
  entrada.step = '0.01';
  entrada.inputMode = 'decimal';
  entrada.required = true;
  entrada.value = Number(conta.saldo || 0).toFixed(2);
  entrada.setAttribute('aria-label', `Novo saldo de ${conta.instituicaoFinanceira}`);
  if (conta.tipoConta !== 'CORRENTE') entrada.min = '0';
  campo.append(prefixo, entrada);

  const salvar = criarElemento('button', 'btn-sm', 'Salvar');
  salvar.type = 'submit';
  const cancelar = criarElemento('button', 'btn-sm conta-item__cancelar', 'Cancelar');
  cancelar.type = 'button';
  const erro = criarElemento('p', 'conta-item__erro');
  erro.setAttribute('role', 'alert');

  const botoes = criarElemento('div', 'conta-item__botoes');
  botoes.append(salvar, cancelar);
  formulario.append(campo, botoes, erro);

  const fechar = () => {
    formulario.replaceWith(coluna);
    coluna.querySelector('.conta-item__acao').focus();
  };
  cancelar.addEventListener('click', fechar);
  formulario.addEventListener('keydown', evento => {
    if (evento.key === 'Escape') fechar();
  });
  formulario.addEventListener('submit', async evento => {
    evento.preventDefault();
    if (entrada.value.trim() === '' || Number.isNaN(Number(entrada.value))) {
      erro.textContent = 'Informe um valor.';
      entrada.focus();
      return;
    }
    salvar.disabled = true;
    erro.textContent = '';
    try {
      await requisicaoApi(`/api/open-finance/contas/${conta.id}/saldo`, {
        method: 'PUT',
        body: JSON.stringify({ saldo: entrada.value })
      });
      mostrarFeedback(document.getElementById('account-feedback'), `Saldo de ${conta.instituicaoFinanceira} atualizado.`);
      await carregarContasDashboard();
      document.querySelector(`.conta-item__acao[data-conta-id="${conta.id}"]`)?.focus();
    } catch (falha) {
      erro.textContent = falha.message;
      salvar.disabled = false;
      entrada.focus();
    }
  });

  coluna.replaceWith(formulario);
  entrada.focus();
  entrada.select();
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
  const formulario = evento.currentTarget;
  const feedback = document.getElementById('account-feedback');
  const valores = Object.fromEntries(new FormData(formulario).entries());
  if (!valores.saldo) delete valores.saldo;
  mostrarFeedback(feedback, 'Adicionando conta...');
  try {
    await requisicaoApi(`/api/open-finance/contas/${sessaoDashboard.usuario.id}`, {
      method: 'POST',
      body: JSON.stringify(valores)
    });
    formulario.reset();
    mostrarFeedback(feedback, 'Conta adicionada. Use "Atualizar saldo" sempre que o valor mudar.');
    await carregarContasDashboard();
  } catch (erro) {
    mostrarFeedback(feedback, erro.message, true);
  }
}

/** Pedidos ainda sem decisao: os que aguardam as 24h e os que ja podem ser decididos. */
async function carregarQuarentenasDashboard() {
  const total = document.getElementById('dashboard-impulso-total');
  const espera = document.getElementById('dashboard-impulso-espera');
  const liberados = document.getElementById('dashboard-impulso-liberados');
  try {
    const travas = await requisicaoApi(`/api/financas/trava-impulso/${sessaoDashboard.usuario.id}`);
    const pendentes = travas.filter(trava => trava.status === 'QUARENTENA');
    const prontos = pendentes.filter(trava => trava.podeSerLiberada).length;
    total.textContent = formatarMoeda(pendentes.reduce((soma, trava) => soma + Number(trava.valor || 0), 0));
    espera.textContent = String(pendentes.length - prontos);
    liberados.textContent = String(prontos);
  } catch {
    total.textContent = '—';
    espera.textContent = '—';
    liberados.textContent = '—';
  }
}
