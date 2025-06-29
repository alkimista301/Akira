package com.akira.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.akira.model.Vendedor;
import com.akira.repository.VendedorRepository;

@Service
public class VendedorServices {

    @Autowired
    private VendedorRepository vendedorRepository;

    /**
     * Listar todos los vendedores
     */
    public List<Vendedor> listarTodos() {
        try {
            return vendedorRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar vendedores: " + e.getMessage());
        }
    }

    /**
     * Buscar vendedor por ID
     */
    public Vendedor buscarPorID(Integer id) {
        try {
            return vendedorRepository.findById(id).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Buscar vendedor por DNI
     */
    public Vendedor buscarPorDni(String dni) {
        try {
            return vendedorRepository.findByDni(dni);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Buscar vendedores por nombre (contiene el texto)
     */
    public List<Vendedor> buscarPorNombre(String nombre) {
        try {
            return vendedorRepository.findByNombreContainingIgnoreCase(nombre);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Buscar vendedores por apellido (contiene el texto)
     */
    public List<Vendedor> buscarPorApellido(String apellido) {
        try {
            return vendedorRepository.findByApellidoContainingIgnoreCase(apellido);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Registrar nuevo vendedor
     */
    public Vendedor registrar(Vendedor vendedor) {
        try {
            // Validar que el DNI no exista
            if (vendedorRepository.findByDni(vendedor.getDni()) != null) {
                throw new RuntimeException("Ya existe un vendedor con el DNI: " + vendedor.getDni());
            }

            return vendedorRepository.save(vendedor);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar vendedor: " + e.getMessage());
        }
    }

    /**
     * Actualizar vendedor existente
     */
    public Vendedor actualizar(Vendedor vendedor) {
        try {
            if (vendedor.getCodigo() != null && vendedorRepository.existsById(vendedor.getCodigo())) {
                return vendedorRepository.save(vendedor);
            }
            throw new RuntimeException("Vendedor no encontrado para actualizar");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar vendedor: " + e.getMessage());
        }
    }

    /**
     * Eliminar vendedor
     */
    public void eliminar(Integer id) {
        try {
            if (vendedorRepository.existsById(id)) {
                vendedorRepository.deleteById(id);
            } else {
                throw new RuntimeException("Vendedor no encontrado para eliminar");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar vendedor: " + e.getMessage());
        }
    }

    /**
     * Verificar si un vendedor existe
     */
    public boolean existeVendedor(Integer id) {
        try {
            return vendedorRepository.existsById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verificar si existe vendedor por DNI
     */
    public boolean existeVendedorPorDni(String dni) {
        try {
            return vendedorRepository.findByDni(dni) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Contar total de vendedores
     */
    public long contarTotalVendedores() {
        try {
            return vendedorRepository.count();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Buscar vendedores por nombre completo (nombre + apellido)
     */
    public List<Vendedor> buscarPorNombreCompleto(String nombreCompleto) {
        try {
            // Dividir el nombre completo en palabras
            String[] palabras = nombreCompleto.trim().split("\\s+");
            
            if (palabras.length == 1) {
                // Si es una sola palabra, buscar en nombre o apellido
                List<Vendedor> porNombre = buscarPorNombre(palabras[0]);
                List<Vendedor> porApellido = buscarPorApellido(palabras[0]);
                
                // Combinar resultados sin duplicados
                porNombre.addAll(porApellido);
                return porNombre.stream().distinct().toList();
            } else if (palabras.length >= 2) {
                // Si son dos o más palabras, buscar como nombre y apellido
                return vendedorRepository.findByNombreAndApellido(palabras[0], palabras[1]);
            }
            
            return List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Obtener vendedores activos (puedes agregar un campo activo en el futuro)
     */
    public List<Vendedor> obtenerVendedoresActivos() {
        // Por ahora devuelve todos, pero puedes implementar lógica de activos
        return listarTodos();
    }
}