const API_BASE = 'http://localhost:8080/api';

if (window.location.protocol === 'file:') {
    alert("WARNING: You are opening this HTML file directly from your folders. The APIs will only work if you access the application via http://localhost:8080/ in your browser!");
}

// Generalized Fetch Helper
async function apiCall(endpoint, method = 'GET', body = null) {
    const headers = {
        'Content-Type': 'application/json'
    };

    const token = localStorage.getItem('token');
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        method,
        headers,
    };

    if (body) {
        config.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, config);
        const data = await response.json().catch(() => null);

        if (!response.ok) {
            if (response.status === 401) {
                logout();
            }
            throw new Error(data?.message || data?.error || 'Validation error or Server issue');
        }
        return data;
    } catch (error) {
        showToast(error.message, 'danger');
        throw error;
    }
}

function showToast(message, type) {
    const toastEl = document.getElementById('liveToast');
    if (!toastEl) return;
    document.getElementById('toast-message').textContent = message;
    toastEl.classList.remove('bg-success', 'bg-danger', 'text-white');
    if (type === 'danger') { toastEl.classList.add('bg-danger', 'text-white'); }
    const toast = new bootstrap.Toast(toastEl, { delay: 3000 });
    toast.show();
}

function redirectBasedOnRole(roles) {
    if (roles.includes('ROLE_ADMIN')) {
        window.location.href = 'admin.html';
    } else {
        window.location.href = 'member.html';
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = 'index.html';
}

// Attach listeners if on login/register page
document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('login-username').value;
            const password = document.getElementById('login-password').value;
            
            try {
                const data = await apiCall('/auth/signin', 'POST', { username, password });
                localStorage.setItem('token', data.token);
                localStorage.setItem('user', JSON.stringify(data));
                showToast(`Welcome back, ${data.username}!`);
                setTimeout(() => redirectBasedOnRole(data.roles), 1000);
            } catch (err) {
                console.error(err);
            }
        });
    }

    const regForm = document.getElementById('register-form');
    if (regForm) {
        regForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('reg-username').value;
            const email = document.getElementById('reg-email').value;
            const phone = document.getElementById('reg-phone').value;
            const password = document.getElementById('reg-password').value;
            
            try {
                await apiCall('/auth/signup', 'POST', { username, email, phone, password });
                showToast('Registration successful! Please sign in.', 'success');
                setTimeout(() => window.location.reload(), 1500);
            } catch (err) {
                console.error(err);
            }
        });
    }

    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            logout();
        });
    }
});
