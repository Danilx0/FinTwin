const sessaoPerfil = exigirSessao();
const formularioPerfil = document.getElementById('perfil-form');
const feedbackPerfil = document.getElementById('perfil-feedback');
const feedbackTwofa = document.getElementById('twofa-feedback');
const feedbackLgpd = document.getElementById('lgpd-feedback');

const FUSOS_BRASIL = [
  ['America/Noronha', 'Fernando de Noronha (UTC−2)'],
  ['America/Sao_Paulo', 'Brasília (UTC−3)'],
  ['America/Bahia', 'Bahia (UTC−3)'],
  ['America/Fortaleza', 'Fortaleza (UTC−3)'],
  ['America/Recife', 'Recife (UTC−3)'],
  ['America/Belem', 'Belém (UTC−3)'],
  ['America/Manaus', 'Manaus (UTC−4)'],
  ['America/Cuiaba', 'Cuiabá (UTC−4)'],
  ['America/Porto_Velho', 'Porto Velho (UTC−4)'],
  ['America/Rio_Branco', 'Rio Branco (UTC−5)']
];

if (sessaoPerfil) {
  iniciarAbas();
  preencherFusos();
  carregarPerfil();
  formularioPerfil.addEventListener('submit', salvarPerfil);
  document.getElementById('twofa-iniciar').addEventListener('click', iniciar2fa);
  document.getElementById('twofa-ativar-form').addEventListener('submit', ativar2fa);
  document.getElementById('twofa-desativar-form').addEventListener('submit', desativar2fa);
  document.getElementById('lgpd-form').addEventListener('submit', solicitarExclusao);
  iniciarFoto();
}

function iniciarAbas() {
  const abas = [...document.querySelectorAll('.perfil-aba')];
  const selecionar = (aba, focar = false) => {
    abas.forEach(item => {
      const ativa = item === aba;
      item.setAttribute('aria-selected', String(ativa));
      item.tabIndex = ativa ? 0 : -1;
      document.getElementById(item.getAttribute('aria-controls')).hidden = !ativa;
    });
    if (focar) aba.focus();
    history.replaceState(null, '', `#${aba.dataset.aba}`);
  };

  abas.forEach(aba => aba.addEventListener('click', () => selecionar(aba)));
  document.querySelector('.perfil-abas').addEventListener('keydown', evento => {
    const atual = abas.indexOf(document.activeElement);
    if (atual < 0) return;
    const destinos = {
      ArrowDown: atual + 1, ArrowRight: atual + 1,
      ArrowUp: atual - 1, ArrowLeft: atual - 1,
      Home: 0, End: abas.length - 1
    };
    if (!(evento.key in destinos)) return;
    evento.preventDefault();
    selecionar(abas[(destinos[evento.key] + abas.length) % abas.length], true);
  });

  const abrirPeloEndereco = () => {
    const alvo = abas.find(aba => `#${aba.dataset.aba}` === window.location.hash);
    if (alvo) selecionar(alvo);
  };
  window.addEventListener('hashchange', abrirPeloEndereco);
  abrirPeloEndereco();
}

function urlUsuario(sufixo = '') {
  return `/api/usuarios/${sessaoPerfil.usuario.id}${sufixo}`;
}

function preencherFusos(atual) {
  const seletor = document.getElementById('fusoHorario');
  const opcoes = [...FUSOS_BRASIL];
  if (atual && !opcoes.some(([id]) => id === atual)) opcoes.push([atual, atual]);
  seletor.replaceChildren(...opcoes.map(([id, rotulo]) => new Option(rotulo, id)));
  if (atual) seletor.value = atual;
}

async function carregarPerfil() {
  try {
    const usuario = await requisicaoApi(urlUsuario());
    renderizarPerfil(usuario);
  } catch (erro) {
    mostrarFeedback(feedbackPerfil, erro.message, true);
  }
}

function renderizarPerfil(usuario) {
  sessionStorage.setItem('fintwin.usuario', JSON.stringify(usuario));
  preencherUsuarioSidebar();

  document.getElementById('perfil-email').textContent = usuario.email;
  document.getElementById('perfil-nome-exibicao').textContent = usuario.nome;
  document.getElementById('perfil-tipo').textContent = `Usuário ${formatarTipoUsuario(usuario.tipoUsuario)}`;
  formularioPerfil.nome.value = usuario.nome || '';
  formularioPerfil.telefone.value = usuario.telefone || '';
  preencherFusos(usuario.fusoHorario || 'America/Sao_Paulo');

  aplicarAvatar(document.getElementById('perfil-avatar'), usuario);
  document.getElementById('foto-remover').hidden = !usuario.fotoPerfilUrl;

  renderizarEstado2fa(Boolean(usuario.doisFatoresAtivo));
}

