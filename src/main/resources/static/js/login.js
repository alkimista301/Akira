document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const loginBtn = document.getElementById('loginBtn');
    const emailInput = document.querySelector('input[name="email"]');
    const passwordInput = document.querySelector('input[name="password"]');
    
    // VERIFICAR ESTADO AL CARGAR PÁGINA
    verificarEstadoSesionHeader();
    
    if (loginForm) {
        // Form submission
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            const usuario = emailInput.value.trim();
            const password = passwordInput.value.trim();
            
            // Basic validation
            if (!usuario || !password) {
                showLoginError('Por favor, completa todos los campos.');
                return false;
            }
            
            // Show loading state
            loginBtn.classList.add('loading');
            loginBtn.textContent = 'Iniciando sesión...';
            loginBtn.disabled = true;
            
            // Call our auth endpoint
            fetch('/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    usuario: usuario,
                    password: password
                })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error en la respuesta del servidor');
                }
                return response.json();
            })
            .then(data => {
                console.log('Respuesta del servidor:', data);
                
                if (data.success) {
                    // Reset button
                    resetLoginButton();
                    
                    if (data.tipoUsuario === 'cliente') {
                        // CLIENTE: Guardar en localStorage y actualizar header
                        const clienteData = {
                            nombre: data.clienteNombre,
                            usuario: usuario,
                            tipoUsuario: 'cliente',
                            timestamp: Date.now()
                        };
                        
                        localStorage.setItem('clienteLogueado', JSON.stringify(clienteData));
                        console.log('Cliente guardado en localStorage:', clienteData);
                        
                        // Actualizar header
                        actualizarHeaderCliente(data.clienteNombre);
                        
                        // Show success message
                        showLoginSuccess('¡Bienvenido ' + data.clienteNombre + '!');
                        
                        // Close modal
                        setTimeout(() => {
                            const loginModal = bootstrap.Modal.getInstance(document.getElementById('loginModal'));
                            if (loginModal) {
                                loginModal.hide();
                            }
                        }, 1500);
                        
                    } else {
                        // USUARIO INTERNO: Redirigir como antes
                        showLoginSuccess('¡Bienvenido de vuelta! Redirigiendo...');
                        
                        setTimeout(() => {
                            const loginModal = bootstrap.Modal.getInstance(document.getElementById('loginModal'));
                            if (loginModal) {
                                loginModal.hide();
                            }
                            
                            // Redirect to dashboard
                            window.location.href = data.redirectUrl || '/dashboard/' + data.usuario;
                        }, 1500);
                    }
                } else {
                    resetLoginButton();
                    showLoginError(data.message || 'Error en el login');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                resetLoginButton();
                showLoginError('Error de conexión. Intente nuevamente.');
            });
            
            return false;
        });
    }
    
    function resetLoginButton() {
        if (loginBtn) {
            loginBtn.classList.remove('loading');
            loginBtn.textContent = 'Ingresar';
            loginBtn.disabled = false;
        }
    }
    
    function showLoginError(message) {
        // Remove existing alerts
        removeExistingLoginAlerts();
        
        // Create error alert
        const alert = document.createElement('div');
        alert.className = 'alert alert-danger alert-dismissible fade show login-alert';
        alert.innerHTML = `
            <i class="fas fa-exclamation-triangle me-2"></i>${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        // Insert before the form
        const modalBody = document.querySelector('#loginModal .modal-body');
        const form = document.getElementById('loginForm');
        modalBody.insertBefore(alert, form);
    }
    
    function showLoginSuccess(message) {
        // Remove existing alerts
        removeExistingLoginAlerts();
        
        // Create success alert
        const alert = document.createElement('div');
        alert.className = 'alert alert-success alert-dismissible fade show login-alert';
        alert.innerHTML = `
            <i class="fas fa-check-circle me-2"></i>${message}
        `;
        
        // Insert before the form
        const modalBody = document.querySelector('#loginModal .modal-body');
        const form = document.getElementById('loginForm');
        modalBody.insertBefore(alert, form);
    }
    
    function removeExistingLoginAlerts() {
        const existingAlerts = document.querySelectorAll('#loginModal .login-alert');
        existingAlerts.forEach(alert => alert.remove());
    }
    
    // Reset form when modal is hidden
    const loginModal = document.getElementById('loginModal');
    if (loginModal) {
        loginModal.addEventListener('hidden.bs.modal', function() {
            if (loginForm) {
                loginForm.reset();
            }
            resetLoginButton();
            removeExistingLoginAlerts();
        });
    }
});

// ========================================
// FUNCIONES PARA EL HEADER
// ========================================

// Verificar estado de sesión al cargar cualquier página
function verificarEstadoSesionHeader() {
    // Primero verificar localStorage para mostrar inmediatamente
    const clienteData = localStorage.getItem('clienteLogueado');
    
    if (clienteData) {
        try {
            const cliente = JSON.parse(clienteData);
            console.log('Cliente encontrado en localStorage:', cliente);
            
            // Verificar si no está expirado (24 horas)
            const ahora = Date.now();
            const tiempoExpiracion = 24 * 60 * 60 * 1000;
            
            if (cliente.timestamp && (ahora - cliente.timestamp) > tiempoExpiracion) {
                console.log('Datos del cliente expirados');
                localStorage.removeItem('clienteLogueado');
                mostrarLoginButtonHeader();
                return;
            }
            
            // Mostrar inmediatamente
            actualizarHeaderCliente(cliente.nombre);
            
        } catch (error) {
            console.error('Error al parsear datos del cliente:', error);
            localStorage.removeItem('clienteLogueado');
            mostrarLoginButtonHeader();
        }
    } else {
        mostrarLoginButtonHeader();
    }
    
    // Luego verificar con el servidor para confirmar
    fetch('/auth/estado-sesion', {
        method: 'GET',
        headers: {
            'Accept': 'application/json',
            'Cache-Control': 'no-cache'
        }
    })
    .then(response => response.json())
    .then(data => {
        console.log('Estado de sesión del servidor:', data);
        
        if (data.autenticado && data.tipoUsuario === 'cliente') {
            // Actualizar localStorage con datos del servidor
            const clienteData = {
                nombre: data.clienteNombre,
                usuario: data.clienteUsuario || '',
                tipoUsuario: 'cliente',
                timestamp: Date.now()
            };
            
            localStorage.setItem('clienteLogueado', JSON.stringify(clienteData));
            actualizarHeaderCliente(data.clienteNombre);
            
        } else {
            // No autenticado en servidor, limpiar localStorage
            localStorage.removeItem('clienteLogueado');
            mostrarLoginButtonHeader();
        }
    })
    .catch(error => {
        console.error('Error al verificar sesión:', error);
        // Si hay error de servidor pero tenemos datos locales, mantenerlos
        if (!localStorage.getItem('clienteLogueado')) {
            mostrarLoginButtonHeader();
        }
    });
}

// Actualizar header para mostrar cliente
function actualizarHeaderCliente(nombreCliente) {
    console.log('Actualizando header para cliente:', nombreCliente);
    
    const loginSection = document.getElementById('loginSection');
    const clienteSection = document.getElementById('clienteSection');
    const clienteNombre = document.getElementById('clienteNombreDisplay');
    
    if (loginSection && clienteSection && clienteNombre) {
        loginSection.style.display = 'none';
        clienteSection.style.display = 'block';
        clienteNombre.textContent = nombreCliente;
        console.log('Header actualizado exitosamente');
    } else {
        console.error('Elementos del header no encontrados');
    }
}

// Mostrar botón de login
function mostrarLoginButtonHeader() {
    console.log('Mostrando botón de login');
    
    const loginSection = document.getElementById('loginSection');
    const clienteSection = document.getElementById('clienteSection');
    
    if (loginSection && clienteSection) {
        loginSection.style.display = 'block';
        clienteSection.style.display = 'none';
    }
}

// Ir al dashboard del cliente
function irDashboardCliente() {
    fetch('/auth/cliente-actual')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                window.location.href = `/cliente-dashboard/${data.data.dni}`;
            } else {
                console.error('Error al obtener datos del cliente:', data.message);
            }
        })
        .catch(error => {
            console.error('Error al obtener datos del cliente:', error);
        });
}

// Cerrar sesión del cliente
function cerrarSesionCliente() {
    if (confirm('¿Estás seguro que deseas cerrar sesión?')) {
        // Limpiar localStorage inmediatamente
        localStorage.removeItem('clienteLogueado');
        console.log('Cliente removido de localStorage');
        
        // Actualizar header
        mostrarLoginButtonHeader();
        
        // Notificar al servidor
        fetch('/auth/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        })
        .then(response => response.json())
        .then(data => {
            console.log('Sesión cerrada en servidor:', data);
        })
        .catch(error => {
            console.error('Error al cerrar sesión en servidor:', error);
        });
        
        alert('Sesión cerrada exitosamente');
    }
}

// Function to show register (if needed)
function showRegister() {
    alert('Función de registro aún no implementada');
}