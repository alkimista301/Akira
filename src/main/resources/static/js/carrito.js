
let carrito = JSON.parse(localStorage.getItem('carrito')) || [];

// Inicializar al cargar la página
document.addEventListener('DOMContentLoaded', function() {
    // PRIMERO: Verificar localStorage para estado inmediato
    verificarEstadoLocalStorage();
    
    // SEGUNDO: Verificar con servidor para confirmar sesión válida
    verificarEstadoSesion();
    
    // TERCERO: Actualizar carrito
    actualizarContadorCarrito();
    
    // Configurar formulario de login
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            procesarLoginCliente();
        });
    }
});

// VERIFICACIÓN INMEDIATA desde localStorage
function verificarEstadoLocalStorage() {
    const clienteData = localStorage.getItem('clienteLogueado');
    
    if (clienteData) {
        try {
            const cliente = JSON.parse(clienteData);
            console.log('Cliente encontrado en localStorage:', cliente);
            
            // Verificar si los datos no están muy antiguos (opcional: 24 horas)
            const ahora = Date.now();
            const tiempoExpiracion = 24 * 60 * 60 * 1000; // 24 horas en millisegundos
            
            if (cliente.timestamp && (ahora - cliente.timestamp) > tiempoExpiracion) {
                console.log('Datos del cliente expirados, removiendo...');
                localStorage.removeItem('clienteLogueado');
                return;
            }
            
            // Mostrar inmediatamente desde localStorage
            mostrarClienteLogueado(cliente.nombre);
        } catch (error) {
            console.error('Error al parsear datos del cliente:', error);
            localStorage.removeItem('clienteLogueado');
        }
    }
}

// VERIFICACIÓN con servidor (para validar sesión)
function verificarEstadoSesion() {
    console.log('Verificando estado de sesión con servidor...');
    
    fetch('/auth/estado-sesion', {
        method: 'GET',
        headers: {
            'Accept': 'application/json',
            'Cache-Control': 'no-cache'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        return response.json();
    })
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
            mostrarClienteLogueado(data.clienteNombre);
            
        } else {
            // No autenticado en servidor, limpiar localStorage
            localStorage.removeItem('clienteLogueado');
            mostrarLoginButton();
        }
    })
    .catch(error => {
        console.error('Error al verificar sesión:', error);
        
        // Si hay error de servidor pero tenemos datos locales, mantenerlos temporalmente
        const clienteData = localStorage.getItem('clienteLogueado');
        if (!clienteData) {
            mostrarLoginButton();
        }
    });
}

function mostrarLoginButton() {
    console.log('Mostrando botón de login');
    const loginSection = document.getElementById('loginSection');
    const clienteSection = document.getElementById('clienteSection');
    
    if (loginSection && clienteSection) {
        loginSection.style.display = 'block';
        clienteSection.style.display = 'none';
    }
}

function mostrarClienteLogueado(nombreCliente) {
    console.log('Mostrando cliente logueado:', nombreCliente);
    const loginSection = document.getElementById('loginSection');
    const clienteSection = document.getElementById('clienteSection');
    const clienteNombre = document.getElementById('clienteNombreDisplay');
    
    if (loginSection && clienteSection && clienteNombre) {
        loginSection.style.display = 'none';
        clienteSection.style.display = 'block';
        clienteNombre.textContent = nombreCliente;
        
        console.log('Header actualizado - Cliente:', nombreCliente);
    } else {
        console.error('Elementos del header no encontrados:', {
            loginSection: !!loginSection,
            clienteSection: !!clienteSection, 
            clienteNombre: !!clienteNombre
        });
    }
}

