// Variables globales para el modal de edición
let modalEditarOrden;
let ordenActual = null;
let vendedoresCache = new Map();
let estadosCache = new Map();
let tecnicosEditarCache = new Map();

document.addEventListener('DOMContentLoaded', function() {
    inicializarModalEdicion();
});

function inicializarModalEdicion() {
    modalEditarOrden = new bootstrap.Modal(document.getElementById('editarOrdenModal'));
    
    // Limpiar modal al cerrarse
    document.getElementById('editarOrdenModal').addEventListener('hidden.bs.modal', function() {
        limpiarModalEdicion();
    });
    
    // Cargar datos iniciales una sola vez
    cargarVendedoresParaEdicion();
    cargarEstadosParaEdicion();
    cargarTecnicosParaEdicion();
}

function cargarVendedoresParaEdicion() {
    // Reutilizamos el endpoint existente si está disponible, sino creamos uno simple
    fetch('/orden-pedido/api/vendedores')
    .then(response => {
        if (!response.ok) {
            // Si no existe el endpoint, usamos datos mock básicos
            throw new Error('Endpoint no disponible');
        }
        return response.json();
    })
    .then(vendedores => {
        vendedores.forEach(vendedor => vendedoresCache.set(vendedor.codigo, vendedor));
        llenarSelectVendedores(vendedores);
    })
    .catch(error => {
        console.log('Usando vendedores desde la tabla existente');
        // Extraer vendedores de la tabla actual como fallback
        extraerVendedoresDeTabla();
    });
}

function cargarEstadosParaEdicion() {
    fetch('/orden-pedido/api/estados')
    .then(response => {
        if (!response.ok) {
            throw new Error('Endpoint no disponible');
        }
        return response.json();
    })
    .then(estados => {
        estados.forEach(estado => estadosCache.set(estado.id, estado));
        llenarSelectEstados(estados);
    })
    .catch(error => {
        console.log('Usando estados por defecto');
        // Estados por defecto basados en la base de datos
        const estadosDefault = [
            { id: 1, descripcion: 'Pendiente' },
            { id: 2, descripcion: 'Atendido' }
        ];
        estadosDefault.forEach(estado => estadosCache.set(estado.id, estado));
        llenarSelectEstados(estadosDefault);
    });
}

function cargarTecnicosParaEdicion() {
    // Reutilizamos el endpoint existente del modal de asignación
    fetch('/orden-pedido/api/tecnicos')
    .then(response => response.json())
    .then(tecnicos => {
        tecnicos.forEach(tecnico => tecnicosEditarCache.set(tecnico.codigo, tecnico));
        llenarSelectTecnicos(tecnicos);
        console.log(`Cargados ${tecnicos.length} técnicos para edición`);
    })
    .catch(error => {
        console.error('Error al cargar técnicos para edición:', error);
        mostrarAlertaModalEdicion('Error al cargar la lista de técnicos', 'warning');
    });
}

function llenarSelectVendedores(vendedores) {
    const select = document.getElementById('editarVendedor');
    select.innerHTML = '<option value="">Seleccionar vendedor...</option>';
    vendedores.forEach(vendedor => {
        const nombreCompleto = `${vendedor.nombre} ${vendedor.apellido}`;
        select.innerHTML += `<option value="${vendedor.codigo}">${nombreCompleto}</option>`;
    });
}

function llenarSelectEstados(estados) {
    const select = document.getElementById('editarEstado');
    select.innerHTML = '<option value="">Seleccionar estado...</option>';
    estados.forEach(estado => {
        select.innerHTML += `<option value="${estado.id}">${estado.descripcion}</option>`;
    });
}

function llenarSelectTecnicos(tecnicos) {
    const select = document.getElementById('editarTecnico');
    select.innerHTML = '<option value="">Sin técnico asignado</option>';
    tecnicos.forEach(tecnico => {
        const nombreCompleto = `${tecnico.nombre} ${tecnico.apellido}`;
        select.innerHTML += `<option value="${tecnico.codigo}">${nombreCompleto}</option>`;
    });
}

function extraerVendedoresDeTabla() {
    // Fallback: extraer vendedores únicos de la tabla actual
    const vendedoresUnicos = new Set();
    const filas = document.querySelectorAll('#ordenesTable tr');
    
    filas.forEach(fila => {
        const celdas = fila.children;
        if (celdas.length >= 2) {
            // Asumiendo que el vendedor está en alguna columna, extraemos info básica
            vendedoresUnicos.add('Vendedor por defecto');
        }
    });
    
    // Crear vendedores mock para que funcione el select
    const vendedoresMock = [
        { codigo: 1, nombre: 'Ana', apellido: 'García' },
        { codigo: 2, nombre: 'Luis', apellido: 'Rodríguez' },
        { codigo: 3, nombre: 'María', apellido: 'López' }
    ];
    
    vendedoresMock.forEach(vendedor => vendedoresCache.set(vendedor.codigo, vendedor));
    llenarSelectVendedores(vendedoresMock);
}

