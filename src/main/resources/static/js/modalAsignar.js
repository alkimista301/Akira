// Variables globales
let modalAsignar;
let tecnicoSeleccionado = null;
let tecnicosCache = new Map();
let ordenesPendientesCache = [];

document.addEventListener('DOMContentLoaded', function() {
    inicializarModal();
    inicializarEventosGenerales();
});

function inicializarModal() {
    modalAsignar = new bootstrap.Modal(document.getElementById('asignarModal'));
    
    // Configurar eventos
    document.getElementById('tecnicoSelect').addEventListener('change', function() {
        if (this.value) {
            seleccionarTecnico(parseInt(this.value));
            document.getElementById('tecnicoCodigoInput').value = this.value;
        } else {
            limpiarTecnicoSeleccionado();
        }
        validarFormulario();
    });
    
    document.getElementById('tecnicoCodigoInput').addEventListener('input', function() {
        if (!this.value) {
            document.getElementById('tecnicoSelect').value = '';
            limpiarTecnicoSeleccionado();
            validarFormulario();
        }
    });
    
    // Limpiar modal al cerrarse
    document.getElementById('asignarModal').addEventListener('hidden.bs.modal', function() {
        limpiarModal();
    });
}

function inicializarEventosGenerales() {
    // Botones de editar - REMOVIDO PARA EVITAR CONFLICTO
    // Ya no manejamos los eventos aquí, se manejan desde modalEditarOrden.js
    
    // Botón de búsqueda (funcionalidad existente)
    const searchBtn = document.querySelector('.search-btn');
    if (searchBtn) {
        searchBtn.addEventListener('click', function() {
            const codigo = document.querySelector('.search-input').value;
            if(codigo.trim()) {
                alert(`Buscando pedidos con código: ${codigo}`);
            } else {
                alert('Por favor ingrese un código para buscar');
            }
        });
    }
    
    // Búsqueda con Enter (funcionalidad existente)
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if(e.key === 'Enter') {
                document.querySelector('.search-btn').click();
            }
        });
    }
    
    // Botón Generar Orden Pedido (funcionalidad existente)
    const btnGenerar = document.querySelector('.btn-generar');
    if (btnGenerar) {
        btnGenerar.addEventListener('click', function() {
            alert('Generando nueva orden de pedido...');
        });
    }
}

function abrirModalAsignar() {
    // Cargar datos necesarios
    cargarTecnicos();
    cargarOrdenesPendientes();
    modalAsignar.show();
}

function cargarTecnicos() {
    fetch('/ordenPedido/api/tecnicos')
    .then(response => response.json())
    .then(tecnicos => {
        // Guardar en caché
        tecnicos.forEach(tecnico => tecnicosCache.set(tecnico.codigo, tecnico));
        
        // Llenar select
        const select = document.getElementById('tecnicoSelect');
        select.innerHTML = '<option value="">Seleccionar técnico...</option>';
        tecnicos.forEach(tecnico => {
            select.innerHTML += `<option value="${tecnico.codigo}">${tecnico.nombre} ${tecnico.apellido}</option>`;
        });
        
        console.log(`Cargados ${tecnicos.length} técnicos`);
    })
    .catch(error => {
        console.error('Error al cargar técnicos:', error);
        mostrarAlertaModal('Error al cargar la lista de técnicos', 'danger');
    });
}

function cargarOrdenesPendientes() {
    fetch('/ordenPedido/api/pendientes')
    .then(response => response.json())
    .then(ordenes => {
        ordenesPendientesCache = ordenes;
        mostrarOrdenesPendientes(ordenes);
        console.log(`Cargadas ${ordenes.length} órdenes pendientes`);
    })
    .catch(error => {
        console.error('Error al cargar órdenes pendientes:', error);
        mostrarAlertaModal('Error al cargar órdenes pendientes', 'danger');
    });
}

function mostrarOrdenesPendientes(ordenes) {
    const tbody = document.getElementById('ordenesPendientesTable');
    const noOrdenes = document.getElementById('noOrdenesPendientes');
    
    if (ordenes.length === 0) {
        tbody.innerHTML = '';
        noOrdenes.style.display = 'block';
        return;
    }
    
    noOrdenes.style.display = 'none';
    tbody.innerHTML = ordenes.map(orden => {
        const clienteNombre = orden.cliente ? `${orden.cliente.nombre} ${orden.cliente.apellido}` : 'Sin cliente';
        const fecha = new Date(orden.fecha).toLocaleDateString('es-PE');
        const total = parseFloat(orden.total || 0).toFixed(2);
        
        return `
            <tr>
                <td>
                    <input type="checkbox" class="orden-checkbox" value="${orden.codigo}" onchange="validarFormulario()">
                </td>
                <td>${orden.codigo}</td>
                <td>${clienteNombre}</td>
                <td>${fecha}</td>
                <td>S/ ${total}</td>
            </tr>
        `;
    }).join('');
}

