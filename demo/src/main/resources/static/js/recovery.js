const recoveryForm = document.getElementById('recovery-form');
const resetForm = document.getElementById('reset-form');
const recoveryFeedback = document.getElementById('recovery-feedback');

recoveryForm.addEventListener('submit', async evento => {
  evento.preventDefault();
  mostrarFeedback(recoveryFeedback, 'Solicitando token...');
  try {
    const dados = await requisicaoApi('/api/auth/recuperar-senha', {
      method: 'POST',
      body: JSON.stringify({ email: document.getElementById('recovery-email').value.trim() })
    });
    document.getElementById('reset-token').value = dados.tokenRecuperacao || '';
    resetForm.hidden = false;
    mostrarFeedback(recoveryFeedback, `Token gerado para este ambiente de demonstração: ${dados.tokenRecuperacao}`);
  } catch (erro) {
    mostrarFeedback(recoveryFeedback, erro.message, true);
  }
});

resetForm.addEventListener('submit', async evento => {
  evento.preventDefault();
  mostrarFeedback(recoveryFeedback, 'Atualizando senha...');
  try {
    await requisicaoApi('/api/auth/redefinir-senha', {
      method: 'POST',
      body: JSON.stringify({
        token: document.getElementById('reset-token').value.trim(),
        novaSenha: document.getElementById('new-password').value
      })
    });
    mostrarFeedback(recoveryFeedback, 'Senha redefinida. Você já pode entrar.');
    recoveryForm.reset();
    resetForm.reset();
    resetForm.hidden = true;
  } catch (erro) {
    mostrarFeedback(recoveryFeedback, erro.message, true);
  }
});
