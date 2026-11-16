const sessaoEstresse = exigirSessao();
const formularioEstresse = document.getElementById('estresse-form');
const feedbackEstresse = document.getElementById('estresse-feedback');
const trilhaMeses = document.getElementById('estresse-meses');
const historicoEstresse = document.getElementById('estresse-historico');
const cenariosProntos = {
  metade: { titulo: 'Renda pela metade', percentualCorteRenda: 50, duracaoMeses: 12 },
  desemprego: { titulo: 'Desemprego', percentualCorteRenda: 100, duracaoMeses: 6 },
  corte: { titulo: 'Corte de 30%', percentualCorteRenda: 30, duracaoMeses: 12 }
};
const simulacoesSessao = [];

if (sessaoEstresse) {
  renderizarHistorico();
  formularioEstresse.addEventListener('submit', simularCenario);
  document.querySelectorAll('[data-preset]').forEach(botao => botao.addEventListener('click', () => {
    const preset = cenariosProntos[botao.dataset.preset];
    Object.entries(preset).forEach(([campo, valor]) => { document.getElementById(campo).value = valor; });
    document.querySelectorAll('[data-preset]').forEach(item => item.setAttribute('aria-pressed', String(item === botao)));
  }));
  ['percentualCorteRenda', 'duracaoMeses'].forEach(id => document.getElementById(id).addEventListener('input', () => {
    document.querySelectorAll('[data-preset]').forEach(item => item.setAttribute('aria-pressed', 'false'));
  }));
}

async function simularCenario(evento) {
  evento.preventDefault();
  if (!formularioEstresse.reportValidity()) return;
  const botao = formularioEstresse.querySelector('button[type="submit"]');
  botao.disabled = true;
  mostrarFeedback(feedbackEstresse, 'Simulando cenário...');
  const valores = Object.fromEntries(new FormData(formularioEstresse).entries());
  for (const campo of ['rendaMensalAtual', 'despesaMensalAtual', 'reservaEmergencia', 'percentualCorteRenda', 'duracaoMeses']) {
    valores[campo] = Number(valores[campo]);
  }

  try {
    const resultado = await requisicaoApi(`/api/inteligencia/teste-estresse/${sessaoEstresse.usuario.id}`, {
      method: 'POST',
      body: JSON.stringify(valores)
    });
    const analise = analisarCenario(valores, resultado);
    exibirResultado(analise);
    simulacoesSessao.unshift(analise);
    renderizarHistorico();
    mostrarFeedback(feedbackEstresse, 'Simulação salva.');
  } catch (erro) {
    mostrarFeedback(feedbackEstresse, erro.message, true);
  } finally {
    botao.disabled = false;
  }
}

function analisarCenario(valores, resultado) {
  const rendaReduzida = valores.rendaMensalAtual * (1 - valores.percentualCorteRenda / 100);
  const deficit = valores.despesaMensalAtual - rendaReduzida;
  const mesesApi = Number(resultado.mesesSobrevivenciaReserva);
  let mesesCobertos;
  if (deficit <= 0) {
    mesesCobertos = Infinity;
  } else if (Number.isFinite(mesesApi)) {
    mesesCobertos = mesesApi;
  } else {
    mesesCobertos = valores.reservaEmergencia / deficit;
  }
  return {
    titulo: resultado.tituloCenario || valores.titulo,
    duracao: valores.duracaoMeses,
    corte: valores.percentualCorteRenda,
    deficit,
    mesesCobertos,
    impacto: Number(resultado.impactoPatrimonialEstimado || 0),
    sobrevive: mesesCobertos >= valores.duracaoMeses
  };
}

