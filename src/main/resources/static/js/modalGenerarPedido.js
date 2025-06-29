// ===========================
// VARIABLES GLOBALES
// ===========================
let clienteSeleccionado = null;
let tipoPedidoSeleccionado = null;
let productosSeleccionados = [];
let componentesSeleccionados = new Map(); // Para armar PC: categoria -> producto
let categoriasDisponibles = [];
let vendedorActual = null;

// ===========================
// INICIALIZACIÓN DEL MODAL
// ===========================
function abrirModalGenerarPedido() {
    // Reiniciar todo
    limpiarFormularioCompleto();
    
    // Cargar datos iniciales
    cargarVendedorActual();
    cargarCategorias();
    
    // Mostrar modal
    const modal = new bootstrap.Modal(document.getElementById('generarPedidoModal'));
    modal.show();
}

function limpiarFormularioCompleto() {
    // Reiniciar variables
    clienteSeleccionado = null;
    tipoPedidoSeleccionado = null;
    productosSeleccionados = [];
    componentesSeleccionados.clear();
    
    // Limpiar campos
    document.getElementById('clienteBuscar').value = '';
    document.getElementById('productoCodigo').value = '';
    document.getElementById('productoNombre').value = '';
    if (document.getElementById('categoriaFiltro')) {
        document.getElementById('categoriaFiltro').value = '';
    }
    
    // Ocultar secciones
    document.getElementById('clienteResultados').style.display = 'none';
    document.getElementById('clienteSeleccionadoInfo').style.display = 'none';
    document.getElementById('seccionTipoPedido').style.display = 'none';
    document.getElementById('seccionProductosCompletos').style.display = 'none';
    document.getElementById('seccionArmarPC').style.display = 'none';
    document.getElementById('seccionResumen').style.display = 'none';
    document.getElementById('productosResultados').style.display = 'none';
    document.getElementById('tipoSeleccionadoInfo').style.display = 'none';
    
    // Limpiar tarjetas seleccionadas
    document.querySelectorAll('.tipo-pedido-card').forEach(card => {
        card.classList.remove('selected');
    });
    
    // Limpiar tablas
    actualizarTablaProductosSeleccionados();
    actualizarTablaComponentesSeleccionados();
    actualizarResumen();
    
    // Limpiar alertas
    document.getElementById('generarModalAlertContainer').innerHTML = '';
}

function cargarVendedorActual() {
    // Obtener vendedor desde la sesión actual o usar mock
    vendedorActual = { id: 'VENDOR001', nombre: 'Vendedor', apellido: 'Sistema' };
}

function cargarCategorias() {
    fetch('/categoria/api/todas')
    .then(response => response.json())
    .then(categorias => {
        categoriasDisponibles = categorias;
        llenarSelectCategorias(categorias);
        crearAccordeonCategorias(categorias);
    })
    .catch(error => {
        console.error('Error al cargar categorías:', error);
        categoriasDisponibles = [];
    });
}