// FUNCIÓN DE DEBUG - Puedes llamarla desde la consola
function debugEstadoHeader() {
    console.log('=== DEBUG ESTADO HEADER ===');
    console.log('localStorage clienteLogueado:', localStorage.getItem('clienteLogueado'));
    
    const loginSection = document.getElementById('loginSection');
    const clienteSection = document.getElementById('clienteSection');
    const clienteNombre = document.getElementById('clienteNombreDisplay');
    
    console.log('Elementos encontrados:', {
        loginSection: loginSection ? 'SÍ' : 'NO',
        clienteSection: clienteSection ? 'SÍ' : 'NO',
        clienteNombre: clienteNombre ? 'SÍ' : 'NO'
    });
    
    if (loginSection) console.log('loginSection display:', loginSection.style.display);
    if (clienteSection) console.log('clienteSection display:', clienteSection.style.display);
    if (clienteNombre) console.log('clienteNombre text:', clienteNombre.textContent);
    
    // Verificar sesión del servidor
    fetch('/auth/estado-sesion')
        .then(response => response.json())
        .then(data => {
            console.log('Estado sesión servidor:', data);
        });
}

// Actualizar contador del carrito
function actualizarContadorCarrito() {
    const contador = document.getElementById('cartCounter');
    if (contador) {
        const totalItems = carrito.reduce((total, item) => total + item.cantidad, 0);
        contador.textContent = totalItems;
        contador.style.display = totalItems > 0 ? 'inline-block' : 'none';
    }
}

// Agregar al final del script
document.addEventListener('click', function(e) {
    if (e.target.closest('.btn-agregar-carrito')) {
        const btn = e.target.closest('.btn-agregar-carrito');
        const id = parseInt(btn.dataset.id);
        const nombre = btn.dataset.nombre;
        const precio = parseFloat(btn.dataset.precio);
        const stock = parseInt(btn.dataset.stock);
        
        agregarAlCarrito(id, nombre, precio, stock);
    }
});

// Agregar producto al carrito //,
function agregarAlCarrito(productoId,  nombre, precio, stock) { 

	console.log("***", productoId, nombre, precio, stock);
    if (stock <= 0) {
        alert('Producto sin stock disponible');
        return;
    }
    
    const existente = carrito.find(item => item.id === productoId);
    
    if (existente) {
        if (existente.cantidad < stock) {
            existente.cantidad += 1;
            existente.subtotal = existente.precio * existente.cantidad;
        } else {
            alert('No hay más stock disponible');
            return;
        }
    } else {
        carrito.push({
            id: productoId,
            nombre: nombre,
            precio: precio,
            cantidad: 1,
            subtotal: precio,
            stock: stock
        });
    }
    
    localStorage.setItem('carrito', JSON.stringify(carrito));
    actualizarContadorCarrito();
    alert('Producto agregado al carrito');
}

// Abrir modal del carrito
function abrirCarrito() {
    cargarCarritoModal();
    const modal = new bootstrap.Modal(document.getElementById('carritoModal'));
    modal.show();
}

