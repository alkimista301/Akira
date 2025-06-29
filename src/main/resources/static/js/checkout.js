let carritoCheckout = [];
        let totalGeneral = 0;

        document.addEventListener('DOMContentLoaded', function() {
            cargarProductosCheckout();
        });

        function cargarProductosCheckout() {
            // Obtener carrito desde localStorage
            carritoCheckout = JSON.parse(localStorage.getItem('carrito')) || [];
            console.log('Carrito cargado en checkout:', carritoCheckout);

            if (carritoCheckout.length === 0) {
                mostrarCarritoVacio();
            } else {
                mostrarProductosCheckout();
            }
        }

        function mostrarCarritoVacio() {
            document.getElementById('carritoVacioMsg').style.display = 'block';
            document.getElementById('tablaProductosCheckout').style.display = 'none';
            document.getElementById('btnConfirmarPedido').disabled = true;
            actualizarResumen();
        }

        function mostrarProductosCheckout() {
            document.getElementById('carritoVacioMsg').style.display = 'none';
            document.getElementById('tablaProductosCheckout').style.display = 'block';
            
            actualizarTablaProductos();
            actualizarResumen();
            document.getElementById('btnConfirmarPedido').disabled = false;
        }

        function actualizarTablaProductos() {
            const tabla = document.getElementById('productosCheckoutTable');
            let html = '';

            carritoCheckout.forEach((producto, index) => {
                html += `
                    <tr>
                        <td>
                            <div>
                                <h6 class="mb-1">${producto.nombre}</h6>
                                <small class="text-muted">ID: ${producto.id}</small>
                            </div>
                        </td>
                        <td class="text-center">
                            <span class="fw-bold">S/ ${producto.precio.toFixed(2)}</span>
                        </td>
                        <td class="text-center">
                            <div class="d-flex align-items-center justify-content-center">
                                <button class="btn btn-outline-secondary btn-sm" 
                                        onclick="cambiarCantidadCheckout(${index}, ${producto.cantidad - 1})">
                                    <i class="fas fa-minus"></i>
                                </button>
                                <input type="number" class="form-control form-control-sm mx-2 text-center" 
                                       style="width: 70px;" value="${producto.cantidad}" min="1" max="${producto.stock}"
                                       onchange="cambiarCantidadCheckout(${index}, this.value)">
                                <button class="btn btn-outline-secondary btn-sm" 
                                        onclick="cambiarCantidadCheckout(${index}, ${producto.cantidad + 1})">
                                    <i class="fas fa-plus"></i>
                                </button>
                            </div>
                            <small class="text-muted d-block">Stock: ${producto.stock}</small>
                        </td>
                        <td class="text-center">
                            <span class="fw-bold text-success">S/ ${producto.subtotal.toFixed(2)}</span>
                        </td>
                        <td class="text-center">
                            <button class="btn btn-outline-danger btn-sm" 
                                    onclick="eliminarProductoCheckout(${index})" 
                                    title="Eliminar producto">
                                <i class="fas fa-trash"></i>
                            </button>
                        </td>
                    </tr>
                `;
            });

            tabla.innerHTML = html;
        }

        function cambiarCantidadCheckout(index, nuevaCantidad) {
            const cantidad = parseInt(nuevaCantidad);
            const producto = carritoCheckout[index];

            if (cantidad <= 0) {
                eliminarProductoCheckout(index);
                return;
            }

            if (cantidad > producto.stock) {
                alert(`Stock máximo disponible: ${producto.stock} unidades`);
                actualizarTablaProductos(); // Restaurar valor anterior
                return;
            }

            // Actualizar cantidad y subtotal
            producto.cantidad = cantidad;
            producto.subtotal = producto.precio * cantidad;

            // Guardar en localStorage
            localStorage.setItem('carrito', JSON.stringify(carritoCheckout));

            // Actualizar interfaz
            actualizarTablaProductos();
            actualizarResumen();
            actualizarContadorCarrito(); // Actualizar contador del header
        }

        function eliminarProductoCheckout(index) {
            if (confirm('¿Estás seguro de eliminar este producto?')) {
                carritoCheckout.splice(index, 1);
                localStorage.setItem('carrito', JSON.stringify(carritoCheckout));

                if (carritoCheckout.length === 0) {
                    mostrarCarritoVacio();
                } else {
                    actualizarTablaProductos();
                    actualizarResumen();
                }
                
                actualizarContadorCarrito(); // Actualizar contador del header
            }
        }

        function actualizarResumen() {
            const cantidadTotal = carritoCheckout.reduce((total, item) => total + item.cantidad, 0);
            totalGeneral = carritoCheckout.reduce((total, item) => total + item.subtotal, 0);

            // Actualizar elementos del resumen
            document.getElementById('totalItems').textContent = cantidadTotal;
            document.getElementById('totalEstimado').textContent = `S/ ${totalGeneral.toFixed(2)}`;
            document.getElementById('resumenSubtotal').textContent = `S/ ${totalGeneral.toFixed(2)}`;
            document.getElementById('resumenCantidad').textContent = cantidadTotal;
            document.getElementById('resumenTotal').textContent = `S/ ${totalGeneral.toFixed(2)}`;
        }

        function confirmarPedido() {
            if (carritoCheckout.length === 0) {
                alert('No hay productos en el carrito');
                return;
            }

            const btnConfirmar = document.getElementById('btnConfirmarPedido');
            btnConfirmar.disabled = true;
            btnConfirmar.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Procesando...';

            // Preparar datos del pedido
            const datosPedido = {
                productos: carritoCheckout.map(producto => ({
                    productoId: producto.id,
                    cantidad: producto.cantidad
                })),
                total: totalGeneral,
                observaciones: document.getElementById('observaciones').value.trim() || null
            };

            console.log('Enviando pedido:', datosPedido);

            // Enviar pedido al servidor
            fetch('/checkout/crear-pedido-productos', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(datosPedido)
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // Limpiar carrito
                    localStorage.removeItem('carrito');
                    carritoCheckout = [];
                    actualizarContadorCarrito();

                    // Mostrar confirmación
                    mostrarConfirmacionPedido(data);
                } else {
                    alert('Error al procesar el pedido: ' + (data.message || 'Error desconocido'));
                    console.error('Error del servidor:', data);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error de conexión. Intente nuevamente.');
            })
            .finally(() => {
                // Restaurar botón
                btnConfirmar.disabled = false;
                btnConfirmar.innerHTML = '<i class="fas fa-check-circle me-2"></i>Confirmar Pedido';
            });
        }

        function mostrarConfirmacionPedido(data) {
            const content = document.getElementById('confirmacionContent');
            content.innerHTML = `
                <div class="text-success mb-4">
                    <i class="fas fa-check-circle fa-4x"></i>
                </div>
                <h4 class="text-success">¡Pedido Confirmado!</h4>
                <p class="mb-3">Tu pedido ha sido registrado exitosamente.</p>
                <div class="alert alert-info">
                    <h6>Detalles del Pedido:</h6>
                    <p class="mb-1"><strong>ID de Pedido:</strong> ${data.pedidoId || 'Generando...'}</p>
                    <p class="mb-1"><strong>Total:</strong> S/ ${totalGeneral.toFixed(2)}</p>
                    <p class="mb-0"><strong>Estado:</strong> Pendiente</p>
                </div>
                <p class="text-muted">
                    Recibirás actualizaciones sobre el estado de tu pedido. 
                    Puedes consultar el progreso en tu dashboard.
                </p>
            `;

            new bootstrap.Modal(document.getElementById('confirmacionModal')).show();
        }

        function continuarComprando() {
            window.location.href = '/categorias';
        }

        // Función para ir al dashboard del cliente
        function irDashboardCliente() {
            fetch('/auth/cliente-actual')
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        window.location.href = `/cliente-dashboard/${data.data.dni}`;
                    } else {
                        window.location.href = '/';
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    window.location.href = '/';
                });
        }
		
		
		// Cargar bancos
		function cargarBancos() {
		    fetch('/banco/api/todos')
		        .then(response => response.json())
		        .then(bancos => {
		            const select = document.getElementById('selectBanco');
		            select.innerHTML = '<option value="">Seleccione un banco...</option>';
		            bancos.forEach(banco => {
		                select.innerHTML += `<option value="${banco.id}">${banco.nombre}</option>`;
		            });
		        });
		}

		// Abrir modal de pago
		function abrirModalPago() {
		    document.getElementById('selectBanco').value = '';
		    document.getElementById('inputNumeroOperacion').value = '';
		    document.getElementById('alertaPago').innerHTML = '';
		    new bootstrap.Modal(document.getElementById('modalPago')).show();
		}

		// Procesar pago
		function procesarPago() {
		    const bancoId = document.getElementById('selectBanco').value;
		    const numeroOperacion = document.getElementById('inputNumeroOperacion').value;
		    
		    if (!bancoId || !numeroOperacion) {
		        document.getElementById('alertaPago').innerHTML = 
		            '<div class="alert alert-warning">Complete todos los campos</div>';
		        return;
		    }
		    
		    // Cerrar modal de pago
		    bootstrap.Modal.getInstance(document.getElementById('modalPago')).hide();
		    
		    // Limpiar carrito
		    localStorage.removeItem('carrito');
		    carritoCheckout = [];
		    actualizarContadorCarrito();
		    
		    // Mostrar confirmación (usa el modal existente)
		    mostrarConfirmacionPedido({ pedidoId: 'PED-' + Date.now() });
		}

		// Cambiar la función actualizarResumen para usar el nuevo botón
		function actualizarResumen() {
		    const cantidadTotal = carritoCheckout.reduce((total, item) => total + item.cantidad, 0);
		    totalGeneral = carritoCheckout.reduce((total, item) => total + item.subtotal, 0);

		    document.getElementById('totalItems').textContent = cantidadTotal;
		    document.getElementById('totalEstimado').textContent = `S/ ${totalGeneral.toFixed(2)}`;
		    document.getElementById('resumenSubtotal').textContent = `S/ ${totalGeneral.toFixed(2)}`;
		    document.getElementById('resumenCantidad').textContent = cantidadTotal;
		    document.getElementById('resumenTotal').textContent = `S/ ${totalGeneral.toFixed(2)}`;
		    
		    // CAMBIAR esta línea
		    document.getElementById('btnConfirmarPago').disabled = carritoCheckout.length === 0;
		}

		// Agregar al DOMContentLoaded existente
		document.addEventListener('DOMContentLoaded', function() {
		    cargarBancos(); // AGREGAR esta línea
		});