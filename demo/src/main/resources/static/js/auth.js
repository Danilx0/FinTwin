function showLogin() {
  document.getElementById('tab-login').classList.add('form-tab--active');
  document.getElementById('tab-cadastro').classList.remove('form-tab--active');
  document.getElementById('form-login').style.display = 'block';
  document.getElementById('form-cadastro').style.display = 'none';
}

function showCadastro() {
  document.getElementById('tab-cadastro').classList.add('form-tab--active');
  document.getElementById('tab-login').classList.remove('form-tab--active');
  document.getElementById('form-login').style.display = 'none';
  document.getElementById('form-cadastro').style.display = 'block';
}

function mostrarResposta(elemento, mensagem, erro = false) {
  elemento.textContent = mensagem;
  elemento.classList.toggle('form-feedback--error', erro);
}

document.getElementById('form-login').addEventListener('submit', async evento => {
  evento.preventDefault();
  const feedback = document.getElementById('login-feedback');
  const email = document.getElementById('email-login').value.trim();
  const senha = document.getElementById('senha-login').value;
  mostrarResposta(feedback, 'Validando acesso...');

  try {
    const resposta = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, senha })
    });
    const dados = await resposta.json();
    if (!resposta.ok) {
      throw new Error(dados.mensagem || dados.message || 'Não foi possível entrar com esses dados.');
    }

    if (dados.twoFactorRequired) {
      sessionStorage.setItem('fintwin.email2fa', email);
      window.location.assign('/2fa');
      return;
    }

    sessionStorage.setItem('fintwin.token', dados.token);
    sessionStorage.setItem('fintwin.usuario', JSON.stringify(dados.usuario));
    window.location.assign('/dashboard');
  } catch (erro) {
    mostrarResposta(feedback, erro.message, true);
  }
});

document.getElementById('form-cadastro').addEventListener('submit', async evento => {
  evento.preventDefault();
  const feedback = document.getElementById('cadastro-feedback');
  const dadosFormulario = new FormData(evento.currentTarget);
  const dados = Object.fromEntries(dadosFormulario.entries());
  mostrarResposta(feedback, 'Criando sua conta...');

  try {
    const resposta = await fetch('/api/usuarios', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dados)
    });
    const corpo = await resposta.json();
    if (!resposta.ok) {
      throw new Error(corpo.mensagem || corpo.message || corpo.erro || 'Não foi possível criar a conta.');
    }

    document.getElementById('email-login').value = dados.email;
    document.getElementById('senha-login').value = '';
    showLogin();
    mostrarResposta(document.getElementById('login-feedback'), 'Conta criada. Entre com seu e-mail e senha.');
  } catch (erro) {
    mostrarResposta(feedback, erro.message, true);
  }
});
