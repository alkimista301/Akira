package com.akira.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.akira.model.Usuario;
import com.akira.model.Rol;
import com.akira.repository.UsuarioRepository;
import com.akira.repository.RolRepository;

@Service
public class UsuarioServices {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;

    public List<Usuario> listarTodos() {
        try {
            return usuarioRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar usuarios: " + e.getMessage());
        }
    }

    public List<Usuario> listarActivos() {
        try {
            return usuarioRepository.findAllOrderByNombreCompleto();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar usuarios activos: " + e.getMessage());
        }
    }

    public Usuario buscarPorId(String id) {
        try {
            return usuarioRepository.findById(id).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Usuario buscarPorUsuario(String usuario) {
        try {
            return usuarioRepository.findByUsuario(usuario).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Usuario buscarPorCorreo(String correo) {
        try {
            return usuarioRepository.findByCorreo(correo).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public List<Usuario> listarEmpleados() {
        try {
            return usuarioRepository.findAllEmpleados();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar empleados: " + e.getMessage());
        }
    }

    public List<Usuario> listarTecnicos() {
        try {
            return usuarioRepository.findTecnicos();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar técnicos: " + e.getMessage());
        }
    }

    public List<Usuario> listarVendedores() {
        try {
            return usuarioRepository.findVendedores();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar vendedores: " + e.getMessage());
        }
    }


    public Usuario registrarCliente(Usuario usuario) {
        try {
            Rol rolCliente = rolRepository.findRolCliente();
            if (rolCliente == null) {
                throw new RuntimeException("Rol CLIENTE no encontrado en el sistema");
            }

            if (usuarioRepository.existsById(usuario.getId())) {
                throw new RuntimeException("Ya existe un usuario con el DNI: " + usuario.getId());
            }

            if (usuario.getCorreo() != null && usuarioRepository.existsByCorreo(usuario.getCorreo())) {
                throw new RuntimeException("Ya existe un usuario con el correo: " + usuario.getCorreo());
            }

            if (usuario.getCelular() != null && usuarioRepository.existsByCelular(usuario.getCelular())) {
                throw new RuntimeException("Ya existe un usuario con el celular: " + usuario.getCelular());
            }

            usuario.setRol(rolCliente);
            usuario.setActivo(true);
            usuario.setFechaRegistro(LocalDateTime.now());

            return usuarioRepository.save(usuario);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar cliente: " + e.getMessage());
        }
    }

    public Usuario registrarEmpleado(Usuario usuario, String nombreRol) {
        try {
            Rol rol = rolRepository.findByNombre(nombreRol);
            if (rol == null) {
                throw new RuntimeException("Rol " + nombreRol + " no encontrado");
            }

            if (usuarioRepository.existsById(usuario.getId())) {
                throw new RuntimeException("Ya existe un usuario con el DNI: " + usuario.getId());
            }

            if (usuario.getUsuario() != null && usuarioRepository.existsByUsuario(usuario.getUsuario())) {
                throw new RuntimeException("Ya existe un usuario con el nombre de usuario: " + usuario.getUsuario());
            }

            if (usuario.getCorreo() != null && usuarioRepository.existsByCorreo(usuario.getCorreo())) {
                throw new RuntimeException("Ya existe un usuario con el correo: " + usuario.getCorreo());
            }

            usuario.setRol(rol);
            usuario.setActivo(true);
            usuario.setFechaRegistro(LocalDateTime.now());

            return usuarioRepository.save(usuario);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar empleado: " + e.getMessage());
        }
    }

    public Usuario actualizar(Usuario usuario) {
        try {
            Usuario usuarioExistente = buscarPorId(usuario.getId());
            if (usuarioExistente == null) {
                throw new RuntimeException("Usuario no encontrado con DNI: " + usuario.getId());
            }

            if (usuario.getCorreo() != null) {
                Usuario usuarioConCorreo = buscarPorCorreo(usuario.getCorreo());
                if (usuarioConCorreo != null && !usuarioConCorreo.getId().equals(usuario.getId())) {
                    throw new RuntimeException("Ya existe otro usuario con el correo: " + usuario.getCorreo());
                }
            }

            if (usuario.getUsuario() != null) {
                Usuario usuarioConNombreUsuario = buscarPorUsuario(usuario.getUsuario());
                if (usuarioConNombreUsuario != null && !usuarioConNombreUsuario.getId().equals(usuario.getId())) {
                    throw new RuntimeException("Ya existe otro usuario con el nombre de usuario: " + usuario.getUsuario());
                }
            }

            return usuarioRepository.save(usuario);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar usuario: " + e.getMessage());
        }
    }

    public Usuario activar(String id) {
        try {
            Usuario usuario = buscarPorId(id);
            if (usuario == null) {
                throw new RuntimeException("Usuario no encontrado con DNI: " + id);
            }

            usuario.setActivo(true);
            return usuarioRepository.save(usuario);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al activar usuario: " + e.getMessage());
        }
    }

    public Usuario desactivar(String id) {
        try {
            Usuario usuario = buscarPorId(id);
            if (usuario == null) {
                throw new RuntimeException("Usuario no encontrado con DNI: " + id);
            }

            usuario.setActivo(false);
            return usuarioRepository.save(usuario);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al desactivar usuario: " + e.getMessage());
        }
    }

    public Usuario autenticar(String usuario, String password) {
        try {
            Usuario user = buscarPorUsuario(usuario);
            if (user == null || !user.getActivo()) {
                return null;
            }

            if (password.equals(user.getPassword())) {
                user.setFechaUltimoAcceso(LocalDateTime.now());
                usuarioRepository.save(user);
                return user;
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean existePorDni(String dni) {
        try {
            return usuarioRepository.existsById(dni);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existePorUsuario(String usuario) {
        try {
            return usuarioRepository.existsByUsuario(usuario);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existePorCorreo(String correo) {
        try {
            return usuarioRepository.existsByCorreo(correo);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long contarPorRol(String nombreRol) {
        try {
            return usuarioRepository.countByRolNombre(nombreRol);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Usuario cambiarPassword(String dni, String passwordNueva) {
        try {
            Usuario usuario = buscarPorId(dni);
            if (usuario == null) {
                throw new RuntimeException("Usuario no encontrado");
            }

            usuario.setPassword(passwordNueva);
            return usuarioRepository.save(usuario);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al cambiar password: " + e.getMessage());
        }
    }
    
    /**
     * Buscar usuarios por nombre o apellido que contenga el texto
     */
    public List<Usuario> buscarPorNombreOApellido(String texto) {
        try {
            if (texto == null || texto.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            return usuarioRepository.findByNombreOrApellidoContaining(texto.trim());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar usuarios por nombre o apellido: " + e.getMessage());
        }
    }
    
    /**
     * Listar todos los clientes activos
     */
    public List<Usuario> listarClientes() {
        try {
            return usuarioRepository.findAllClientes();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar clientes: " + e.getMessage());
        }
    }
}