package com.akira.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.akira.model.Cliente;
import com.akira.services.ClienteServices;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private ClienteServices clienteServices;

    // ===========================
    // PÁGINAS WEB
    // ===========================

    /**
     * Listar todos los clientes
     */
    @GetMapping("/lista")
    public String listarClientes(Model model) {
        try {
            List<Cliente> clientes = clienteServices.listarTodos();
            model.addAttribute("clientes", clientes);
            model.addAttribute("totalClientes", clientes.size());
            return "cliente/lista";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar clientes: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Formulario para nuevo cliente
     */
    @GetMapping("/nuevo")
    public String nuevoCliente(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("esNuevo", true);
        return "cliente/formulario";
    }

    /**
     * Formulario para editar cliente
     */
    @GetMapping("/editar/{dni}")
    public String editarCliente(@PathVariable String dni, Model model) {
        try {
            Cliente cliente = clienteServices.buscarPorDni(dni);
            if (cliente == null) {
                model.addAttribute("error", "Cliente no encontrado");
                return "error";
            }
            
            model.addAttribute("cliente", cliente);
            model.addAttribute("esNuevo", false);
            return "cliente/formulario";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar cliente: " + e.getMessage());
            return "error";
        }
    }

    // ===========================
    // APIs REST PARA BÚSQUEDA
    // ===========================

    /**
     * Buscar cliente por DNI específico
     */
    @GetMapping("/api/buscar/{dni}")
    @ResponseBody
    public ResponseEntity<Cliente> buscarClientePorDni(@PathVariable String dni) {
        try {
            System.out.println("API: Buscando cliente con DNI: " + dni);
            Cliente cliente = clienteServices.buscarActivoPorDni(dni);
            
            if (cliente == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(cliente);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Buscar clientes por texto (nombre, apellido o DNI)
     */
    @GetMapping("/api/buscar")
    @ResponseBody
    public ResponseEntity<?> buscarClientes(@RequestParam String texto) {
        try {
            System.out.println("API: Buscando clientes con texto: " + texto);
            
            List<Cliente> clientes = clienteServices.busquedaAvanzada(texto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", clientes);
            response.put("total", clientes.size());
            response.put("termino", texto);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al buscar clientes: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Buscar clientes por nombre específico
     */
    @GetMapping("/api/buscarPorNombre")
    @ResponseBody
    public ResponseEntity<List<Cliente>> buscarClientesPorNombre(@RequestParam String nombre) {
        try {
            System.out.println("API: Buscando clientes con nombre: " + nombre);
            List<Cliente> clientes = clienteServices.buscarPorNombre(nombre);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener todos los clientes activos
     */
    @GetMapping("/api/todos")
    @ResponseBody
    public ResponseEntity<List<Cliente>> obtenerTodosLosClientes() {
        try {
            List<Cliente> clientes = clienteServices.listarTodos();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===========================
    // APIs REST PARA CRUD
    // ===========================

    /**
     * Registrar nuevo cliente
     */
    @PostMapping("/api/registrar")
    @ResponseBody
    public ResponseEntity<?> registrarCliente(@RequestBody Map<String, String> datos) {
        try {
            // Extraer datos del request
            String dni = datos.get("dni");
            String nombre = datos.get("nombre");
            String apellido = datos.get("apellido");
            String correo = datos.get("correo");
            String celular = datos.get("celular");
            String direccion = datos.get("direccion");
            
            // Crear nuevo cliente
            Cliente nuevoCliente = new Cliente();
            nuevoCliente.setDni(dni);
            nuevoCliente.setNombre(nombre);
            nuevoCliente.setApellido(apellido != null ? apellido : "");
            nuevoCliente.setCorreo(correo);
            nuevoCliente.setCelular(celular);
            nuevoCliente.setDireccion(direccion);
            
            Cliente clienteGuardado = clienteServices.registrar(nuevoCliente);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente registrado exitosamente");
            response.put("cliente", clienteGuardado);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar cliente: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Actualizar cliente existente
     */
    @PutMapping("/api/actualizar")
    @ResponseBody
    public ResponseEntity<?> actualizarCliente(@RequestBody Cliente cliente) {
        try {
            Cliente clienteActualizado = clienteServices.actualizar(cliente);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente actualizado exitosamente");
            response.put("cliente", clienteActualizado);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al actualizar cliente: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Guardar cliente (crear o actualizar)
     */
    @PostMapping("/api/guardar")
    @ResponseBody
    public ResponseEntity<?> guardarCliente(@RequestBody Cliente cliente) {
        try {
            Cliente clienteGuardado;
            String mensaje;
            
            if (clienteServices.existePorDni(cliente.getDni())) {
                clienteGuardado = clienteServices.actualizar(cliente);
                mensaje = "Cliente actualizado exitosamente";
            } else {
                clienteGuardado = clienteServices.registrar(cliente);
                mensaje = "Cliente registrado exitosamente";
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", mensaje);
            response.put("cliente", clienteGuardado);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Activar cliente
     */
    @PostMapping("/api/activar/{dni}")
    @ResponseBody
    public ResponseEntity<?> activarCliente(@PathVariable String dni) {
        try {
            Cliente cliente = clienteServices.activar(dni);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente activado exitosamente");
            response.put("cliente", cliente);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Desactivar cliente
     */
    @PostMapping("/api/desactivar/{dni}")
    @ResponseBody
    public ResponseEntity<?> desactivarCliente(@PathVariable String dni) {
        try {
            Cliente cliente = clienteServices.desactivar(dni);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente desactivado exitosamente");
            response.put("cliente", cliente);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ===========================
    // APIs PARA VALIDACIONES
    // ===========================

    /**
     * Verificar si existe cliente por DNI
     */
    @GetMapping("/api/existe-dni/{dni}")
    @ResponseBody
    public ResponseEntity<?> existePorDni(@PathVariable String dni) {
        try {
            boolean existe = clienteServices.existePorDni(dni);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("existe", existe);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al verificar DNI: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Verificar si existe cliente por correo
     */
    @GetMapping("/api/existe-correo/{correo}")
    @ResponseBody
    public ResponseEntity<?> existePorCorreo(@PathVariable String correo) {
        try {
            boolean existe = clienteServices.existePorCorreo(correo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("existe", existe);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al verificar correo: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Verificar si cliente es válido para pedidos
     */
    @GetMapping("/api/valido-para-pedidos/{dni}")
    @ResponseBody
    public ResponseEntity<?> esValidoParaPedidos(@PathVariable String dni) {
        try {
            boolean esValido = clienteServices.esValidoParaPedidos(dni);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("valido", esValido);
            
            if (!esValido) {
                response.put("message", "Cliente no válido para realizar pedidos");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al validar cliente: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ===========================
    // APIs PARA ESTADÍSTICAS
    // ===========================

    /**
     * Obtener estadísticas de clientes
     */
    @GetMapping("/api/estadisticas")
    @ResponseBody
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            long totalActivos = clienteServices.contarClientesActivos();
            List<Cliente> conContactoCompleto = clienteServices.obtenerClientesConContactoCompleto();
            List<Cliente> conDNI = clienteServices.obtenerClientesConDNI();
            List<Cliente> conRUC = clienteServices.obtenerClientesConRUC();
            
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalActivos", totalActivos);
            estadisticas.put("conContactoCompleto", conContactoCompleto.size());
            estadisticas.put("conDNI", conDNI.size());
            estadisticas.put("conRUC", conRUC.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("estadisticas", estadisticas);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}