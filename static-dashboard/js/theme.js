// TICKET-ADV100 — persisted theme toggle with no flash on reload.
(function () {
  const storageKey = 'reconx-theme';
  const storedTheme = localStorage.getItem(storageKey);
  const initialTheme =
    storedTheme === 'dark' || storedTheme === 'light'
      ? storedTheme
      : 'light';

  // Runs from <head> before the stylesheet and body paint.
  document.documentElement.setAttribute('data-theme', initialTheme);

  document.addEventListener('DOMContentLoaded', () => {
    const button = document.getElementById('theme-toggle');

    if (!button) {
      return;
    }

    function updateButton(theme) {
      const isDark = theme === 'dark';
      button.setAttribute('aria-pressed', String(isDark));
      button.textContent = isDark ? 'Light theme' : 'Dark theme';
    }

    updateButton(initialTheme);

    button.addEventListener('click', () => {
      const currentTheme =
        document.documentElement.getAttribute('data-theme');

      const nextTheme =
        currentTheme === 'dark' ? 'light' : 'dark';

      document.documentElement.setAttribute(
        'data-theme',
        nextTheme
      );

      localStorage.setItem(storageKey, nextTheme);
      updateButton(nextTheme);
    });
  });
})();