// Perfil Familiar - pre-visualizacao. Tudo aqui e visual: nenhuma chamada a API, nada e salvo.
const sessaoFamilia = exigirSessao();
const CHAVE_VISAO = 'fintwin.familia.visao';

if (sessaoFamilia) {
  iniciarFamilia();
}

function iniciarFamilia() {
  const usuario = sessaoFamilia.usuario;
  const nome = usuario.nome || usuario.email;

  document.querySelectorAll('[data-familia-nome]').forEach(el => { el.textContent = nome; });
  document.querySelectorAll('[data-familia-primeiro-nome]').forEach(el => { el.textContent = String(nome).split(/\s+/)[0]; });
  document.querySelectorAll('[data-familia-avatar]').forEach(el => aplicarAvatar(el, usuario));

  if (usuario.tipoUsuario !== 'FAMILIAR') {
    document.getElementById('familia-aviso-tipo').textContent =
      `Sua conta é ${formatarTipoUsuario(usuario.tipoUsuario)}; esta tela mostra como ficaria no perfil Familiar.`;
  }

  document.querySelectorAll('.visao__opcao').forEach(botao => {
    botao.addEventListener('click', () => trocarVisao(botao.dataset.visao));
  });

  let visaoSalva = null;
  try { visaoSalva = sessionStorage.getItem(CHAVE_VISAO); } catch { /* sem armazenamento */ }
  trocarVisao(visaoSalva === 'membro' ? 'membro' : 'responsavel');

  document.querySelectorAll('[data-demo]').forEach(botao => {
    botao.addEventListener('click', () => {
      const aprovacao = botao.closest('.aprovacao');
      if (aprovacao) {
        aprovacao.classList.add('aprovacao--decidida');
        aprovacao.querySelectorAll('button').forEach(b => { b.disabled = true; });
      }
      mostrarAvisoFamilia(botao.dataset.demo);
    });
  });

  document.querySelectorAll('[data-so-edita]').forEach(chave => {
    chave.addEventListener('change', () => mostrarAvisoFamilia('Permissão alterada só nesta tela (pré-visualização).'));
  });
}

function trocarVisao(visao) {
  document.getElementById('familia').dataset.visao = visao;
  document.querySelectorAll('.visao__opcao').forEach(botao => {
    botao.setAttribute('aria-pressed', String(botao.dataset.visao === visao));
  });
  // Membro ve as permissoes, mas nao pode altera-las.
  document.querySelectorAll('[data-so-edita]').forEach(chave => { chave.disabled = visao === 'membro'; });
  try { sessionStorage.setItem(CHAVE_VISAO, visao); } catch { /* sem armazenamento */ }
}

let temporizadorAvisoFamilia;

function mostrarAvisoFamilia(mensagem) {
  const aviso = document.getElementById('familia-toast');
  aviso.textContent = mensagem;
  aviso.classList.add('familia-toast--visivel');
  clearTimeout(temporizadorAvisoFamilia);
  temporizadorAvisoFamilia = setTimeout(() => aviso.classList.remove('familia-toast--visivel'), 3200);
}