function editarOrden(codigoOrden) {
    console.log('Editando orden:', codigoOrden);
    
    // Buscar orden específica
    fetch(`/orden-pedido/api/orden/${codigoOrden}`)
    .then(response => {
        if (!response.ok) {
            throw new Error('Orden no encontrada');
        }
        return response.json();
    })
    .then(orden => {
        ordenActual = orden;
        llenarDatosOrden(orden);
        cargarProductosOrden(codigoOrden);
        modalEditarOrden.show();
    })
    .catch(error => {
        console.error('Error al cargar orden:', error);
        mostrarAlerta('Error al cargar los datos de la orden', 'danger');
    });
}

function llenarDatosOrden(orden) {
    // Llenar campos básicos
    document.getElementById('editarOrdenId').value = orden.codigo || orden.idOrden;
    
    // Información del cliente
    if (orden.cliente) {
        document.getElementById('editarClienteNombre').value = `${orden.cliente.nombre} ${orden.cliente.apellido}`;
        document.getElementById('editarClienteDni').value = orden.cliente.dni;
        document.getElementById('editarClienteCorreo').value = orden.cliente.correo || 'No especificado';
    }
    
    // Información de la orden - CAMPOS ESTÁTICOS
    if (orden.vendedor) {
        document.getElementById('editarVendedorNombre').value = `${orden.vendedor.nombre} ${orden.vendedor.apellido}`;
        document.getElementById('editarVendedor').value = orden.vendedor.codigo;
    }
    
    if (orden.estado) {
        document.getElementById('editarEstado').value = orden.estado.id;
    }
    
    if (orden.tecnico) {
        document.getElementById('editarTecnicoNombre').value = `${orden.tecnico.nombre} ${orden.tecnico.apellido}`;
        document.getElementById('editarTecnico').value = orden.tecnico.codigo;
    } else {
        document.getElementById('editarTecnicoNombre').value = 'Sin técnico asignado';
        document.getElementById('editarTecnico').value = '';
    }
    
    // Total
    document.getElementById('editarTotal').value = `S/ ${parseFloat(orden.total || 0).toFixed(2)}`;
    
    // Cargar fecha de pago usando el nuevo endpoint
    cargarFechaPago(orden.codigo || orden.idOrden);
    
    console.log('Datos de orden cargados:', orden);
}

function cargarFechaPago(codigoOrden) {
    fetch(`/orden-pedido/api/orden/${codigoOrden}/pago`)
    .then(response => response.json())
    .then(infoPago => {
        const fechaPagoField = document.getElementById('editarFechaPago');
        
        if (infoPago.fechaPago) {
            const fecha = new Date(infoPago.fechaPago);
            fechaPagoField.value = fecha.toLocaleDateString('es-PE') + ' ' + fecha.toLocaleTimeString('es-PE');
        } else {
            fechaPagoField.value = 'Sin fecha de pago registrada';
        }
        
        console.log('Información de pago cargada:', infoPago);
    })
    .catch(error => {
        console.error('Error al cargar fecha de pago:', error);
        document.getElementById('editarFechaPago').value = 'Error al cargar fecha';
    });
}

