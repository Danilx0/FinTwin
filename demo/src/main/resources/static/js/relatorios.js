const sessaoRelatorios = exigirSessao();
const formularioRelatorios = document.getElementById('relatorio-form');
const feedbackRelatorios = document.getElementById('relatorio-feedback');
const listaRelatorios = document.getElementById('relatorio-resultado');
const LIMITE_REGISTROS = 5000;
const arquivosGerados = [];

let transacoesRelatorio = [];

if (sessaoRelatorios) {
  renderizarArquivos();
  carregarPrevia();
  formularioRelatorios.addEventListener('submit', gerarRelatorio);
  window.addEventListener('pagehide', () => arquivosGerados.forEach(arquivo => URL.revokeObjectURL(arquivo.endereco)));
}

async function carregarPrevia() {
  try {
    const dados = await requisicaoApi(`/api/financas/transacoes/${sessaoRelatorios.usuario.id}`);
    transacoesRelatorio = dados.slice().sort((a, b) => new Date(b.dataHora) - new Date(a.dataHora)).slice(0, LIMITE_REGISTROS);
    renderizarResumo(dados.length);
    renderizarPrevia();
  } catch (erro) {
    mostrarFeedback(feedbackRelatorios, erro.message, true);
  }
}

function renderizarResumo(totalDisponivel) {
  const quantidade = transacoesRelatorio.length;
  document.getElementById('relatorio-qtd').textContent = quantidade.toLocaleString('pt-BR');
  document.getElementById('relatorio-qtd-nota').textContent = totalDisponivel > LIMITE_REGISTROS
    ? `${(totalDisponivel - LIMITE_REGISTROS).toLocaleString('pt-BR')} mais antigas ficam de fora`
    : 'Limite de 5.000 por arquivo';

  const periodo = document.getElementById('relatorio-periodo');
  if (!quantidade) {
    periodo.textContent = '—';
    document.getElementById('relatorio-liquido').textContent = '—';
    return;
  }
  const mesAno = data => `${data.toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '')}/${String(data.getFullYear()).slice(-2)}`;
  const maisAntiga = new Date(transacoesRelatorio.at(-1).dataHora);
  const maisRecente = new Date(transacoesRelatorio[0].dataHora);
  periodo.textContent = `${mesAno(maisAntiga)} – ${mesAno(maisRecente)}`;
  document.getElementById('relatorio-periodo-nota').textContent = `${maisAntiga.toLocaleDateString('pt-BR')} a ${maisRecente.toLocaleDateString('pt-BR')}`;

  const liquido = transacoesRelatorio.reduce((soma, item) => soma + (item.tipoTransacao === 'RECEITA' ? 1 : -1) * Number(item.valor || 0), 0);
  const liquidoElemento = document.getElementById('relatorio-liquido');
  liquidoElemento.textContent = `${liquido < 0 ? '−' : ''}${formatarMoeda(Math.abs(liquido))}`;
  liquidoElemento.className = `stat__value ${liquido < 0 ? 'stat__value--negative' : 'stat__value--positive'}`;
}

function linhasCsv(registros) {
  return registros.map(item => [
    new Date(item.dataHora).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }),
    item.descricao,
    formatarEnum(item.tipoTransacao),
    formatarEnum(item.categoria),
    Number(item.valor || 0).toFixed(2).replace('.', ',')
  ]);
}

function renderizarPrevia() {
  const corpo = document.getElementById('relatorio-previa');
  corpo.replaceChildren();
  if (!transacoesRelatorio.length) {
    const linha = document.createElement('tr');
    const celula = document.createElement('td');
    celula.colSpan = 5;
    const vazio = criarElemento('div', 'empty-state');
    vazio.append(criarElemento('strong', null, 'Nada para exportar ainda'), 'Registre transações para gerar o primeiro relatório.');
    celula.append(vazio);
    linha.append(celula);
    corpo.append(linha);
    return;
  }
  linhasCsv(transacoesRelatorio.slice(0, 6)).forEach(valores => {
    const linha = document.createElement('tr');
    valores.forEach((valor, indice) => linha.append(criarElemento('td', indice === 4 ? 'is-numeric' : '', valor)));
    corpo.append(linha);
  });
}

