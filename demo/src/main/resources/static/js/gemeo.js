const sessaoGemeo = exigirSessao();
const formularioGemeo = document.getElementById('gemeo-form');
const feedbackGemeo = document.getElementById('gemeo-feedback');
const listaGemeos = document.getElementById('gemeo-resultados');
const graficoGemeo = document.getElementById('gemeo-grafico');
const leituraGemeo = document.getElementById('gemeo-readout');
const campoHorizonte = document.getElementById('horizonteAnos');
const saidaHorizonte = document.getElementById('horizonte-valor');
const SVG_NS = 'http://www.w3.org/2000/svg';
const anoAtual = new Date().getFullYear();

let gemeosSalvos = [];
let gemeoSelecionadoId = null;
let serieAtual = [];

if (sessaoGemeo) {
  atualizarHorizonte();
  campoHorizonte.addEventListener('input', atualizarHorizonte);
  formularioGemeo.addEventListener('submit', gerarProjecao);
  new ResizeObserver(() => desenharGrafico()).observe(graficoGemeo);
  carregarGemeos();
}

function atualizarHorizonte() {
  const anos = Number(campoHorizonte.value);
  saidaHorizonte.textContent = `${anos} ${anos === 1 ? 'ano' : 'anos'}`;
  campoHorizonte.style.setProperty('--progresso', `${(anos - 1) / 34 * 100}%`);
}

async function gerarProjecao(evento) {
  evento.preventDefault();
  if (!formularioGemeo.reportValidity()) return;
  const botao = formularioGemeo.querySelector('button[type="submit"]');
  botao.disabled = true;
  mostrarFeedback(feedbackGemeo, 'Calculando projeção...');

  const valores = Object.fromEntries(new FormData(formularioGemeo).entries());
  for (const campo of ['horizonteAnos', 'patrimonioInicial', 'aporteMensal', 'taxaRendimentoAnual']) {
    valores[campo] = Number(valores[campo]);
  }
  valores.inflacaoAnual = valores.inflacaoAnual === '' ? null : Number(valores.inflacaoAnual);

  try {
    const resultado = await requisicaoApi(`/api/inteligencia/gemeo-digital/${sessaoGemeo.usuario.id}`, {
      method: 'POST',
      body: JSON.stringify(valores)
    });
    const tempo = resultado.tempoProcessamentoMs ?? 0;
    mostrarFeedback(feedbackGemeo, `Projeção gerada em ${tempo} ms.`);
    document.getElementById('gemeo-tempo').textContent = `Última projeção em ${tempo} ms`;
    gemeoSelecionadoId = resultado.id ?? null;
    await carregarGemeos();
  } catch (erro) {
    mostrarFeedback(feedbackGemeo, erro.message, true);
  } finally {
    botao.disabled = false;
  }
}

async function carregarGemeos() {
  try {
    const dados = await requisicaoApi(`/api/inteligencia/gemeo-digital/${sessaoGemeo.usuario.id}`);
    gemeosSalvos = dados.slice().sort((a, b) => new Date(b.dataCriacao || 0) - new Date(a.dataCriacao || 0));
    if (!gemeosSalvos.some(gemeo => gemeo.id === gemeoSelecionadoId)) {
      gemeoSelecionadoId = gemeosSalvos[0]?.id ?? null;
    }
    renderizarLista();
    selecionarGemeo(gemeoSelecionadoId);
  } catch (erro) {
    mostrarFeedback(feedbackGemeo, erro.message, true);
  }
}

