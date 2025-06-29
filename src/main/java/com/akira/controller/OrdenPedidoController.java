package com.akira.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import java.util.Set;
import java.util.stream.Collectors;
import com.akira.model.DetallePedido;
import com.akira.model.Producto;
import com.akira.services.DetallePedidoServices;
import com.akira.services.ProductoServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.akira.model.OrdenPedido;
import com.akira.model.Usuario;
import com.akira.model.Estado;
import com.akira.services.OrdenPedidoServices;
import com.akira.services.UsuarioServices;
import com.akira.services.EstadoServices;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/orden-pedido")
public class OrdenPedidoController {

    @Autowired
    private OrdenPedidoServices ordenPedidoServices;
    
    @Autowired
    private UsuarioServices usuarioServices;
    
    @Autowired
    private EstadoServices estadoServices;
    
    @Autowired
    private DetallePedidoServices detallePedidoService;

    @Autowired
    private ProductoServices productoService;

    @GetMapping("/lista")
    public String listarPedidos(Model model) {
        try {
            List<OrdenPedido> pedidos = ordenPedidoServices.listarTodos();
            List<Usuario> tecnicos = usuarioServices.listarTecnicos();
            List<Usuario> vendedores = usuarioServices.listarVendedores();
            List<Estado> estados = estadoServices.listarTodos();
            
            model.addAttribute("pedidos", pedidos);
            model.addAttribute("tecnicos", tecnicos);
            model.addAttribute("vendedores", vendedores);
            model.addAttribute("estados", estados);
            return "orden-pedido/lista";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar pedidos: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/pendientes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarPendientes() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<OrdenPedido> pedidos = ordenPedidoServices.listarPendientesSinAsignar();
            response.put("success", true);
            response.put("data", pedidos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cargar pedidos pendientes: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/tecnico/{tecnicoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarPedidosTecnico(@PathVariable String tecnicoId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<OrdenPedido> pedidos = ordenPedidoServices.listarAsignadosATecnico(tecnicoId);
            response.put("success", true);
            response.put("data", pedidos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cargar pedidos del técnico: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/vendedor/{vendedorId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarPedidosVendedor(@PathVariable String vendedorId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<OrdenPedido> pedidosAtendidos = ordenPedidoServices.listarAtendidosDeVendedor(vendedorId);
            List<OrdenPedido> pedidosCerrados = ordenPedidoServices.listarCerradosDeVendedor(vendedorId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("atendidos", pedidosAtendidos);
            data.put("cerrados", pedidosCerrados);
            
            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cargar pedidos del vendedor: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/cliente/{clienteId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarPedidosCliente(@PathVariable String clienteId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<OrdenPedido> pedidos = ordenPedidoServices.listarPorCliente(clienteId);
            response.put("success", true);
            response.put("data", pedidos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cargar pedidos del cliente: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/atendidos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarAtendidos() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<OrdenPedido> pedidos = ordenPedidoServices.listarTodosAtendidos();
            response.put("success", true);
            response.put("data", pedidos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cargar pedidos atendidos: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/crear-cliente")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearPedidoCliente(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("=== CREANDO PEDIDO CLIENTE ===");
            System.out.println("Datos recibidos: " + requestData);
            
            String clienteId = (String) requestData.get("clienteId");
            BigDecimal total = new BigDecimal(requestData.get("total").toString());
            String tipoPedido = (String) requestData.get("tipoPedido");
            String detallesComponentes = (String) requestData.get("detallesComponentes");
            List<Map<String, Object>> productosData = (List<Map<String, Object>>) requestData.get("productos");
            
            System.out.println("Cliente ID: " + clienteId);
            System.out.println("Tipo pedido: " + tipoPedido);
            System.out.println("Total: " + total);
            
            // Crear la orden usando el servicio de cliente (corregido para usar BigDecimal)
            OrdenPedido pedido = ordenPedidoServices.crearPedidoCliente(clienteId, total, tipoPedido, detallesComponentes);
            
            // Crear los detalles de pedido si se proporcionaron productos
            if (productosData != null && !productosData.isEmpty()) {
                crearDetallesPedido(pedido, productosData);
            }
            
            System.out.println("Pedido creado exitosamente con ID: " + pedido.getId());
            
            response.put("success", true);
            response.put("message", "Pedido creado exitosamente");
            response.put("data", pedido);
            response.put("pedidoId", pedido.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al crear pedido: " + e.getMessage());
            
            response.put("success", false);
            response.put("message", "Error al crear pedido: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/crear-vendedor")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearPedidoVendedor(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            String clienteId = (String) requestData.get("clienteId");
            String vendedorId = (String) requestData.get("vendedorId");
            BigDecimal total = new BigDecimal(requestData.get("total").toString());
            String tipoPedido = (String) requestData.get("tipoPedido");
            String detallesComponentes = (String) requestData.get("detallesComponentes");

            OrdenPedido pedido = ordenPedidoServices.crearPedidoVendedor(clienteId, vendedorId, total, tipoPedido, detallesComponentes);
            
            response.put("success", true);
            response.put("message", "Pedido creado exitosamente por vendedor");
            response.put("data", pedido);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/asignar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> asignarTecnicoYVendedor(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer ordenId = Integer.parseInt(requestData.get("ordenId").toString());
            String tecnicoId = (String) requestData.get("tecnicoId");
            String vendedorId = (String) requestData.get("vendedorId");

            OrdenPedido pedido = ordenPedidoServices.asignarTecnicoYVendedor(ordenId, tecnicoId, vendedorId);
            
            response.put("success", true);
            response.put("message", "Técnico y vendedor asignados exitosamente");
            response.put("data", pedido);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/atender/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> marcarComoAtendido(@PathVariable Integer id, @RequestBody Map<String, String> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            String observaciones = requestData.get("observaciones");
            
            OrdenPedido pedido = ordenPedidoServices.marcarComoAtendido(id, observaciones);
            
            response.put("success", true);
            response.put("message", "Pedido marcado como atendido exitosamente");
            response.put("data", pedido);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/cerrar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cerrarPedido(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            OrdenPedido pedido = ordenPedidoServices.cerrarPedido(id);
            
            response.put("success", true);
            response.put("message", "Pedido cerrado exitosamente");
            response.put("data", pedido);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/buscar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarPorId(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            OrdenPedido pedido = ordenPedidoServices.buscarPorId(id);
            if (pedido != null) {
                response.put("success", true);
                response.put("data", pedido);
            } else {
                response.put("success", false);
                response.put("message", "Pedido no encontrado");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al buscar pedido: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/buscar-cliente")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarPorCliente(@RequestParam String nombre) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<OrdenPedido> pedidos = ordenPedidoServices.buscarPorClienteNombre(nombre);
            response.put("success", true);
            response.put("data", pedidos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error en la búsqueda: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/por-estado/{estado}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarPorEstado(@PathVariable String estado) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<OrdenPedido> pedidos = ordenPedidoServices.listarPorEstado(estado);
            response.put("success", true);
            response.put("data", pedidos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al filtrar por estado: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/por-tipo/{tipo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarPorTipo(@PathVariable String tipo) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<OrdenPedido> pedidos = ordenPedidoServices.listarPorTipoPedido(tipo);
            response.put("success", true);
            response.put("data", pedidos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al filtrar por tipo: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/estadisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("pendientes", ordenPedidoServices.contarPorEstado("PENDIENTE"));
            estadisticas.put("asignados", ordenPedidoServices.contarPorEstado("ASIGNADO"));
            estadisticas.put("atendidos", ordenPedidoServices.contarPorEstado("ATENDIDO"));
            estadisticas.put("cerrados", ordenPedidoServices.contarPorEstado("CERRADO"));
            
            LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime finMes = LocalDateTime.now();
            Double ventasMes = ordenPedidoServices.calcularVentasPorPeriodo(inicioMes, finMes);
            estadisticas.put("ventasMes", ventasMes);
            
            response.put("success", true);
            response.put("data", estadisticas);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/reportes/ventas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reporteVentas(@RequestParam String fechaInicio, @RequestParam String fechaFin, @RequestParam(required = false) String vendedorId) {
        Map<String, Object> response = new HashMap<>();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio + " 00:00:00", formatter);
            LocalDateTime fin = LocalDateTime.parse(fechaFin + " 23:59:59", formatter);
            
            Double totalVentas;
            if (vendedorId != null && !vendedorId.isEmpty()) {
                totalVentas = ordenPedidoServices.calcularVentasVendedor(vendedorId, inicio, fin);
            } else {
                totalVentas = ordenPedidoServices.calcularVentasPorPeriodo(inicio, fin);
            }
            
            List<OrdenPedido> pedidos = ordenPedidoServices.listarPorFechas(inicio, fin);
            
            Map<String, Object> reporte = new HashMap<>();
            reporte.put("totalVentas", totalVentas);
            reporte.put("cantidadPedidos", pedidos.size());
            reporte.put("pedidos", pedidos);
            reporte.put("fechaInicio", fechaInicio);
            reporte.put("fechaFin", fechaFin);
            
            response.put("success", true);
            response.put("data", reporte);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al generar reporte: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

   
    
    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearPedidoUnificado(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("=== CREANDO PEDIDO UNIFICADO ===");
            System.out.println("Datos recibidos: " + requestData);
            
            // Extraer datos directos (no anidados)
            String clienteDni = (String) requestData.get("clienteDni");
            String vendedorId = (String) requestData.get("vendedorId");
            String tipoPedido = (String) requestData.get("tipoPedido");
            String detallesComponentes = (String) requestData.get("detallesComponentes");
            List<Map<String, Object>> productosData = (List<Map<String, Object>>) requestData.get("productos");
            
            Double totalDouble = ((Number) requestData.get("total")).doubleValue();
            BigDecimal total = BigDecimal.valueOf(totalDouble);
            
            System.out.println("Cliente DNI: " + clienteDni);
            System.out.println("Vendedor: " + vendedorId);
            System.out.println("Tipo pedido: " + tipoPedido);
            System.out.println("Total: " + total);
            
            // Crear la orden usando el servicio existente
            OrdenPedido pedido = ordenPedidoServices.crearPedidoVendedor(
                clienteDni, 
                vendedorId, 
                total, 
                tipoPedido, 
                detallesComponentes
            );
            
            // Crear los detalles de pedido
            crearDetallesPedido(pedido, productosData);
            
            System.out.println("Pedido creado exitosamente con ID: " + pedido.getId());
            
            response.put("success", true);
            response.put("message", "Pedido creado exitosamente");
            response.put("data", pedido);
            response.put("pedidoId", pedido.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al crear pedido: " + e.getMessage());
            
            response.put("success", false);
            response.put("message", "Error al crear pedido: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Determinar el tipo de pedido basado en las categorías de productos
     */
    private String determinarTipoPedido(List<Map<String, Object>> productos) {
        // Categorías típicas de componentes para armar PC
        Set<String> categoriasPC = Set.of(
            "Procesador", "Tarjeta Madre", "Memoria RAM", "Disco Duro", 
            "Tarjeta de Video", "Fuente de Poder", "Case", "Ventilador"
        );
        
        boolean tieneComponentesPC = productos.stream()
            .anyMatch(producto -> {
                String categoria = (String) producto.get("categoria");
                return categoria != null && categoriasPC.contains(categoria);
            });
        
        // Si tiene 3 o más componentes de PC, considerarlo como "Armar PC"
        long componentesPC = productos.stream()
            .mapToLong(producto -> {
                String categoria = (String) producto.get("categoria");
                return (categoria != null && categoriasPC.contains(categoria)) ? 1 : 0;
            })
            .sum();
        
        return (componentesPC >= 3) ? "ARMAR_PC" : "PRODUCTO_COMPLETO";
    }

    /**
     * Construir detalles de componentes para el pedido
     */
    private String construirDetallesComponentes(List<Map<String, Object>> productos, String tipoPedido) {
        StringBuilder detalles = new StringBuilder();
        
        if ("ARMAR_PC".equals(tipoPedido)) {
            detalles.append("CONFIGURACIÓN PC PERSONALIZADA:\n\n");
            
            // Agrupar por categoría
            Map<String, List<Map<String, Object>>> productosPorCategoria = productos.stream()
                .collect(Collectors.groupingBy(p -> (String) p.get("categoria")));
            
            for (Map.Entry<String, List<Map<String, Object>>> entry : productosPorCategoria.entrySet()) {
                String categoria = entry.getKey();
                List<Map<String, Object>> prods = entry.getValue();
                
                detalles.append(categoria).append(":\n");
                for (Map<String, Object> prod : prods) {
                    detalles.append("- ").append(prod.get("nombre"))
                           .append(" (Cant: ").append(prod.get("cantidad"))
                           .append(", Marca: ").append(prod.get("marca")).append(")\n");
                }
                detalles.append("\n");
            }
        } else {
            detalles.append("PRODUCTOS INDIVIDUALES:\n\n");
            for (Map<String, Object> producto : productos) {
                detalles.append("- ").append(producto.get("nombre"))
                       .append(" (Cant: ").append(producto.get("cantidad"))
                       .append(", Precio: S/ ").append(producto.get("precio")).append(")\n");
            }
        }
        
        return detalles.toString();
    }

    private void crearDetallesPedido(OrdenPedido orden, List<Map<String, Object>> productosData) {
        if (productosData == null || productosData.isEmpty()) {
            System.out.println("⚠️ No hay productos para crear detalles");
            return;
        }
        
        System.out.println("📦 Creando detalles del pedido para " + productosData.size() + " productos");
        
        for (Map<String, Object> productoData : productosData) {
            try {
                // Extraer datos del producto
                Object idObj = productoData.get("id");
                if (idObj == null) {
                    idObj = productoData.get("codigo");
                }
                
                if (idObj == null) {
                    System.err.println("❌ Producto sin ID válido: " + productoData);
                    continue;
                }
                
                Integer productoId = null;
                if (idObj instanceof Integer) {
                    productoId = (Integer) idObj;
                } else if (idObj instanceof String) {
                    try {
                        productoId = Integer.parseInt((String) idObj);
                    } catch (NumberFormatException e) {
                        System.err.println("❌ ID de producto no válido: " + idObj);
                        continue;
                    }
                }
                
                // Obtener cantidad
                Integer cantidad = 1;
                Object cantidadObj = productoData.get("cantidad");
                if (cantidadObj != null) {
                    if (cantidadObj instanceof Integer) {
                        cantidad = (Integer) cantidadObj;
                    } else if (cantidadObj instanceof String) {
                        try {
                            cantidad = Integer.parseInt((String) cantidadObj);
                        } catch (NumberFormatException e) {
                            cantidad = 1;
                        }
                    }
                }
                
                // Buscar producto en la base de datos
                Producto producto = productoService.buscarPorID(productoId);
                if (producto == null) {
                    System.err.println("❌ Producto no encontrado: " + productoId);
                    continue;
                }
                
                // Verificar stock
                if (!productoService.verificarStock(productoId, cantidad)) {
                    System.err.println("❌ Stock insuficiente para producto: " + producto.getNombre());
                    continue;
                }
                
                // Crear detalle
                DetallePedido detalle = new DetallePedido();
                detalle.setOrden(orden);
                detalle.setProducto(producto);
                detalle.setCantidad(cantidad);
                
                detallePedidoService.registrar(detalle);
                
                // Reducir stock
                productoService.reducirStock(productoId, cantidad);
                
                System.out.println("✅ Detalle creado: " + producto.getNombre() + " x" + cantidad);
                
            } catch (Exception e) {
                System.err.println("❌ Error al crear detalle: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}