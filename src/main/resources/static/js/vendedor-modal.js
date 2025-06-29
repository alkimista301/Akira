// ===========================
// VARIABLES GLOBALES PARA VENDEDOR
// ===========================
let clienteSeleccionadoVendedor = null;
let tipoPedidoSeleccionadoVendedor = null;
let productosSeleccionadosVendedor = [];
let componentesSeleccionadosVendedor = new Map();
let categoriasDisponiblesVendedor = [];
let vendedorActualId = null;

// ===========================
// INICIALIZACIÓN DEL MODAL
// ===========================
function showNewOrderModal() {
    // Reiniciar todo
    limpiarFormularioCompletoVendedor();
    
    // Cargar datos iniciales
    cargarVendedorActualId();
    cargarCategoriasVendedor();
    
    // Mostrar modal
    new bootstrap.Modal(document.getElementById('nuevoPedidoModal')).show();
}

function limpiarFormularioCompletoVendedor() {
    // Reiniciar variables
    clienteSeleccionadoVendedor = null;
    tipoPedidoSeleccionadoVendedor = null;
    productosSeleccionadosVendedor = [];
    componentesSeleccionadosVendedor.clear();
    
    // Limpiar campos
    document.getElementById('clienteBuscarVendedor').value = '';
    if (document.getElementById('productoCodigoVendedor')) {
        document.getElementById('productoCodigoVendedor').value = '';
    }
    if (document.getElementById('productoNombreVendedor')) {
        document.getElementById('productoNombreVendedor').value = '';
    }
    
    // Ocultar secciones
    document.getElementById('clienteResultadosVendedor').style.display = 'none';
    document.getElementById('clienteSeleccionadoInfoVendedor').style.display = 'none';
    document.getElementById('seccionTipoPedidoVendedor').style.display = 'none';
    document.getElementById('seccionProductosCompletosVendedor').style.display = 'none';
    document.getElementById('seccionArmarPCVendedor').style.display = 'none';
    document.getElementById('seccionResumenVendedor').style.display = 'none';
    document.getElementById('productosResultadosVendedor').style.display = 'none';
    document.getElementById('tipoSeleccionadoInfoVendedor').style.display = 'none';
    
    // Limpiar tarjetas seleccionadas
    document.querySelectorAll('.tipo-pedido-card-vendedor').forEach(card => {
        card.classList.remove('selected');
    });
    
    // Limpiar tablas
    actualizarTablaProductosSeleccionadosVendedor();
    actualizarTablaComponentesSeleccionadosVendedor();
    actualizarResumenVendedor();
    
    // Limpiar alertas
    document.getElementById('nuevoPedidoModalAlertContainer').innerHTML = '';
}

// ✅ FUNCIÓN CORREGIDA PARA CARGAR VENDEDOR (SIN USAR VENDOR001)
function cargarVendedorActualId() {
    // Obtener vendedor logueado desde el endpoint real
    fetch('/auth/usuario-actual')
    .then(response => response.json())
    .then(data => {
        if (data.success && data.data) {
            vendedorActualId = data.data.id;
            console.log('✅ Vendedor logueado cargado:', vendedorActualId, data.data.nombreCompleto);
        } else {
            console.error('❌ Error al cargar vendedor logueado:', data.message);
            mostrarAlertaVendedor('Error al cargar información del vendedor logueado', 'warning');
        }
    })
    .catch(error => {
        console.error('❌ Error al obtener vendedor logueado:', error);
        mostrarAlertaVendedor('Error de conexión al cargar vendedor', 'warning');
    });
}

function cargarCategoriasVendedor() {
    fetch('/categoria/api/todas')
    .then(response => response.json())
    .then(categorias => {
        categoriasDisponiblesVendedor = categorias;
        llenarSelectCategoriasVendedor(categorias);
        crearAccordeonCategoriasVendedor(categorias);
        console.log('Categorías cargadas:', categorias.length);
    })
    .catch(error => {
        console.error('Error al cargar categorías:', error);
        categoriasDisponiblesVendedor = [];
        mostrarAlertaVendedor('Error al cargar categorías', 'warning');
    });
}

function llenarSelectCategoriasVendedor(categorias) {
    const select = document.getElementById('categoriaFiltroVendedor');
    if (select) {
        select.innerHTML = '<option value="">Todas las categorías</option>';
        categorias.forEach(categoria => {
            select.innerHTML += `<option value="${categoria.id}">${categoria.nombre}</option>`;
        });
    }
}


// ===========================
// PASO 1: GESTIÓN DE CLIENTES
// ===========================

