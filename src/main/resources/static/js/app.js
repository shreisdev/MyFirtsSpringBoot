// Comportamentos leves da interface da barbearia.
document.addEventListener('DOMContentLoaded', function () {
  var pad = function (n) { return String(n).padStart(2, '0'); };

  // Formata um Date no padrao aceito por <input type="datetime-local"> (hora local).
  function paraInput(d) {
    return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) +
      'T' + pad(d.getHours()) + ':' + pad(d.getMinutes());
  }

  var agora = paraInput(new Date());

  // Nao permite escolher horario no passado e usa passos de 30 minutos.
  document.querySelectorAll('input[type="datetime-local"]').forEach(function (input) {
    if (!input.min) { input.min = agora; }
    if (!input.step) { input.step = 1800; }
  });

  // Esconde mensagens de alerta depois de alguns segundos.
  document.querySelectorAll('.alert').forEach(function (alerta) {
    setTimeout(function () {
      alerta.style.transition = 'opacity .4s ease';
      alerta.style.opacity = '0';
      setTimeout(function () { alerta.remove(); }, 400);
    }, 6000);
  });
});
