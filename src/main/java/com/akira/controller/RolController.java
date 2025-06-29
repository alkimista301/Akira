package com.akira.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.akira.model.Rol;
import com.akira.services.RolServices;

@Controller
@RequestMapping("/rol")
public class RolController {

    @Autowired
    private RolServices rolServices;

    @GetMapping("/lista")
    public String listarRoles(Model model) {
        try {
            List<Rol> roles = rolServices.listarTodos();
            model.addAttribute("roles", roles);
            return "rol/lista";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar roles: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/activos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarActivos() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Rol> roles = rolServices.listarActivos();
            response.put("success", true);
            response.put("data", roles);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cargar roles activos: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/empleados")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerRolesEmpleados() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Rol> roles = rolServices.obtenerRolesEmpleados();
            response.put("success", true);
            response.put("data", roles);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cargar roles de empleados: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/cliente")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerRolCliente() {
        Map<String, Object> response = new HashMap<>();
        try {
            Rol rol = rolServices.obtenerRolCliente();
            if (rol != null) {
                response.put("success", true);
                response.put("data", rol);
            } else {
                response.put("success", false);
                response.put("message", "Rol cliente no encontrado");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al obtener rol cliente: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/nuevo")
    public String nuevoRol(Model model) {
        model.addAttribute("rol", new Rol());
        return "rol/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editarRol(@PathVariable Integer id, Model model) {
        try {
            Rol rol = rolServices.buscarPorId(id);
            if (rol == null) {
                model.addAttribute("error", "Rol no encontrado");
                return "error";
            }
            model.addAttribute("rol", rol);
            return "rol/formulario";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar rol: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/guardar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> guardarRol(@RequestBody Rol rol) {
        Map<String, Object> response = new HashMap<>();
        try {
            Rol rolGuardado;
            if (rol.getId() == null) {
                rolGuardado = rolServices.registrar(rol);
                response.put("message", "Rol registrado exitosamente");
            } else {
                rolGuardado = rolServices.actualizar(rol);
                response.put("message", "Rol actualizado exitosamente");
            }
            
            response.put("success", true);
            response.put("data", rolGuardado);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/activar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> activarRol(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Rol rol = rolServices.activar(id);
            response.put("success", true);
            response.put("message", "Rol activado exitosamente");
            response.put("data", rol);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/desactivar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> desactivarRol(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Rol rol = rolServices.desactivar(id);
            response.put("success", true);
            response.put("message", "Rol desactivado exitosamente");
            response.put("data", rol);
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
            Rol rol = rolServices.buscarPorId(id);
            if (rol != null) {
                response.put("success", true);
                response.put("data", rol);
            } else {
                response.put("success", false);
                response.put("message", "Rol no encontrado");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al buscar rol: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/existe/{nombre}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> existePorNombre(@PathVariable String nombre) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean existe = rolServices.existePorNombre(nombre);
            response.put("success", true);
            response.put("existe", existe);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al verificar existencia: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/inicializar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inicializarRoles() {
        Map<String, Object> response = new HashMap<>();
        try {
            rolServices.inicializarRoles();
            response.put("success", true);
            response.put("message", "Roles inicializados exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al inicializar roles: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}