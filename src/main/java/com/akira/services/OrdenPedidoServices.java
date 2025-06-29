package com.akira.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.akira.model.OrdenPedido;
import com.akira.model.Usuario;
import com.akira.model.Estado;
import com.akira.repository.OrdenPedidoRepository;
import com.akira.repository.UsuarioRepository;
import com.akira.repository.EstadoRepository;
import com.akira.controller.ClienteController;
import com.akira.model.Cliente;
import com.akira.repository.ClienteRepository;

@Service
public class OrdenPedidoServices {

    private final ClienteController clienteController;

    @Autowired
    private OrdenPedidoRepository Orden_pedidoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private EstadoRepository estadoRepository;
    
    @Autowired
    private ClienteRepository clienteRepository;

    OrdenPedidoServices(ClienteController clienteController) {
        this.clienteController = clienteController;
    }

    public List<OrdenPedido> listarTodos() {
        try {
            return Orden_pedidoRepository.findAllOrderByFechaDesc();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar pedidos: " + e.getMessage());
        }
    }

    public OrdenPedido buscarPorId(Integer id) {
        try {
            return Orden_pedidoRepository.findById(id).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<OrdenPedido> listarPorCliente(String clienteDni) {
        try {
            // CAMBIADO: usar el método corregido del repository
            return Orden_pedidoRepository.findByClienteDniOrderByFechaDesc(clienteDni);
        } catch (Exception e) {
            System.err.println("Error al listar pedidos por cliente: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<OrdenPedido> listarPendientesSinAsignar() {
        try {
            return Orden_pedidoRepository.findPedidosPendientesSinAsignar();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar pedidos pendientes: " + e.getMessage());
        }
    }

    public List<OrdenPedido> listarAsignadosATecnico(String tecnicoId) {
        try {
            return Orden_pedidoRepository.findPedidosAsignadosATecnico(tecnicoId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar pedidos del técnico: " + e.getMessage());
        }
    }

    public List<OrdenPedido> listarAtendidosDeVendedor(String vendedorId) {
        try {
            return Orden_pedidoRepository.findPedidosAtendidosDeVendedor(vendedorId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar pedidos atendidos del vendedor: " + e.getMessage());
        }
    }

    public List<OrdenPedido> listarTodosAtendidos() {
        try {
            return Orden_pedidoRepository.findTodosPedidosAtendidos();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar pedidos atendidos: " + e.getMessage());
        }
    }

    public List<OrdenPedido> listarCerradosDeVendedor(String vendedorId) {
        try {
            return Orden_pedidoRepository.findPedidosCerradosDeVendedor(vendedorId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar pedidos cerrados del vendedor: " + e.getMessage());
        }
    }


    public OrdenPedido crearPedidoVendedor(String clienteDni, String vendedorId, BigDecimal total, String tipoPedido, String detallesComponentes) {
        try {
            // Buscar cliente en ClienteRepository (usa DNI como ID)
            Cliente cliente = clienteRepository.findById(clienteDni).orElse(null);
            if (cliente == null || !cliente.getActivo()) {
                throw new RuntimeException("Cliente no encontrado o inactivo. DNI: " + clienteDni);
            }

            // Buscar vendedor en UsuarioRepository (empleados)
            Usuario vendedor = usuarioRepository.findById(vendedorId).orElse(null);
            if (vendedor == null || !vendedor.esVendedor()) {
                throw new RuntimeException("Vendedor no encontrado o inválido. ID: " + vendedorId);
            }

            Estado estadoPendiente = estadoRepository.findByDescripcion("PENDIENTE");
            if (estadoPendiente == null) {
                throw new RuntimeException("Estado PENDIENTE no encontrado");
            }

            OrdenPedido orden = new OrdenPedido();
            orden.setCliente(cliente);  // Ahora usa Cliente
            orden.setVendedorAsignado(vendedor);
            orden.setTotal(total);
            orden.setEstado(estadoPendiente);
            orden.setTipoPedido(tipoPedido);
            orden.setDetallesComponentes(detallesComponentes);
            orden.setFecha(LocalDateTime.now());

            return Orden_pedidoRepository.save(orden);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al crear pedido por vendedor: " + e.getMessage());
        }
    }
    
    public OrdenPedido asignarTecnicoYVendedor(Integer ordenId, String tecnicoId, String vendedorId) {
        try {
            OrdenPedido orden = buscarPorId(ordenId);
            if (orden == null) {
                throw new RuntimeException("Pedido no encontrado");
            }

            if (!orden.estaPendiente()) {
                throw new RuntimeException("Solo se pueden asignar técnicos a pedidos pendientes");
            }

            Usuario tecnico = usuarioRepository.findById(tecnicoId).orElse(null);
            if (tecnico == null || !tecnico.esTecnico()) {
                throw new RuntimeException("Técnico no encontrado o inválido");
            }

            Usuario vendedor = null;
            if (vendedorId != null) {
                vendedor = usuarioRepository.findById(vendedorId).orElse(null);
                if (vendedor == null || !vendedor.esVendedor()) {
                    throw new RuntimeException("Vendedor no encontrado o inválido");
                }
            }

            Estado estadoAsignado = estadoRepository.findByDescripcion("ASIGNADO");
            if (estadoAsignado == null) {
                throw new RuntimeException("Estado ASIGNADO no encontrado");
            }

            orden.setTecnicoAsignado(tecnico);
            if (vendedor != null) {
                orden.setVendedorAsignado(vendedor);
            }
            orden.setEstado(estadoAsignado);
            orden.setFechaAsignacion(LocalDateTime.now());

            return Orden_pedidoRepository.save(orden);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al asignar técnico y vendedor: " + e.getMessage());
        }
    }

    public OrdenPedido marcarComoAtendido(Integer ordenId, String observaciones) {
        try {
            OrdenPedido orden = buscarPorId(ordenId);
            if (orden == null) {
                throw new RuntimeException("Pedido no encontrado");
            }

            if (!orden.estaAsignada()) {
                throw new RuntimeException("Solo se pueden atender pedidos asignados");
            }

            Estado estadoAtendido = estadoRepository.findByDescripcion("ATENDIDO");
            if (estadoAtendido == null) {
                throw new RuntimeException("Estado ATENDIDO no encontrado");
            }

            orden.setEstado(estadoAtendido);
            orden.setObservaciones(observaciones);
            orden.setFechaAtencion(LocalDateTime.now());

            return Orden_pedidoRepository.save(orden);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al marcar pedido como atendido: " + e.getMessage());
        }
    }

    public OrdenPedido cerrarPedido(Integer ordenId) {
        try {
            OrdenPedido orden = buscarPorId(ordenId);
            if (orden == null) {
                throw new RuntimeException("Pedido no encontrado");
            }

            if (!orden.estaAtendida()) {
                throw new RuntimeException("Solo se pueden cerrar pedidos atendidos");
            }

            Estado estadoCerrado = estadoRepository.findByDescripcion("CERRADO");
            if (estadoCerrado == null) {
                throw new RuntimeException("Estado CERRADO no encontrado");
            }

            orden.setEstado(estadoCerrado);
            orden.setFechaCierre(LocalDateTime.now());

            return Orden_pedidoRepository.save(orden);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al cerrar pedido: " + e.getMessage());
        }
    }

    public List<OrdenPedido> buscarPorClienteNombre(String nombre) {
        try {
            return Orden_pedidoRepository.findByClienteNombreContaining(nombre);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar pedidos por cliente: " + e.getMessage());
        }
    }

    public List<OrdenPedido> listarPorEstado(String estadoDescripcion) {
        try {
            return Orden_pedidoRepository.findByEstadoDescripcion(estadoDescripcion);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar pedidos por estado: " + e.getMessage());
        }
    }

    public List<OrdenPedido> listarPorTipoPedido(String tipoPedido) {
        try {
            return Orden_pedidoRepository.findByTipoPedido(tipoPedido);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar pedidos por tipo: " + e.getMessage());
        }
    }

    public List<OrdenPedido> listarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            return Orden_pedidoRepository.findByFechaBetween(fechaInicio, fechaFin);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar pedidos por fechas: " + e.getMessage());
        }
    }

    public List<OrdenPedido> listarActivos() {
        try {
            return Orden_pedidoRepository.findPedidosActivos();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar pedidos activos: " + e.getMessage());
        }
    }

    public long contarPorEstado(String estadoDescripcion) {
        try {
            return Orden_pedidoRepository.countByEstadoDescripcion(estadoDescripcion);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public long contarPedidosTecnico(String tecnicoId) {
        try {
            return Orden_pedidoRepository.countPedidosAsignadosATecnico(tecnicoId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public long contarPedidosVendedor(String vendedorId) {
        try {
            return Orden_pedidoRepository.countPedidosAtendidosDeVendedor(vendedorId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Double calcularVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            Double total = Orden_pedidoRepository.sumTotalVentasPorPeriodo(fechaInicio, fechaFin);
            return total != null ? total : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public Double calcularVentasVendedor(String vendedorId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            Double total = Orden_pedidoRepository.sumVentasVendedorPorPeriodo(vendedorId, fechaInicio, fechaFin);
            return total != null ? total : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public OrdenPedido actualizar(OrdenPedido orden) {
        try {
            return Orden_pedidoRepository.save(orden);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar pedido: " + e.getMessage());
        }
    }
    
    /**
     * Crear pedido para cliente (desde web)
     */
    public OrdenPedido crearPedidoCliente(String clienteDni, BigDecimal total, String tipoPedido, String detallesComponentes) {
        try {
            Cliente cliente = clienteRepository.findById(clienteDni).orElse(null);
            if (cliente == null) {
                throw new RuntimeException("Cliente no encontrado con DNI: " + clienteDni);
            }

            Estado estadoPendiente = estadoRepository.findByDescripcion("PENDIENTE");
            if (estadoPendiente == null) {
                throw new RuntimeException("Estado PENDIENTE no encontrado");
            }

            OrdenPedido orden = new OrdenPedido();
            orden.setCliente(cliente);
            orden.setTotal(total); // Directo, no BigDecimal.valueOf()
            orden.setEstado(estadoPendiente);
            orden.setTipoPedido(tipoPedido);
            orden.setDetallesComponentes(detallesComponentes);
            orden.setFecha(LocalDateTime.now());

            return Orden_pedidoRepository.save(orden);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al crear pedido de cliente: " + e.getMessage());
        }
    }
     
	
	
}