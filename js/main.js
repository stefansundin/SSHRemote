document.addEventListener('DOMContentLoaded', function () {
  // Pass theme=dark in the query string to force dark mode
  const theme = window.location.search.substring(1).split('&').find(v => v.startsWith('theme='))?.split('=')?.[1];
  if (theme) {
    document.documentElement.setAttribute('data-bs-theme', theme);
  } else {
    const darkMode = window.matchMedia('(prefers-color-scheme: dark)').matches;
    document.documentElement.setAttribute('data-bs-theme', darkMode ? 'dark' : 'light');

    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      document.documentElement.setAttribute('data-bs-theme', e.matches ? 'dark' : 'light');
    });
  }

  for (const form of document.querySelectorAll('form[action="https://www.paypal.com/cgi-bin/webscr"]')) {
    form.addEventListener('submit', function (e) {
      if (parseInt(this.amount.value) < 1) {
        e.preventDefault();
        alert("The minimum donation amount is one dollar. Anything less than one dollar and you're just giving PayPal everything because of their fees.");
        return false;
      }
    });
  }
});
