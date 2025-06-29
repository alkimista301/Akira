// Script de búsqueda mejorada para orden pedido
		document.addEventListener('DOMContentLoaded', function() {
		    inicializarBusquedaMejorada();
		});

		function inicializarBusquedaMejorada() {
		    const searchInput = document.querySelector('.search-input');
		    const searchBtn = document.querySelector('.search-btn');
		    
		    // Crear botón de limpiar si no existe
		    let clearBtn = document.querySelector('.clear-btn');
		    if (!clearBtn) {
		        clearBtn = document.createElement('button');
		        clearBtn.className = 'search-btn clear-btn';
		        clearBtn.innerHTML = '<i class="fas fa-times"></i>';
		        clearBtn.title = 'Limpiar búsqueda';
		        clearBtn.style.display = 'none';
		        searchBtn.parentNode.insertBefore(clearBtn, searchBtn.nextSibling);
		    }
		    
		    // Crear contenedor de resultados de búsqueda
		    let searchResults = document.getElementById('searchResults');
		    if (!searchResults) {
		        searchResults = document.createElement('div');
		        searchResults.id = 'searchResults';
		        searchResults.className = 'search-results-container';
		        searchResults.style.cssText = `
		            margin-bottom: 15px;
		            padding: 10px;
		            background: #f8f9fa;
		            border-radius: 5px;
		            border-left: 4px solid #007bff;
		            display: none;
		        `;
		        
		        const searchContainer = document.querySelector('.search-container');
		        searchContainer.parentNode.insertBefore(searchResults, searchContainer.nextSibling);
		    }
		    
		    // Eventos
		    searchBtn.addEventListener('click', realizarBusqueda);
		    clearBtn.addEventListener('click', limpiarBusqueda);
		    
		    // Búsqueda con Enter
		    searchInput.addEventListener('keypress', function(e) {
		        if (e.key === 'Enter') {
		            e.preventDefault();
		            realizarBusqueda();
		        }
		    });
		    
		    // Mostrar/ocultar botón limpiar según contenido
		    searchInput.addEventListener('input', function() {
		        if (this.value.trim()) {
		            clearBtn.style.display = 'inline-block';
		        } else {
		            clearBtn.style.display = 'none';
		            // Si se borra el contenido, mostrar todas las órdenes
		            limpiarBusqueda();
		        }
		    });
		}

		function realizarBusqueda() {
		    const termino = document.querySelector('.search-input').value.trim();
		    const searchResults = document.getElementById('searchResults');
		    const searchBtn = document.querySelector('.search-btn');
		    
		    // Mostrar estado de carga
		    searchBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
		    searchBtn.disabled = true;
		    
		    // Mostrar mensaje de búsqueda
		    searchResults.style.display = 'block';
		    searchResults.innerHTML = `
		        <div class="d-flex align-items-center">
		            <i class="fas fa-search me-2"></i>
		            <span>Buscando...</span>
		        </div>
		    `;
		    
		    fetch('/ordenPedido/buscar', {
		        method: 'POST',
		        headers: {
		            'Content-Type': 'application/x-www-form-urlencoded',
		        },
		        body: `codigo=${encodeURIComponent(termino)}`
		    })
		    .then(response => response.json())
		    .then(data => {
		        console.log('Resultado de búsqueda:', data);
		        actualizarTablaOrdenes(data.ordenes);
		        mostrarResultadoBusqueda(data);
		    })
		    .catch(error => {
		        console.error('Error en búsqueda:', error);
		        mostrarError('Error al realizar la búsqueda');
		    })
		    .finally(() => {
		        // Restaurar botón
		        searchBtn.innerHTML = '<i class="fas fa-search"></i>';
		        searchBtn.disabled = false;
		    });
		}

		function limpiarBusqueda() {
		    const searchInput = document.querySelector('.search-input');
		    const clearBtn = document.querySelector('.clear-btn');
		    const searchResults = document.getElementById('searchResults');
		    
		    // Limpiar input
		    searchInput.value = '';
		    clearBtn.style.display = 'none';
		    
		    // Ocultar resultados
		    searchResults.style.display = 'none';
		    
		    // Cargar todas las órdenes
		    fetch('/ordenPedido/limpiarBusqueda', {
		        method: 'POST',
		        headers: {
		            'Content-Type': 'application/x-www-form-urlencoded',
		        }
		    })
		    .then(response => response.json())
		    .then(data => {
		        console.log('Búsqueda limpiada:', data);
		        actualizarTablaOrdenes(data.ordenes);
		        
		        // Mostrar mensaje temporal
		        searchResults.style.display = 'block';
		        searchResults.style.borderLeftColor = '#28a745';
		        searchResults.innerHTML = `
		            <div class="d-flex align-items-center">
		                <i class="fas fa-check me-2 text-success"></i>
		                <span>${data.mensaje}</span>
		            </div>
		        `;
		        
		        setTimeout(() => {
		            searchResults.style.display = 'none';
		        }, 2000);
		    })
		    .catch(error => {
		        console.error('Error al limpiar búsqueda:', error);
		        mostrarError('Error al cargar todas las órdenes');
		    });
		}

		function actualizarTablaOrdenes(ordenes) {
		    const tbody = document.getElementById('ordenesTable');
		    
		    if (!tbody) {
		        console.error('Tabla de órdenes no encontrada');
		        return;
		    }
		    
		    if (ordenes.length === 0) {
		        tbody.innerHTML = `
		            <tr>
		                <td colspan="8" class="text-center text-muted py-4">
		                    <i class="fas fa-search me-2"></i>
		                    No se encontraron órdenes con los criterios de búsqueda
		                </td>
		            </tr>
		        `;
		        return;
		    }
		    
		    tbody.innerHTML = ordenes.map(orden => {
		        const clienteNombre = orden.cliente ? 
		            `${orden.cliente.nombre} ${orden.cliente.apellido}` : 'Sin cliente';
		        const clienteDni = orden.cliente ? orden.cliente.dni : 'Sin DNI';
		        const fecha = orden.fecha ? new Date(orden.fecha).toLocaleDateString('es-PE') : 'Sin fecha';
		        const total = orden.total ? `S/ ${parseFloat(orden.total).toFixed(2)}` : 'S/ 0.00';
		        const estado = orden.estado ? orden.estado.descripcion : 'Sin estado';
		        const estadoClass = estado === 'Pendiente' ? 'estado-pendiente' : 'estado-atendido';
		        const tecnico = orden.tecnico ? 
		            `${orden.tecnico.nombre} ${orden.tecnico.apellido}` : 'Sin asignar';
		        const tecnicoClass = orden.tecnico ? '' : 'text-muted';
		        
		        return `
		            <tr>
		                <td>${orden.codigo || orden.idOrden}</td>
		                <td>${clienteNombre}</td>
		                <td>${clienteDni}</td>
		                <td>${fecha}</td>
		                <td>${total}</td>
		                <td class="${estadoClass}">${estado}</td>
		                <td class="${tecnicoClass}">${tecnico}</td>
		                <td class="action-cell">
		                    <button class="btn-edit" onclick="editarOrden(${orden.codigo || orden.idOrden})" title="Editar">
		                        <i class="fas fa-edit"></i>
		                    </button>
		                </td>
		            </tr>
		        `;
		    }).join('');
		}

		function mostrarResultadoBusqueda(data) {
		    const searchResults = document.getElementById('searchResults');
		    
		    let colorBorde = '#007bff';
		    let icono = 'fa-search';
		    let colorIcono = 'text-primary';
		    
		    if (data.total === 0) {
		        colorBorde = '#ffc107';
		        icono = 'fa-exclamation-triangle';
		        colorIcono = 'text-warning';
		    } else if (data.total > 0) {
		        colorBorde = '#28a745';
		        icono = 'fa-check-circle';
		        colorIcono = 'text-success';
		    }
		    
		    searchResults.style.borderLeftColor = colorBorde;
		    searchResults.style.display = 'block';
		    searchResults.innerHTML = `
		        <div class="d-flex align-items-center justify-content-between">
		            <div class="d-flex align-items-center">
		                <i class="fas ${icono} me-2 ${colorIcono}"></i>
		                <div>
		                    <strong>${data.tipo}:</strong> ${data.mensaje}
		                    ${data.termino ? `<br><small class="text-muted">Término: "${data.termino}"</small>` : ''}
		                </div>
		            </div>
		            <div class="text-end">
		                <span class="badge bg-primary">${data.total} resultado(s)</span>
		            </div>
		        </div>
		    `;
		}

		function mostrarError(mensaje) {
		    const searchResults = document.getElementById('searchResults');
		    searchResults.style.display = 'block';
		    searchResults.style.borderLeftColor = '#dc3545';
		    searchResults.innerHTML = `
		        <div class="d-flex align-items-center">
		            <i class="fas fa-exclamation-circle me-2 text-danger"></i>
		            <span>${mensaje}</span>
		        </div>
		    `;
		    
		    setTimeout(() => {
		        searchResults.style.display = 'none';
		    }, 5000);
		}

		// Función auxiliar para mostrar alertas generales
		function mostrarAlerta(mensaje, tipo) {
		    const alertContainer = document.getElementById('alertContainer');
		    if (!alertContainer) return;
		    
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

		// Función placeholder para el modal de generar pedido (próximo paso)
		function abrirModalGenerarPedido() {
		    const modal = new bootstrap.Modal(document.getElementById('generarPedidoModal'));
		    modal.show();
		}
		// Función modificada para generar las filas de la tabla (dentro de ordenPedido.html)
		function generarFilasTabla(ordenes) {
		    return ordenes.map(orden => {
		        const clienteNombre = orden.cliente ? 
		            `${orden.cliente.nombre} ${orden.cliente.apellido}` : 'Sin cliente';
		        const clienteDni = orden.cliente ? orden.cliente.dni : 'Sin DNI';
		        const fecha = orden.fecha ? new Date(orden.fecha).toLocaleDateString('es-PE') : 'Sin fecha';
		        const total = orden.total ? `S/ ${parseFloat(orden.total).toFixed(2)}` : 'S/ 0.00';
		        const estado = orden.estado ? orden.estado.descripcion : 'Sin estado';
		        const estadoClass = estado === 'Pendiente' ? 'estado-pendiente' : 'estado-atendido';
		        
		        // LÓGICA CORREGIDA: Solo mostrar técnico si el estado es 'Atendido'
		        const tecnico = (estado === 'Atendido' && orden.tecnico) ? 
		            `${orden.tecnico.nombre} ${orden.tecnico.apellido}` : 'Sin asignar';
		        const tecnicoClass = (estado === 'Atendido' && orden.tecnico) ? '' : 'text-muted';
		        
		        return `
		            <tr>
		                <td>${orden.codigo || orden.idOrden}</td>
		                <td>${clienteNombre}</td>
		                <td>${clienteDni}</td>
		                <td>${fecha}</td>
		                <td>${total}</td>
		                <td class="${estadoClass}">${estado}</td>
		                <td class="${tecnicoClass}">${tecnico}</td>
		                <td class="action-cell">
		                    <button class="btn-edit" onclick="editarOrden(${orden.codigo || orden.idOrden})" title="Editar">
		                        <i class="fas fa-edit"></i>
		                    </button>
		                </td>
		            </tr>
		        `;
		    }).join('');
		}