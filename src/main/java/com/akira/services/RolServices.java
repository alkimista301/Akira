package com.akira.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.akira.model.Rol;
import com.akira.repository.RolRepository;

@Service
public class RolServices {

    @Autowired
    private RolRepository rolRepository;

    /**
     * Listar todos los roles
     */
    public List<Rol> listarTodos() {
        try {
            return rolRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar roles: " + e.getMessage());
        }
    }

    /**
     * Listar roles activos ordenados por nombre
     */
    public List<Rol> listarActivos() {
        try {
            return rolRepository.findActivosOrderByNombre();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar roles activos: " + e.getMessage());
        }
    }

    /**
     * Buscar rol por ID
     */
    public Rol buscarPorId(Integer id) {
        try {
            return rolRepository.findById(id).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Buscar rol por nombre
     */
    public Rol buscarPorNombre(String nombre) {
        try {
            return rolRepository.findByNombre(nombre);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Buscar rol por nombre ignorando mayúsculas/minúsculas
     */
    public Rol buscarPorNombreIgnoreCase(String nombre) {
        try {
            return rolRepository.findByNombreIgnoreCase(nombre);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtener roles para empleados (no clientes)
     */
    public List<Rol> obtenerRolesEmpleados() {
        try {
            return rolRepository.findRolesEmpleados();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener roles de empleados: " + e.getMessage());
        }
    }

    /**
     * Obtener rol de cliente
     */
    public Rol obtenerRolCliente() {
        try {
            return rolRepository.findRolCliente();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Registrar nuevo rol
     */
    public Rol registrar(Rol rol) {
        try {
            // Validar que el nombre no exista
            if (rolRepository.existsByNombreIgnoreCase(rol.getNombre())) {
                throw new RuntimeException("Ya existe un rol con el nombre: " + rol.getNombre());
            }

            // Asegurar que esté activo por defecto
            if (rol.getActivo() == null) {
                rol.setActivo(true);
            }

            return rolRepository.save(rol);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar rol: " + e.getMessage());
        }
    }

    /**
     * Actualizar rol existente
     */
    public Rol actualizar(Rol rol) {
        try {
            // Verificar que el rol existe
            if (!rolRepository.existsById(rol.getId())) {
                throw new RuntimeException("El rol con ID " + rol.getId() + " no existe");
            }

            // Validar que el nombre no esté siendo usado por otro rol
            Rol rolExistente = rolRepository.findByNombreIgnoreCase(rol.getNombre());
            if (rolExistente != null && !rolExistente.getId().equals(rol.getId())) {
                throw new RuntimeException("Ya existe otro rol con el nombre: " + rol.getNombre());
            }

            return rolRepository.save(rol);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar rol: " + e.getMessage());
        }
    }

    /**
     * Activar rol
     */
    public Rol activar(Integer id) {
        try {
            Rol rol = buscarPorId(id);
            if (rol == null) {
                throw new RuntimeException("Rol no encontrado con ID: " + id);
            }

            rol.setActivo(true);
            return rolRepository.save(rol);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al activar rol: " + e.getMessage());
        }
    }

    /**
     * Desactivar rol
     */
    public Rol desactivar(Integer id) {
        try {
            Rol rol = buscarPorId(id);
            if (rol == null) {
                throw new RuntimeException("Rol no encontrado con ID: " + id);
            }

            rol.setActivo(false);
            return rolRepository.save(rol);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al desactivar rol: " + e.getMessage());
        }
    }

    /**
     * Verificar si existe rol por nombre
     */
    public boolean existePorNombre(String nombre) {
        try {
            return rolRepository.existsByNombreIgnoreCase(nombre);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Contar roles activos
     */
    public long contarActivos() {
        try {
            return rolRepository.countByActivos();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Inicializar roles del sistema (método para primera configuración)
     */
    public void inicializarRoles() {
        try {
            // Crear roles básicos si no existen
            String[] rolesBasicos = {"DUEÑO", "VENDEDOR", "TECNICO", "CLIENTE"};
            String[] descripciones = {
                "Administrador general del sistema",
                "Encargado de ventas y atención al cliente",
                "Técnico especializado en ensamblaje y reparación",
                "Cliente del sistema"
            };

            for (int i = 0; i < rolesBasicos.length; i++) {
                if (!existePorNombre(rolesBasicos[i])) {
                    Rol rol = new Rol(rolesBasicos[i], descripciones[i]);
                    registrar(rol);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al inicializar roles: " + e.getMessage());
        }
    }
}