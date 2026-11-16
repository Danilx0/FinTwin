const formatadorMoeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const formatadorMoedaCompacta = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL', notation: 'compact', maximumFractionDigits: 1 });

function obterUsuarioAtual() {
  try {
    return JSON.parse(sessionStorage.getItem('fintwin.usuario') || 'null');
  } catch {
    return null;
  }
}

function exigirSessao() {
  const token = sessionStorage.getItem('fintwin.token');
  const usuario = obterUsuarioAtual();
  if (!token || !usuario?.id) {
    window.location.replace('/login');
    return null;
  }
  return { token, usuario };
}

async function requisicaoApi(caminho, opcoes = {}) {
  const token = sessionStorage.getItem('fintwin.token');
  const cabecalhos = new Headers(opcoes.headers || {});
  if (token) cabecalhos.set('Authorization', `Bearer ${token}`);
  if (opcoes.body && !(opcoes.body instanceof FormData) && !cabecalhos.has('Content-Type')) {
    cabecalhos.set('Content-Type', 'application/json');
  }

  const resposta = await fetch(caminho, { ...opcoes, headers: cabecalhos });
  const texto = await resposta.text();
  let corpo = null;
  if (texto) {
    try {
      corpo = JSON.parse(texto);
    } catch {
      corpo = texto;
    }
  }
  if (!resposta.ok) {
    if (resposta.status === 401) {
      sessionStorage.clear();
      window.location.replace('/login');
    }
    const mensagem = corpo?.mensagem || corpo?.message || corpo?.erro || 'Não foi possível concluir a solicitação.';
    const erro = new Error(mensagem);
    erro.status = resposta.status;
    throw erro;
  }
  return corpo;
}

function mostrarFeedback(elemento, mensagem, erro = false) {
  if (!elemento) return;
  elemento.textContent = mensagem;
  elemento.classList.toggle('feedback--error', erro);
  elemento.classList.toggle('workflow-feedback--error', erro);
  elemento.classList.toggle('form-feedback--error', erro);
}

function formatarMoeda(valor) {
  return formatadorMoeda.format(Number(valor || 0));
}

function formatarMoedaCompacta(valor) {
  return formatadorMoedaCompacta.format(Number(valor || 0));
}

const rotulosEnum = {
  ALIMENTACAO: 'Alimentação', SAUDE: 'Saúde', EDUCACAO: 'Educação', SERVICOS: 'Serviços', SALARIO: 'Salário',
  VESTUARIO: 'Vestuário', POUPANCA: 'Poupança', MORADIA: 'Moradia', TRANSPORTE: 'Transporte', LAZER: 'Lazer',
  INVESTIMENTO: 'Investimento', OUTROS: 'Outros', CORRENTE: 'Corrente', RECEITA: 'Receita', DESPESA: 'Despesa'
};

function formatarEnum(valor) {
  if (rotulosEnum[valor]) return rotulosEnum[valor];
  return String(valor || '').toLocaleLowerCase('pt-BR').replaceAll('_', ' ').replace(/(^|\s)\p{L}/gu, letra => letra.toLocaleUpperCase('pt-BR'));
}

function formatarTipoUsuario(tipo) {
  return ({ INDIVIDUAL: 'Individual', FAMILIAR: 'Familiar', CONSULTOR: 'Consultor', ADMINISTRADOR: 'Administrador' })[tipo] || 'Usuário';
}

function iniciaisDe(texto) {
  return String(texto || 'FT').split(/[\s@.]+/).filter(Boolean).slice(0, 2).map(parte => parte[0]).join('').toUpperCase();
}

function criarElemento(tag, classe, texto) {
  const elemento = document.createElement(tag);
  if (classe) elemento.className = classe;
  if (texto !== undefined && texto !== null) elemento.textContent = texto;
  return elemento;
}

function preencherUsuarioSidebar() {
  const usuario = obterUsuarioAtual();
  const nome = document.getElementById('app-user-name');
  if (!usuario || !nome) return;
  nome.textContent = usuario.nome || usuario.email;
  document.getElementById('app-user-role').textContent = formatarTipoUsuario(usuario.tipoUsuario);
  document.querySelectorAll('[data-user-avatar]').forEach(avatar => aplicarAvatar(avatar, usuario));
}

const fotosCarregadas = new Map();

/** A foto exige o token, entao e buscada via fetch e exibida como blob (o cache HTTP evita downloads repetidos). */
function obterFotoUsuario(url) {
  if (!fotosCarregadas.has(url)) {
    const token = sessionStorage.getItem('fintwin.token');
    const promessa = fetch(url, { headers: { Authorization: `Bearer ${token}` } })
      .then(resposta => {
        if (!resposta.ok) throw new Error('Foto indisponível');
        return resposta.blob();
      })
      .then(blob => URL.createObjectURL(blob))
      .catch(erro => {
        fotosCarregadas.delete(url);
        throw erro;
      });
    fotosCarregadas.set(url, promessa);
  }
  return fotosCarregadas.get(url);
}

function aplicarAvatar(elemento, usuario) {
  const iniciais = iniciaisDe(usuario?.nome || usuario?.email);
  const url = usuario?.fotoPerfilUrl;
  elemento.dataset.fotoUrl = url || '';
  if (!url) {
    elemento.style.backgroundImage = '';
    elemento.textContent = iniciais;
    return;
  }
  obterFotoUsuario(url)
    .then(endereco => {
      if (elemento.dataset.fotoUrl !== url) return;
      elemento.style.backgroundImage = `url("${endereco}")`;
      elemento.textContent = '';
    })
    .catch(() => {
      elemento.style.backgroundImage = '';
      elemento.textContent = iniciais;
    });
}

function iniciarMenuMovel() {
  const menu = document.getElementById('app-sidebar');
  const botaoAbrir = document.querySelector('[data-menu-abrir]');
  const fundo = document.querySelector('.sidebar-backdrop');
  if (!menu || !botaoAbrir || !fundo) return;
  const telaPequena = window.matchMedia('(max-width: 900px)');
  const fora = [document.querySelector('.mobile-bar'), document.querySelector('.main')].filter(Boolean);

  const abrir = () => {
    menu.classList.add('sidebar--aberta');
    fundo.hidden = false;
    document.body.classList.add('menu-aberto');
    botaoAbrir.setAttribute('aria-expanded', 'true');
    fora.forEach(elemento => { elemento.inert = true; });
    menu.querySelector('.sidebar__fechar')?.focus();
  };

  const fechar = (devolverFoco = true) => {
    if (!menu.classList.contains('sidebar--aberta')) return;
    menu.classList.remove('sidebar--aberta');
    fundo.hidden = true;
    document.body.classList.remove('menu-aberto');
    botaoAbrir.setAttribute('aria-expanded', 'false');
    fora.forEach(elemento => { elemento.inert = false; });
    if (devolverFoco) botaoAbrir.focus();
  };

  botaoAbrir.addEventListener('click', abrir);
  document.querySelectorAll('[data-menu-fechar]').forEach(elemento => elemento.addEventListener('click', () => fechar()));
  menu.addEventListener('click', evento => {
    if (evento.target.closest('a')) fechar(false);
  });
  document.addEventListener('keydown', evento => {
    if (evento.key === 'Escape') fechar();
  });
  telaPequena.addEventListener('change', evento => {
    if (!evento.matches) fechar(false);
  });
}

document.querySelectorAll('[data-logout]').forEach(elemento => {
  elemento.addEventListener('click', evento => {
    evento.preventDefault();
    sessionStorage.clear();
    window.location.assign('/login');
  });
});

preencherUsuarioSidebar();
iniciarMenuMovel();