function buscarClientesVendedor() {
    const textoBusqueda = document.getElementById('clienteBuscarVendedor').value.trim();
    
    if (!textoBusqueda) {
        mostrarAlertaVendedor('Ingrese texto para buscar (nombre, apellido o DNI)', 'warning');
        return;
    }
    
    const resultados = document.getElementById('clienteResultadosVendedor');
    resultados.innerHTML = '<div class="text-center"><i class="fas fa-spinner fa-spin"></i> Buscando clientes...</div>';
    resultados.style.display = 'block';
    
    // Usar el endpoint de clientes
    fetch(`/cliente/api/buscar?texto=${encodeURIComponent(textoBusqueda)}`)
    .then(response => response.json())
    .then(data => {
        if (data.success && data.data.length > 0) {
            mostrarResultadosClientesVendedor(data.data);
        } else {
            mostrarMensajeNoEncontradoClientesVendedor('No se encontraron clientes con ese criterio');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarAlertaVendedor('Error al buscar clientes', 'danger');
        resultados.style.display = 'none';
    });
}

function mostrarResultadosClientesVendedor(clientes) {
    const resultados = document.getElementById('clienteResultadosVendedor');
    
    let html = '<h6 class="mb-3">Seleccione un cliente:</h6>';
    html += '<div class="row">';
    
    clientes.forEach(cliente => {
        html += `
            <div class="col-md-6 mb-2">
                <div class="card cliente-item-vendedor" onclick="seleccionarClienteVendedor('${cliente.dni}')">
                    <div class="card-body p-3">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <h6 class="card-title mb-1">${cliente.nombreCompleto}</h6>
                                <small class="text-muted">${cliente.tipoDocumento}: ${cliente.dni}</small>
                            </div>
                            <span class="badge bg-${cliente.activo ? 'success' : 'secondary'}">
                                ${cliente.activo ? 'Activo' : 'Inactivo'}
                            </span>
                        </div>
                        ${cliente.correo ? `<div class="mt-2"><small><i class="fas fa-envelope me-1"></i>${cliente.correo}</small></div>` : ''}
                        ${cliente.celular ? `<div><small><i class="fas fa-phone me-1"></i>${cliente.celular}</small></div>` : ''}
                    </div>
                </div>
            </div>
        `;
    });
    
    html += '</div>';
    html += '<div class="text-center mt-2"><small class="text-muted">Haz clic en un cliente para seleccionarlo</small></div>';
    
    resultados.innerHTML = html;
}

function mostrarMensajeNoEncontradoClientesVendedor(mensaje) {
    const resultados = document.getElementById('clienteResultadosVendedor');
    resultados.innerHTML = `
        <div class="text-center py-4">
            <i class="fas fa-user-slash fa-3x text-muted mb-3"></i>
            <h6 class="text-muted">${mensaje}</h6>
            <button class="btn btn-outline-primary btn-sm mt-2" onclick="abrirModalRegistrarClienteVendedor()">
                <i class="fas fa-plus me-1"></i>Registrar nuevo cliente
            </button>
        </div>
    `;
}

function seleccionarClienteVendedor(dni) {
    fetch(`/cliente/api/buscar/${dni}`)
    .then(response => response.json())
    .then(cliente => {
        clienteSeleccionadoVendedor = cliente;
        mostrarClienteSeleccionadoVendedor(cliente);
        
        // Ocultar resultados de búsqueda
        document.getElementById('clienteResultadosVendedor').style.display = 'none';
        document.getElementById('clienteBuscarVendedor').value = '';
        
        // Mostrar paso 2 (tipo de pedido)
        document.getElementById('seccionTipoPedidoVendedor').style.display = 'block';
        
        mostrarAlertaVendedor(`Cliente seleccionado: ${cliente.nombreCompleto}`, 'success');
        actualizarResumenVendedor();
    })
    .catch(error => {
        console.error('Error al obtener datos del cliente:', error);
        mostrarAlertaVendedor('Error al cargar datos del cliente', 'danger');
    });
}

function mostrarClienteSeleccionadoVendedor(cliente) {
    const infoDiv = document.getElementById('clienteSeleccionadoInfoVendedor');
    infoDiv.innerHTML = `
        <div class="d-flex justify-content-between align-items-start">
            <div>
                <h6 class="mb-1">
                    <i class="fas fa-user-check me-2"></i>Cliente Seleccionado
                </h6>
                <strong>${cliente.nombreCompleto}</strong><br>
                <small class="text-muted">${cliente.tipoDocumento}: ${cliente.dni}</small>
                ${cliente.correo ? `<br><small><i class="fas fa-envelope me-1"></i>${cliente.correo}</small>` : ''}
                ${cliente.celular ? `<br><small><i class="fas fa-phone me-1"></i>${cliente.celular}</small>` : ''}
            </div>
            <button class="btn btn-outline-secondary btn-sm" onclick="cambiarClienteVendedor()">
                <i class="fas fa-edit me-1"></i>Cambiar
            </button>
        </div>
    `;
    infoDiv.style.display = 'block';
}

function cambiarClienteVendedor() {
    clienteSeleccionadoVendedor = null;
    document.getElementById('clienteSeleccionadoInfoVendedor').style.display = 'none';
    document.getElementById('seccionTipoPedidoVendedor').style.display = 'none';
    document.getElementById('seccionProductosCompletosVendedor').style.display = 'none';
    document.getElementById('seccionArmarPCVendedor').style.display = 'none';
    document.getElementById('seccionResumenVendedor').style.display = 'none';
    
    // Reiniciar tipo de pedido
    tipoPedidoSeleccionadoVendedor = null;
    document.querySelectorAll('.tipo-pedido-card-vendedor').forEach(card => {
        card.classList.remove('selected');
    });
    
    actualizarResumenVendedor();
}

// ===========================
// REGISTRO DE NUEVOS CLIENTES
// ===========================

function abrirModalRegistrarClienteVendedor() {
    // Prellenar DNI si hay texto de búsqueda
    const textoBusqueda = document.getElementById('clienteBuscarVendedor').value.trim();
    if (textoBusqueda && /^\d{8,11}$/.test(textoBusqueda)) {
        document.getElementById('registroDniVendedor').value = textoBusqueda;
    }
    
    new bootstrap.Modal(document.getElementById('registrarClienteModalVendedor')).show();
}

function registrarNuevoClienteVendedor() {
    const dni = document.getElementById('registroDniVendedor').value.trim();
    const nombre = document.getElementById('registroNombreVendedor').value.trim();
    const apellido = document.getElementById('registroApellidoVendedor').value.trim();
    const correo = document.getElementById('registroCorreoVendedor').value.trim();
    const telefono = document.getElementById('registroTelefonoVendedor').value.trim();
    const direccion = document.getElementById('registroDireccionVendedor').value.trim();
    
    // Validaciones
    if (!dni || (!dni.match(/^\d{8}$/) && !dni.match(/^\d{11}$/))) {
        mostrarAlertaModalClienteVendedor('El DNI debe tener 8 dígitos o RUC debe tener 11 dígitos', 'warning');
        return;
    }
    
    if (!nombre) {
        mostrarAlertaModalClienteVendedor('El nombre es obligatorio', 'warning');
        return;
    }
    
    if (correo && !correo.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
        mostrarAlertaModalClienteVendedor('Ingrese un correo electrónico válido', 'warning');
        return;
    }
    
    const btnGuardar = document.getElementById('btnGuardarClienteVendedor');
    btnGuardar.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Registrando...';
    btnGuardar.disabled = true;
    
    const datosCliente = {
        dni: dni,
        nombre: nombre,
        apellido: apellido,
        correo: correo,
        celular: telefono,
        direccion: direccion
    };
    
    fetch('/cliente/api/registrar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(datosCliente)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            mostrarAlertaModalClienteVendedor('Cliente registrado exitosamente', 'success');
            
            setTimeout(() => {
                const modal = bootstrap.Modal.getInstance(document.getElementById('registrarClienteModalVendedor'));
                modal.hide();
                
                // Seleccionar automáticamente el cliente recién registrado
                if (data.cliente) {
                    clienteSeleccionadoVendedor = data.cliente;
                    mostrarClienteSeleccionadoVendedor(data.cliente);
                    document.getElementById('seccionTipoPedidoVendedor').style.display = 'block';
                    actualizarResumenVendedor();
                }
            }, 1500);
        } else {
            mostrarAlertaModalClienteVendedor(data.message || 'Error al registrar el cliente', 'danger');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarAlertaModalClienteVendedor('Error de conexión al registrar el cliente', 'danger');
    })
    .finally(() => {
        btnGuardar.innerHTML = '<i class="fas fa-save me-2"></i>Registrar Cliente';
        btnGuardar.disabled = false;
    });
}


// ===========================
// PASO 2: SELECCIÓN DE TIPO DE PEDIDO
// ===========================

function seleccionarTipoPedidoVendedor(tipo) {
    tipoPedidoSeleccionadoVendedor = tipo;
    
    // Actualizar UI de selección
    document.querySelectorAll('.tipo-pedido-card-vendedor').forEach(card => {
        card.classList.remove('selected');
    });
    event.currentTarget.classList.add('selected');
    
    // Mostrar información del tipo seleccionado
    const infoDiv = document.getElementById('tipoSeleccionadoInfoVendedor');
    const descripcion = tipo === 'PRODUCTO_COMPLETO' ? 
        'Seleccionarás productos individuales del inventario' : 
        'Seleccionarás componentes por categoría para armar una PC completa';
    
    infoDiv.innerHTML = `
        <strong>Tipo seleccionado:</strong> ${tipo === 'PRODUCTO_COMPLETO' ? 'Producto Completo' : 'Armar PC'}<br>
        <small class="text-muted">${descripcion}</small>
    `;
    infoDiv.style.display = 'block';
    
    // Mostrar sección correspondiente
    if (tipo === 'PRODUCTO_COMPLETO') {
        document.getElementById('seccionProductosCompletosVendedor').style.display = 'block';
        document.getElementById('seccionArmarPCVendedor').style.display = 'none';
    } else {
        document.getElementById('seccionArmarPCVendedor').style.display = 'block';
        document.getElementById('seccionProductosCompletosVendedor').style.display = 'none';
    }
    
    // Mostrar sección de resumen
    document.getElementById('seccionResumenVendedor').style.display = 'block';
    
    // Limpiar selecciones anteriores
    productosSeleccionadosVendedor = [];
    componentesSeleccionadosVendedor.clear();
    actualizarTablaProductosSeleccionadosVendedor();
    actualizarTablaComponentesSeleccionadosVendedor();
    actualizarResumenVendedor();
    
    mostrarAlertaVendedor(`Tipo de pedido seleccionado: ${tipo === 'PRODUCTO_COMPLETO' ? 'Producto Completo' : 'Armar PC'}`, 'success');
}

