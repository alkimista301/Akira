package com.akira.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.akira.model.Cliente;
import com.akira.repository.ClienteRepository;

@Service
public class ClienteServices {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Listar todos los clientes activos
     */
    public List<Cliente> listarTodos() {
        try {
            return clienteRepository.findAllActivosOrdenados();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar clientes: " + e.getMessage());
        }
    }

    /**
     * Listar todos los clientes (incluye inactivos)
     */
    public List<Cliente> listarTodosInclusoInactivos() {
        try {
            return clienteRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar todos los clientes: " + e.getMessage());
        }
    }

    /**
     * Buscar cliente por DNI
     */
    public Cliente buscarPorDni(String dni) {
        try {
            return clienteRepository.findById(dni).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Buscar cliente activo por DNI
     */
    public Cliente buscarActivoPorDni(String dni) {
        try {
            Cliente cliente = buscarPorDni(dni);
            return (cliente != null && cliente.getActivo()) ? cliente : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Buscar clientes por nombre o apellido
     */
    public List<Cliente> buscarPorNombreOApellido(String texto) {
        try {
            if (texto == null || texto.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return clienteRepository.findByNombreOrApellidoContaining(texto.trim());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar clientes por nombre o apellido: " + e.getMessage());
        }
    }

    /**
     * Buscar clientes por nombre
     */
    public List<Cliente> buscarPorNombre(String nombre) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return clienteRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre.trim());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar clientes por nombre: " + e.getMessage());
        }
    }

    /**
     * Buscar clientes por apellido
     */
    public List<Cliente> buscarPorApellido(String apellido) {
        try {
            if (apellido == null || apellido.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return clienteRepository.findByApellidoContainingIgnoreCaseAndActivoTrue(apellido.trim());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar clientes por apellido: " + e.getMessage());
        }
    }

    /**
     * Buscar cliente por correo
     */
    public Cliente buscarPorCorreo(String correo) {
        try {
            return clienteRepository.findByCorreoAndActivoTrue(correo);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Buscar cliente por celular
     */
    public Cliente buscarPorCelular(String celular) {
        try {
            return clienteRepository.findByCelularAndActivoTrue(celular);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Búsqueda avanzada por múltiples criterios
     */
    public List<Cliente> busquedaAvanzada(String texto) {
        try {
            if (texto == null || texto.trim().isEmpty()) {
                return listarTodos();
            }
            return clienteRepository.busquedaAvanzada(texto.trim());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error en búsqueda avanzada: " + e.getMessage());
        }
    }

    /**
     * Registrar nuevo cliente
     */
    public Cliente registrar(Cliente cliente) {
        try {
            // Validaciones
            if (cliente.getDni() == null || cliente.getDni().trim().isEmpty()) {
                throw new RuntimeException("El DNI es obligatorio");
            }

            if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
                throw new RuntimeException("El nombre es obligatorio");
            }

            if (cliente.getApellido() == null || cliente.getApellido().trim().isEmpty()) {
                throw new RuntimeException("El apellido es obligatorio");
            }

            // Verificar que no exista cliente con mismo DNI
            if (clienteRepository.existsById(cliente.getDni())) {
                throw new RuntimeException("Ya existe un cliente con el DNI: " + cliente.getDni());
            }

            // Verificar correo único (si se proporciona)
            if (cliente.getCorreo() != null && !cliente.getCorreo().trim().isEmpty()) {
                if (clienteRepository.existsByCorreo(cliente.getCorreo())) {
                    throw new RuntimeException("Ya existe un cliente con el correo: " + cliente.getCorreo());
                }
            }

            // Verificar celular único (si se proporciona)
            if (cliente.getCelular() != null && !cliente.getCelular().trim().isEmpty()) {
                if (clienteRepository.existsByCelular(cliente.getCelular())) {
                    throw new RuntimeException("Ya existe un cliente con el celular: " + cliente.getCelular());
                }
            }

            // Validar formato de DNI/RUC
            String dni = cliente.getDni().trim();
            if (!dni.matches("\\d{8}") && !dni.matches("\\d{11}")) {
                throw new RuntimeException("El DNI debe tener 8 dígitos o RUC debe tener 11 dígitos");
            }

            // Establecer valores por defecto
            cliente.setFechaRegistro(LocalDateTime.now());
            cliente.setActivo(true);

            return clienteRepository.save(cliente);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar cliente: " + e.getMessage());
        }
    }

    /**
     * Actualizar cliente existente
     */
    public Cliente actualizar(Cliente cliente) {
        try {
            Cliente clienteExistente = buscarPorDni(cliente.getDni());
            if (clienteExistente == null) {
                throw new RuntimeException("Cliente no encontrado con DNI: " + cliente.getDni());
            }

            // Verificar correo único (si cambió)
            if (cliente.getCorreo() != null && !cliente.getCorreo().trim().isEmpty()) {
                Cliente clienteConCorreo = buscarPorCorreo(cliente.getCorreo());
                if (clienteConCorreo != null && !clienteConCorreo.getDni().equals(cliente.getDni())) {
                    throw new RuntimeException("Ya existe otro cliente con el correo: " + cliente.getCorreo());
                }
            }

            // Verificar celular único (si cambió)
            if (cliente.getCelular() != null && !cliente.getCelular().trim().isEmpty()) {
                Cliente clienteConCelular = buscarPorCelular(cliente.getCelular());
                if (clienteConCelular != null && !clienteConCelular.getDni().equals(cliente.getDni())) {
                    throw new RuntimeException("Ya existe otro cliente con el celular: " + cliente.getCelular());
                }
            }

            // Mantener fecha de registro original
            cliente.setFechaRegistro(clienteExistente.getFechaRegistro());

            return clienteRepository.save(cliente);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar cliente: " + e.getMessage());
        }
    }

    /**
     * Activar cliente
     */
    public Cliente activar(String dni) {
        try {
            Cliente cliente = buscarPorDni(dni);
            if (cliente == null) {
                throw new RuntimeException("Cliente no encontrado con DNI: " + dni);
            }

            cliente.setActivo(true);
            return clienteRepository.save(cliente);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al activar cliente: " + e.getMessage());
        }
    }

    /**
     * Desactivar cliente (no eliminar, solo marcar como inactivo)
     */
    public Cliente desactivar(String dni) {
        try {
            Cliente cliente = buscarPorDni(dni);
            if (cliente == null) {
                throw new RuntimeException("Cliente no encontrado con DNI: " + dni);
            }

            cliente.setActivo(false);
            return clienteRepository.save(cliente);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al desactivar cliente: " + e.getMessage());
        }
    }

    /**
     * Verificar si existe cliente por DNI
     */
    public boolean existePorDni(String dni) {
        try {
            return clienteRepository.existsById(dni);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verificar si existe cliente por correo
     */
    public boolean existePorCorreo(String correo) {
        try {
            return clienteRepository.existsByCorreo(correo);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verificar si existe cliente por celular
     */
    public boolean existePorCelular(String celular) {
        try {
            return clienteRepository.existsByCelular(celular);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Contar clientes activos
     */
    public long contarClientesActivos() {
        try {
            return clienteRepository.countByActivoTrue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Obtener clientes con información de contacto completa
     */
    public List<Cliente> obtenerClientesConContactoCompleto() {
        try {
            return clienteRepository.findClientesConContactoCompleto();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener clientes con contacto completo: " + e.getMessage());
        }
    }

    /**
     * Obtener clientes por tipo de documento
     */
    public List<Cliente> obtenerClientesConDNI() {
        try {
            return clienteRepository.findClientesConDNI();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener clientes con DNI: " + e.getMessage());
        }
    }

    public List<Cliente> obtenerClientesConRUC() {
        try {
            return clienteRepository.findClientesConRUC();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener clientes con RUC: " + e.getMessage());
        }
    }

    /**
     * Validar cliente para realizar pedidos
     */
    public boolean esValidoParaPedidos(String dni) {
        try {
            Cliente cliente = buscarActivoPorDni(dni);
            return cliente != null && cliente.esValidoParaPedidos();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtener información resumida de un cliente
     */
    public String obtenerInfoResumida(String dni) {
        try {
            Cliente cliente = buscarPorDni(dni);
            return cliente != null ? cliente.getInfoResumida() : "Cliente no encontrado";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al obtener información del cliente";
        }
    }
    
    /**
     * Autenticar cliente
     */
    public Cliente autenticarCliente(String usuario, String password) {
        try {
            Cliente cliente = clienteRepository.findByUsuarioAndActivoTrue(usuario);
            if (cliente == null || !cliente.getActivo()) {
                return null;
            }

            if (password.equals(cliente.getPassword())) {
                // Actualizar fecha de último acceso
                cliente.setFechaRegistro(LocalDateTime.now()); // O agregar campo fechaUltimoAcceso
                clienteRepository.save(cliente);
                return cliente;
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verificar si existe usuario
     */
    public boolean existePorUsuario(String usuario) {
        try {
            return clienteRepository.existsByUsuario(usuario);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Buscar cliente por usuario
     */
    public Cliente buscarPorUsuario(String usuario) {
        try {
            return clienteRepository.findByUsuarioAndActivoTrue(usuario);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Cambiar contraseña del cliente
     */
    public boolean cambiarPassword(String dni, String passwordActual, String passwordNuevo) {
        try {
            Cliente cliente = buscarPorDni(dni);
            if (cliente == null) {
                return false;
            }
            
            // Verificar password actual
            if (!passwordActual.equals(cliente.getPassword())) {
                return false;
            }
            
            // Actualizar password
            cliente.setPassword(passwordNuevo);
            clienteRepository.save(cliente);
            
            return true;
        } catch (Exception e) {
            System.err.println("Error al cambiar password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Listar clientes activos
     */
    public List<Cliente> listarActivos() {
        try {
            return clienteRepository.findByActivoTrueOrderByNombreAsc();
        } catch (Exception e) {
            System.err.println("Error al listar clientes activos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

   
}