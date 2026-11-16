const sessaoImpulso = exigirSessao();
const formularioImpulso = document.getElementById('impulso-form');
const feedbackImpulso = document.getElementById('impulso-feedback');
const listaImpulso = document.getElementById('impulso-resultado');
const SVG_IMPULSO = 'http://www.w3.org/2000/svg';
const RAIO_ANEL = 42;
const CIRCUNFERENCIA_ANEL = 2 * Math.PI * RAIO_ANEL;
const VINTE_QUATRO_HORAS = 24 * 3600000;

let quarentenas = [];
let idNovaQuarentena = null;

if (sessaoImpulso) {
  quarentenas = lerQuarentenas();
  renderizarQuarentenas();
  window.setInterval(atualizarContagens, 1000);
  formularioImpulso.addEventListener('submit', registrarQuarentena);
}

function chaveArmazenamento() {
  return `fintwin.quarentenas.${sessaoImpulso.usuario.id}`;
}

function lerQuarentenas() {
  try {
    return JSON.parse(sessionStorage.getItem(chaveArmazenamento()) || '[]');
  } catch {
    return [];
  }
}

function salvarQuarentenas() {
  try {
    sessionStorage.setItem(chaveArmazenamento(), JSON.stringify(quarentenas.slice(0, 20)));
  } catch {
    return;
  }
}

async function registrarQuarentena(evento) {
  evento.preventDefault();
  if (!formularioImpulso.reportValidity()) return;
  const botao = formularioImpulso.querySelector('button[type="submit"]');
  botao.disabled = true;
  mostrarFeedback(feedbackImpulso, 'Registrando quarentena...');
  const valores = Object.fromEntries(new FormData(formularioImpulso).entries());
  valores.valor = Number(valores.valor);
  valores.justificativa = valores.justificativa?.trim() || null;

  try {
    const resultado = await requisicaoApi(`/api/financas/trava-impulso/${sessaoImpulso.usuario.id}`, {
      method: 'POST',
      body: JSON.stringify(valores)
    });
    const agora = Date.now();
    const liberacao = resultado.dataLiberacaoObrigatoria ? new Date(resultado.dataLiberacaoObrigatoria).getTime() : agora + VINTE_QUATRO_HORAS;
    const registro = {
      id: resultado.id ?? `local-${agora}`,
      itemDesejado: resultado.itemDesejado ?? valores.itemDesejado,
      valor: Number(resultado.valor ?? valores.valor),
      justificativa: resultado.justificativa ?? valores.justificativa,
      inicio: Math.min(agora, liberacao - VINTE_QUATRO_HORAS) || agora,
      liberacao
    };
    quarentenas = [registro, ...quarentenas.filter(item => item.id !== registro.id)];
    idNovaQuarentena = registro.id;
    salvarQuarentenas();
    renderizarQuarentenas();
    formularioImpulso.reset();
    mostrarFeedback(feedbackImpulso, `Quarentena iniciada. Volte em ${new Date(liberacao).toLocaleString('pt-BR', { weekday: 'long', hour: '2-digit', minute: '2-digit' })} para decidir.`);
  } catch (erro) {
    mostrarFeedback(feedbackImpulso, erro.message, true);
  } finally {
    botao.disabled = false;
  }
}

function renderizarQuarentenas() {
  listaImpulso.replaceChildren();
  const emEspera = quarentenas.filter(item => item.liberacao > Date.now());
  const total = document.getElementById('impulso-total');
  total.hidden = !emEspera.length;
  total.textContent = `${formatarMoeda(emEspera.reduce((soma, item) => soma + item.valor, 0))} em espera`;

  if (!quarentenas.length) {
    const vazio = criarElemento('div', 'empty-state');
    vazio.append(criarElemento('strong', null, 'Nenhuma compra em quarentena'), 'Quando bater a vontade de comprar algo, registre aqui antes de pagar.');
    listaImpulso.append(vazio);
    return;
  }

  quarentenas.forEach(item => listaImpulso.append(criarCartao(item)));
  idNovaQuarentena = null;
  atualizarContagens();
}

function criarCartao(item) {
  const cartao = criarElemento('article', 'quarantine');
  cartao.setAttribute('role', 'listitem');
  cartao.dataset.id = String(item.id);
  if (item.id === idNovaQuarentena) cartao.classList.add('quarantine--nova');

  const anel = criarElemento('div', 'ring');
  const desenho = document.createElementNS(SVG_IMPULSO, 'svg');
  desenho.setAttribute('viewBox', '0 0 100 100');
  desenho.setAttribute('aria-hidden', 'true');
  const trilha = document.createElementNS(SVG_IMPULSO, 'circle');
  const progresso = document.createElementNS(SVG_IMPULSO, 'circle');
  [trilha, progresso].forEach(circulo => {
    circulo.setAttribute('cx', '50');
    circulo.setAttribute('cy', '50');
    circulo.setAttribute('r', String(RAIO_ANEL));
  });
  trilha.setAttribute('class', 'ring__track');
  progresso.setAttribute('class', 'ring__progress');
  progresso.setAttribute('stroke-dasharray', CIRCUNFERENCIA_ANEL.toFixed(2));
  desenho.append(trilha, progresso);
  const tempo = criarElemento('div', 'ring__time');
  tempo.append(criarElemento('span', 'ring__clock'), criarElemento('span', 'ring__unit'));
  anel.append(desenho, tempo);

  const corpo = criarElemento('div', 'quarantine__body');
  const topo = criarElemento('div', 'quarantine__top');
  topo.append(criarElemento('span', 'quarantine__item', item.itemDesejado), criarElemento('span', 'quarantine__value', formatarMoeda(item.valor)));
  corpo.append(topo);
  if (item.justificativa) corpo.append(criarElemento('p', 'quarantine__reason', `“${item.justificativa}”`));
  corpo.append(criarElemento('p', 'quarantine__meta'));

  cartao.append(anel, corpo);
  return cartao;
}

function atualizarContagens() {
  const agora = Date.now();
  listaImpulso.querySelectorAll('.quarantine').forEach(cartao => {
    const item = quarentenas.find(registro => String(registro.id) === cartao.dataset.id);
    if (!item) return;
    const duracao = Math.max(item.liberacao - item.inicio, 1);
    const restante = Math.max(item.liberacao - agora, 0);
    const liberada = restante === 0;
    cartao.classList.toggle('quarantine--liberada', liberada);
    cartao.querySelector('.ring__progress').setAttribute('stroke-dashoffset', (CIRCUNFERENCIA_ANEL * (restante / duracao)).toFixed(2));

    const relogio = cartao.querySelector('.ring__clock');
    const unidade = cartao.querySelector('.ring__unit');
    const meta = cartao.querySelector('.quarantine__meta');
    if (liberada) {
      relogio.textContent = '✓';
      unidade.textContent = 'liberada';
      meta.textContent = 'Prazo cumprido. Releia o motivo e decida se ainda vale a compra.';
    } else {
      const horas = Math.floor(restante / 3600000);
      const minutos = Math.floor(restante % 3600000 / 60000);
      relogio.textContent = `${String(horas).padStart(2, '0')}:${String(minutos).padStart(2, '0')}`;
      unidade.textContent = 'restantes';
      meta.textContent = `Liberada em ${new Date(item.liberacao).toLocaleString('pt-BR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })}`;
    }
  });
}