function renderizarLista() {
  listaGemeos.replaceChildren();
  if (!gemeosSalvos.length) {
    const vazio = criarElemento('div', 'empty-state');
    vazio.append(criarElemento('strong', null, 'Nenhum cenário salvo'), 'Os cenários que você gerar aparecem aqui para comparação.');
    listaGemeos.append(vazio);
    return;
  }

  gemeosSalvos.forEach(gemeo => {
    const final = gemeo.pontosProjecao?.at(-1);
    const item = criarElemento('div', 'scenario-item');
    item.setAttribute('role', 'listitem');
    item.dataset.gemeoId = gemeo.id;

    const botao = criarElemento('button', 'scenario');
    botao.type = 'button';
    botao.setAttribute('aria-pressed', String(gemeo.id === gemeoSelecionadoId));
    botao.append(
      criarElemento('span', 'scenario__name', gemeo.nomeCenario),
      criarElemento('span', 'scenario__meta', `${gemeo.horizonteAnos} anos · até ${anoAtual + Number(gemeo.horizonteAnos)}`),
      criarElemento('span', 'scenario__value', final ? formatarMoedaCompacta(final.patrimonioProjetado) : '—')
    );
    botao.addEventListener('click', () => selecionarGemeo(gemeo.id));

    const excluir = criarElemento('button', 'scenario__excluir');
    excluir.type = 'button';
    excluir.setAttribute('aria-label', `Excluir o cenário ${gemeo.nomeCenario}`);
    excluir.title = 'Excluir cenário';
    excluir.innerHTML = '<svg width="15" height="15" fill="none" viewBox="0 0 16 16" aria-hidden="true">'
      + '<path d="M2.5 4h11M6.5 4V2.75h3V4M4 4l.6 9.25h6.8L12 4M6.75 6.75v4M9.25 6.75v4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>';
    excluir.addEventListener('click', () => pedirConfirmacaoExclusao(item, gemeo));

    item.append(botao, excluir);
    listaGemeos.append(item);
  });
}

function pedirConfirmacaoExclusao(item, gemeo) {
  listaGemeos.querySelectorAll('.scenario-confirmar').forEach(caixa => caixa.closest('.scenario-item')?.classList.remove('scenario-item--confirmando'));
  listaGemeos.querySelectorAll('.scenario-confirmar').forEach(caixa => caixa.remove());

  const caixa = criarElemento('div', 'scenario-confirmar');
  caixa.setAttribute('role', 'group');
  caixa.setAttribute('aria-label', `Confirmar exclusão de ${gemeo.nomeCenario}`);
  const texto = criarElemento('span', 'scenario-confirmar__texto', 'Excluir este cenário? Não dá para desfazer.');
  const confirmar = criarElemento('button', 'btn-sm btn-sm--perigo', 'Excluir');
  confirmar.type = 'button';
  const cancelar = criarElemento('button', 'btn-sm scenario-confirmar__cancelar', 'Cancelar');
  cancelar.type = 'button';
  const botoes = criarElemento('div', 'scenario-confirmar__botoes');
  botoes.append(confirmar, cancelar);
  caixa.append(texto, botoes);

  const fechar = () => {
    caixa.remove();
    item.classList.remove('scenario-item--confirmando');
    item.querySelector('.scenario__excluir')?.focus();
  };
  cancelar.addEventListener('click', fechar);
  caixa.addEventListener('keydown', evento => {
    if (evento.key === 'Escape') fechar();
  });
  confirmar.addEventListener('click', () => excluirGemeo(gemeo, confirmar, cancelar));

  item.classList.add('scenario-item--confirmando');
  item.append(caixa);
  cancelar.focus();
}

async function excluirGemeo(gemeo, confirmar, cancelar) {
  confirmar.disabled = true;
  cancelar.disabled = true;
  const posicao = gemeosSalvos.findIndex(item => item.id === gemeo.id);
  try {
    await requisicaoApi(`/api/inteligencia/gemeo-digital/projecoes/${gemeo.id}`, { method: 'DELETE' });
    if (gemeoSelecionadoId === gemeo.id) gemeoSelecionadoId = null;
    await carregarGemeos();
    mostrarFeedback(feedbackGemeo, `Cenário "${gemeo.nomeCenario}" excluído.`);
    // Mantem o foco perto de onde estava: o cenario seguinte, ou o anterior se era o ultimo.
    const itens = listaGemeos.querySelectorAll('.scenario');
    (itens[Math.min(posicao, itens.length - 1)] || formularioGemeo.querySelector('input'))?.focus();
  } catch (erro) {
    confirmar.disabled = false;
    cancelar.disabled = false;
    mostrarFeedback(feedbackGemeo, erro.message, true);
  }
}