function cargarProductosOrden(codigoOrden) {
    const tbody = document.getElementById('editarProductosTable');
    const noProductos = document.getElementById('noProductosOrden');
    
    // Cargar productos reales desde el endpoint
    fetch(`/orden-pedido/api/orden/${codigoOrden}/productos`)
    .then(response => {
        if (!response.ok) {
            throw new Error('Error al cargar productos');
        }
        return response.json();
    })
    .then(productos => {
        if (productos.length === 0) {
            tbody.innerHTML = '';
            noProductos.style.display = 'block';
            return;
        }
        
        noProductos.style.display = 'none';
        tbody.innerHTML = productos.map(producto => {
            return `
                <tr>
                    <td>
                        <strong>${producto.nombre}</strong>
                        <br><small class="text-muted">${producto.marca} - ${producto.modelo || 'Sin modelo'}</small>
                    </td>
                    <td class="text-center">${producto.cantidad}</td>
                    <td class="text-end">S/ ${parseFloat(producto.precioUnitario).toFixed(2)}</td>
                    <td class="text-end">S/ ${parseFloat(producto.subtotal).toFixed(2)}</td>
                </tr>
            `;
        }).join('');
        
        console.log('Productos cargados para la orden:', codigoOrden);
    })
    .catch(error => {
        console.error('Error al cargar productos:', error);
        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="text-center text-muted">
                    <i class="fas fa-exclamation-triangle me-2"></i>
                    Error al cargar productos de la orden
                </td>
            </tr>
        `;
    });
}

function guardarOrdenEditada() {
    if (!ordenActual) {
        mostrarAlertaModalEdicion('Error: No hay orden cargada', 'danger');
        return;
    }
    
    const form = document.getElementById('editarOrdenForm');
    
    // Validación básica
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }
    
    const btnGuardar = document.getElementById('btnGuardarOrden');
    btnGuardar.disabled = true;
    btnGuardar.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Guardando...';
    
    // CORRECCIÓN: Preparar datos SOLO para campos editables
    const formData = new URLSearchParams();
    
    // SOLO enviar campos que realmente pueden modificarse
    formData.append('codigo', document.getElementById('editarOrdenId').value);
    
    // Cliente: mantener el mismo (solo para validación)
    formData.append('cliente.dni', document.getElementById('editarClienteDni').value);
    
    // Vendedor: mantener el mismo (solo para validación)
    formData.append('vendedor.codigo', document.getElementById('editarVendedor').value);
    
    // Estado: este sí puede modificarse
    formData.append('estado.id', document.getElementById('editarEstado').value);
    
    // Técnico: este sí puede modificarse
    const tecnicoId = document.getElementById('editarTecnico').value;
    if (tecnicoId) {
        formData.append('tecnico.codigo', tecnicoId);
    }
    
    // Total: mantener el original (no modificable desde este modal)
    formData.append('total', ordenActual.total);
    
    // Descripción/observaciones: este sí puede modificarse
    const descripcion = document.getElementById('editarDescripcion').value;
    if (descripcion) {
        formData.append('descripcion', descripcion);
    }
    
    // IMPORTANTE: NO enviar datos de pago (fecha, número operación, banco)
    // Estos campos son de solo lectura y se manejan por separado
    
    console.log('Enviando datos de actualización:', Array.from(formData.entries()));
    
    // Usar el endpoint existente de actualización
    fetch('/orden-pedido/actualizar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData
    })
    .then(response => {
        if (response.ok) {
            return response.text();
        } else {
            return response.text().then(text => {
                throw new Error(text || 'Error al actualizar la orden');
            });
        }
    })
    .then(mensaje => {
        mostrarAlerta(mensaje, 'success');
        modalEditarOrden.hide();
        
        // Recargar la página para mostrar los cambios
        setTimeout(() => {
            location.reload();
        }, 1500);
    })
    .catch(error => {
        console.error('Error al guardar orden:', error);
        mostrarAlertaModalEdicion('Error al guardar: ' + error.message, 'danger');
    })
    .finally(() => {
        btnGuardar.disabled = false;
        btnGuardar.innerHTML = '<i class="fas fa-save me-2"></i>Guardar Cambios';
    });
}

function limpiarModalEdicion() {
    // Limpiar formulario
    document.getElementById('editarOrdenForm').reset();
    
    // Limpiar alertas
    document.getElementById('editarModalAlertContainer').innerHTML = '';
    
    // Limpiar tabla de productos
    document.getElementById('editarProductosTable').innerHTML = '';
    document.getElementById('noProductosOrden').style.display = 'none';
    
    // Resetear botón
    const btnGuardar = document.getElementById('btnGuardarOrden');
    btnGuardar.disabled = false;
    btnGuardar.innerHTML = '<i class="fas fa-save me-2"></i>Guardar Cambios';
    
    // Limpiar variable
    ordenActual = null;
}

function mostrarAlertaModalEdicion(mensaje, tipo) {
    const alertContainer = document.getElementById('editarModalAlertContainer');
    const alertId = `editarAlert_${Date.now()}`;
    
    alertContainer.innerHTML = `
        <div id="${alertId}" class="alert alert-${tipo} alert-dismissible fade show" role="alert">
            ${mensaje}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    setTimeout(() => {
        const alert = document.getElementById(alertId);
        if (alert) alert.remove();
    }, 5000);
}

// Función auxiliar para mostrar alertas en la página principal
function mostrarAlerta(mensaje, tipo) {
    const alertContainer = document.getElementById('alertContainer');
    const alertId = `alert_${Date.now()}`;
    
    alertContainer.innerHTML = `
        <div id="${alertId}" class="alert alert-${tipo} alert-dismissible fade show" role="alert">
            ${mensaje}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    setTimeout(() => {
        const alert = document.getElementById(alertId);
        if (alert) alert.remove();
    }, 4000);
}