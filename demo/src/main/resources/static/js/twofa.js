const camposCodigo = Array.from(document.querySelectorAll('.otp-input'));
const email2fa = sessionStorage.getItem('fintwin.email2fa');
const retorno2fa = document.getElementById('twofa-feedback');
document.getElementById('twofa-email').value = email2fa || '';

if (!email2fa) {
  retorno2fa.textContent = 'Sua etapa de login expirou. Volte ao login para continuar.';
  document.querySelector('#twofa-form button[type="submit"]').disabled = true;
}

camposCodigo.forEach((campo, indice) => {
  campo.addEventListener('input', () => {
    const digitos = campo.value.replace(/\D/g, '');
    if (digitos.length > 1) {
      digitos.slice(0, camposCodigo.length).split('').forEach((digito, posicao) => {
        camposCodigo[posicao].value = digito;
        camposCodigo[posicao].classList.add('otp-input--filled');
      });
      camposCodigo[Math.min(digitos.length, camposCodigo.length) - 1].focus();
      return;
    }
    campo.value = digitos.slice(-1);
    campo.classList.toggle('otp-input--filled', campo.value.length > 0);
    if (campo.value && indice < camposCodigo.length - 1) {
      camposCodigo[indice + 1].focus();
    }
  });
  campo.addEventListener('keydown', evento => {
    if (evento.key === 'Backspace' && !campo.value && indice > 0) {
      camposCodigo[indice - 1].focus();
    }
  });
  campo.addEventListener('paste', evento => {
    const colado = evento.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    if (colado.length === 6) {
      evento.preventDefault();
      camposCodigo.forEach((item, posicao) => {
        item.value = colado[posicao];
        item.classList.add('otp-input--filled');
      });
      camposCodigo[5].focus();
    }
  });
});

camposCodigo[0].focus();

let segundosRestantes = 30;
const barraTempo = document.getElementById('timerBar');
const textoTempo = document.getElementById('timerCount');

(function atualizarContagem() {
  barraTempo.style.width = `${segundosRestantes / 30 * 100}%`;
  textoTempo.textContent = `${segundosRestantes}s`;
  if (segundosRestantes > 0) {
    segundosRestantes--;
    window.setTimeout(atualizarContagem, 1000);
  } else {
    textoTempo.textContent = 'Expirado';
    barraTempo.classList.add('timer-bar--expired');
  }
})();

document.getElementById('twofa-form').addEventListener('submit', async evento => {
  evento.preventDefault();
  const codigoTotp = camposCodigo.map(campo => campo.value).join('');
  retorno2fa.textContent = 'Validando código...';

  try {
    const resposta = await fetch('/api/auth/validar-2fa', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email2fa, codigoTotp })
    });
    const dados = await resposta.json();
    if (!resposta.ok) {
      throw new Error(dados.mensagem || dados.message || 'Código inválido ou expirado.');
    }

    sessionStorage.removeItem('fintwin.email2fa');
    sessionStorage.setItem('fintwin.token', dados.token);
    sessionStorage.setItem('fintwin.usuario', JSON.stringify(dados.usuario));
    window.location.assign('/dashboard');
  } catch (erro) {
    retorno2fa.textContent = erro.message;
    retorno2fa.classList.add('form-feedback--error');
  }
});