function selecionarGemeo(id) {
  gemeoSelecionadoId = id;
  listaGemeos.querySelectorAll('.scenario').forEach((botao, indice) => {
    botao.setAttribute('aria-pressed', String(gemeosSalvos[indice]?.id === id));
  });
  const gemeo = gemeosSalvos.find(item => item.id === id);
  serieAtual = gemeo ? montarSerie(gemeo) : [];
  atualizarResumo(gemeo);
  preencherTabela();
  desenharGrafico();
}

function montarSerie(gemeo) {
  const inicial = Number(gemeo.patrimonioInicial || 0);
  const aporte = Number(gemeo.aporteMensal || 0);
  const pontos = (gemeo.pontosProjecao || []).slice().sort((a, b) => a.ano - b.ano);
  const primeiro = pontos[0];
  let incluiInicial = false;
  if (primeiro && primeiro.aportesAcumulados != null && inicial > 0) {
    const semInicial = aporte * 12 * primeiro.ano;
    incluiInicial = Math.abs(Number(primeiro.aportesAcumulados) - (semInicial + inicial)) < Math.abs(Number(primeiro.aportesAcumulados) - semInicial);
  }
  const investidoEm = ponto => {
    if (ponto.aportesAcumulados == null) return null;
    return Number(ponto.aportesAcumulados) + (incluiInicial ? 0 : inicial);
  };
  return [
    { ano: 0, patrimonio: inicial, investido: inicial },
    ...pontos.map(ponto => ({ ano: Number(ponto.ano), patrimonio: Number(ponto.patrimonioProjetado), investido: investidoEm(ponto) }))
  ];
}

function atualizarResumo(gemeo) {
  const final = serieAtual.at(-1);
  const valorFinal = document.getElementById('gemeo-stat-final');
  const valorInvestido = document.getElementById('gemeo-stat-investido');
  const valorGanho = document.getElementById('gemeo-stat-ganho');
  const subtitulo = document.getElementById('gemeo-chart-subtitle');

  if (!gemeo || !final || serieAtual.length < 2) {
    valorFinal.textContent = '—';
    valorInvestido.textContent = '—';
    valorGanho.textContent = '—';
    document.getElementById('gemeo-stat-final-nota').textContent = 'Selecione ou gere um cenário';
    subtitulo.textContent = 'Nenhum cenário selecionado';
    return;
  }

  valorFinal.textContent = formatarMoeda(final.patrimonio);
  document.getElementById('gemeo-stat-final-nota').textContent = `Em ${anoAtual + final.ano}, cenário ${gemeo.nomeCenario}`;
  subtitulo.textContent = `${gemeo.nomeCenario} · ${gemeo.horizonteAnos} anos · ${Number(gemeo.taxaRendimentoAnual ?? 0).toLocaleString('pt-BR')}% ao ano`;

  if (final.investido == null) {
    valorInvestido.textContent = '—';
    valorGanho.textContent = '—';
    return;
  }
  const ganho = final.patrimonio - final.investido;
  valorInvestido.textContent = formatarMoeda(final.investido);
  valorGanho.textContent = `${ganho >= 0 ? '+' : '−'}${formatarMoeda(Math.abs(ganho))}`;
  valorGanho.className = `stat__value ${ganho >= 0 ? 'stat__value--positive' : 'stat__value--negative'}`;
  const percentual = final.investido > 0 ? Math.round(ganho / final.investido * 100) : 0;
  document.getElementById('gemeo-stat-ganho-nota').textContent = `${percentual}% sobre o valor investido`;
}

function preencherTabela() {
  const wrap = document.getElementById('gemeo-tabela-wrap');
  const corpo = document.getElementById('gemeo-tabela');
  corpo.replaceChildren();
  wrap.hidden = serieAtual.length < 2;
  serieAtual.slice(1).forEach(ponto => {
    const linha = document.createElement('tr');
    const rendimento = ponto.investido == null ? null : ponto.patrimonio - ponto.investido;
    [
      [String(anoAtual + ponto.ano), ''],
      [ponto.investido == null ? '—' : formatarMoeda(ponto.investido), 'is-numeric'],
      [formatarMoeda(ponto.patrimonio), 'is-numeric'],
      [rendimento == null ? '—' : formatarMoeda(rendimento), 'is-numeric']
    ].forEach(([texto, classe]) => linha.append(criarElemento('td', classe, texto)));
    corpo.append(linha);
  });
}

