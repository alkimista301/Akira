package com.akira.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.akira.model.Cliente;
import com.akira.model.Usuario;
import com.akira.services.ClienteServices;
import com.akira.services.ProductoServices;
import com.akira.services.UsuarioServices;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioServices usuarioServices;
    
    @Autowired
    private ClienteServices clienteServices;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        return "auth/login";
    }

    /**
     * MÉTODO PRINCIPAL - Autenticación unificada (usuarios internos + clientes)
     */
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> autenticar(@RequestBody Map<String, String> credentials, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            String usuario = credentials.get("usuario");
            String password = credentials.get("password");
            
            // PASO 1: Intentar autenticar como usuario interno (dueño, vendedor, técnico)
            Usuario usuarioInterno = usuarioServices.autenticar(usuario, password);
            
            if (usuarioInterno != null) {
                // Login exitoso como usuario interno
                session.setAttribute("usuarioId", usuarioInterno.getId());
                session.setAttribute("usuarioNombre", usuarioInterno.getNombreCompleto());
                session.setAttribute("usuarioRol", usuarioInterno.getRol().getNombre());
                
                response.put("success", true);
                response.put("message", "Login exitoso");
                response.put("tipoUsuario", "interno");
                response.put("redirectUrl", "/dashboard/" + usuarioInterno.getId());
                
                return ResponseEntity.ok(response);
            }
            
            // PASO 2: Si no es usuario interno, intentar autenticar como cliente
            Cliente cliente = clienteServices.autenticarCliente(usuario, password);
            
            if (cliente != null) {
                // Login exitoso como cliente
                session.setAttribute("clienteId", cliente.getDni());
                session.setAttribute("clienteNombre", cliente.getNombreCompleto());
                session.setAttribute("clienteUsuario", cliente.getUsuario());
                
                response.put("success", true);
                response.put("message", "Login exitoso");
                response.put("tipoUsuario", "cliente");
                response.put("clienteNombre", cliente.getNombreCompleto());
                response.put("redirectUrl", "/cliente-dashboard/" + cliente.getDni());
                
                return ResponseEntity.ok(response);
            }
            
            // PASO 3: Si ninguna autenticación funcionó
            response.put("success", false);
            response.put("message", "Usuario o contraseña incorrectos");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error en la autenticación: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cerrar sesión unificada
     */
    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Limpiar toda la sesión
            session.invalidate();
            
            response.put("success", true);
            response.put("message", "Sesión cerrada exitosamente");
            response.put("redirectUrl", "/");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cerrar sesión: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Verificar estado de autenticación
     */
    @GetMapping("/estado-sesion")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> estadoSesion(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            String clienteId = (String) session.getAttribute("clienteId");
            
            if (usuarioId != null) {
                // Usuario interno autenticado
                response.put("autenticado", true);
                response.put("tipoUsuario", "interno");
                response.put("usuarioNombre", session.getAttribute("usuarioNombre"));
                response.put("usuarioRol", session.getAttribute("usuarioRol"));
            } else if (clienteId != null) {
                // Cliente autenticado
                response.put("autenticado", true);
                response.put("tipoUsuario", "cliente");
                response.put("clienteNombre", session.getAttribute("clienteNombre"));
                response.put("clienteUsuario", session.getAttribute("clienteUsuario"));
            } else {
                // No autenticado
                response.put("autenticado", false);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al verificar estado de sesión: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Registro de nuevos clientes
     */
    @PostMapping("/registro-cliente")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registroCliente(@RequestBody Map<String, String> datos, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            String dni = datos.get("dni");
            String nombre = datos.get("nombre");
            String apellido = datos.get("apellido");
            String correo = datos.get("correo");
            String celular = datos.get("celular");
            String direccion = datos.get("direccion");
            String usuario = datos.get("usuario");
            String password = datos.get("password");
            
            // Verificar si ya existe
            if (clienteServices.existePorDni(dni)) {
                response.put("success", false);
                response.put("message", "Ya existe un cliente con ese DNI");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (clienteServices.existePorUsuario(usuario)) {
                response.put("success", false);
                response.put("message", "El nombre de usuario ya está en uso");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Crear nuevo cliente
            Cliente nuevoCliente = new Cliente();
            nuevoCliente.setDni(dni);
            nuevoCliente.setNombre(nombre);
            nuevoCliente.setApellido(apellido != null ? apellido : "");
            nuevoCliente.setCorreo(correo);
            nuevoCliente.setCelular(celular);
            nuevoCliente.setDireccion(direccion);
            nuevoCliente.setUsuario(usuario);
            nuevoCliente.setPassword(password);
            
            Cliente clienteGuardado = clienteServices.registrar(nuevoCliente);
            
            // Auto-login después del registro
            session.setAttribute("clienteId", clienteGuardado.getDni());
            session.setAttribute("clienteNombre", clienteGuardado.getNombreCompleto());
            session.setAttribute("clienteUsuario", clienteGuardado.getUsuario());
            
            response.put("success", true);
            response.put("message", "Cliente registrado exitosamente");
            response.put("tipoUsuario", "cliente");
            response.put("clienteNombre", clienteGuardado.getNombreCompleto());
            response.put("redirectUrl", "/cliente-dashboard/" + clienteGuardado.getDni());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al registrar cliente: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Obtener cliente actual de la sesión
     */
    @GetMapping("/cliente-actual")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerClienteActual(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            String clienteId = (String) session.getAttribute("clienteId");
            
            if (clienteId != null) {
                Cliente cliente = clienteServices.buscarPorDni(clienteId);
                
                if (cliente != null) {
                    Map<String, Object> clienteData = new HashMap<>();
                    clienteData.put("dni", cliente.getDni());
                    clienteData.put("nombre", cliente.getNombreCompleto());
                    clienteData.put("usuario", cliente.getUsuario());
                    clienteData.put("correo", cliente.getCorreo());
                    
                    response.put("success", true);
                    response.put("data", clienteData);
                } else {
                    response.put("success", false);
                    response.put("message", "Cliente no encontrado");
                }
            } else {
                response.put("success", false);
                response.put("message", "Cliente no autenticado");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener cliente actual: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtener información del usuario actual logueado
     */
    @GetMapping("/usuario-actual")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerUsuarioActual(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            String usuarioId = (String) session.getAttribute("usuarioId");
            
            if (usuarioId != null) {
                // Usuario interno logueado
                Usuario usuario = usuarioServices.buscarPorId(usuarioId);
                
                if (usuario != null) {
                    Map<String, Object> usuarioData = new HashMap<>();
                    usuarioData.put("id", usuario.getId());
                    usuarioData.put("nombre", usuario.getNombre());
                    usuarioData.put("apellido", usuario.getApellido());
                    usuarioData.put("nombreCompleto", usuario.getNombreCompleto());
                    usuarioData.put("rol", usuario.getRol().getNombre());
                    usuarioData.put("tipoUsuario", "interno");
                    
                    response.put("success", true);
                    response.put("data", usuarioData);
                    response.put("message", "Usuario encontrado");
                } else {
                    response.put("success", false);
                    response.put("message", "Usuario no encontrado en base de datos");
                }
            } else {
                // Verificar si es un cliente logueado
                String clienteId = (String) session.getAttribute("clienteId");
                if (clienteId != null) {
                    Cliente cliente = clienteServices.buscarPorDni(clienteId);
                    if (cliente != null) {
                        Map<String, Object> clienteData = new HashMap<>();
                        clienteData.put("id", cliente.getDni());
                        clienteData.put("nombre", cliente.getNombre());
                        clienteData.put("apellido", cliente.getApellido());
                        clienteData.put("nombreCompleto", cliente.getNombreCompleto());
                        clienteData.put("tipoUsuario", "cliente");
                        
                        response.put("success", true);
                        response.put("data", clienteData);
                        response.put("message", "Cliente encontrado");
                    } else {
                        response.put("success", false);
                        response.put("message", "Cliente no encontrado");
                    }
                } else {
                    response.put("success", false);
                    response.put("message", "No hay usuario autenticado");
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener usuario actual: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}