async function gerarRelatorio(evento) {
  evento.preventDefault();
  const botao = formularioRelatorios.querySelector('button[type="submit"]');
  const formato = new FormData(formularioRelatorios).get('formato') || 'CSV';
  botao.disabled = true;
  mostrarFeedback(feedbackRelatorios, 'Preparando arquivo...');
  try {
    const registro = await requisicaoApi(`/api/financas/relatorios/exportar/${sessaoRelatorios.usuario.id}?formato=${encodeURIComponent(formato)}`, { method: 'POST' });
    const dados = await requisicaoApi(`/api/financas/transacoes/${sessaoRelatorios.usuario.id}`);
    transacoesRelatorio = dados.slice().sort((a, b) => new Date(b.dataHora) - new Date(a.dataHora)).slice(0, LIMITE_REGISTROS);
    renderizarResumo(dados.length);
    renderizarPrevia();

    const linhas = [['Data', 'Descrição', 'Tipo', 'Categoria', 'Valor'], ...linhasCsv(transacoesRelatorio)];
    const csv = '﻿' + linhas.map(linha => linha.map(escaparCsv).join(';')).join('\r\n');
    const arquivo = new Blob([csv], { type: 'text/csv;charset=utf-8' });
    const nome = `fintwin-relatorio-${new Date().toISOString().slice(0, 10)}${arquivosGerados.length ? `-${arquivosGerados.length + 1}` : ''}.csv`;
    const gerado = { nome, endereco: URL.createObjectURL(arquivo), quantidade: transacoesRelatorio.length, tamanho: arquivo.size, codigo: registro?.id, criadoEm: new Date() };
    arquivosGerados.unshift(gerado);
    renderizarArquivos();
    baixar(gerado);
    mostrarFeedback(feedbackRelatorios, `Relatório gerado com ${gerado.quantidade.toLocaleString('pt-BR')} transações.`);
  } catch (erro) {
    mostrarFeedback(feedbackRelatorios, erro.message, true);
  } finally {
    botao.disabled = false;
  }
}

function baixar(gerado) {
  const link = document.createElement('a');
  link.href = gerado.endereco;
  link.download = gerado.nome;
  document.body.append(link);
  link.click();
  link.remove();
}

function renderizarArquivos() {
  listaRelatorios.replaceChildren();
  if (!arquivosGerados.length) {
    const vazio = criarElemento('div', 'empty-state');
    vazio.append(criarElemento('strong', null, 'Nenhum relatório gerado'), 'Os arquivos que você exportar aparecem aqui.');
    listaRelatorios.append(vazio);
    return;
  }
  arquivosGerados.forEach(gerado => {
    const item = criarElemento('div', 'report-file');
    const icone = criarElemento('span', 'report-file__icon', 'CSV');
    icone.setAttribute('aria-hidden', 'true');
    const info = criarElemento('div');
    const tamanho = gerado.tamanho < 1024 ? `${gerado.tamanho} B` : `${(gerado.tamanho / 1024).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} KB`;
    info.append(
      criarElemento('div', 'report-file__name', gerado.nome),
      criarElemento('div', 'report-file__meta', `${gerado.quantidade.toLocaleString('pt-BR')} transações · ${tamanho} · ${gerado.criadoEm.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}${gerado.codigo != null ? ` · registro ${gerado.codigo}` : ''}`)
    );
    const link = criarElemento('a', 'btn-sm', 'Baixar');
    link.href = gerado.endereco;
    link.download = gerado.nome;
    item.append(icone, info, link);
    listaRelatorios.append(item);
  });
}

function escaparCsv(valor) {
  let texto = String(valor ?? '');
  if (/^[=+@\-\t\r]/.test(texto)) texto = `'${texto}`;
  return `"${texto.replaceAll('"', '""')}"`;
}