function svg(tag, atributos = {}) {
  const elemento = document.createElementNS(SVG_NS, tag);
  Object.entries(atributos).forEach(([nome, valor]) => elemento.setAttribute(nome, valor));
  return elemento;
}

function desenharGrafico() {
  if (serieAtual.length < 2) {
    if (!graficoGemeo.querySelector('.empty-state')) {
      const vazio = criarElemento('div', 'empty-state');
      vazio.append(criarElemento('strong', null, 'Seu gêmeo ainda não tem projeções'), 'Preencha os parâmetros ao lado para ver como seu patrimônio pode evoluir.');
      graficoGemeo.replaceChildren(vazio);
    }
    leituraGemeo.textContent = '';
    return;
  }

  const largura = Math.max(graficoGemeo.clientWidth, 280);
  const altura = Math.max(graficoGemeo.clientHeight, 220);
  const margem = { topo: 24, direita: 16, base: 28, esquerda: largura < 480 ? 52 : 64 };
  const areaLargura = largura - margem.esquerda - margem.direita;
  const areaAltura = altura - margem.topo - margem.base;
  const valores = serieAtual.flatMap(ponto => [ponto.patrimonio, ponto.investido]).filter(valor => valor != null);
  const { maximo, divisoes } = escalaMaxima(Math.max(...valores, 1));
  const ultimoAno = serieAtual.at(-1).ano;
  const x = ano => margem.esquerda + ano / ultimoAno * areaLargura;
  const y = valor => margem.topo + areaAltura - valor / maximo * areaAltura;

  const raiz = svg('svg', { viewBox: `0 0 ${largura} ${altura}`, role: 'img', 'aria-label': `Projeção de ${formatarMoeda(serieAtual[0].patrimonio)} para ${formatarMoeda(serieAtual.at(-1).patrimonio)} em ${ultimoAno} anos` });

  for (let indice = 0; indice <= divisoes; indice++) {
    const valor = maximo / divisoes * indice;
    raiz.append(svg('line', { class: 'grade', x1: margem.esquerda, x2: largura - margem.direita, y1: y(valor), y2: y(valor) }));
    const rotulo = svg('text', { class: 'eixo-texto', x: margem.esquerda - 10, y: y(valor) + 4, 'text-anchor': 'end' });
    rotulo.textContent = formatarMoedaCompacta(valor).replace('R$', '').trim();
    raiz.append(rotulo);
  }

  const passoAnos = ultimoAno <= 5 ? 1 : ultimoAno <= 12 ? 2 : ultimoAno <= 24 ? 5 : 7;
  const marcas = [];
  for (let ano = 0; ano <= ultimoAno; ano += passoAnos) marcas.push(ano);
  if (marcas.at(-1) !== ultimoAno) {
    if (ultimoAno - marcas.at(-1) < passoAnos / 2) marcas.pop();
    marcas.push(ultimoAno);
  }
  marcas.forEach(ano => {
    const rotulo = svg('text', { class: 'eixo-texto', x: x(ano), y: altura - 6, 'text-anchor': ano === 0 ? 'start' : ano === ultimoAno ? 'end' : 'middle' });
    rotulo.textContent = String(anoAtual + ano);
    raiz.append(rotulo);
  });

  const caminho = (chave) => serieAtual
    .filter(ponto => ponto[chave] != null)
    .map((ponto, indice) => `${indice ? 'L' : 'M'}${x(ponto.ano).toFixed(1)} ${y(ponto[chave]).toFixed(1)}`)
    .join(' ');
  const possuiInvestido = serieAtual.every(ponto => ponto.investido != null);
  const linhaPatrimonio = caminho('patrimonio');
  const base = y(0).toFixed(1);

  if (possuiInvestido) {
    const linhaInvestido = caminho('investido');
    const investidoReverso = serieAtual.slice().reverse().map(ponto => `L${x(ponto.ano).toFixed(1)} ${y(ponto.investido).toFixed(1)}`).join(' ');
    raiz.append(svg('path', { class: 'area-investido', d: `${linhaInvestido} L${x(ultimoAno).toFixed(1)} ${base} L${x(0).toFixed(1)} ${base} Z` }));
    raiz.append(svg('path', { class: 'area-ganho', d: `${linhaPatrimonio} ${investidoReverso} Z` }));
    raiz.append(svg('path', { class: 'linha-investido', d: linhaInvestido }));
  } else {
    raiz.append(svg('path', { class: 'area-ganho', d: `${linhaPatrimonio} L${x(ultimoAno).toFixed(1)} ${base} L${x(0).toFixed(1)} ${base} Z` }));
  }
  raiz.append(svg('path', { class: 'linha-patrimonio', d: linhaPatrimonio }));

  const final = serieAtual.at(-1);
  raiz.append(svg('circle', { class: 'ponto-patrimonio', cx: x(final.ano), cy: y(final.patrimonio), r: 4 }));

  const guia = svg('line', { class: 'guia', y1: margem.topo, y2: margem.topo + areaAltura, visibility: 'hidden' });
  const marcadorPatrimonio = svg('circle', { class: 'ponto-patrimonio', r: 5, visibility: 'hidden' });
  const marcadorInvestido = svg('circle', { class: 'ponto-investido', r: 4, visibility: 'hidden' });
  const captura = svg('rect', { x: margem.esquerda, y: margem.topo, width: areaLargura, height: areaAltura, fill: 'transparent' });
  raiz.append(guia, marcadorInvestido, marcadorPatrimonio, captura);

  const mostrarPonto = (ponto) => {
    const px = x(ponto.ano);
    guia.setAttribute('x1', px);
    guia.setAttribute('x2', px);
    marcadorPatrimonio.setAttribute('cx', px);
    marcadorPatrimonio.setAttribute('cy', y(ponto.patrimonio));
    [guia, marcadorPatrimonio].forEach(item => item.setAttribute('visibility', 'visible'));
    if (ponto.investido != null) {
      marcadorInvestido.setAttribute('cx', px);
      marcadorInvestido.setAttribute('cy', y(ponto.investido));
      marcadorInvestido.setAttribute('visibility', 'visible');
    }
    escreverLeitura(ponto);
  };

  captura.addEventListener('pointermove', evento => {
    const caixa = raiz.getBoundingClientRect();
    const posicao = (evento.clientX - caixa.left) / caixa.width * largura;
    const ano = Math.round((posicao - margem.esquerda) / areaLargura * ultimoAno);
    const ponto = serieAtual.reduce((proximo, atual) => Math.abs(atual.ano - ano) < Math.abs(proximo.ano - ano) ? atual : proximo);
    mostrarPonto(ponto);
  });
  captura.addEventListener('pointerleave', () => {
    [guia, marcadorPatrimonio, marcadorInvestido].forEach(item => item.setAttribute('visibility', 'hidden'));
    escreverLeitura(final);
  });

  graficoGemeo.replaceChildren(raiz);
  escreverLeitura(final);
}

function escreverLeitura(ponto) {
  leituraGemeo.replaceChildren(
    criarElemento('strong', null, String(anoAtual + ponto.ano)),
    document.createTextNode(' · Patrimônio '),
    criarElemento('em', null, formatarMoeda(ponto.patrimonio))
  );
  if (ponto.investido != null) {
    leituraGemeo.append(document.createTextNode(` · Investido ${formatarMoeda(ponto.investido)}`));
  }
}

function escalaMaxima(valor) {
  const potencia = 10 ** Math.floor(Math.log10(valor));
  const fracao = valor / potencia;
  const passo = fracao <= 1 ? 1 : fracao <= 2 ? 2 : fracao <= 2.5 ? 2.5 : fracao <= 4 ? 4 : fracao <= 5 ? 5 : fracao <= 8 ? 8 : 10;
  const divisoes = { 1: 5, 2: 4, 2.5: 5, 4: 4, 5: 5, 8: 4, 10: 5 }[passo];
  return { maximo: passo * potencia, divisoes };
}