// ---------- Foto de perfil ----------
const LADO_ENVIO_FOTO = 640;
const TIPOS_FOTO = ['image/jpeg', 'image/png', 'image/webp'];
const feedbackFoto = document.getElementById('foto-feedback');

function iniciarFoto() {
  const area = document.getElementById('perfil-foto');
  const seletor = document.getElementById('foto-arquivo');
  const abrirSeletor = () => seletor.click();

  document.getElementById('foto-escolher').addEventListener('click', abrirSeletor);
  document.getElementById('foto-escolher-avatar').addEventListener('click', abrirSeletor);
  document.getElementById('foto-remover').addEventListener('click', removerFoto);
  seletor.addEventListener('change', () => {
    if (seletor.files[0]) enviarFoto(seletor.files[0]);
    seletor.value = '';
  });

  ['dragenter', 'dragover'].forEach(tipo => area.addEventListener(tipo, evento => {
    if (![...evento.dataTransfer.types].includes('Files')) return;
    evento.preventDefault();
    area.classList.add('perfil-foto--arrastando');
  }));
  area.addEventListener('dragleave', evento => {
    if (!area.contains(evento.relatedTarget)) area.classList.remove('perfil-foto--arrastando');
  });
  area.addEventListener('drop', evento => {
    evento.preventDefault();
    area.classList.remove('perfil-foto--arrastando');
    const arquivo = evento.dataTransfer.files[0];
    if (arquivo) enviarFoto(arquivo);
  });
}

/** Recorta o centro em quadrado e reduz para 640px antes de enviar (respeita a rotacao da foto do celular). */
async function prepararFoto(arquivo) {
  let imagem;
  try {
    imagem = await createImageBitmap(arquivo, { imageOrientation: 'from-image' });
  } catch {
    throw new Error('Não foi possível abrir essa imagem. Use JPG, PNG ou WebP.');
  }
  const lado = Math.min(imagem.width, imagem.height);
  const destino = Math.min(lado, LADO_ENVIO_FOTO);
  const tela = document.createElement('canvas');
  tela.width = destino;
  tela.height = destino;
  const contexto = tela.getContext('2d');
  contexto.imageSmoothingQuality = 'high';
  contexto.fillStyle = '#111827';
  contexto.fillRect(0, 0, destino, destino);
  contexto.drawImage(imagem, (imagem.width - lado) / 2, (imagem.height - lado) / 2, lado, lado, 0, 0, destino, destino);
  imagem.close();
  return new Promise((resolver, rejeitar) => tela.toBlob(
    blob => (blob ? resolver(blob) : rejeitar(new Error('Não foi possível preparar a imagem.'))),
    'image/jpeg', 0.9));
}

function alternarEnvioFoto(enviando) {
  document.getElementById('perfil-foto').classList.toggle('perfil-foto--enviando', enviando);
  ['foto-escolher', 'foto-remover', 'foto-escolher-avatar'].forEach(id => {
    document.getElementById(id).disabled = enviando;
  });
}

async function enviarFoto(arquivo) {
  if (!TIPOS_FOTO.includes(arquivo.type)) {
    mostrarFeedback(feedbackFoto, 'Formato não suportado. Use JPG, PNG ou WebP.', true);
    return;
  }
  const avatar = document.getElementById('perfil-avatar');
  const fundoAnterior = avatar.style.backgroundImage;
  const textoAnterior = avatar.textContent;
  alternarEnvioFoto(true);
  mostrarFeedback(feedbackFoto, 'Enviando foto...');
  let previa = null;
  try {
    const foto = await prepararFoto(arquivo);
    previa = URL.createObjectURL(foto);
    avatar.style.backgroundImage = `url("${previa}")`;
    avatar.textContent = '';

    const dados = new FormData();
    dados.append('arquivo', foto, 'foto.jpg');
    const usuario = await requisicaoApi(urlUsuario('/foto'), { method: 'POST', body: dados });
    renderizarPerfil(usuario);
    mostrarFeedback(feedbackFoto, 'Foto atualizada.');
  } catch (erro) {
    avatar.style.backgroundImage = fundoAnterior;
    avatar.textContent = textoAnterior;
    mostrarFeedback(feedbackFoto, erro.message, true);
  } finally {
    alternarEnvioFoto(false);
    if (previa) window.setTimeout(() => URL.revokeObjectURL(previa), 5000);
  }
}