function llenarSelectCategorias(categorias) {
    const select = document.getElementById('categoriaFiltro');
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
function buscarClientes() {
    const textoBusqueda = document.getElementById('clienteBuscar').value.trim();
    
    if (!textoBusqueda) {
        mostrarAlerta('Ingrese texto para buscar (nombre, apellido o DNI)', 'warning');
        return;
    }
    
    const resultados = document.getElementById('clienteResultados');
    resultados.innerHTML = '<div class="text-center"><i class="fas fa-spinner fa-spin"></i> Buscando clientes...</div>';
    resultados.style.display = 'block';
    
    // Buscar usando el nuevo endpoint de clientes
    fetch(`/cliente/api/buscar?texto=${encodeURIComponent(textoBusqueda)}`)
    .then(response => response.json())
    .then(data => {
        if (data.success && data.data.length > 0) {
            mostrarResultadosClientes(data.data);
        } else {
            mostrarMensajeNoEncontradoClientes('No se encontraron clientes con ese criterio');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarAlerta('Error al buscar clientes', 'danger');
        resultados.style.display = 'none';
    });
}

function mostrarResultadosClientes(clientes) {
    const resultados = document.getElementById('clienteResultados');
    
    let html = '<h6 class="mb-3">Resultados de búsqueda:</h6>';
    html += '<div class="row">';
    
    clientes.forEach(cliente => {
        html += `
            <div class="col-md-6 mb-2">
                <div class="card cliente-item" onclick="seleccionarCliente('${cliente.dni}')">
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

function mostrarMensajeNoEncontradoClientes(mensaje) {
    const resultados = document.getElementById('clienteResultados');
    resultados.innerHTML = `
        <div class="text-center py-4">
            <i class="fas fa-user-slash fa-3x text-muted mb-3"></i>
            <h6 class="text-muted">${mensaje}</h6>
            <button class="btn btn-outline-primary btn-sm mt-2" onclick="abrirModalRegistrarCliente()">
                <i class="fas fa-plus me-1"></i>Registrar nuevo cliente
            </button>
        </div>
    `;
}

function seleccionarCliente(dni) {
    fetch(`/cliente/api/buscar/${dni}`)
    .then(response => response.json())
    .then(cliente => {
        clienteSeleccionado = cliente;
        mostrarClienteSeleccionado(cliente);
        
        // Ocultar resultados de búsqueda
        document.getElementById('clienteResultados').style.display = 'none';
        document.getElementById('clienteBuscar').value = '';
        
        // Mostrar paso 2 (tipo de pedido)
        document.getElementById('seccionTipoPedido').style.display = 'block';
        
        mostrarAlerta(`Cliente seleccionado: ${cliente.nombreCompleto}`, 'success');
        actualizarResumen();
    })
    .catch(error => {
        console.error('Error al obtener datos del cliente:', error);
        mostrarAlerta('Error al cargar datos del cliente', 'danger');
    });
}

function mostrarClienteSeleccionado(cliente) {
    const infoDiv = document.getElementById('clienteSeleccionadoInfo');
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
            <button class="btn btn-outline-secondary btn-sm" onclick="cambiarCliente()">
                <i class="fas fa-edit me-1"></i>Cambiar
            </button>
        </div>
    `;
    infoDiv.style.display = 'block';
}

function cambiarCliente() {
    clienteSeleccionado = null;
    document.getElementById('clienteSeleccionadoInfo').style.display = 'none';
    document.getElementById('seccionTipoPedido').style.display = 'none';
    document.getElementById('seccionProductosCompletos').style.display = 'none';
    document.getElementById('seccionArmarPC').style.display = 'none';
    document.getElementById('seccionResumen').style.display = 'none';
    
    // Reiniciar tipo de pedido
    tipoPedidoSeleccionado = null;
    document.querySelectorAll('.tipo-pedido-card').forEach(card => {
        card.classList.remove('selected');
    });
    
    actualizarResumen();
}

// ===========================
// PASO 2: SELECCIÓN DE TIPO DE PEDIDO
// ===========================
function seleccionarTipoPedido(tipo) {
    tipoPedidoSeleccionado = tipo;
    
    // Actualizar UI de selección
    document.querySelectorAll('.tipo-pedido-card').forEach(card => {
        card.classList.remove('selected');
    });
    event.currentTarget.classList.add('selected');
    
    // Mostrar información del tipo seleccionado
    const infoDiv = document.getElementById('tipoSeleccionadoInfo');
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
        document.getElementById('seccionProductosCompletos').style.display = 'block';
        document.getElementById('seccionArmarPC').style.display = 'none';
    } else {
        document.getElementById('seccionArmarPC').style.display = 'block';
        document.getElementById('seccionProductosCompletos').style.display = 'none';
    }
    
    // Mostrar sección de resumen
    document.getElementById('seccionResumen').style.display = 'block';
    
    // Limpiar selecciones anteriores
    productosSeleccionados = [];
    componentesSeleccionados.clear();
    actualizarTablaProductosSeleccionados();
    actualizarTablaComponentesSeleccionados();
    actualizarResumen();
    
    mostrarAlerta(`Tipo de pedido seleccionado: ${tipo === 'PRODUCTO_COMPLETO' ? 'Producto Completo' : 'Armar PC'}`, 'success');
}

// ===========================
// PASO 3A: PRODUCTOS COMPLETOS
// ===========================
function buscarProductoPorCodigo() {
    const codigo = document.getElementById('productoCodigo').value.trim();
    
    if (!codigo) {
        mostrarAlerta('Ingrese un código para buscar', 'warning');
        return;
    }
    
    fetch(`/producto/api/buscar/${codigo}`)
    .then(response => response.json())
    .then(producto => {
        if (producto && producto.codigo) {
            mostrarResultadosProductos([producto]);
        } else {
            mostrarAlerta('No se encontró producto con código: ' + codigo, 'warning');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarAlerta('Error al buscar producto', 'danger');
    });
}

function buscarProductosPorNombre() {
    const nombre = document.getElementById('productoNombre').value.trim();
    
    if (!nombre) {
        mostrarAlerta('Ingrese un nombre para buscar', 'warning');
        return;
    }
    
    fetch(`/producto/api/buscarPorNombre?nombre=${encodeURIComponent(nombre)}`)
    .then(response => response.json())
    .then(productos => {
        if (productos && productos.length > 0) {
            mostrarResultadosProductos(productos);
        } else {
            mostrarAlerta('No se encontraron productos con nombre: ' + nombre, 'warning');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarAlerta('Error al buscar productos', 'danger');
    });
}

function buscarProductosPorCategoria() {
    const categoriaId = document.getElementById('categoriaFiltro').value;
    
    if (!categoriaId) {
        mostrarAlerta('Seleccione una categoría', 'warning');
        return;
    }
    
    fetch(`/producto/api/categoria/${categoriaId}`)
    .then(response => response.json())
    .then(productos => {
        if (productos && productos.length > 0) {
            mostrarResultadosProductos(productos);
        } else {
            mostrarAlerta('No se encontraron productos en esta categoría', 'warning');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarAlerta('Error al buscar productos por categoría', 'danger');
    });
}

function mostrarResultadosProductos(productos) {
    const resultados = document.getElementById('productosResultados');
    
    let html = '';
    productos.forEach(producto => {
        const yaAgregado = productosSeleccionados.some(p => p.codigo === producto.codigo);
        const sinStock = producto.cantidad <= 0;
        
        html += `
            <div class="col-md-6 col-lg-4 mb-3">
                <div class="card producto-card h-100">
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
                                `<button class="btn btn-primary btn-sm w-100" onclick="agregarProductoCompleto(${producto.codigo})"><i class="fas fa-plus me-1"></i>Agregar</button>`
                        }
                    </div>
                </div>
            </div>
        `;
    });
    
    resultados.innerHTML = html;
    resultados.style.display = 'block';
}

function agregarProductoCompleto(codigoProducto) {
    fetch(`/producto/api/buscar/${codigoProducto}`)
    .then(response => response.json())
    .then(producto => {
        if (producto && producto.cantidad > 0) {
            const yaAgregado = productosSeleccionados.some(p => p.codigo === producto.codigo);
            
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
                
                productosSeleccionados.push(productoParaPedido);
                actualizarTablaProductosSeleccionados();
                actualizarResumen();
                
                // Actualizar botón en resultados
                const btnAgregar = document.querySelector(`[onclick="agregarProductoCompleto(${codigoProducto})"]`);
                if (btnAgregar) {
                    btnAgregar.className = 'btn btn-success btn-sm w-100';
                    btnAgregar.innerHTML = '<i class="fas fa-check me-1"></i>Agregado';
                    btnAgregar.disabled = true;
                }
                
                mostrarAlerta('Producto agregado al pedido', 'success');
            }
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarAlerta('Error al agregar producto', 'danger');
    });
}

function actualizarTablaProductosSeleccionados() {
    const tbody = document.getElementById('productosSeleccionadosTable');
    
    if (productosSeleccionados.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No hay productos seleccionados</td></tr>';
        return;
    }
    
    tbody.innerHTML = productosSeleccionados.map((producto, index) => `
        <tr>
            <td>${producto.codigo}</td>
            <td>${producto.nombre}</td>
            <td>S/ ${producto.precio.toFixed(2)}</td>
            <td>
                <input type="number" class="form-control form-control-sm" 
                       value="${producto.cantidad}" min="1" max="${producto.stock}"
                       onchange="cambiarCantidadProducto(${index}, this.value)">
            </td>
            <td>S/ ${producto.subtotal.toFixed(2)}</td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="eliminarProducto(${index})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function cambiarCantidadProducto(index, nuevaCantidad) {
    const cantidad = parseInt(nuevaCantidad);
    const producto = productosSeleccionados[index];
    
    if (cantidad >= 1 && cantidad <= producto.stock) {
        producto.cantidad = cantidad;
        producto.subtotal = producto.precio * cantidad;
        actualizarTablaProductosSeleccionados();
        actualizarResumen();
    } else {
        actualizarTablaProductosSeleccionados();
    }
}

function eliminarProducto(index) {
    const producto = productosSeleccionados[index];
    productosSeleccionados.splice(index, 1);
    
    actualizarTablaProductosSeleccionados();
    actualizarResumen();
    
    // Rehabilitar botón en resultados si están visibles
    const btnAgregar = document.querySelector(`[onclick="agregarProductoCompleto(${producto.codigo})"]`);
    if (btnAgregar) {
        btnAgregar.className = 'btn btn-primary btn-sm w-100';
        btnAgregar.innerHTML = '<i class="fas fa-plus me-1"></i>Agregar';
        btnAgregar.disabled = false;
    }
}

// ===========================
// PASO 3B: ARMAR PC
// ===========================
function crearAccordeonCategorias(categorias) {
    const accordion = document.getElementById('categoriasAccordion');
    
    let html = '';
    categorias.forEach((categoria, index) => {
        const collapseId = `collapse${categoria.id}`;
        const headingId = `heading${categoria.id}`;
        
        html += `
            <div class="accordion-item">
                <h2 class="accordion-header" id="${headingId}">
                    <button class="accordion-button collapsed" type="button" 
                            data-bs-toggle="collapse" data-bs-target="#${collapseId}"
                            onclick="cargarProductosCategoria(${categoria.id}, '${collapseId}')">
                        <i class="fas fa-microchip me-2"></i>
                        ${categoria.nombre}
                        <span id="badge${categoria.id}" class="badge bg-secondary ms-2" style="display: none;">
                            Seleccionado
                        </span>
                    </button>
                </h2>
                <div id="${collapseId}" class="accordion-collapse collapse" 
                     data-bs-parent="#categoriasAccordion">
                    <div class="accordion-body">
                        <div id="productos${categoria.id}" class="row">
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

function cargarProductosCategoria(categoriaId, collapseId) {
    const productosDiv = document.getElementById(`productos${categoriaId}`);
    
    // Solo cargar si no se ha cargado antes
    if (productosDiv.innerHTML.includes('Cargando productos...')) {
        fetch(`/producto/api/categoria/${categoriaId}`)
        .then(response => response.json())
        .then(productos => {
            mostrarProductosCategoria(categoriaId, productos);
        })
        .catch(error => {
            console.error('Error:', error);
            productosDiv.innerHTML = '<div class="col-12 text-center text-danger">Error al cargar productos</div>';
        });
    }
}

function mostrarProductosCategoria(categoriaId, productos) {
    const productosDiv = document.getElementById(`productos${categoriaId}`);
    
    if (productos.length === 0) {
        productosDiv.innerHTML = '<div class="col-12 text-center text-muted">No hay productos en esta categoría</div>';
        return;
    }
    
    let html = '';
    productos.forEach(producto => {
        const isSelected = componentesSeleccionados.has(categoriaId) && 
                          componentesSeleccionados.get(categoriaId).codigo === producto.codigo;
        const sinStock = producto.cantidad <= 0;
        
        html += `
            <div class="col-md-6 col-lg-4 mb-3">
                <div class="card producto-card h-100 ${isSelected ? 'selected' : ''}">
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
                                `<button class="btn btn-success btn-sm w-100" onclick="deseleccionarComponente(${categoriaId})"><i class="fas fa-check me-1"></i>Seleccionado</button>` :
                                `<button class="btn btn-outline-primary btn-sm w-100" onclick="seleccionarComponente(${categoriaId}, ${producto.codigo})"><i class="fas fa-plus me-1"></i>Seleccionar</button>`
                        }
                    </div>
                </div>
            </div>
        `;
    });
    
    productosDiv.innerHTML = html;
}

function seleccionarComponente(categoriaId, codigoProducto) {
    fetch(`/producto/api/buscar/${codigoProducto}`)
    .then(response => response.json())
    .then(producto => {
        if (producto && producto.cantidad > 0) {
            // Guardar componente seleccionado
            componentesSeleccionados.set(categoriaId, {
                categoria: categoriasDisponibles.find(c => c.id === categoriaId),
                codigo: producto.codigo,
                nombre: producto.nombre,
                precio: producto.precio,
                modelo: producto.modelo || 'N/A',
                marca: producto.marca ? producto.marca.nombre : 'N/A'
            });
            
            // Actualizar UI
            actualizarTablaComponentesSeleccionados();
            actualizarResumen();
            mostrarBadgeCategoria(categoriaId);
            
            // Refrescar productos de esta categoría para mostrar selección
            cargarProductosCategoria(categoriaId, `collapse${categoriaId}`);
            
            mostrarAlerta(`Componente seleccionado para ${categoriasDisponibles.find(c => c.id === categoriaId).nombre}`, 'success');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarAlerta('Error al seleccionar componente', 'danger');
    });
}

function deseleccionarComponente(categoriaId) {
    componentesSeleccionados.delete(categoriaId);
    
    actualizarTablaComponentesSeleccionados();
    actualizarResumen();
    ocultarBadgeCategoria(categoriaId);
    
    // Refrescar productos de esta categoría
    cargarProductosCategoria(categoriaId, `collapse${categoriaId}`);
}

function mostrarBadgeCategoria(categoriaId) {
    const badge = document.getElementById(`badge${categoriaId}`);
    if (badge) {
        badge.style.display = 'inline';
    }
}

function ocultarBadgeCategoria(categoriaId) {
    const badge = document.getElementById(`badge${categoriaId}`);
    if (badge) {
        badge.style.display = 'none';
    }
}

function actualizarTablaComponentesSeleccionados() {
    const tbody = document.getElementById('componentesSeleccionadosTable');
    
    if (componentesSeleccionados.size === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No hay componentes seleccionados</td></tr>';
        return;
    }
    
    let html = '';
    componentesSeleccionados.forEach((componente, categoriaId) => {
        html += `
            <tr>
                <td>${componente.categoria.nombre}</td>
                <td>${componente.nombre}</td>
                <td>${componente.modelo}</td>
                <td>S/ ${componente.precio.toFixed(2)}</td>
                <td>
                    <button class="btn btn-danger btn-sm" onclick="deseleccionarComponente(${categoriaId})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `;
    });
    
    tbody.innerHTML = html;
}

// ===========================
// RESUMEN Y GENERACIÓN
// ===========================
function actualizarResumen() {
    // Actualizar información del cliente
    document.getElementById('resumenCliente').textContent = 
        clienteSeleccionado ? clienteSeleccionado.nombreCompleto : '-';
    
    // Actualizar tipo de pedido
    let tipoTexto = '-';
    if (tipoPedidoSeleccionado === 'PRODUCTO_COMPLETO') {
        tipoTexto = 'Producto Completo';
    } else if (tipoPedidoSeleccionado === 'ARMAR_PC') {
        tipoTexto = 'Armar PC';
    }
    document.getElementById('resumenTipo').textContent = tipoTexto;
    
    // Calcular totales
    let totalItems = 0;
    let montoTotal = 0;
    
    if (tipoPedidoSeleccionado === 'PRODUCTO_COMPLETO') {
        totalItems = productosSeleccionados.reduce((total, producto) => total + producto.cantidad, 0);
        montoTotal = productosSeleccionados.reduce((total, producto) => total + producto.subtotal, 0);
    } else if (tipoPedidoSeleccionado === 'ARMAR_PC') {
        totalItems = componentesSeleccionados.size;
        componentesSeleccionados.forEach(componente => {
            montoTotal += componente.precio;
        });
    }
    
    document.getElementById('resumenCantidad').textContent = totalItems;
    document.getElementById('resumenTotal').textContent = montoTotal.toFixed(2);
    
    // Habilitar/deshabilitar botón de generar
    const btnGenerar = document.getElementById('btnGenerarPedido');
    const puedeGenerar = clienteSeleccionado && tipoPedidoSeleccionado && 
                        ((tipoPedidoSeleccionado === 'PRODUCTO_COMPLETO' && productosSeleccionados.length > 0) ||
                         (tipoPedidoSeleccionado === 'ARMAR_PC' && componentesSeleccionados.size > 0));
    
    btnGenerar.disabled = !puedeGenerar;
    
    if (!clienteSeleccionado) {
        btnGenerar.innerHTML = '<i class="fas fa-user me-2"></i>Seleccione un cliente';
    } else if (!tipoPedidoSeleccionado) {
        btnGenerar.innerHTML = '<i class="fas fa-list me-2"></i>Seleccione tipo de pedido';
    } else if (totalItems === 0) {
        btnGenerar.innerHTML = '<i class="fas fa-box me-2"></i>Agregue productos/componentes';
    } else {
        btnGenerar.innerHTML = '<i class="fas fa-plus-circle me-2"></i>Generar Orden de Pedido';
    }
}

function generarPedido() {
    const btnGenerar = document.getElementById('btnGenerarPedido');
    
    // Validación final
    if (!clienteSeleccionado || !tipoPedidoSeleccionado) {
        mostrarAlerta('Complete todos los pasos requeridos', 'warning');
        return;
    }
    
    if (tipoPedidoSeleccionado === 'PRODUCTO_COMPLETO' && productosSeleccionados.length === 0) {
        mostrarAlerta('Debe agregar al menos un producto', 'warning');
        return;
    }
    
    if (tipoPedidoSeleccionado === 'ARMAR_PC' && componentesSeleccionados.size === 0) {
        mostrarAlerta('Debe seleccionar al menos un componente', 'warning');
        return;
    }
    
    // Estado de carga
    btnGenerar.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Generando pedido...';
    btnGenerar.disabled = true;
    
    // ✅ VENDEDOR REAL: Usar ID válido de la base de datos
    const vendedorId = '1'; // ← CAMBIO CRÍTICO: Usar vendedor real de tu BD
    
    // Preparar datos del pedido
    let datosPedido = {
        clienteDni: clienteSeleccionado.dni,
        vendedorId: vendedorId,  // ✅ Ahora usa vendedor real: '1'
        tipoPedido: tipoPedidoSeleccionado,
        productos: [],
        total: 0,
        detallesComponentes: ''
    };
    
    // Procesar productos según el tipo de pedido
    if (tipoPedidoSeleccionado === 'PRODUCTO_COMPLETO') {
        datosPedido.productos = productosSeleccionados.map(producto => ({
            codigo: producto.codigo || producto.id,
            cantidad: producto.cantidad || 1,
            precio: producto.precio
        }));
        datosPedido.total = productosSeleccionados.reduce((total, producto) => 
            total + (producto.subtotal || producto.precio * (producto.cantidad || 1)), 0);
        datosPedido.detallesComponentes = productosSeleccionados
            .map(p => `${p.nombre} (${p.cantidad || 1}x)`)
            .join(', ');
    } else if (tipoPedidoSeleccionado === 'ARMAR_PC') {
        const componentesArray = Array.from(componentesSeleccionados.values());
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
    
    console.log('✅ Datos corregidos a enviar:', datosPedido);
    
    // ✅ ENVÍO DIRECTO al servidor
    fetch('/orden-pedido/crear', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(datosPedido)
    })
    .then(response => {
        console.log('Respuesta del servidor:', response.status);
        
        if (response.ok) {
            // Éxito - intentar leer como JSON primero, luego como texto
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return response.json();
            } else {
                return response.text();
            }
        } else {
            // Error - intentar leer el mensaje de error
            return response.text().then(errorText => {
                throw new Error(errorText || 'Error al crear el pedido');
            });
        }
    })
    .then(data => {
        // Determinar mensaje de éxito
        let mensaje = 'Pedido generado exitosamente';
        if (typeof data === 'object' && data.message) {
            mensaje = data.message;
        } else if (typeof data === 'string') {
            mensaje = data;
        }
        
        mostrarAlerta(mensaje, 'success');
        
        // Cerrar modal y recargar después de éxito
        setTimeout(() => {
            const modal = bootstrap.Modal.getInstance(document.getElementById('generarPedidoModal'));
            if (modal) {
                modal.hide();
            }
            
            // Recargar la página
            if (typeof cargarOrdenes === 'function') {
                cargarOrdenes();
            } else {
                location.reload();
            }
        }, 2000);
    })
    .catch(error => {
        console.error('❌ Error al generar pedido:', error);
        
        let mensajeError = 'Error al generar el pedido';
        if (error.message) {
            mensajeError = error.message;
        }
        
        mostrarAlerta(mensajeError, 'danger');
    })
    .finally(() => {
        // Restaurar botón siempre
        btnGenerar.innerHTML = '<i class="fas fa-plus-circle me-2"></i>Generar Orden de Pedido';
        btnGenerar.disabled = false;
    });
}

// ===========================
// MODAL REGISTRAR CLIENTE
// ===========================
function abrirModalRegistrarCliente() {
    // Prellenar DNI si hay texto de búsqueda
    const textoBusqueda = document.getElementById('clienteBuscar').value.trim();
    if (textoBusqueda && /^\d{8,11}$/.test(textoBusqueda)) {
        document.getElementById('registroDni').value = textoBusqueda;
    }
    
    new bootstrap.Modal(document.getElementById('registrarClienteModal')).show();
}

function registrarNuevoCliente() {
    const dni = document.getElementById('registroDni').value.trim();
    const nombre = document.getElementById('registroNombre').value.trim();
    const apellido = document.getElementById('registroApellido').value.trim();
    const correo = document.getElementById('registroCorreo').value.trim();
    const telefono = document.getElementById('registroTelefono').value.trim();
    const direccion = document.getElementById('registroDireccion').value.trim();
    
    // Validaciones
    if (!dni || (!dni.match(/^\d{8}$/) && !dni.match(/^\d{11}$/))) {
        mostrarAlertaModalCliente('El DNI debe tener 8 dígitos o RUC debe tener 11 dígitos', 'warning');
        return;
    }
    
    if (!nombre) {
        mostrarAlertaModalCliente('El nombre es obligatorio', 'warning');
        return;
    }
    
    if (correo && !correo.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
        mostrarAlertaModalCliente('Ingrese un correo electrónico válido', 'warning');
        return;
    }
    
    const btnGuardar = document.getElementById('btnGuardarCliente');
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
            mostrarAlertaModalCliente('Cliente registrado exitosamente', 'success');
            
            setTimeout(() => {
                const modal = bootstrap.Modal.getInstance(document.getElementById('registrarClienteModal'));
                modal.hide();
                
                // Seleccionar automáticamente el cliente recién registrado
                if (data.cliente) {
                    clienteSeleccionado = data.cliente;
                    mostrarClienteSeleccionado(data.cliente);
                    document.getElementById('seccionTipoPedido').style.display = 'block';
                    actualizarResumen();
                }
            }, 1500);
        } else {
            mostrarAlertaModalCliente(data.message || 'Error al registrar el cliente', 'danger');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarAlertaModalCliente('Error de conexión al registrar el cliente', 'danger');
    })
    .finally(() => {
        btnGuardar.innerHTML = '<i class="fas fa-save me-2"></i>Registrar Cliente';
        btnGuardar.disabled = false;
    });
}

// ===========================
// UTILIDADES
// ===========================
function mostrarAlerta(mensaje, tipo) {
    const container = document.getElementById('generarModalAlertContainer');
    const alertClass = tipo === 'success' ? 'alert-success' : 
                      tipo === 'warning' ? 'alert-warning' : 'alert-danger';
    
    container.innerHTML = `
        <div class="alert ${alertClass} alert-dismissible fade show" role="alert">
            ${mensaje}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    setTimeout(() => {
        container.innerHTML = '';
    }, 5000);
}

function mostrarAlertaModalCliente(mensaje, tipo) {
    const container = document.getElementById('registrarClienteModalAlertContainer');
    const alertClass = tipo === 'success' ? 'alert-success' : 
                      tipo === 'warning' ? 'alert-warning' : 'alert-danger';
    
    container.innerHTML = `
        <div class="alert ${alertClass} alert-dismissible fade show" role="alert">
            ${mensaje}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    setTimeout(() => {
        container.innerHTML = '';
    }, 5000);
}

function limpiarFormulario() {
    if (confirm('¿Está seguro que desea limpiar todo el formulario?')) {
        limpiarFormularioCompleto();
    }
}

// Event listeners
document.addEventListener('DOMContentLoaded', function() {
    // Enter key para búsquedas
    document.getElementById('clienteBuscar').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') buscarClientes();
    });
    
    document.getElementById('productoCodigo').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') buscarProductoPorCodigo();
    });
    
    document.getElementById('productoNombre').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') buscarProductosPorNombre();
    });
});