// ===========================
// FUNCIONES DE ACTUALIZACIÓN DE TABLAS (PLACEHOLDERS)
// ===========================

function actualizarTablaProductosSeleccionadosVendedor() {
    const tbody = document.getElementById('productosSeleccionadosTableVendedor');
    
    if (productosSeleccionadosVendedor.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No hay productos seleccionados</td></tr>';
        return;
    }
    
    tbody.innerHTML = productosSeleccionadosVendedor.map((producto, index) => `
        <tr>
            <td>${producto.codigo}</td>
            <td>${producto.nombre}</td>
            <td>S/ ${producto.precio.toFixed(2)}</td>
            <td>
                <input type="number" class="form-control form-control-sm" 
                       value="${producto.cantidad}" min="1" max="${producto.stock}"
                       onchange="cambiarCantidadProductoVendedor(${index}, this.value)">
            </td>
            <td>S/ ${producto.subtotal.toFixed(2)}</td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="eliminarProductoVendedor(${index})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function actualizarTablaComponentesSeleccionadosVendedor() {
    const tbody = document.getElementById('componentesSeleccionadosTableVendedor');
    
    if (componentesSeleccionadosVendedor.size === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No hay componentes seleccionados</td></tr>';
        return;
    }
    
    let html = '';
    componentesSeleccionadosVendedor.forEach((componente, categoriaId) => {
        html += `
            <tr>
                <td>${componente.categoria.nombre}</td>
                <td>${componente.nombre}</td>
                <td>${componente.modelo}</td>
                <td>S/ ${componente.precio.toFixed(2)}</td>
                <td>
                    <button class="btn btn-danger btn-sm" onclick="deseleccionarComponenteVendedor(${categoriaId})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `;
    });
    
    tbody.innerHTML = html;
}

function actualizarResumenVendedor() {
    // ✅ CÓDIGO EXISTENTE SIN CAMBIOS - Actualizar información del cliente
    document.getElementById('resumenClienteVendedor').textContent = 
        clienteSeleccionadoVendedor ? 
        clienteSeleccionadoVendedor.nombreCompleto : '-';
    
    // ✅ CÓDIGO EXISTENTE SIN CAMBIOS - Actualizar tipo de pedido
    let tipoTexto = '-';
    if (tipoPedidoSeleccionadoVendedor === 'PRODUCTO_COMPLETO') {
        tipoTexto = 'Producto Completo';
    } else if (tipoPedidoSeleccionadoVendedor === 'ARMAR_PC') {
        tipoTexto = 'Armar PC';
    }
    document.getElementById('resumenTipoVendedor').textContent = tipoTexto;
    
    // ✅ CÓDIGO EXISTENTE SIN CAMBIOS - Calcular totales
    let totalItems = 0;
    let montoTotal = 0;
    
    if (tipoPedidoSeleccionadoVendedor === 'PRODUCTO_COMPLETO') {
        totalItems = productosSeleccionadosVendedor.reduce((total, producto) => total + producto.cantidad, 0);
        montoTotal = productosSeleccionadosVendedor.reduce((total, producto) => total + producto.subtotal, 0);
    } else if (tipoPedidoSeleccionadoVendedor === 'ARMAR_PC') {
        totalItems = componentesSeleccionadosVendedor.size;
        componentesSeleccionadosVendedor.forEach(componente => {
            montoTotal += componente.precio;
        });
    }
    
    document.getElementById('resumenCantidadVendedor').textContent = totalItems;
    document.getElementById('resumenTotalVendedor').textContent = montoTotal.toFixed(2);
    
    // ✅ NUEVA LÓGICA DE CONTROL DE BOTONES
    const btnRegistrarPago = document.getElementById('btnRegistrarPagoVendedor');
    const btnGenerarPedido = document.getElementById('btnGenerarPedidoVendedor');
    
    // Verificar si el formulario está completo
    const formularioCompleto = clienteSeleccionadoVendedor && tipoPedidoSeleccionadoVendedor && 
        ((tipoPedidoSeleccionadoVendedor === 'PRODUCTO_COMPLETO' && productosSeleccionadosVendedor.length > 0) ||
         (tipoPedidoSeleccionadoVendedor === 'ARMAR_PC' && componentesSeleccionadosVendedor.size > 0));
    
    // ✅ CONTROL DE ESTADO DE BOTONES SEGÚN EL FLUJO
    if (!formularioCompleto) {
        // Formulario incompleto - ambos botones deshabilitados
        btnRegistrarPago.disabled = true;
        btnGenerarPedido.disabled = true;
        
        // Mensaje descriptivo en el botón de generar
        if (!clienteSeleccionadoVendedor) {
            btnGenerarPedido.innerHTML = '<i class="fas fa-user me-2"></i>Seleccione un cliente';
        } else if (!tipoPedidoSeleccionadoVendedor) {
            btnGenerarPedido.innerHTML = '<i class="fas fa-list me-2"></i>Seleccione tipo de pedido';
        } else if (tipoPedidoSeleccionadoVendedor === 'PRODUCTO_COMPLETO' && productosSeleccionadosVendedor.length === 0) {
            btnGenerarPedido.innerHTML = '<i class="fas fa-plus me-2"></i>Agregue productos';
        } else if (tipoPedidoSeleccionadoVendedor === 'ARMAR_PC' && componentesSeleccionadosVendedor.size === 0) {
            btnGenerarPedido.innerHTML = '<i class="fas fa-microchip me-2"></i>Seleccione componentes';
        }
        
    } else if (formularioCompleto && !pagoRegistradoVendedor) {
        // Formulario completo pero sin pago - habilitar solo "Registrar Pago"
        btnRegistrarPago.disabled = false;
        btnGenerarPedido.disabled = true;
        btnGenerarPedido.innerHTML = '<i class="fas fa-credit-card me-2"></i>Registre el pago primero';
        
    } else if (pagoRegistradoVendedor) {
        // Pago registrado - habilitar solo "Crear Pedido"
        btnGenerarPedido.disabled = false;
        btnGenerarPedido.innerHTML = '<i class="fas fa-plus-circle me-2"></i>Crear Orden de Pedido';
    }
}

// ===========================
// PASO 3A: PRODUCTOS COMPLETOS
// ===========================