async function removerFoto() {
  alternarEnvioFoto(true);
  mostrarFeedback(feedbackFoto, 'Removendo foto...');
  try {
    const usuario = await requisicaoApi(urlUsuario('/foto'), { method: 'DELETE' });
    renderizarPerfil(usuario);
    mostrarFeedback(feedbackFoto, 'Foto removida. Suas iniciais aparecem no lugar.');
  } catch (erro) {
    mostrarFeedback(feedbackFoto, erro.message, true);
  } finally {
    alternarEnvioFoto(false);
    document.getElementById('foto-remover').hidden = !obterUsuarioAtual()?.fotoPerfilUrl;
  }
}

function renderizarEstado2fa(ativo) {
  const status = document.getElementById('twofa-status');
  status.textContent = ativo ? 'Ativo' : 'Inativo';
  status.className = `badge ${ativo ? 'badge--cyan' : 'badge--warning'}`;
  document.getElementById('twofa-inativo').hidden = ativo;
  document.getElementById('twofa-configuracao').hidden = true;
  document.getElementById('twofa-desativar-form').hidden = !ativo;
}

async function salvarPerfil(evento) {
  evento.preventDefault();
  if (!formularioPerfil.reportValidity()) return;
  const botao = formularioPerfil.querySelector('button[type="submit"]');
  botao.disabled = true;
  mostrarFeedback(feedbackPerfil, 'Salvando...');
  const dados = Object.fromEntries(new FormData(formularioPerfil).entries());
  try {
    const usuario = await requisicaoApi(urlUsuario(), { method: 'PUT', body: JSON.stringify(dados) });
    renderizarPerfil(usuario);
    mostrarFeedback(feedbackPerfil, 'Perfil atualizado.');
  } catch (erro) {
    mostrarFeedback(feedbackPerfil, erro.message, true);
  } finally {
    botao.disabled = false;
  }
}

async function iniciar2fa() {
  const botao = document.getElementById('twofa-iniciar');
  botao.disabled = true;
  mostrarFeedback(feedbackTwofa, 'Gerando QR Code...');
  try {
    const configuracao = await requisicaoApi(urlUsuario('/2fa/configurar'), { method: 'POST' });
    document.getElementById('twofa-qr').src = configuracao.qrCodeDataUri;
    document.getElementById('twofa-segredo').textContent = configuracao.segredo.replace(/(.{4})/g, '$1 ').trim();
    document.getElementById('twofa-inativo').hidden = true;
    document.getElementById('twofa-configuracao').hidden = false;
    mostrarFeedback(feedbackTwofa, '');
    document.getElementById('codigo-ativar').focus();
  } catch (erro) {
    mostrarFeedback(feedbackTwofa, erro.message, true);
  } finally {
    botao.disabled = false;
  }
}

async function enviarCodigo2fa(evento, caminho, mensagemSucesso) {
  evento.preventDefault();
  const formulario = evento.currentTarget;
  if (!formulario.reportValidity()) return;
  const botao = formulario.querySelector('button[type="submit"]');
  botao.disabled = true;
  try {
    const usuario = await requisicaoApi(urlUsuario(caminho), {
      method: 'POST',
      body: JSON.stringify({ codigoTotp: formulario.codigoTotp.value.trim() })
    });
    formulario.reset();
    renderizarPerfil(usuario);
    mostrarFeedback(feedbackTwofa, mensagemSucesso);
  } catch (erro) {
    mostrarFeedback(feedbackTwofa, erro.message, true);
  } finally {
    botao.disabled = false;
  }
}

function ativar2fa(evento) {
  return enviarCodigo2fa(evento, '/2fa/ativar', '2FA ativado. O próximo login vai pedir o código do aplicativo.');
}

function desativar2fa(evento) {
  return enviarCodigo2fa(evento, '/2fa/desativar', '2FA desativado.');
}

async function solicitarExclusao(evento) {
  evento.preventDefault();
  const confirmacao = document.getElementById('lgpd-confirmacao').value.trim();
  if (confirmacao !== 'EXCLUIR') {
    mostrarFeedback(feedbackLgpd, 'Digite EXCLUIR, em maiúsculas, para confirmar.', true);
    return;
  }
  const botao = evento.currentTarget.querySelector('button[type="submit"]');
  botao.disabled = true;
  try {
    await requisicaoApi(urlUsuario('/esquecimento'), { method: 'POST' });
    mostrarFeedback(feedbackLgpd, 'Pedido registrado. Seus dados serão apagados em até 24 horas. Encerrando a sessão...');
    window.setTimeout(() => {
      sessionStorage.clear();
      window.location.assign('/login');
    }, 2500);
  } catch (erro) {
    mostrarFeedback(feedbackLgpd, erro.message, true);
    botao.disabled = false;
  }
}