// Cargar contenido del carrito
function cargarCarritoModal() {
    const carritoVacio = document.getElementById('carritoVacio');
    const carritoConProductos = document.getElementById('carritoConProductos');
    const listaProductos = document.getElementById('listaProductosCarrito');
    const totalElement = document.getElementById('totalCarrito');
    
    if (carrito.length === 0) {
        carritoVacio.style.display = 'block';
        carritoConProductos.style.display = 'none';
        return;
    }
    
    carritoVacio.style.display = 'none';
    carritoConProductos.style.display = 'block';
    
    let productosHTML = '';
    let total = 0;
    
    carrito.forEach((item, index) => {
        total += item.subtotal;
        productosHTML += `
            <div class="cart-item mb-3 p-3 border rounded">
                <div class="row align-items-center">
                    <div class="col-md-6">
                        <h6 class="mb-1">${item.nombre}</h6>
                        <small class="text-muted">Precio: S/ ${item.precio}</small>
                    </div>
                    <div class="col-md-3">
                        <div class="d-flex align-items-center">
                            <button class="btn btn-sm btn-outline-secondary" onclick="cambiarCantidad(${index}, ${item.cantidad - 1})">-</button>
                            <span class="mx-2">${item.cantidad}</span>
                            <button class="btn btn-sm btn-outline-secondary" onclick="cambiarCantidad(${index}, ${item.cantidad + 1})">+</button>
                        </div>
                    </div>
                    <div class="col-md-2">
                        <strong>S/ ${item.subtotal}</strong>
                    </div>
                    <div class="col-md-1">
                        <button class="btn btn-sm btn-outline-danger" onclick="eliminarDelCarrito(${index})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;
    });
    
    listaProductos.innerHTML = productosHTML;
    totalElement.textContent = `S/ ${total}`;
}

function cambiarCantidad(index, nuevaCantidad) {
    if (nuevaCantidad <= 0) {
        eliminarDelCarrito(index);
        return;
    }
    
    const item = carrito[index];
    if (nuevaCantidad <= item.stock) {
        item.cantidad = nuevaCantidad;
        item.subtotal = item.precio * nuevaCantidad;
        localStorage.setItem('carrito', JSON.stringify(carrito));
        actualizarContadorCarrito();
        cargarCarritoModal();
    } else {
        alert(`Stock máximo: ${item.stock} unidades`);
    }
}

function eliminarDelCarrito(index) {
    carrito.splice(index, 1);
    localStorage.setItem('carrito', JSON.stringify(carrito));
    actualizarContadorCarrito();
    cargarCarritoModal();
}

function vaciarCarrito() {
    if (confirm('¿Estás seguro de vaciar todo el carrito?')) {
        carrito = [];
        localStorage.setItem('carrito', JSON.stringify(carrito));
        actualizarContadorCarrito();
        cargarCarritoModal();
    }
}

function procederCheckout() {
    if (carrito.length === 0) {
        alert('El carrito está vacío');
        return;
    }
    
    // Verificar si es cliente
    fetch('/auth/estado-sesion')
        .then(response => response.json())
        .then(data => {
            if (data.autenticado && data.tipoUsuario === 'cliente') {
                window.location.href = '/checkout';
            } else {
                alert('Debes iniciar sesión como cliente para realizar una compra');
                bootstrap.Modal.getInstance(document.getElementById('carritoModal')).hide();
                bootstrap.Modal.getOrCreateInstance(document.getElementById('loginModal')).show();
            }
        });
}

function irDashboardCliente() {
    fetch('/auth/cliente-actual')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                window.location.href = `/cliente-dashboard/${data.data.dni}`;
            }
        })
        .catch(error => {
            console.error('Error al obtener datos del cliente:', error);
            // Si hay error, intentar recargar sesión
            verificarEstadoSesion();
        });
}

// FUNCIÓN PARA CERRAR SESIÓN
function cerrarSesionCliente() {
    if (confirm('¿Estás seguro que deseas cerrar sesión?')) {
        // LIMPIAR localStorage INMEDIATAMENTE
        localStorage.removeItem('clienteLogueado');
        console.log('Cliente removido de localStorage');
        
        // Actualizar interfaz inmediatamente
        mostrarLoginButton();
        
        // Notificar al servidor (opcional, para limpiar sesión del servidor)
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
            // No importa si falla, ya limpiamos localStorage
        });
        
        alert('Sesión cerrada exitosamente');
    }
}

// Modificar login para incluir clientes
function procesarLoginCliente() {
    const formData = new FormData(document.getElementById('loginForm'));
    const usuario = formData.get('email');
    const password = formData.get('password');
    
    if (!usuario || !password) {
        alert('Complete todos los campos');
        return;
    }
    
    fetch('/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            usuario: usuario,
            password: password
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            if (data.tipoUsuario === 'cliente') {
                // GUARDAR en localStorage
                const clienteData = {
                    nombre: data.clienteNombre,
                    usuario: usuario,
                    tipoUsuario: 'cliente',
                    timestamp: Date.now()
                };
                
                localStorage.setItem('clienteLogueado', JSON.stringify(clienteData));
                console.log('Cliente guardado en localStorage:', clienteData);
                
                // Actualizar header inmediatamente
                mostrarClienteLogueado(data.clienteNombre);
                
                // Cerrar modal
                bootstrap.Modal.getInstance(document.getElementById('loginModal')).hide();
                
                // Mostrar mensaje de éxito
                alert('¡Bienvenido ' + data.clienteNombre + '!');
                
            } else {
                // Usuario interno - redirigir
                window.location.href = data.redirectUrl;
            }
        } else {
            alert(data.message || 'Error de autenticación');
        }
    })
    .catch(error => {
        console.error('Error en login:', error);
        alert('Error de conexión');
    });
}



function filtrarProductos() {
           const filtro = document.getElementById('buscar').value.toLowerCase();
           const productos = document.querySelectorAll('.producto-item');
           
           productos.forEach(producto => {
               const nombre = producto.dataset.nombre.toLowerCase();
               if (nombre.includes(filtro)) {
                   producto.style.display = 'block';
               } else {
                   producto.style.display = 'none';
               }
           });
       }

       function ordenarProductos() {
           const orden = document.getElementById('ordenar').value;
           const grid = document.getElementById('productosGrid');
           const productos = Array.from(grid.children);
           
           productos.sort((a, b) => {
               switch (orden) {
                   case 'nombre':
                       return a.dataset.nombre.localeCompare(b.dataset.nombre);
                   case 'precio-asc':
                       return parseFloat(a.dataset.precio) - parseFloat(b.dataset.precio);
                   case 'precio-desc':
                       return parseFloat(b.dataset.precio) - parseFloat(a.dataset.precio);
                   default:
                       return 0;
               }
           });
           
           productos.forEach(producto => grid.appendChild(producto));
       }

	

       function verDetalles(productoId) {
           // Obtener detalles del producto via API
           fetch(`/producto/api/buscar/${productoId}`)
           .then(response => response.json())
           .then(producto => {
               const modal = document.getElementById('detallesProductoModal');
               const content = document.getElementById('detallesProductoContent');
               
               content.innerHTML = `
                   <div class="row">
                       <div class="col-md-4">
                           <img src="/images/producto_${producto.codigo}.jpg" 
                                alt="${producto.nombre}" 
                                class="img-fluid"
                                onerror="this.src='/images/noimg.jpg'">
                       </div>
                       <div class="col-md-8">
                           <h4>${producto.nombre}</h4>
                           <p class="text-muted">${producto.descripcion || 'Sin descripción disponible'}</p>
                           <div class="product-details">
                               <p><strong>Marca:</strong> ${producto.marca ? producto.marca.nombre : 'No especificada'}</p>
                               <p><strong>Modelo:</strong> ${producto.modelo || 'No especificado'}</p>
                               <p><strong>Precio:</strong> <span class="text-success fs-4">S/ ${producto.precio.toFixed(2)}</span></p>
                               <p><strong>Stock:</strong> 
                                   <span class="${producto.cantidadStock > 0 ? 'text-success' : 'text-danger'}">
                                       ${producto.cantidadStock > 0 ? producto.cantidadStock + ' unidades' : 'Sin stock'}
                                   </span>
                               </p>
                           </div>
                       </div>
                   </div>
               `;
               
               // Configurar botón de agregar al carrito
               const btnAgregar = document.getElementById('btnAgregarCarrito');
               btnAgregar.onclick = () => {
                   agregarAlCarrito(producto.codigo);
                   bootstrap.Modal.getInstance(modal).hide();
               };
               btnAgregar.disabled = producto.cantidadStock === 0;
               
               new bootstrap.Modal(modal).show();
           })
           .catch(error => {
               console.error('Error:', error);
               mostrarNotificacion('Error al cargar detalles del producto', 'danger');
           });
       }

       function mostrarNotificacion(mensaje, tipo) {
           const alertClass = tipo === 'success' ? 'alert-success' : 'alert-danger';
           const alerta = document.createElement('div');
           alerta.className = `alert ${alertClass} alert-dismissible fade show position-fixed`;
           alerta.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
           alerta.innerHTML = `
               ${mensaje}
               <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
           `;
           
           document.body.appendChild(alerta);
           
           setTimeout(() => {
               if (alerta.parentNode) {
                   alerta.parentNode.removeChild(alerta);
               }
           }, 3000);
       }