function buscarTecnicoPorCodigo() {
    const codigo = document.getElementById('tecnicoCodigoInput').value.trim();
    if (!codigo) {
        mostrarAlertaModal('Ingrese un código para buscar', 'warning');
        return;
    }
    
    fetch(`/ordenPedido/api/tecnico/${codigo}`)
    .then(response => {
        if (response.ok) {
            return response.json();
        } else if (response.status === 404) {
            throw new Error('Técnico no encontrado');
        } else {
            throw new Error('Error en la búsqueda');
        }
    })
    .then(tecnico => {
        seleccionarTecnico(tecnico.codigo, tecnico);
        document.getElementById('tecnicoSelect').value = tecnico.codigo;
        mostrarAlertaModal(`Técnico encontrado: ${tecnico.nombre} ${tecnico.apellido}`, 'success');
        validarFormulario();
    })
    .catch(error => {
        console.error('Error al buscar técnico:', error);
        mostrarAlertaModal(error.message, 'danger');
        limpiarTecnicoSeleccionado();
        validarFormulario();
    });
}

function seleccionarTecnico(codigo, tecnicoData = null) {
    const tecnico = tecnicoData || tecnicosCache.get(codigo);
    if (tecnico) {
        tecnicoSeleccionado = tecnico;
        document.getElementById('tecnicoNombre').textContent = `${tecnico.nombre} ${tecnico.apellido}`;
        document.getElementById('tecnicoDni').textContent = tecnico.dni;
        document.getElementById('tecnicoInfo').style.display = 'block';
    }
}

function limpiarTecnicoSeleccionado() {
    tecnicoSeleccionado = null;
    document.getElementById('tecnicoInfo').style.display = 'none';
}

function toggleAllOrdenes() {
    const selectAll = document.getElementById('selectAllOrdenes');
    const checkboxes = document.querySelectorAll('.orden-checkbox');
    
    checkboxes.forEach(checkbox => {
        checkbox.checked = selectAll.checked;
    });
    
    validarFormulario();
}

function validarFormulario() {
    const btnAsignar = document.getElementById('btnAsignar');
    const ordenesSeleccionadas = document.querySelectorAll('.orden-checkbox:checked');
    
    const tieneTecnico = tecnicoSeleccionado !== null;
    const tieneOrdenes = ordenesSeleccionadas.length > 0;
    
    btnAsignar.disabled = !(tieneTecnico && tieneOrdenes);
    
    // Actualizar texto del botón
    if (tieneOrdenes) {
        btnAsignar.innerHTML = `<i class="fas fa-user-check me-2"></i>Asignar a ${ordenesSeleccionadas.length} orden(es)`;
    } else {
        btnAsignar.innerHTML = `<i class="fas fa-user-check me-2"></i>Asignar Técnico`;
    }
}

function asignarTecnico() {
    if (!tecnicoSeleccionado) {
        mostrarAlertaModal('Debe seleccionar un técnico', 'warning');
        return;
    }
    
    const ordenesSeleccionadas = Array.from(document.querySelectorAll('.orden-checkbox:checked'))
                                      .map(checkbox => parseInt(checkbox.value));
    
    if (ordenesSeleccionadas.length === 0) {
        mostrarAlertaModal('Debe seleccionar al menos una orden', 'warning');
        return;
    }
    
    const btnAsignar = document.getElementById('btnAsignar');
    btnAsignar.disabled = true;
    btnAsignar.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Asignando...';
    
    // Preparar datos para enviar
    const formData = new URLSearchParams();
    formData.append('tecnicoId', tecnicoSeleccionado.codigo);
    ordenesSeleccionadas.forEach(ordenId => {
        formData.append('ordenesIds', ordenId);
    });
    
    fetch('/ordenPedido/asignar', {
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
                throw new Error(text || 'Error en la asignación');
            });
        }
    })
    .then(mensaje => {
        mostrarAlerta(mensaje, 'success');
        modalAsignar.hide();
        
        // Recargar la página para mostrar los cambios
        setTimeout(() => {
            location.reload();
        }, 1500);
    })
    .catch(error => {
        console.error('Error al asignar técnico:', error);
        mostrarAlertaModal('Error al asignar técnico: ' + error.message, 'danger');
    })
    .finally(() => {
        btnAsignar.disabled = false;
        btnAsignar.innerHTML = '<i class="fas fa-user-check me-2"></i>Asignar Técnico';
    });
}

function limpiarModal() {
    // Limpiar formulario
    document.getElementById('tecnicoCodigoInput').value = '';
    document.getElementById('tecnicoSelect').value = '';
    
    // Limpiar técnico seleccionado
    limpiarTecnicoSeleccionado();
    
    // Limpiar checkboxes
    document.querySelectorAll('.orden-checkbox').forEach(checkbox => {
        checkbox.checked = false;
    });
    document.getElementById('selectAllOrdenes').checked = false;
    
    // Limpiar alertas
    document.getElementById('modalAlertContainer').innerHTML = '';
    
    // Resetear botón
    const btnAsignar = document.getElementById('btnAsignar');
    btnAsignar.disabled = true;
    btnAsignar.innerHTML = '<i class="fas fa-user-check me-2"></i>Asignar Técnico';
    
    // Limpiar variables
    tecnicoSeleccionado = null;
    ordenesPendientesCache = [];
}

function mostrarAlertaModal(mensaje, tipo) {
    const alertContainer = document.getElementById('modalAlertContainer');
    const alertId = `modalAlert_${Date.now()}`;
    
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