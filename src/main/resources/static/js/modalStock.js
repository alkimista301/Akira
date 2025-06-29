// Variables globales simplificadas
let productosCache = new Map();
let categoriasCache = new Map();
let marcasCache = new Map();
let modalProducto, modalEliminar;
let modoEdicion = false;

document.addEventListener('DOMContentLoaded', function() {
    inicializar();
});

function inicializar() {
    // Inicializar modales
    modalProducto = new bootstrap.Modal(document.getElementById('productoModal'));
    modalEliminar = new bootstrap.Modal(document.getElementById('eliminarModal'));
    
    // Cargar productos desde la tabla existente (evita AJAX innecesario)
    cargarProductosDesdeTabla();
    
    // Cargar categorías y marcas una sola vez
    cargarCategorias();
    cargarMarcas();
    
    // Configurar eventos
    configurarEventos();
}

function cargarProductosDesdeTabla() {
    const filas = document.querySelectorAll('#productosTable tr[data-codigo]');
    filas.forEach(fila => {
        const codigo = parseInt(fila.dataset.codigo);
        const producto = {
            codigo: codigo,
            nombre: fila.children[1].textContent,
            cantidad: parseInt(fila.children[2].textContent),
            categoria: fila.children[4].textContent,
            marca: fila.children[5].textContent,
            modelo: fila.children[6].textContent,
            precio: parseFloat(fila.children[7].textContent.replace('S/ ', ''))
        };
        productosCache.set(codigo, producto);
    });
}

function configurarEventos() {
    // Búsqueda con Enter
    document.getElementById('searchInput').addEventListener('keypress', (e) => {
        if(e.key === 'Enter') buscarProducto();
    });
    
    // Contador de caracteres simplificado
    const descripcion = document.getElementById('productoDescripcion');
    if (descripcion) {
        descripcion.addEventListener('input', () => {
            const contador = document.getElementById('contadorCaracteres');
            contador.textContent = `${descripcion.value.length}/500 caracteres`;
        });
    }
    
    // Mostrar información de categoría al seleccionar
    const selectCategoria = document.getElementById('productoCategoria');
    if (selectCategoria) {
        selectCategoria.addEventListener('change', function() {
            mostrarInfoCategoria(this.value);
        });
    }
    
    // Limpiar modal al cerrarse
    document.getElementById('productoModal').addEventListener('hidden.bs.modal', limpiarFormulario);
}

function cargarCategorias() {
    fetch('/categoria/api/todas')
    .then(response => response.json())
    .then(categorias => {
        categorias.forEach(cat => categoriasCache.set(cat.id || cat.idCategoria, cat));
        llenarSelectCategorias(categorias);
    })
    .catch(() => {
        // Fallback - usar categorías por defecto si falla
        const categoriasPorDefecto = [
            { id: 1, nombre: 'Computadoras Gamer', estante: 'B1-B2' },
            { id: 2, nombre: 'Laptops', estante: 'C1-C4' },
            { id: 3, nombre: 'Accesorios', estante: 'D1-D6' }
        ];
        llenarSelectCategorias(categoriasPorDefecto);
    });
}

function cargarMarcas() {
    fetch('/marca/api/todas')
    .then(response => response.json())
    .then(marcas => {
        marcas.forEach(marca => marcasCache.set(marca.idMarca, marca));
        llenarSelectMarcas(marcas);
    })
    .catch(() => {
        console.log('Error al cargar marcas, usando marcas por defecto');
        // Fallback - usar marcas por defecto si falla
        const marcasPorDefecto = [
            { idMarca: 1, nombre: 'ASUS' },
            { idMarca: 2, nombre: 'HP' },
            { idMarca: 3, nombre: 'Dell' },
            { idMarca: 4, nombre: 'Logitech' },
            { idMarca: 5, nombre: 'AMD' },
            { idMarca: 6, nombre: 'Intel' },
            { idMarca: 7, nombre: 'NVIDIA' },
            { idMarca: 8, nombre: 'Kingston' },
            { idMarca: 9, nombre: 'Corsair' },
            { idMarca: 10, nombre: 'Samsung' }
        ];
        llenarSelectMarcas(marcasPorDefecto);
    });
}

