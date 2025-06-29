package com.akira.controller;

import java.time.LocalDateTime; // ← AGREGAR ESTE IMPORT
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // ← AGREGAR ESTE IMPORT

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

import com.akira.model.Usuario;
import com.akira.model.Rol;
import com.akira.services.UsuarioServices;
import com.akira.services.RolServices;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioServices usuarioServices;
    
    @Autowired
    private RolServices rolServices;

    @GetMapping("/lista")
    public String listarUsuarios(Model model) {
        try {
            List<Usuario> usuarios = usuarioServices.listarActivos();
            List<Rol> roles = rolServices.listarActivos();
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("roles", roles);
            return "usuario/lista";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar usuarios: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/clientes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarClientes() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Usuario> clientes = usuarioServices.listarClientes();
            response.put("success", true);
            response.put("data", clientes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cargar clientes: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/empleados")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarEmpleados() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Usuario> empleados = usuarioServices.listarEmpleados();
            response.put("success", true);
            response.put("data", empleados);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cargar empleados: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/tecnicos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarTecnicos() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Usuario> tecnicos = usuarioServices.listarTecnicos();
            response.put("success", true);
            response.put("data", tecnicos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cargar técnicos: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/vendedores")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarVendedores() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Usuario> vendedores = usuarioServices.listarVendedores();
            response.put("success", true);
            response.put("data", vendedores);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cargar vendedores: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/nuevo-cliente")
    public String nuevoCliente(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuario/formulario-cliente";
    }

    @GetMapping("/nuevo-empleado")
    public String nuevoEmpleado(Model model) {
        try {
            List<Rol> rolesEmpleados = rolServices.obtenerRolesEmpleados();
            model.addAttribute("usuario", new Usuario());
            model.addAttribute("roles", rolesEmpleados);
            return "usuario/formulario-empleado";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar formulario: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/editar/{dni}")
    public String editarUsuario(@PathVariable String dni, Model model) {
        try {
            Usuario usuario = usuarioServices.buscarPorId(dni);
            if (usuario == null) {
                model.addAttribute("error", "Usuario no encontrado");
                return "error";
            }
            
            List<Rol> roles = rolServices.listarActivos();
            model.addAttribute("usuario", usuario);
            model.addAttribute("roles", roles);
            
            if (usuario.esCliente()) {
                return "usuario/formulario-cliente";
            } else {
                return "usuario/formulario-empleado";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar usuario: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/guardar-cliente")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> guardarCliente(@RequestBody Usuario usuario) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario usuarioGuardado;
            if (usuarioServices.existePorDni(usuario.getId())) {
                usuarioGuardado = usuarioServices.actualizar(usuario);
                response.put("message", "Cliente actualizado exitosamente");
            } else {
                usuarioGuardado = usuarioServices.registrarCliente(usuario);
                response.put("message", "Cliente registrado exitosamente");
            }
            
            response.put("success", true);
            response.put("data", usuarioGuardado);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/guardar-empleado")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> guardarEmpleado(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario usuario = new Usuario();
            usuario.setId((String) requestData.get("id"));
            usuario.setNombre((String) requestData.get("nombre"));
            usuario.setApellido((String) requestData.get("apellido"));
            usuario.setCorreo((String) requestData.get("correo"));
            usuario.setCelular((String) requestData.get("celular"));
            usuario.setDireccion((String) requestData.get("direccion"));
            usuario.setUsuario((String) requestData.get("usuario"));
            usuario.setPassword((String) requestData.get("password"));
            
            String nombreRol = (String) requestData.get("nombreRol");
            
            Usuario usuarioGuardado;
            if (usuarioServices.existePorDni(usuario.getId())) {
                usuarioGuardado = usuarioServices.actualizar(usuario);
                response.put("message", "Empleado actualizado exitosamente");
            } else {
                usuarioGuardado = usuarioServices.registrarEmpleado(usuario, nombreRol);
                response.put("message", "Empleado registrado exitosamente");
            }
            
            response.put("success", true);
            response.put("data", usuarioGuardado);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/activar/{dni}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> activarUsuario(@PathVariable String dni) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario usuario = usuarioServices.activar(dni);
            response.put("success", true);
            response.put("message", "Usuario activado exitosamente");
            response.put("data", usuario);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/desactivar/{dni}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> desactivarUsuario(@PathVariable String dni) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario usuario = usuarioServices.desactivar(dni);
            response.put("success", true);
            response.put("message", "Usuario desactivado exitosamente");
            response.put("data", usuario);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/buscar/{dni}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarPorDni(@PathVariable String dni) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario usuario = usuarioServices.buscarPorId(dni);
            if (usuario != null) {
                response.put("success", true);
                response.put("data", usuario);
            } else {
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al buscar usuario: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ===========================
    // APIs PARA BÚSQUEDA DE CLIENTES (CORREGIDAS)
    // ===========================
    
    /**
     * API para buscar usuarios por texto (nombre o apellido)
     */
    @GetMapping("/buscar-por-texto")
    @ResponseBody
    public ResponseEntity<?> buscarUsuariosPorTexto(@RequestParam String texto) {
        try {
            System.out.println("API: Buscando usuarios con texto: " + texto);
            
            List<Usuario> usuarios = usuarioServices.buscarPorNombreOApellido(texto);
            
            // Crear respuesta estructurada
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", usuarios);
            response.put("total", usuarios.size());
            response.put("termino", texto);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al buscar usuarios: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * API para buscar cliente específico por ID (DNI)
     */
    @GetMapping("/api/buscar/{id}")
    @ResponseBody
    public ResponseEntity<Usuario> buscarClientePorId(@PathVariable String id) {
        try {
            System.out.println("API: Buscando cliente con ID: " + id);
            Usuario usuario = usuarioServices.buscarPorId(id);
            
            if (usuario == null || !usuario.esCliente()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * API para obtener todos los clientes
     */
    @GetMapping("/api/clientes")
    @ResponseBody
    public ResponseEntity<List<Usuario>> obtenerTodosLosClientes() {
        try {
            List<Usuario> clientes = usuarioServices.listarClientes();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * API para buscar clientes por nombre específico
     */
    @GetMapping("/api/buscarPorNombre")
    @ResponseBody
    public ResponseEntity<List<Usuario>> buscarClientesPorNombre(@RequestParam String nombre) {
        try {
            System.out.println("API: Buscando clientes con nombre: " + nombre);
            
            List<Usuario> todosLosUsuarios = usuarioServices.buscarPorNombreOApellido(nombre);
            
            // Filtrar solo clientes
            List<Usuario> clientes = todosLosUsuarios.stream()
                .filter(Usuario::esCliente)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * API para registrar nuevo cliente
     */
    @PostMapping("/api/registrar-cliente")
    @ResponseBody
    public ResponseEntity<?> registrarCliente(@RequestBody Map<String, String> datos) {
        try {
            String dni = datos.get("dni");
            String nombre = datos.get("nombre");
            String apellido = datos.get("apellido");
            String correo = datos.get("correo");
            String celular = datos.get("celular");
            String direccion = datos.get("direccion");
            
            // Buscar el rol CLIENTE
            Rol rolCliente = rolServices.buscarPorNombre("CLIENTE");
            if (rolCliente == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Rol CLIENTE no encontrado");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Crear nuevo usuario cliente
            Usuario nuevoCliente = new Usuario();
            nuevoCliente.setId(dni);
            nuevoCliente.setNombre(nombre);
            nuevoCliente.setApellido(apellido != null ? apellido : "");
            nuevoCliente.setCorreo(correo);
            nuevoCliente.setCelular(celular);
            nuevoCliente.setDireccion(direccion);
            nuevoCliente.setRol(rolCliente);
            nuevoCliente.setActivo(true);
            nuevoCliente.setFechaRegistro(LocalDateTime.now());
            
            // ← CORREGIR AQUÍ: usar registrarCliente en lugar de registrar
            Usuario clienteGuardado = usuarioServices.registrarCliente(nuevoCliente);
            
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
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ===========================
    // RESTO DE ENDPOINTS ORIGINALES
    // ===========================

    @PostMapping("/autenticar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> autenticar(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        try {
            String usuario = credentials.get("usuario");
            String password = credentials.get("password");
            
            Usuario usuarioAutenticado = usuarioServices.autenticar(usuario, password);
            if (usuarioAutenticado != null) {
                response.put("success", true);
                response.put("message", "Autenticación exitosa");
                response.put("data", usuarioAutenticado);
            } else {
                response.put("success", false);
                response.put("message", "Credenciales inválidas");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error en autenticación: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/cambiar-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cambiarPassword(@RequestBody Map<String, String> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            String dni = requestData.get("dni");
            String passwordNueva = requestData.get("passwordNueva");
            
            Usuario usuario = usuarioServices.cambiarPassword(dni, passwordNueva);
            response.put("success", true);
            response.put("message", "Contraseña cambiada exitosamente");
            response.put("data", usuario);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/existe-dni/{dni}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> existePorDni(@PathVariable String dni) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean existe = usuarioServices.existePorDni(dni);
            response.put("success", true);
            response.put("existe", existe);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al verificar DNI: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/existe-usuario/{usuario}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> existePorUsuario(@PathVariable String usuario) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean existe = usuarioServices.existePorUsuario(usuario);
            response.put("success", true);
            response.put("existe", existe);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al verificar usuario: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/existe-correo/{correo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> existePorCorreo(@PathVariable String correo) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean existe = usuarioServices.existePorCorreo(correo);
            response.put("success", true);
            response.put("existe", existe);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al verificar correo: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}