function buscarProductoPorCodigoVendedor() {
    const codigo = document.getElementById('productoCodigoVendedor').value.trim();
    
    if (!codigo) {
        mostrarAlertaVendedor('Ingrese un código para buscar', 'warning');
        return;
    }
    
    fetch(`/producto/api/buscar/${codigo}`)
    .then(response => response.json())
    .then(producto => {
        if (producto && producto.codigo) {
            mostrarResultadosProductosVendedor([producto]);
        } else {
            mostrarAlertaVendedor('No se encontró producto con código: ' + codigo, 'warning');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarAlertaVendedor('Error al buscar producto', 'danger');
    });
}

function buscarProductosPorNombreVendedor() {
    const nombre = document.getElementById('productoNombreVendedor').value.trim();
    
    if (!nombre) {
        mostrarAlertaVendedor('Ingrese un nombre para buscar', 'warning');
        return;
    }
    
    fetch(`/producto/api/buscarPorNombre?nombre=${encodeURIComponent(nombre)}`)
    .then(response => response.json())
    .then(productos => {
        if (productos && productos.length > 0) {
            mostrarResultadosProductosVendedor(productos);
        } else {
            mostrarAlertaVendedor('No se encontraron productos con nombre: ' + nombre, 'warning');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarAlertaVendedor('Error al buscar productos', 'danger');
    });
}

function buscarProductosPorCategoriaVendedor() {
    const categoriaId = document.getElementById('categoriaFiltroVendedor').value;
    
    if (!categoriaId) {
        mostrarAlertaVendedor('Seleccione una categoría', 'warning');
        return;
    }
    
    fetch(`/producto/api/categoria/${categoriaId}`)
    .then(response => response.json())
    .then(productos => {
        if (productos && productos.length > 0) {
            mostrarResultadosProductosVendedor(productos);
        } else {
            mostrarAlertaVendedor('No se encontraron productos en esta categoría', 'warning');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarAlertaVendedor('Error al buscar productos por categoría', 'danger');
    });
}

function mostrarResultadosProductosVendedor(productos) {
    const resultados = document.getElementById('productosResultadosVendedor');
    
    let html = '';
    productos.forEach(producto => {
        const yaAgregado = productosSeleccionadosVendedor.some(p => p.codigo === producto.codigo);
        const sinStock = producto.cantidad <= 0;
        
        html += `
            <div class="col-md-6 col-lg-4 mb-3">
                <div class="card producto-card-vendedor h-100">
                    <div class="card-body">
                        <h6 class="card-title">${producto.nombre}</h6>
                        <p class="card-text">
                            <strong>Código:</strong> ${producto.codigo}<br>
                            <strong>Precio:</strong> S/ ${producto.precio.toFixed(2)}<br>
                            <strong>Stock:</strong> ${producto.cantidad} unidades<br>
                            <strong>Categoría:</strong> ${producto.categoria ? producto.categoria.nombre : 'Sin categoría'}<br>
                            <strong>Marca:</strong> ${producto.marca ? producto.marca.nombre : 'Sin marca'}
                        </p>
                    </div>
                    <div class="card-footer">
                        ${sinStock ? 
                            '<button class="btn btn-secondary btn-sm w-100" disabled><i class="fas fa-times me-1"></i>Sin stock</button>' :
                            yaAgregado ? 
                                '<button class="btn btn-success btn-sm w-100" disabled><i class="fas fa-check me-1"></i>Agregado</button>' :
                                `<button class="btn btn-primary btn-sm w-100" onclick="agregarProductoCompletoVendedor(${producto.codigo})"><i class="fas fa-plus me-1"></i>Agregar</button>`
                        }
                    </div>
                </div>
            </div>
        `;
    });
    
    resultados.innerHTML = html;
    resultados.style.display = 'flex';
}

function agregarProductoCompletoVendedor(codigoProducto) {
    fetch(`/producto/api/buscar/${codigoProducto}`)
    .then(response => response.json())
    .then(producto => {
        if (producto && producto.cantidad > 0) {
            const yaAgregado = productosSeleccionadosVendedor.some(p => p.codigo === producto.codigo);
            
            if (!yaAgregado) {
                const productoParaPedido = {
                    codigo: producto.codigo,
                    nombre: producto.nombre,
                    precio: producto.precio,
                    cantidad: 1,
                    stock: producto.cantidad,
                    subtotal: producto.precio * 1,
                    categoria: producto.categoria ? producto.categoria.nombre : 'Sin categoría',
                    marca: producto.marca ? producto.marca.nombre : 'Sin marca'
                };
                
                productosSeleccionadosVendedor.push(productoParaPedido);
                actualizarTablaProductosSeleccionadosVendedor();
                actualizarResumenVendedor();
                
                // Actualizar botón en resultados
                const btnAgregar = document.querySelector(`[onclick="agregarProductoCompletoVendedor(${codigoProducto})"]`);
                if (btnAgregar) {
                    btnAgregar.className = 'btn btn-success btn-sm w-100';
                    btnAgregar.innerHTML = '<i class="fas fa-check me-1"></i>Agregado';
                    btnAgregar.disabled = true;
                }
                
                mostrarAlertaVendedor('Producto agregado al pedido', 'success');
            }
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarAlertaVendedor('Error al agregar producto', 'danger');
    });
}

function cambiarCantidadProductoVendedor(index, nuevaCantidad) {
    const cantidad = parseInt(nuevaCantidad);
    const producto = productosSeleccionadosVendedor[index];
    
    if (cantidad >= 1 && cantidad <= producto.stock) {
        producto.cantidad = cantidad;
        producto.subtotal = producto.precio * cantidad;
        actualizarTablaProductosSeleccionadosVendedor();
        actualizarResumenVendedor();
    } else {
        // Si la cantidad no es válida, restaurar la tabla
        actualizarTablaProductosSeleccionadosVendedor();
        mostrarAlertaVendedor(`La cantidad debe estar entre 1 y ${producto.stock}`, 'warning');
    }
}

function eliminarProductoVendedor(index) {
    const producto = productosSeleccionadosVendedor[index];
    productosSeleccionadosVendedor.splice(index, 1);
    
    actualizarTablaProductosSeleccionadosVendedor();
    actualizarResumenVendedor();
    
    // Rehabilitar botón en resultados si están visibles
    const btnAgregar = document.querySelector(`[onclick="agregarProductoCompletoVendedor(${producto.codigo})"]`);
    if (btnAgregar) {
        btnAgregar.className = 'btn btn-primary btn-sm w-100';
        btnAgregar.innerHTML = '<i class="fas fa-plus me-1"></i>Agregar';
        btnAgregar.disabled = false;
    }
    
    mostrarAlertaVendedor('Producto eliminado del pedido', 'info');
}


// ===========================
// PASO 3B: ARMAR PC (COMPONENTES POR CATEGORÍA)
// ===========================

function crearAccordeonCategoriasVendedor(categorias) {
    const accordion = document.getElementById('categoriasAccordionVendedor');
    
    let html = '';
    categorias.forEach((categoria, index) => {
        const collapseId = `collapseVendedor${categoria.id}`;
        const headingId = `headingVendedor${categoria.id}`;
        
        html += `
            <div class="accordion-item">
                <h2 class="accordion-header" id="${headingId}">
                    <button class="accordion-button collapsed" type="button" 
                            data-bs-toggle="collapse" data-bs-target="#${collapseId}"
                            onclick="cargarProductosCategoriaVendedor(${categoria.id}, '${collapseId}')">
                        <i class="fas fa-microchip me-2"></i>
                        ${categoria.nombre}
                        <span id="badgeVendedor${categoria.id}" class="badge bg-secondary ms-2" style="display: none;">
                            Seleccionado
                        </span>
                    </button>
                </h2>
                <div id="${collapseId}" class="accordion-collapse collapse" 
                     data-bs-parent="#categoriasAccordionVendedor">
                    <div class="accordion-body">
                        <div id="productosVendedor${categoria.id}" class="row">
                            <div class="col-12 text-center">
                                <i class="fas fa-spinner fa-spin"></i> Cargando productos...
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });
    
    accordion.innerHTML = html;
}

function cargarProductosCategoriaVendedor(categoriaId, collapseId) {
    const productosDiv = document.getElementById(`productosVendedor${categoriaId}`);
    
    // Solo cargar si no se ha cargado antes
    if (productosDiv.innerHTML.includes('Cargando productos...')) {
        fetch(`/producto/api/categoria/${categoriaId}`)
        .then(response => response.json())
        .then(productos => {
            mostrarProductosCategoriaVendedor(categoriaId, productos);
        })
        .catch(error => {
            console.error('Error:', error);
            productosDiv.innerHTML = '<div class="col-12 text-center text-danger">Error al cargar productos</div>';
        });
    }
}

function mostrarProductosCategoriaVendedor(categoriaId, productos) {
    const productosDiv = document.getElementById(`productosVendedor${categoriaId}`);
    
    if (productos.length === 0) {
        productosDiv.innerHTML = '<div class="col-12 text-center text-muted">No hay productos en esta categoría</div>';
        return;
    }
    
    let html = '';
    productos.forEach(producto => {
        const isSelected = componentesSeleccionadosVendedor.has(categoriaId) && 
                          componentesSeleccionadosVendedor.get(categoriaId).codigo === producto.codigo;
        const sinStock = producto.cantidad <= 0;
        
        html += `
            <div class="col-md-6 col-lg-4 mb-3">
                <div class="card producto-card-vendedor h-100 ${isSelected ? 'selected' : ''}">
                    <div class="card-body">
                        <h6 class="card-title">${producto.nombre}</h6>
                        <p class="card-text">
                            <strong>Código:</strong> ${producto.codigo}<br>
                            <strong>Precio:</strong> S/ ${producto.precio.toFixed(2)}<br>
                            <strong>Stock:</strong> ${producto.cantidad}<br>
                            ${producto.marca ? `<strong>Marca:</strong> ${producto.marca.nombre}<br>` : ''}
                            ${producto.modelo ? `<strong>Modelo:</strong> ${producto.modelo}` : ''}
                        </p>
                    </div>
                    <div class="card-footer">
                        ${sinStock ? 
                            '<button class="btn btn-secondary btn-sm w-100" disabled>Sin stock</button>' :
                            isSelected ?
                                `<button class="btn btn-success btn-sm w-100" onclick="deseleccionarComponenteVendedor(${categoriaId})"><i class="fas fa-check me-1"></i>Seleccionado</button>` :
                                `<button class="btn btn-outline-primary btn-sm w-100" onclick="seleccionarComponenteVendedor(${categoriaId}, ${producto.codigo})"><i class="fas fa-plus me-1"></i>Seleccionar</button>`
                        }
                    </div>
                </div>
            </div>
        `;
    });
    
    productosDiv.innerHTML = html;
}

function seleccionarComponenteVendedor(categoriaId, codigoProducto) {
    fetch(`/producto/api/buscar/${codigoProducto}`)
    .then(response => response.json())
    .then(producto => {
        if (producto && producto.cantidad > 0) {
            // Guardar componente seleccionado
            componentesSeleccionadosVendedor.set(categoriaId, {
                categoria: categoriasDisponiblesVendedor.find(c => c.id === categoriaId),
                codigo: producto.codigo,
                nombre: producto.nombre,
                precio: producto.precio,
                modelo: producto.modelo || 'N/A',
                marca: producto.marca ? producto.marca.nombre : 'N/A'
            });
            
            // Actualizar UI
            actualizarTablaComponentesSeleccionadosVendedor();
            actualizarResumenVendedor();
            mostrarBadgeCategoriaVendedor(categoriaId);
            
            // Refrescar productos de esta categoría para mostrar selección
            cargarProductosCategoriaVendedor(categoriaId, `collapseVendedor${categoriaId}`);
            
            const nombreCategoria = categoriasDisponiblesVendedor.find(c => c.id === categoriaId)?.nombre || 'Categoría';
            mostrarAlertaVendedor(`Componente seleccionado para ${nombreCategoria}`, 'success');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarAlertaVendedor('Error al seleccionar componente', 'danger');
    });
}

function deseleccionarComponenteVendedor(categoriaId) {
    const componente = componentesSeleccionadosVendedor.get(categoriaId);
    componentesSeleccionadosVendedor.delete(categoriaId);
    
    actualizarTablaComponentesSeleccionadosVendedor();
    actualizarResumenVendedor();
    ocultarBadgeCategoriaVendedor(categoriaId);
    
    // Refrescar productos de esta categoría
    cargarProductosCategoriaVendedor(categoriaId, `collapseVendedor${categoriaId}`);
    
    if (componente && componente.categoria) {
        mostrarAlertaVendedor(`Componente removido de ${componente.categoria.nombre}`, 'info');
    }
}

function mostrarBadgeCategoriaVendedor(categoriaId) {
    const badge = document.getElementById(`badgeVendedor${categoriaId}`);
    if (badge) {
        badge.style.display = 'inline';
        badge.className = 'badge bg-success ms-2';
        badge.textContent = 'Seleccionado';
    }
}

function ocultarBadgeCategoriaVendedor(categoriaId) {
    const badge = document.getElementById(`badgeVendedor${categoriaId}`);
    if (badge) {
        badge.style.display = 'none';
    }
}

// ===========================
// VALIDACIONES PARA ARMAR PC
// ===========================

function validarConfiguracionPC() {
    const categoriasRequeridas = ['Procesador', 'Memoria RAM', 'Placa Madre', 'Disco Duro'];
    const categoriasSeleccionadas = Array.from(componentesSeleccionadosVendedor.values())
        .map(comp => comp.categoria.nombre);
    
    const faltantes = categoriasRequeridas.filter(cat => !categoriasSeleccionadas.includes(cat));
    
    if (faltantes.length > 0) {
        return {
            valido: false,
            mensaje: `Faltan componentes esenciales: ${faltantes.join(', ')}`
        };
    }
    
    return {
        valido: true,
        mensaje: 'Configuración de PC completa'
    };
}

function mostrarEstadoConfiguracionPC() {
    const validacion = validarConfiguracionPC();
    const estadoDiv = document.getElementById('estadoConfiguracionPC');
    
    if (estadoDiv) {
        if (validacion.valido) {
            estadoDiv.innerHTML = `
                <div class="alert alert-success">
                    <i class="fas fa-check-circle me-2"></i>${validacion.mensaje}
                </div>
            `;
        } else {
            estadoDiv.innerHTML = `
                <div class="alert alert-warning">
                    <i class="fas fa-exclamation-triangle me-2"></i>${validacion.mensaje}
                </div>
            `;
        }
    }
}


// ===========================
// GENERACIÓN DEL PEDIDO
// ===========================

function generarPedidoVendedor() {
    const btnGenerar = document.getElementById('btnGenerarPedidoVendedor');
    
    // ✅ NUEVA VALIDACIÓN: Verificar que el pago esté registrado
    if (!pagoRegistradoVendedor) {
        mostrarAlertaVendedor('Debe registrar el pago antes de crear la orden de pedido', 'warning');
        return;
    }
    
    // ✅ VALIDACIONES FINALES (EXISTENTES - SIN CAMBIOS)
    if (!clienteSeleccionadoVendedor || !tipoPedidoSeleccionadoVendedor) {
        mostrarAlertaVendedor('Complete todos los pasos requeridos', 'warning');
        return;
    }
    
    if (tipoPedidoSeleccionadoVendedor === 'PRODUCTO_COMPLETO' && productosSeleccionadosVendedor.length === 0) {
        mostrarAlertaVendedor('Debe agregar al menos un producto', 'warning');
        return;
    }
    
    if (tipoPedidoSeleccionadoVendedor === 'ARMAR_PC' && componentesSeleccionadosVendedor.size === 0) {
        mostrarAlertaVendedor('Debe seleccionar al menos un componente', 'warning');
        return;
    }
    
    // ✅ ESTADO DE CARGA
    btnGenerar.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Creando pedido...';
    btnGenerar.disabled = true;
    
    // ✅ OBTENER VENDEDOR LOGUEADO DINÁMICAMENTE
    fetch('/auth/usuario-actual')
    .then(response => response.json())
    .then(userData => {
        let vendedorId = null;
        
        if (userData.success && userData.data && userData.data.id) {
            vendedorId = userData.data.id;
            console.log('✅ Vendedor logueado obtenido:', vendedorId, userData.data.nombreCompleto);
        } else {
            throw new Error('No se pudo obtener el vendedor logueado: ' + userData.message);
        }
        
        // ✅ PREPARAR DATOS DEL PEDIDO
        let datosPedido = {
            clienteDni: clienteSeleccionadoVendedor.dni,
            vendedorId: vendedorId,
            tipoPedido: tipoPedidoSeleccionadoVendedor,
            productos: [],
            total: 0,
            detallesComponentes: ''
        };
        
        // ✅ PROCESAR PRODUCTOS SEGÚN TIPO
        if (tipoPedidoSeleccionadoVendedor === 'PRODUCTO_COMPLETO') {
            datosPedido.productos = productosSeleccionadosVendedor.map(producto => ({
                codigo: producto.codigo || producto.id,
                cantidad: producto.cantidad || 1,
                precio: producto.precio
            }));
            datosPedido.total = productosSeleccionadosVendedor.reduce((total, producto) => 
                total + (producto.subtotal || producto.precio * (producto.cantidad || 1)), 0);
            datosPedido.detallesComponentes = productosSeleccionadosVendedor
                .map(p => `${p.nombre} (${p.cantidad || 1}x)`)
                .join(', ');
        } else if (tipoPedidoSeleccionadoVendedor === 'ARMAR_PC') {
            const componentesArray = Array.from(componentesSeleccionadosVendedor.values());
            datosPedido.productos = componentesArray.map(componente => ({
                codigo: componente.codigo || componente.id,
                cantidad: 1,
                precio: componente.precio
            }));
            datosPedido.total = componentesArray.reduce((total, componente) => 
                total + componente.precio, 0);
            datosPedido.detallesComponentes = componentesArray
                .map(c => `${c.categoria?.nombre || 'Categoría'}: ${c.nombre} (${c.modelo || 'Sin modelo'})`)
                .join(', ');
        }
        
        console.log('✅ Datos del pedido con vendedor dinámico:', datosPedido);
        
        // ✅ ENVIAR AL SERVIDOR
        return fetch('/orden-pedido/crear', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(datosPedido)
        });
    })
    .then(response => {
        console.log('📡 Respuesta del servidor:', response.status);
        
        if (response.ok) {
            // Éxito - intentar leer respuesta JSON
            return response.json().catch(() => {
                // Si no es JSON, intentar como texto
                return response.text();
            });
        } else {
            // Error - leer mensaje de error
            return response.text().then(errorText => {
                throw new Error(errorText || 'Error al crear el pedido');
            });
        }
    })
    .then(data => {
        // ✅ ÉXITO - MOSTRAR MODAL DE CONFIRMACIÓN
        console.log('✅ Pedido creado exitosamente:', data);
        
        // ✅ NUEVO: Resetear estado del pago para próximo pedido
        pagoRegistradoVendedor = false;
        
        // Cerrar el modal de creación
        const modalCreacion = bootstrap.Modal.getInstance(document.getElementById('nuevoPedidoModal'));
        if (modalCreacion) {
            modalCreacion.hide();
        }
        
        // ✅ MOSTRAR MODAL DE CONFIRMACIÓN CON DATOS
        mostrarModalConfirmacionVendedor(data, {
            cliente: clienteSeleccionadoVendedor,
            tipoPedido: tipoPedidoSeleccionadoVendedor,
            productos: tipoPedidoSeleccionadoVendedor === 'PRODUCTO_COMPLETO' ? 
                      productosSeleccionadosVendedor : 
                      Array.from(componentesSeleccionadosVendedor.values()),
            total: tipoPedidoSeleccionadoVendedor === 'PRODUCTO_COMPLETO' ?
                   productosSeleccionadosVendedor.reduce((total, producto) => 
                       total + (producto.subtotal || producto.precio * (producto.cantidad || 1)), 0) :
                   Array.from(componentesSeleccionadosVendedor.values()).reduce((total, componente) => 
                       total + componente.precio, 0)
        });
    })
    .catch(error => {
        // ❌ ERROR
        console.error('❌ Error al crear pedido:', error);
        
        let mensajeError = 'Error al crear el pedido';
        if (error.message) {
            mensajeError = error.message;
        }
        
        mostrarAlertaVendedor(mensajeError, 'danger');
    })
    .finally(() => {
        // ✅ RESTAURAR BOTÓN SIEMPRE
        btnGenerar.innerHTML = '<i class="fas fa-plus-circle me-2"></i>Crear Orden de Pedido';
        btnGenerar.disabled = false;
    });
}

// ✅ FUNCIÓN AUXILIAR PARA MOSTRAR RESUMEN
function mostrarResumenPedidoCreado(datosPedido) {
    const tipoTexto = datosPedido.tipoPedido === 'PRODUCTO_COMPLETO' ? 'Producto Completo' : 'Armar PC';
    const itemsTexto = datosPedido.productos.length + ' items';
    
    mostrarAlertaVendedor(`
        <strong>✅ Pedido creado exitosamente:</strong><br>
        📋 Cliente: ${clienteSeleccionadoVendedor.nombreCompleto || clienteSeleccionadoVendedor.nombre}<br>
        🔧 Tipo: ${tipoTexto}<br>
        📦 Items: ${itemsTexto}<br>
        💰 Total: S/ ${datosPedido.total.toFixed(2)}
    `, 'success');
}
function mostrarResumenPedidoCreado(datosPedido) {
    const tipoTexto = datosPedido.tipoPedido === 'PRODUCTO_COMPLETO' ? 'Producto Completo' : 'Armar PC';
    const itemsTexto = datosPedido.tipoPedido === 'PRODUCTO_COMPLETO' ? 
        `${datosPedido.productos.length} productos` : 
        `${datosPedido.productos.length} componentes`;
    
    mostrarAlertaVendedor(`
        <strong>Pedido creado exitosamente:</strong><br>
        Cliente: ${clienteSeleccionadoVendedor.nombreCompleto}<br>
        Tipo: ${tipoTexto}<br>
        Items: ${itemsTexto}<br>
        Total: S/ ${datosPedido.total.toFixed(2)}
    `, 'success');
}

// ===========================
// UTILIDADES Y FUNCIONES DE APOYO
// ===========================

function mostrarAlertaVendedor(mensaje, tipo) {
    const container = document.getElementById('nuevoPedidoModalAlertContainer');
    const alertClass = tipo === 'success' ? 'alert-success' : 
                      tipo === 'warning' ? 'alert-warning' : 
                      tipo === 'info' ? 'alert-info' : 'alert-danger';
    
    container.innerHTML = `
        <div class="alert ${alertClass} alert-dismissible fade show" role="alert">
            ${mensaje}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    setTimeout(() => {
        const alertElement = container.querySelector('.alert');
        if (alertElement) {
            alertElement.remove();
        }
    }, 5000);
}

function mostrarAlertaModalClienteVendedor(mensaje, tipo) {
    const container = document.getElementById('registrarClienteModalAlertContainerVendedor');
    const alertClass = tipo === 'success' ? 'alert-success' : 
                      tipo === 'warning' ? 'alert-warning' : 'alert-danger';
    
    container.innerHTML = `
        <div class="alert ${alertClass} alert-dismissible fade show" role="alert">
            ${mensaje}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    setTimeout(() => {
        const alertElement = container.querySelector('.alert');
        if (alertElement) {
            alertElement.remove();
        }
    }, 5000);
}

function limpiarFormularioVendedor() {
    if (confirm('¿Está seguro que desea limpiar todo el formulario?')) {
        // ✅ CÓDIGO EXISTENTE SIN CAMBIOS
        limpiarFormularioCompletoVendedor();
        
        // ✅ NUEVO: Resetear estado del pago
        pagoRegistradoVendedor = false;
        
        // ✅ NUEVO: Restaurar botón de registrar pago a su estado original
        const btnRegistrarPago = document.getElementById('btnRegistrarPagoVendedor');
        if (btnRegistrarPago) {
            btnRegistrarPago.disabled = true;
            btnRegistrarPago.innerHTML = '<i class="fas fa-credit-card me-2"></i>Registrar Pago';
            btnRegistrarPago.classList.remove('btn-success');
            btnRegistrarPago.classList.add('btn-warning');
        }
        
        // ✅ CÓDIGO EXISTENTE SIN CAMBIOS
        mostrarAlertaVendedor('Formulario limpiado', 'info');
    }
}

// ===========================
// EVENT LISTENERS
// ===========================

document.addEventListener('DOMContentLoaded', function() {
    // Event listeners para búsquedas con Enter
    const clienteBuscarVendedor = document.getElementById('clienteBuscarVendedor');
    if (clienteBuscarVendedor) {
        clienteBuscarVendedor.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                buscarClientesVendedor();
            }
        });
    }
    
    const productoCodigoVendedor = document.getElementById('productoCodigoVendedor');
    if (productoCodigoVendedor) {
        productoCodigoVendedor.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                buscarProductoPorCodigoVendedor();
            }
        });
    }
    
    const productoNombreVendedor = document.getElementById('productoNombreVendedor');
    if (productoNombreVendedor) {
        productoNombreVendedor.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                buscarProductosPorNombreVendedor();
            }
        });
    }
    
    // Event listener para el modal de cliente
    const modalCliente = document.getElementById('registrarClienteModalVendedor');
    if (modalCliente) {
        modalCliente.addEventListener('hidden.bs.modal', function () {
            // Limpiar formulario de cliente cuando se cierra el modal
            document.getElementById('registroDniVendedor').value = '';
            document.getElementById('registroNombreVendedor').value = '';
            document.getElementById('registroApellidoVendedor').value = '';
            document.getElementById('registroCorreoVendedor').value = '';
            document.getElementById('registroTelefonoVendedor').value = '';
            document.getElementById('registroDireccionVendedor').value = '';
            document.getElementById('registrarClienteModalAlertContainerVendedor').innerHTML = '';
        });
    }
});

// ===========================
// FUNCIONES DE COMPATIBILIDAD (LEGACY)
// ===========================

// Mantener compatibilidad con funciones existentes en vendedor.html
function buscarCliente() {
    buscarClientesVendedor();
}

function crearPedido() {
    generarPedidoVendedor();
}

// ===========================
// FUNCIONES DE DEBUG (SOLO DESARROLLO)
// ===========================

function debugEstadoModal() {
    console.log('=== ESTADO DEL MODAL VENDEDOR ===');
    console.log('Cliente seleccionado:', clienteSeleccionadoVendedor);
    console.log('Tipo de pedido:', tipoPedidoSeleccionadoVendedor);
    console.log('Productos seleccionados:', productosSeleccionadosVendedor);
    console.log('Componentes seleccionados:', componentesSeleccionadosVendedor);
    console.log('Categorías disponibles:', categoriasDisponiblesVendedor);
    console.log('Vendedor actual ID:', vendedorActualId);
}

// Función para mostrar estado en consola (solo para debugging)
function mostrarEstadoConsola() {
    if (typeof debugEstadoModal === 'function') {
        debugEstadoModal();
    }
}
// ✅ FUNCIÓN PARA MOSTRAR MODAL DE CONFIRMACIÓN
function mostrarModalConfirmacionVendedor(respuestaServidor, datosPedido) {
    // Extraer código de la orden de la respuesta
    let codigoOrden = 'Generando...';
    if (typeof respuestaServidor === 'object' && respuestaServidor.data && respuestaServidor.data.id) {
        codigoOrden = respuestaServidor.data.id;
    } else if (typeof respuestaServidor === 'object' && respuestaServidor.pedidoId) {
        codigoOrden = respuestaServidor.pedidoId;
    } else if (typeof respuestaServidor === 'string' && respuestaServidor.includes('ID:')) {
        // Intentar extraer ID de un mensaje de texto
        const match = respuestaServidor.match(/ID:\s*(\d+)/);
        if (match) {
            codigoOrden = match[1];
        }
    }
    
    // Llenar datos del modal
    document.getElementById('codigoOrdenGeneradaVendedor').textContent = codigoOrden;
    document.getElementById('clienteOrdenGenerada').textContent = 
        datosPedido.cliente.nombreCompleto || `${datosPedido.cliente.nombre} ${datosPedido.cliente.apellido}`;
    document.getElementById('totalOrdenGenerada').textContent = `S/ ${datosPedido.total.toFixed(2)}`;
    document.getElementById('tipoOrdenGenerada').textContent = 
        datosPedido.tipoPedido === 'PRODUCTO_COMPLETO' ? 'Producto Completo' : 'Armar PC';
    document.getElementById('itemsOrdenGenerada').textContent = `${datosPedido.productos.length} items`;
    
    // Mostrar el modal de confirmación
    const modalConfirmacion = new bootstrap.Modal(document.getElementById('confirmacionPedidoVendedorModal'));
    modalConfirmacion.show();
    
    // Auto-recargar después de cerrar el modal
    document.getElementById('confirmacionPedidoVendedorModal').addEventListener('hidden.bs.modal', function() {
        setTimeout(() => {
            location.reload();
        }, 500);
    }, { once: true });
}

// ✅ FUNCIÓN PARA CREAR NUEVA ORDEN (BOTÓN DEL MODAL)
function crearNuevaOrden() {
    // Cerrar modal de confirmación
    const modalConfirmacion = bootstrap.Modal.getInstance(document.getElementById('confirmacionPedidoVendedorModal'));
    if (modalConfirmacion) {
        modalConfirmacion.hide();
    }
    
    // Abrir modal de nueva orden después de un breve delay
    setTimeout(() => {
        showNewOrderModal();
    }, 500);
}

// ===========================
// FUNCIONES PARA REGISTRO DE PAGO
// ===========================

// Variable global para almacenar el estado del pago
let pagoRegistradoVendedor = false;

/**
 * Abrir modal de registro de pago
 */
function abrirModalRegistrarPago() {
    // Validar que haya cliente seleccionado y productos
    if (!clienteSeleccionadoVendedor) {
        mostrarAlertaVendedor('Debe seleccionar un cliente primero', 'warning');
        return;
    }
    
    if (tipoPedidoSeleccionadoVendedor === 'PRODUCTO_COMPLETO' && productosSeleccionadosVendedor.length === 0) {
        mostrarAlertaVendedor('Debe seleccionar al menos un producto', 'warning');
        return;
    }
    
    if (tipoPedidoSeleccionadoVendedor === 'ARMAR_PC' && componentesSeleccionadosVendedor.size === 0) {
        mostrarAlertaVendedor('Debe seleccionar componentes para armar PC', 'warning');
        return;
    }
    
    // Llenar datos del resumen en el modal de pago
    llenarResumenPagoVendedor();
    
    // Cargar bancos disponibles
    cargarBancosVendedor();
    
    // Mostrar modal
    const modalPago = new bootstrap.Modal(document.getElementById('registrarPagoVendedorModal'));
    modalPago.show();
}

/**
 * Llenar resumen del pedido en el modal de pago
 */
function llenarResumenPagoVendedor() {
    // Datos del cliente
    document.getElementById('pagoVendedorClienteNombre').textContent = 
        clienteSeleccionadoVendedor.nombreCompleto || 
        `${clienteSeleccionadoVendedor.nombre} ${clienteSeleccionadoVendedor.apellido}`;
    document.getElementById('pagoVendedorClienteDni').textContent = clienteSeleccionadoVendedor.dni;
    
    // Calcular cantidad y total
    let cantidadTotal = 0;
    let montoTotal = 0;
    
    if (tipoPedidoSeleccionadoVendedor === 'PRODUCTO_COMPLETO') {
        cantidadTotal = productosSeleccionadosVendedor.length;
        montoTotal = productosSeleccionadosVendedor.reduce((total, producto) => total + producto.precio, 0);
    } else {
        cantidadTotal = componentesSeleccionadosVendedor.size;
        montoTotal = Array.from(componentesSeleccionadosVendedor.values())
            .reduce((total, componente) => total + componente.precio, 0);
    }
    
    document.getElementById('pagoVendedorCantidadProductos').textContent = cantidadTotal;
    document.getElementById('pagoVendedorMontoTotal').textContent = montoTotal.toFixed(2);
}

/**
 * Cargar bancos desde la base de datos
 */
function cargarBancosVendedor() {
    fetch('/banco/api/listar')
    .then(response => response.json())
    .then(bancos => {
        const selectBanco = document.getElementById('pagoVendedorBanco');
        selectBanco.innerHTML = '<option value="">Seleccione un banco...</option>';
        
        bancos.forEach(banco => {
            const option = document.createElement('option');
            option.value = banco.id;
            option.textContent = banco.nombre;
            selectBanco.appendChild(option);
        });
    })
    .catch(error => {
        console.error('Error al cargar bancos:', error);
        mostrarAlertaPagoVendedor('Error al cargar la lista de bancos', 'danger');
    });
}

/**
 * Validar y habilitar botón de confirmar pago
 */
function validarCamposPagoVendedor() {
    const banco = document.getElementById('pagoVendedorBanco').value;
    const numeroOperacion = document.getElementById('pagoVendedorNumeroOperacion').value.trim();
    const btnConfirmar = document.getElementById('btnConfirmarPagoVendedor');
    
    if (banco && numeroOperacion.length >= 6) {
        btnConfirmar.disabled = false;
    } else {
        btnConfirmar.disabled = true;
    }
}

/**
 * Confirmar registro de pago
 */
function confirmarRegistroPago() {
    const banco = document.getElementById('pagoVendedorBanco').value;
    const numeroOperacion = document.getElementById('pagoVendedorNumeroOperacion').value.trim();
    
    if (!banco || !numeroOperacion) {
        mostrarAlertaPagoVendedor('Todos los campos son obligatorios', 'warning');
        return;
    }
    
    // Simular registro exitoso (aquí se podría hacer una llamada al servidor para validar)
    pagoRegistradoVendedor = true;
    
    // Mostrar mensaje de éxito
    mostrarAlertaPagoVendedor('Pago registrado exitosamente', 'success');
    
    // Cerrar modal después de 1.5 segundos
    setTimeout(() => {
        const modalPago = bootstrap.Modal.getInstance(document.getElementById('registrarPagoVendedorModal'));
        modalPago.hide();
        
        // Habilitar botón de crear pedido
        document.getElementById('btnGenerarPedidoVendedor').disabled = false;
        document.getElementById('btnRegistrarPagoVendedor').innerHTML = 
            '<i class="fas fa-check-circle me-2"></i>Pago Registrado';
        document.getElementById('btnRegistrarPagoVendedor').disabled = true;
        document.getElementById('btnRegistrarPagoVendedor').classList.remove('btn-warning');
        document.getElementById('btnRegistrarPagoVendedor').classList.add('btn-success');
        
        mostrarAlertaVendedor('Pago registrado correctamente. Ahora puede crear la orden de pedido.', 'success');
    }, 1500);
}

/**
 * Mostrar alertas en el modal de pago
 */
function mostrarAlertaPagoVendedor(mensaje, tipo) {
    const alertContainer = document.getElementById('registrarPagoVendedorModalAlertContainer');
    alertContainer.innerHTML = `
        <div class="alert alert-${tipo} alert-dismissible fade show" role="alert">
            ${mensaje}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
}

// Event listeners para validación en tiempo real
document.addEventListener('DOMContentLoaded', function() {
    // Validar campos al escribir
    if (document.getElementById('pagoVendedorBanco')) {
        document.getElementById('pagoVendedorBanco').addEventListener('change', validarCamposPagoVendedor);
        document.getElementById('pagoVendedorNumeroOperacion').addEventListener('input', validarCamposPagoVendedor);
    }
});