function llenarSelectCategorias(categorias) {
    const select = document.getElementById('productoCategoria');
    select.innerHTML = '<option value="">Seleccionar categoría...</option>';
    categorias.forEach(cat => {
        select.innerHTML += `<option value="${cat.id || cat.idCategoria}">${cat.nombre}</option>`;
    });
}

function llenarSelectMarcas(marcas) {
    const select = document.getElementById('productoMarca');
    select.innerHTML = '<option value="">Seleccionar marca...</option>';
    marcas.forEach(marca => {
        select.innerHTML += `<option value="${marca.idMarca}">${marca.nombre}</option>`;
    });
}

function mostrarInfoCategoria(categoriaId) {
    const categoriaInfo = document.getElementById('categoriaInfo');
    const categoriaEstante = document.getElementById('categoriaEstante');
    
    if (categoriaId && categoriasCache.has(parseInt(categoriaId))) {
        const categoria = categoriasCache.get(parseInt(categoriaId));
        categoriaEstante.textContent = `Ubicación: ${categoria.estante || 'No asignado'}`;
        categoriaInfo.style.display = 'block';
    } else {
        categoriaInfo.style.display = 'none';
    }
}

// === FUNCIONES PRINCIPALES ===

function buscarProducto() {
    const termino = document.getElementById('searchInput').value.trim();
    if (!termino) return mostrarAlerta('Ingresa un término de búsqueda', 'warning');
    
    mostrarCargando();
    
    fetch('/stock/buscar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `codigo=${encodeURIComponent(termino)}`
    })
    .then(response => response.json())
    .then(productos => {
        actualizarTabla(productos);
        mostrarAlerta(`${productos.length} producto(s) encontrado(s)`, 'success');
    })
    .catch(() => {
        mostrarAlerta('Error en la búsqueda', 'danger');
        ocultarCargando();
    });
}

function limpiarBusqueda() {
    document.getElementById('searchInput').value = '';
    location.reload();
}

function abrirModalNuevoProducto() {
    modoEdicion = false;
    document.getElementById('modalTitle').textContent = 'Nuevo Producto';
    limpiarFormulario();
    modalProducto.show();
}

function editarProducto(codigo) {
    modoEdicion = true;
    document.getElementById('modalTitle').textContent = 'Editar Producto';
    
    // Buscar en caché primero
    const producto = productosCache.get(codigo);
    if (producto) {
        llenarFormulario(producto);
        modalProducto.show();
    } else {
        // Si no está en caché, buscar en servidor
        fetch(`/stock/api/producto/${codigo}`)
        .then(response => response.json())
        .then(producto => {
            llenarFormulario(producto);
            modalProducto.show();
        })
        .catch(() => mostrarAlerta('Producto no encontrado', 'danger'));
    }
}

function llenarFormulario(producto) {
    document.getElementById('productoCodigo').value = producto.codigo || producto.idProducto;
    document.getElementById('productoNombre').value = producto.nombre || '';
    document.getElementById('productoCantidad').value = producto.cantidad || producto.cantidadStock || 0;
    document.getElementById('productoPrecio').value = producto.precio || 0;
    document.getElementById('productoDescripcion').value = producto.descripcion || '';
    document.getElementById('productoModelo').value = producto.modelo || '';
    
    // Asignar categoría
    if (producto.categoria) {
        const categoriaId = producto.categoria.id || producto.categoria.idCategoria;
        document.getElementById('productoCategoria').value = categoriaId;
        mostrarInfoCategoria(categoriaId);
    }
    
    // Asignar marca
    if (producto.marca) {
        const marcaId = producto.marca.idMarca;
        document.getElementById('productoMarca').value = marcaId;
    }
}

function guardarProducto() {
    const form = document.getElementById('productoForm');
    const formData = new FormData(form);
    const btn = document.querySelector('#productoModal .btn-primary-custom');
    
    // Validación básica
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }
    
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Guardando...';
    
    const url = modoEdicion ? '/producto/actualizar' : '/producto/guardar';
    
    fetch(url, { method: 'POST', body: formData })
    .then(response => response.text())
    .then(() => {
        mostrarAlerta('Producto guardado exitosamente', 'success');
        modalProducto.hide();
        setTimeout(() => location.reload(), 1000);
    })
    .catch(() => {
        mostrarAlerta('Error al guardar', 'danger');
    })
    .finally(() => {
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-save me-2"></i>Guardar Producto';
    });
}

