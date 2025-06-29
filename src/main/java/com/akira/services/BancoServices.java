package com.akira.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.akira.model.Banco;
import com.akira.repository.BancoRepository;

@Service
public class BancoServices {

    @Autowired
    private BancoRepository bancoRepository;

    /**
     * Listar todos los bancos
     */
    public List<Banco> listarTodos() {
        try {
            return bancoRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar bancos: " + e.getMessage());
        }
    }

    /**
     * Buscar banco por ID
     */
    public Banco buscarPorID(Integer id) {
        try {
            return bancoRepository.findById(id).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Buscar banco por nombre
     */
    public Banco buscarPorNombre(String nombre) {
        try {
            return bancoRepository.findByNombre(nombre);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Buscar bancos por nombre (contiene el texto)
     */
    public List<Banco> buscarPorNombreContiene(String nombre) {
        try {
            return bancoRepository.findByNombreContainingIgnoreCase(nombre);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar bancos por nombre: " + e.getMessage());
        }
    }

    /**
     * Registrar nuevo banco
     */
    public Banco registrar(Banco banco) {
        try {
            // Validar que no exista banco con el mismo nombre
            if (bancoRepository.findByNombre(banco.getNombre()) != null) {
                throw new RuntimeException("Ya existe un banco con el nombre: " + banco.getNombre());
            }

            // Validar datos obligatorios
            if (banco.getNombre() == null || banco.getNombre().trim().isEmpty()) {
                throw new RuntimeException("El nombre del banco es obligatorio");
            }

            return bancoRepository.save(banco);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar banco: " + e.getMessage());
        }
    }

    /**
     * Actualizar banco existente
     */
    public Banco actualizar(Banco banco) {
        try {
            if (banco.getId() == null || !bancoRepository.existsById(banco.getId())) {
                throw new RuntimeException("Banco no encontrado para actualizar");
            }

            // Verificar que no exista otro banco con el mismo nombre
            Banco bancoExistente = bancoRepository.findByNombre(banco.getNombre());
            if (bancoExistente != null && !bancoExistente.getId().equals(banco.getId())) {
                throw new RuntimeException("Ya existe otro banco con el nombre: " + banco.getNombre());
            }

            return bancoRepository.save(banco);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar banco: " + e.getMessage());
        }
    }

    /**
     * Eliminar banco
     */
    public void eliminar(Integer id) {
        try {
            if (!bancoRepository.existsById(id)) {
                throw new RuntimeException("Banco no encontrado para eliminar");
            }

            bancoRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar banco: " + e.getMessage());
        }
    }

    /**
     * Verificar si existe banco por ID
     */
    public boolean existeBanco(Integer id) {
        try {
            return bancoRepository.existsById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verificar si existe banco por nombre
     */
    public boolean existeBancoPorNombre(String nombre) {
        try {
            return bancoRepository.findByNombre(nombre) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Contar total de bancos
     */
    public long contarTotalBancos() {
        try {
            return bancoRepository.count();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Obtener bancos más utilizados (se puede implementar con estadísticas)
     */
    public List<Banco> obtenerBancosMasUtilizados() {
        try {
            // Por ahora devuelve todos, pero se puede implementar lógica de estadísticas
            return listarTodos();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener bancos más utilizados: " + e.getMessage());
        }
    }
}