function exibirResultado(analise) {
  const veredito = document.getElementById('estresse-veredito');
  const sub = document.getElementById('estresse-veredito-sub');
  const numero = criarElemento('span', 'stress-verdict__num');
  veredito.classList.toggle('stress-verdict--alerta', !analise.sobrevive);

  if (analise.deficit <= 0) {
    numero.textContent = 'sem usar a reserva';
    veredito.replaceChildren('Você atravessa este cenário ', numero, '.');
    sub.textContent = 'Mesmo com a queda de renda, o que entra ainda cobre suas despesas mensais.';
  } else if (analise.sobrevive) {
    numero.textContent = `todos os ${analise.duracao} meses`;
    veredito.replaceChildren('Sua reserva cobre ', numero, ' do cenário.');
    const sobra = Math.floor(analise.mesesCobertos - analise.duracao);
    sub.textContent = sobra > 0
      ? `Ao fim da crise ainda restariam cerca de ${sobra} ${sobra === 1 ? 'mês' : 'meses'} de reserva.`
      : 'A reserva termina praticamente junto com o cenário. Qualquer imprevisto extra fica descoberto.';
  } else {
    const cobertos = Math.floor(analise.mesesCobertos);
    numero.textContent = `${cobertos} de ${analise.duracao} meses`;
    veredito.replaceChildren('Sua reserva cobre ', numero, '.');
    const inicioFalta = mesFuturo(cobertos);
    sub.textContent = `A partir de ${inicioFalta.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' })}, faltariam ${formatarMoeda(analise.deficit)} por mês para fechar as contas.`;
  }

  desenharMeses(analise);

  const deficitElemento = document.getElementById('estresse-deficit');
  deficitElemento.textContent = analise.deficit <= 0 ? 'Nenhum' : formatarMoeda(analise.deficit);
  deficitElemento.className = `stat__value ${analise.deficit > 0 ? 'stat__value--negative' : 'stat__value--positive'}`;

  const impactoElemento = document.getElementById('estresse-impacto');
  impactoElemento.textContent = `${analise.impacto < 0 ? '−' : ''}${formatarMoeda(Math.abs(analise.impacto))}`;
  impactoElemento.className = `stat__value ${analise.impacto < 0 ? 'stat__value--negative' : ''}`;

  const folego = document.getElementById('estresse-folego');
  folego.textContent = Number.isFinite(analise.mesesCobertos) ? `${analise.mesesCobertos.toLocaleString('pt-BR', { maximumFractionDigits: 1 })} meses` : 'Ilimitado';
  folego.className = `stat__value ${analise.sobrevive ? 'stat__value--positive' : 'stat__value--negative'}`;
  document.getElementById('estresse-folego-nota').textContent = `Cenário de ${analise.duracao} meses`;
}

function desenharMeses(analise) {
  trilhaMeses.replaceChildren();
  const cobertos = Number.isFinite(analise.mesesCobertos) ? analise.mesesCobertos : analise.duracao;
  trilhaMeses.setAttribute('aria-label', Number.isFinite(analise.mesesCobertos)
    ? `${Math.min(Math.floor(cobertos), analise.duracao)} de ${analise.duracao} meses cobertos pela reserva`
    : `Todos os ${analise.duracao} meses cobertos`);

  for (let indice = 0; indice < analise.duracao; indice++) {
    const cobertura = Math.max(0, Math.min(1, cobertos - indice));
    const data = mesFuturo(indice);
    const mes = criarElemento('div', 'month');
    if (analise.deficit <= 0) mes.classList.add('month--neutro');
    else if (cobertura >= 1) mes.classList.add('month--coberto');
    mes.style.setProperty('--cobertura', `${analise.deficit <= 0 ? 0 : cobertura * 100}%`);
    mes.style.setProperty('--i', String(indice));
    mes.append(
      criarElemento('span', 'month__label', data.toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '')),
      criarElemento('span', 'month__year', String(data.getFullYear()).slice(-2))
    );
    trilhaMeses.append(mes);
  }
}

function mesFuturo(deslocamento) {
  const hoje = new Date();
  return new Date(hoje.getFullYear(), hoje.getMonth() + 1 + deslocamento, 1);
}

function renderizarHistorico() {
  historicoEstresse.replaceChildren();
  if (!simulacoesSessao.length) {
    const vazio = criarElemento('div', 'empty-state');
    vazio.append(criarElemento('strong', null, 'Nenhuma simulação ainda'), 'Teste mais de um cenário para comparar o fôlego da sua reserva.');
    historicoEstresse.append(vazio);
    return;
  }
  simulacoesSessao.forEach(analise => {
    const item = criarElemento('div', 'stress-history__item');
    const valor = Number.isFinite(analise.mesesCobertos)
      ? `${analise.mesesCobertos.toLocaleString('pt-BR', { maximumFractionDigits: 1 })} meses`
      : 'Sem déficit';
    item.append(
      criarElemento('span', 'stress-history__name', analise.titulo),
      criarElemento('span', 'stress-history__meta', `Queda de ${analise.corte}% por ${analise.duracao} meses`),
      criarElemento('span', `stress-history__value ${analise.sobrevive ? 'stress-history__value--ok' : 'stress-history__value--alerta'}`, valor)
    );
    historicoEstresse.append(item);
  });
}