function eliminarProducto(codigo) {
    const producto = productosCache.get(codigo);
    const nombre = producto ? producto.nombre : 'Producto';
    
    document.getElementById('productoEliminar').textContent = nombre;
    document.getElementById('confirmarEliminar').onclick = () => ejecutarEliminacion(codigo);
    modalEliminar.show();
}

function ejecutarEliminacion(codigo) {
    const btn = document.getElementById('confirmarEliminar');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Eliminando...';
    
    fetch(`/producto/eliminar/${codigo}`, { method: 'POST' })
    .then(() => {
        modalEliminar.hide();
        mostrarAlerta('Producto eliminado', 'success');
        setTimeout(() => location.reload(), 1000);
    })
    .catch(() => mostrarAlerta('Error al eliminar', 'danger'))
    .finally(() => {
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-trash-alt me-2"></i>Eliminar Producto';
    });
}

function descargarStock() {
    fetch('/stock/api/todos')
    .then(response => response.json())
    .then(productos => {
        const csv = generarCSV(productos);
        descargarArchivo(csv, `stock_${new Date().toISOString().split('T')[0]}.csv`);
        mostrarAlerta('Descarga iniciada', 'success');
    })
    .catch(() => mostrarAlerta('Error al descargar', 'danger'));
}

// === FUNCIONES AUXILIARES ===

function mostrarCargando() {
    document.getElementById('productosTable').innerHTML = 
        '<tr><td colspan="10" class="text-center"><i class="fas fa-spinner fa-spin"></i> Cargando...</td></tr>';
}

function ocultarCargando() {
    location.reload();
}

function actualizarTabla(productos) {
    const tbody = document.getElementById('productosTable');
    if (productos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" class="text-center">No se encontraron productos</td></tr>';
        return;
    }
    
    tbody.innerHTML = productos.map(producto => {
        const codigo = producto.codigo || producto.idProducto;
        const cantidad = producto.cantidad || producto.cantidadStock || 0;
        const categoria = producto.categoria?.nombre || 'Sin categoría';
        const marca = producto.marca?.nombre || 'Sin marca';
        const modelo = producto.modelo || 'Sin modelo';
        const precio = parseFloat(producto.precio || 0).toFixed(2);
        
        return `
            <tr data-codigo="${codigo}">
                <td>${codigo}</td>
                <td>${producto.nombre}</td>
                <td>${cantidad}</td>
                <td>${producto.categoria?.estante || 'No asignado'}</td>
                <td>${categoria}</td>
                <td>${marca}</td>
                <td>${modelo}</td>
                <td>S/ ${precio}</td>
                <td class="action-cell">
                    <button class="btn-edit" onclick="editarProducto(${codigo})" title="Editar">
                        <i class="fas fa-edit"></i>
                    </button>
                </td>
                <td class="action-cell">
                    <button class="btn-delete" onclick="eliminarProducto(${codigo})" title="Eliminar">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function generarCSV(productos) {
    const headers = 'Código,Producto,Cantidad,Ubicación,Categoría,Marca,Modelo,Precio\n';
    const filas = productos.map(p => {
        const codigo = p.codigo || p.idProducto;
        const cantidad = p.cantidad || p.cantidadStock || 0;
        const categoria = p.categoria?.nombre || 'Sin categoría';
        const estante = p.categoria?.estante || 'No asignado';
        const marca = p.marca?.nombre || 'Sin marca';
        const modelo = p.modelo || 'Sin modelo';
        const precio = parseFloat(p.precio || 0).toFixed(2);
        return `${codigo},"${p.nombre}",${cantidad},"${estante}","${categoria}","${marca}","${modelo}",${precio}`;
    }).join('\n');
    
    return headers + filas;
}

function descargarArchivo(contenido, nombreArchivo) {
    const blob = new Blob([contenido], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = nombreArchivo;
    link.click();
    URL.revokeObjectURL(link.href);
}

function limpiarFormulario() {
    document.getElementById('productoForm').reset();
    document.querySelectorAll('.is-valid, .is-invalid').forEach(el => {
        el.classList.remove('is-valid', 'is-invalid');
    });
    document.getElementById('contadorCaracteres').textContent = '0/500 caracteres';
    document.getElementById('categoriaInfo').style.display = 'none';
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
    }, 3000);
}