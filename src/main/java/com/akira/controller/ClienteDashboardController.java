// CREAR NUEVO ARCHIVO: ClienteDashboardController.java

package com.akira.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.akira.model.Cliente;
import com.akira.model.OrdenPedido;
import com.akira.services.ClienteServices;
import com.akira.services.DetallePedidoServices;
import com.akira.services.OrdenPedidoServices;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;

@Controller
@RequestMapping("/cliente-dashboard")
public class ClienteDashboardController {

    @Autowired
    private ClienteServices clienteServices;
    
    @Autowired
    private OrdenPedidoServices ordenPedidoServices;
    
    @Autowired
    private DetallePedidoServices detallePedidoServices; // Asegúrate que esta línea existe


    /**
     * Dashboard principal del cliente
     */
    @GetMapping("/{dni}")
    public String dashboard(@PathVariable String dni, Model model, HttpSession session) {
        try {
            // Verificar sesión del cliente
            String clienteSessionId = (String) session.getAttribute("clienteId");
            
            if (clienteSessionId == null || !clienteSessionId.equals(dni)) {
                return "redirect:/auth/login-cliente";
            }
            
            // Obtener datos del cliente
            Cliente cliente = clienteServices.buscarPorDni(dni);
            if (cliente == null) {
                model.addAttribute("error", "Cliente no encontrado");
                return "error";
            }
            
            // Obtener pedidos del cliente
            List<OrdenPedido> misPedidos = ordenPedidoServices.listarPorCliente(dni);
            
            // Estadísticas del cliente
            Map<String, Object> estadisticas = calcularEstadisticasCliente(misPedidos);
            
            model.addAttribute("cliente", cliente);
            model.addAttribute("misPedidos", misPedidos);
            model.addAttribute("estadisticas", estadisticas);
            
            return "cliente-dashboard"; // archivo cliente-dashboard.html
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar dashboard: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Obtener pedidos del cliente vía AJAX
     */
    @GetMapping("/api/{dni}/pedidos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerPedidos(@PathVariable String dni, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Verificar sesión
            String clienteSessionId = (String) session.getAttribute("clienteId");
            if (clienteSessionId == null || !clienteSessionId.equals(dni)) {
                response.put("success", false);
                response.put("message", "No autorizado");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<OrdenPedido> pedidos = ordenPedidoServices.listarPorCliente(dni);
            
            response.put("success", true);
            response.put("pedidos", pedidos);
            response.put("total", pedidos.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener pedidos: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

 // REEMPLAZAR COMPLETAMENTE el método obtenerDetallePedido en ClienteDashboardController.java
    @GetMapping("/api/{dni}/pedido/{pedidoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDetallePedido(
            @PathVariable String dni, 
            @PathVariable Integer pedidoId, 
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Verificar sesión
            String clienteSessionId = (String) session.getAttribute("clienteId");
            if (clienteSessionId == null || !clienteSessionId.equals(dni)) {
                response.put("success", false);
                response.put("message", "No autorizado");
                return ResponseEntity.badRequest().body(response);
            }
            
            OrdenPedido pedido = ordenPedidoServices.buscarPorId(pedidoId);
            
            if (pedido == null || !pedido.getCliente().getDni().equals(dni)) {
                response.put("success", false);
                response.put("message", "Pedido no encontrado o no autorizado");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Crear una respuesta enriquecida con los detalles
            Map<String, Object> pedidoCompleto = new HashMap<>();
            pedidoCompleto.put("id", pedido.getId());
            pedidoCompleto.put("codigo", pedido.getCodigo());
            pedidoCompleto.put("fecha", pedido.getFecha()); // CORREGIDO: usar getFecha()
            pedidoCompleto.put("total", pedido.getTotal());
            pedidoCompleto.put("tipoPedido", pedido.getTipoPedido());
            pedidoCompleto.put("detallesComponentes", pedido.getDetallesComponentes());
            
            // Estado
            if (pedido.getEstado() != null) {
                Map<String, Object> estado = new HashMap<>();
                estado.put("id", pedido.getEstado().getId());
                estado.put("descripcion", pedido.getEstado().getDescripcion());
                pedidoCompleto.put("estado", estado);
            }
            
            // Personal asignado
            pedidoCompleto.put("tecnico", pedido.getTecnicoAsignado() != null ? 
                pedido.getTecnicoAsignado().getNombreCompleto() : "No asignado");
            pedidoCompleto.put("vendedor", pedido.getVendedorAsignado() != null ? 
                pedido.getVendedorAsignado().getNombreCompleto() : "No asignado");
            
            // NUEVO: Obtener detalles de productos si existen
            try {
                // CORREGIDO: usar detallePedidoServices (variable correcta) y método correcto
                List<Map<String, Object>> detallesProductos = detallePedidoServices.obtenerProductosDeOrden(pedido.getId());
                if (!detallesProductos.isEmpty()) {
                    pedidoCompleto.put("detalles", detallesProductos);
                }
            } catch (Exception e) {
                System.out.println("No se pudieron cargar los detalles de productos: " + e.getMessage());
                // No es crítico, continuar sin los detalles
            }
            
            response.put("success", true);
            response.put("pedido", pedidoCompleto);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al obtener detalle del pedido: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    /**
     * Actualizar perfil del cliente
     */
    @PostMapping("/api/{dni}/actualizar-perfil")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarPerfil(
            @PathVariable String dni, 
            @RequestBody Map<String, String> datos, 
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Verificar sesión
            String clienteSessionId = (String) session.getAttribute("clienteId");
            if (clienteSessionId == null || !clienteSessionId.equals(dni)) {
                response.put("success", false);
                response.put("message", "No autorizado");
                return ResponseEntity.badRequest().body(response);
            }
            
            Cliente cliente = clienteServices.buscarPorDni(dni);
            if (cliente == null) {
                response.put("success", false);
                response.put("message", "Cliente no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Actualizar datos
            if (datos.containsKey("nombre")) {
                cliente.setNombre(datos.get("nombre"));
            }
            if (datos.containsKey("apellido")) {
                cliente.setApellido(datos.get("apellido"));
            }
            if (datos.containsKey("correo")) {
                cliente.setCorreo(datos.get("correo"));
            }
            if (datos.containsKey("celular")) {
                cliente.setCelular(datos.get("celular"));
            }
            if (datos.containsKey("direccion")) {
                cliente.setDireccion(datos.get("direccion"));
            }
            
            Cliente clienteActualizado = clienteServices.actualizar(cliente);
            
            // Actualizar sesión
            session.setAttribute("clienteNombre", clienteActualizado.getNombreCompleto());
            
            response.put("success", true);
            response.put("message", "Perfil actualizado exitosamente");
            response.put("cliente", clienteActualizado);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar perfil: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Crear nuevo pedido de PC armada
     */
    @PostMapping("/api/{dni}/crear-pedido-pc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearPedidoPC(
            @PathVariable String dni, 
            @RequestBody Map<String, Object> datosPedido, 
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Verificar sesión
            String clienteSessionId = (String) session.getAttribute("clienteId");
            if (clienteSessionId == null || !clienteSessionId.equals(dni)) {
                response.put("success", false);
                response.put("message", "No autorizado");
                return ResponseEntity.badRequest().body(response);
            }
            
            Cliente cliente = clienteServices.buscarPorDni(dni);
            if (cliente == null) {
                response.put("success", false);
                response.put("message", "Cliente no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Extraer datos del pedido
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productos = (List<Map<String, Object>>) datosPedido.get("productos");
            Double total = Double.valueOf(datosPedido.get("total").toString());
            String observaciones = (String) datosPedido.get("observaciones");
            
            // ELIMINAR ESTAS LÍNEAS PROBLEMÁTICAS - Ya no son necesarias
            // OrdenPedido nuevoPedido = new OrdenPedido();
            // nuevoPedido.setClienteDni(dni);
            // nuevoPedido.setTipoPedido("ARMAR_PC");
            // nuevoPedido.setTotal(total);
            // nuevoPedido.setObservaciones(observaciones);
            
            // Construir descripción de componentes
            StringBuilder descripcionComponentes = new StringBuilder();
            for (Map<String, Object> producto : productos) {
                if (descripcionComponentes.length() > 0) {
                    descripcionComponentes.append(", ");
                }
                descripcionComponentes.append(producto.get("nombre"));
            }
            
            // USAR EL MÉTODO EXISTENTE crearPedidoVendedor adaptado para cliente web
            OrdenPedido pedidoCreado = ordenPedidoServices.crearPedidoVendedor(
                dni,                                          // DNI del cliente
                null,                                        // Sin vendedor (cliente web)
                BigDecimal.valueOf(total),                   // Total
                "ARMAR_PC",                                  // Tipo
                descripcionComponentes.toString()           // Descripción
            );
            
            response.put("success", true);
            response.put("message", "Pedido creado exitosamente");
            response.put("pedido", pedidoCreado);
            response.put("pedidoId", pedidoCreado.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al crear pedido: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cerrar sesión del cliente
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        // Limpiar solo las variables de sesión del cliente
        session.removeAttribute("clienteId");
        session.removeAttribute("clienteNombre");
        session.removeAttribute("clienteUsuario");
        
        return "redirect:/";
    }

    /**
     * Calcular estadísticas del cliente
     */
    private Map<String, Object> calcularEstadisticasCliente(List<OrdenPedido> pedidos) {
        Map<String, Object> stats = new HashMap<>();
        
        long totalPedidos = pedidos.size();
        long pedidosEnProceso = pedidos.stream()
            .filter(p -> p.getEstado() != null && p.getEstado().getId() != null && 
                        p.getEstado().getId() >= 1 && p.getEstado().getId() <= 3)
            .count();
        long pedidosCompletados = pedidos.stream()
            .filter(p -> p.getEstado() != null && p.getEstado().getId() != null && 
                        p.getEstado().getId() == 4)
            .count();
        
        double montoTotal = pedidos.stream()
            .mapToDouble(p -> p.getTotal() != null ? p.getTotal().doubleValue() : 0.0)
            .sum();
        
        long pedidosArmarPC = pedidos.stream()
            .filter(p -> "ARMAR_PC".equals(p.getTipoPedido()))
            .count();
        
        stats.put("totalPedidos", totalPedidos);
        stats.put("pedidosEnProceso", pedidosEnProceso);
        stats.put("pedidosCompletados", pedidosCompletados);
        stats.put("montoTotal", montoTotal);
        stats.put("pedidosArmarPC", pedidosArmarPC);
        
        return stats;
    }
    
 // REEMPLAZA el método descargarPedido con esta versión ULTRA SIMPLE:

    @GetMapping("/{dni}/pedido/{pedidoId}/descargar")
    public ResponseEntity<byte[]> descargarPedido(@PathVariable String dni, @PathVariable Integer pedidoId, HttpSession session) {
        try {
            // Verificar sesión
            String clienteSessionId = (String) session.getAttribute("clienteId");
            if (clienteSessionId == null || !clienteSessionId.equals(dni)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Buscar el pedido
            OrdenPedido pedido = ordenPedidoServices.buscarPorId(pedidoId);
            if (pedido == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Obtener productos del pedido
            List<Map<String, Object>> productos = detallePedidoServices.obtenerProductosDeOrden(pedidoId);
            
            // Generar HTML
            byte[] htmlBytes = detallePedidoServices.generarHTMLPedido(pedido, productos);
            
            // Crear headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            headers.setContentDisposition(ContentDisposition.attachment().filename("pedido_" + pedidoId + ".html").build());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(htmlBytes);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}