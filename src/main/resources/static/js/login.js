// ===========================================================
//  Smart Power On S.A.C. - JS de la página de login (RF01)
//  Toggle de password y guarda el JWT en sessionStorage
// ===========================================================

document.addEventListener('DOMContentLoaded', () => {
    const toggleBtn = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');
    const eyeIcon = document.getElementById('eyeIcon');

    if (toggleBtn && passwordInput) {
        toggleBtn.addEventListener('click', () => {
            const isPassword = passwordInput.type === 'password';
            passwordInput.type = isPassword ? 'text' : 'password';
            eyeIcon.classList.toggle('bi-eye');
            eyeIcon.classList.toggle('bi-eye-slash');
        });
    }

    // Si ya hay un token guardado y es válido, redirige al home
    const token = sessionStorage.getItem('jwt');
    if (token) {
        // Solo mostramos un mensaje de sesión activa
        console.log('Sesión JWT presente. Cierre por inactividad se gestiona en SecurityConfig.');
    }
});

// Helper global para fetch autenticado
window.authFetch = async function(url, options = {}) {
    const token = sessionStorage.getItem('jwt');
    if (token) {
        options.headers = options.headers || {};
        options.headers['Authorization'] = 'Bearer ' + token;
    }
    const res = await fetch(url, options);
    if (res.status === 401) {
        alert('Tu sesión ha expirado. Por favor, inicia sesión de nuevo.');
        sessionStorage.removeItem('jwt');
        window.location.href = '/login';
        return null;
    }
    return res;
};
