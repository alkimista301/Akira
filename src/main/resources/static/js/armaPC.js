// Variables globales para Armar PC
					        let componentesSeleccionados = {};
					        let totalGeneral = 0;
					        let categoriasData = {};
					        
					        // Cargar productos de una categoría
					        function cargarProductosCategoria(categoriaId) {
					            const loadingDiv = document.getElementById('loading' + categoriaId);
					            const productosDiv = document.getElementById('productos' + categoriaId);
					            const noProductosDiv = document.getElementById('noProductos' + categoriaId);
					            
					            // Si ya se cargaron los productos, no hacer otra petición
					            if (categoriasData[categoriaId]) {
					                return;
					            }
					            
					            // Mostrar loading
					            loadingDiv.style.display = 'block';
					            productosDiv.style.display = 'none';
					            noProductosDiv.style.display = 'none';
					            
					            // Hacer petición AJAX
					            fetch(`/api/productos/categoria/${categoriaId}`)
					                .then(response => {
					                    if (!response.ok) {
					                        throw new Error('Error en la respuesta del servidor');
					                    }
					                    return response.json();
					                })
					                .then(productos => {
					                    // Ocultar loading
					                    loadingDiv.style.display = 'none';
					                    
					                    // Guardar productos en cache
					                    categoriasData[categoriaId] = productos;
					                    
					                    if (productos && productos.length > 0) {
					                        // Generar HTML de productos
					                        let productosHTML = '';
					                        productos.forEach(producto => {
					                            // Verificar si ya está en el carrito (localStorage)
					                            const carrito = JSON.parse(localStorage.getItem('carrito')) || [];
					                            const enCarrito = carrito.find(item => item.id === producto.id && item.armarPC === true);
					                            const isSelected = componentesSeleccionados[categoriaId]?.id === producto.id;
					                            
					                            productosHTML += `
					                                <div class="col-md-6 mb-3">
					                                    <div class="producto-item p-3 ${isSelected || enCarrito ? 'componente-seleccionado' : ''}" 
					                                         onclick="agregarComponenteAlCarrito(${categoriaId}, ${producto.idProducto}, '${producto.nombre.replace(/'/g, "\\'")}', ${producto.precio}, ${producto.cantidadStock || 0})"
					                                         ${producto.stock <= 0 ? 'style="opacity: 0.6; cursor: not-allowed;"' : ''}>
					                                        <div class="d-flex justify-content-between align-items-start">
					                                            <div class="flex-grow-1">
					                                                <h6 class="mb-1">${producto.nombre}</h6>
					                                                <p class="text-muted small mb-2">${producto.descripcion || 'Sin descripción'}</p>
					                                                <div class="d-flex justify-content-between align-items-center">
					                                                    <span class="precio-destacado">S/ ${producto.precio ? producto.precio.toFixed(2) : '0.00'}</span>
					                                                    <small class="text-muted">Stock: ${producto.cantidadStock || 0}</small>
					                                                </div>
					                                            </div>
					                                            ${(isSelected || enCarrito) ? '<i class="fas fa-check-circle text-success ms-2 fa-2x"></i>' : ''}
					                                        </div>
					                                    </div>
					                                </div>
					                            `;
					                        });
					                        
					                        productosDiv.innerHTML = productosHTML;
					                        productosDiv.style.display = 'flex';
					                    } else {
					                        // No hay productos
					                        noProductosDiv.style.display = 'block';
					                    }
					                })
					                .catch(error => {
					                    console.error('Error al cargar productos:', error);
					                    loadingDiv.style.display = 'none';
					                    productosDiv.innerHTML = '<div class="col-12 text-center text-danger p-4"><i class="fas fa-exclamation-triangle fa-2x mb-3"></i><br>Error al cargar productos de esta categoría</div>';
					                    productosDiv.style.display = 'block';
					                });
					        }
							
							
							
							// Agregar componente al carrito con indicador armarPC
							function agregarComponenteAlCarrito(categoriaId, productoId, productoNombre, productoPrecio, productoStock) {
							    console.log('Agregando componente:', {categoriaId, productoId, productoNombre, productoPrecio, productoStock});
							    
							    // Verificar stock
							    if (productoStock <= 0) {
							        Swal.fire('Sin Stock', 'Este producto no tiene stock disponible', 'warning');
							        return;
							    } 
							    
							    // Obtener carrito del localStorage
							    let carrito2 = JSON.parse(localStorage.getItem('carrito')) || [];
							    console.log('Carrito actual:', carrito2);
							    
							    // Verificar si ya hay un producto de esta categoría en el carrito con armarPC=true
							    const existeComponenteCategoria = carrito.find(item => 
							        item.categoriaId === categoriaId && item.armarPC === true
							    );
							    
							    if (existeComponenteCategoria) {
							        console.log('Reemplazando componente existente de categoría:', categoriaId);
							        // Reemplazar el componente existente de esta categoría
							        const index = carrito.findIndex(item => 
							            item.categoriaId === categoriaId && item.armarPC === true
							        );
							        carrito2.splice(index, 1);
							    }
							    
							    // Crear objeto del componente con TODOS los datos necesarios
							    const componenteCarrito = {
							        id: productoId,                    // ✅ ID del producto 
							        nombre: productoNombre,
							        precio: productoPrecio,
							        cantidad: 1,
							        subtotal: productoPrecio,
							        stock: productoStock,
							        armarPC: true,                     // ✅ Indicador especial para armar PC
							        categoriaId: categoriaId           // ✅ ID de la categoría
							    };
							    
							    console.log('Componente a agregar:', componenteCarrito);
							    
							    // Agregar nuevo componente al carrito
							    carrito2.push(componenteCarrito);
							    
							    // Guardar en localStorage
							    localStorage.setItem('carrito', JSON.stringify(carrito2));
							    console.log('Carrito guardado:', carrito2);
							    
							    // Actualizar contador del carrito (función del carrito.js)
							    if (typeof actualizarContadorCarrito === 'function') {
							        actualizarContadorCarrito();
							    }
							    
							    // Actualizar selección visual
							    componentesSeleccionados[categoriaId] = {
							        id: productoId,
							        nombre: productoNombre,
							        precio: productoPrecio
							    };
							    
							    // Mostrar badge de seleccionado
							    const badge = document.getElementById(`selectedBadge_${categoriaId}`);
							    if (badge) {
							        badge.style.display = 'inline-block';
							    }
							    
							    // Actualizar visualización
							    actualizarVisualizacionCategoria(categoriaId);
							    actualizarResumenArmarPC();
							    
							    // Feedback visual
							    Swal.fire({
							        toast: true,
							        position: 'top-end',
							        icon: 'success',
							        title: `${productoNombre} agregado (ID: ${productoId})`,
							        showConfirmButton: false,
							        timer: 2000
							    });
							}

							        
							        // Actualizar visualización de productos en categoría
							        function actualizarVisualizacionCategoria(categoriaId) {
							            const productosDiv = document.getElementById('productos' + categoriaId);
							            if (!productosDiv) return;
							            
							            const productos = productosDiv.querySelectorAll('.producto-item');
							            
							            productos.forEach(item => {
							                item.classList.remove('componente-seleccionado');
							                const checkIcon = item.querySelector('.fa-check-circle');
							                if (checkIcon) checkIcon.remove();
							            });
							            
							            // Marcar el seleccionado
							            const selectedProduct = componentesSeleccionados[categoriaId];
							            if (selectedProduct) {
							                productos.forEach(item => {
							                    const onclickAttr = item.getAttribute('onclick');
							                    if (onclickAttr && onclickAttr.includes(`${selectedProduct.id},`)) {
							                        item.classList.add('componente-seleccionado');
							                        const flexDiv = item.querySelector('.flex-grow-1');
							                        if (flexDiv) {
							                            flexDiv.insertAdjacentHTML('afterend', 
							                                '<i class="fas fa-check-circle text-success ms-2 fa-2x"></i>');
							                        }
							                    }
							                });
							            }
							        }
									
									
									
									
									// Actualizar resumen de componentes seleccionados
									        function actualizarResumenArmarPC() {
									            const resumenDiv = document.getElementById('componentesSeleccionados');
									            const totalSection = document.getElementById('totalSection');
									            const botonPedido = document.getElementById('botonPedido');
									            const totalSpan = document.getElementById('totalGeneral');
									            
									            // Obtener componentes del carrito con armarPC=true
									            const carrito = JSON.parse(localStorage.getItem('carrito')) || [];
									            const componentesPC = carrito.filter(item => item.armarPC === true);
									            
									            if (componentesPC.length === 0) {
									                resumenDiv.innerHTML = `
									                    <div class="text-center text-muted py-4">
									                        <i class="fas fa-desktop fa-3x mb-3"></i>
									                        <p>Aún no has seleccionado componentes</p>
									                        <small>Selecciona productos de las categorías para armar tu PC</small>
									                    </div>
									                `;
									                totalSection.style.display = 'none';
									                botonPedido.style.display = 'none';
									                return;
									            }
									            
									            let resumenHTML = '';
									            totalGeneral = 0;
									            
									            componentesPC.forEach(componente => {
									                totalGeneral += componente.precio;
									                resumenHTML += `
									                    <div class="d-flex justify-content-between align-items-center mb-2 p-3 border rounded bg-light">
									                        <div class="flex-grow-1">
									                            <h6 class="mb-0">${componente.nombre}</h6>
									                            <small class="text-muted">
									                                <span class="arma-pc-badge">
									                                    <i class="fas fa-microchip me-1"></i>Armar PC
									                                </span>
									                            </small>
									                        </div>
									                        <div class="text-end">
									                            <div class="precio-destacado">S/ ${componente.precio.toFixed(2)}</div>
									                            <button class="btn btn-sm btn-outline-danger mt-1" onclick="removerComponenteDelCarrito(${componente.categoriaId})">
									                                <i class="fas fa-times"></i>
									                            </button>
									                        </div>
									                    </div>
									                `;
									            });
									            
									            resumenDiv.innerHTML = resumenHTML;
									            totalSpan.textContent = totalGeneral.toFixed(2);
									            totalSection.style.display = 'block';
									            botonPedido.style.display = 'block';
									            
									            // Activar botón si hay componentes
									            const continuarBtn = document.getElementById('continuarCompra');
									            continuarBtn.disabled = false;
									        }
									        
									        // Remover componente del carrito
									        function removerComponenteDelCarrito(categoriaId) {
									            let carrito3 = JSON.parse(localStorage.getItem('carrito')) || [];
									            
									            // Eliminar componente de esta categoría del carrito
												carrito3 = carrito3.filter(item => !(item.categoriaId === categoriaId && item.armarPC === true));
									            
									            // Guardar carrito actualizado
									            localStorage.setItem('carrito', JSON.stringify(carrito3));
									            
									            // Actualizar contador del carrito
									            if (typeof actualizarContadorCarrito === 'function') {
									                actualizarContadorCarrito();
									            }
									            
									            // Limpiar selección visual
									            delete componentesSeleccionados[categoriaId];
									            
									            // Ocultar badge de seleccionado
									            const badge = document.getElementById(`selectedBadge_${categoriaId}`);
									            if (badge) badge.style.display = 'none';
									            
									            // Actualizar visualización
									            actualizarVisualizacionCategoria(categoriaId);
									            actualizarResumenArmarPC();
									            
									            Swal.fire({
									                toast: true,
									                position: 'top-end',
									                icon: 'info',
									                title: 'Componente removido del carrito',
									                showConfirmButton: false,
									                timer: 1500
									            });
									        }
									        
									        // Ir al checkout (igual que el carrito normal)
									        function irACheckout() {
									            const carrito = JSON.parse(localStorage.getItem('carrito')) || [];
									            const componentesPC = carrito.filter(item => item.armarPC === true);
									            
									            if (componentesPC.length === 0) {
									                Swal.fire('Error', 'Debes seleccionar al menos un componente', 'error');
									                return;
									            }
									            
									            // Verificar si el cliente está logueado (usa la función del carrito.js)
									            fetch('/auth/estado-sesion')
									                .then(response => response.json())
									                .then(data => {
									                    if (data.autenticado && data.tipoUsuario === 'cliente') {
									                        // Ir al checkout normal - el checkout ya maneja productos con armarPC=true
									                        window.location.href = '/checkout';
									                    } else {
									                        Swal.fire('Requiere Login', 'Debes iniciar sesión como cliente para continuar', 'warning');
									                        // Mostrar modal de login si está disponible
									                        const loginModal = document.getElementById('loginModal');
									                        if (loginModal) {
									                            new bootstrap.Modal(loginModal).show();
									                        }
									                    }
									                })
									                .catch(error => {
									                    console.error('Error:', error);
									                    Swal.fire('Error', 'Error al verificar sesión', 'error');
									                });
									        }
											
											
											
											
											
											// Login de cliente
											        function iniciarSesion() {
											            const usuario = document.getElementById('loginUsuario').value;
											            const password = document.getElementById('loginPassword').value;
											            
											            if (!usuario || !password) {
											                mostrarAlertaLogin('Complete todos los campos', 'warning');
											                return;
											            }
											            
											            fetch('/auth/login-cliente', {
											                method: 'POST',
											                headers: {
											                    'Content-Type': 'application/json'
											                },
											                body: JSON.stringify({ usuario, password })
											            })
											            .then(response => response.json())
											            .then(data => {
											                if (data.success) {
											                    mostrarAlertaLogin('Sesión iniciada correctamente', 'success');
											                    setTimeout(() => location.reload(), 1500);
											                } else {
											                    mostrarAlertaLogin(data.message || 'Credenciales incorrectas', 'danger');
											                }
											            })
											            .catch(error => {
											                console.error('Error:', error);
											                mostrarAlertaLogin('Error al iniciar sesión', 'danger');
											            });
											        }
											        
											        // Registro de cliente
											        function registrarCliente() {
											            const dni = document.getElementById('regDni').value.trim();
											            const usuario = document.getElementById('regUsuario').value.trim();
											            const nombre = document.getElementById('regNombre').value.trim();
											            const apellido = document.getElementById('regApellido').value.trim();
											            const correo = document.getElementById('regCorreo').value.trim();
											            const celular = document.getElementById('regCelular').value.trim();
											            const direccion = document.getElementById('regDireccion').value.trim();
											            const password = document.getElementById('regPassword').value;
											            const passwordConfirm = document.getElementById('regPasswordConfirm').value;
											            
											            // Validaciones
											            if (!dni || !usuario || !nombre || !apellido || !correo || !password) {
											                mostrarAlerta('Todos los campos marcados con * son obligatorios', 'warning');
											                return;
											            }
											            
											            if (dni.length !== 8 || !/^\d+$/.test(dni)) {
											                mostrarAlerta('El DNI debe tener 8 dígitos', 'warning');
											                return;
											            }
											            
											            if (password !== passwordConfirm) {
											                mostrarAlerta('Las contraseñas no coinciden', 'warning');
											                return;
											            }
											            
											            // Enviar datos
											            fetch('/auth/registro-cliente', {
											                method: 'POST',
											                headers: {
											                    'Content-Type': 'application/json'
											                },
											                body: JSON.stringify({
											                    dni, usuario, nombre, apellido, correo, celular, direccion, password
											                })
											            })
											            .then(response => response.json())
											            .then(data => {
											                if (data.success) {
											                    mostrarAlerta('Cliente registrado exitosamente', 'success');
											                    setTimeout(() => {
											                        location.reload();
											                    }, 1500);
											                } else {
											                    mostrarAlerta(data.message, 'danger');
											                }
											            })
											            .catch(error => {
											                console.error('Error:', error);
											                mostrarAlerta('Error de conexión', 'danger');
											            });
											        }
											        
											        function mostrarAlerta(mensaje, tipo) {
											            const alertContainer = document.getElementById('registroAlert');
											            alertContainer.innerHTML = `
											                <div class="alert alert-${tipo} alert-dismissible fade show" role="alert">
											                    ${mensaje}
											                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
											                </div>
											            `;
											        }
											        
											        function mostrarAlertaLogin(mensaje, tipo) {
											            const alertContainer = document.getElementById('loginAlert');
											            alertContainer.innerHTML = `
											                <div class="alert alert-${tipo} alert-dismissible fade show" role="alert">
											                    ${mensaje}
											                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
											                </div>
											            `;
											        }
											        
											        // Inicialización - cargar componentes ya en carrito
											        document.addEventListener('DOMContentLoaded', function() {
											            // Auto-expandir primera categoría si hay categorías
											            const firstAccordion = document.querySelector('.accordion-button');
											            if (firstAccordion) {
											                setTimeout(() => {
											                    firstAccordion.click();
											                }, 500);
											            }
											            
											            // Cargar componentes ya seleccionados del carrito
											            const carrito = JSON.parse(localStorage.getItem('carrito')) || [];
											            const componentesPC = carrito.filter(item => item.armarPC === true);
											            
											            componentesPC.forEach(componente => {
											                if (componente.categoriaId) {
											                    componentesSeleccionados[componente.categoriaId] = {
											                        id: componente.id,
											                        nombre: componente.nombre,
											                        precio: componente.precio
											                    };
											                    
											                    // Mostrar badge de seleccionado
											                    const badge = document.getElementById(`selectedBadge_${componente.categoriaId}`);
											                    if (badge) badge.style.display = 'inline-block';
											                }
											            });
											            
											            // Actualizar resumen inicial
											            actualizarResumenArmarPC();
											        });