package com.akira.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.akira.model.Estado;
import com.akira.repository.EstadoRepository;

@Service
public class EstadoServices {

    @Autowired
    private EstadoRepository estadoRepository;

    /**
     * Listar todos los estados
     */
    public List<Estado> listarTodos() {
        try {
            return estadoRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar estados: " + e.getMessage());
        }
    }

    /**
     * Buscar estado por ID
     */
    public Estado buscarPorID(Integer id) {
        try {
            return estadoRepository.findById(id).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Buscar estado por descripción
     */
    public Estado buscarPorDescripcion(String descripcion) {
        try {
            return estadoRepository.findByDescripcion(descripcion);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Registrar nuevo estado
     */
    public Estado registrar(Estado estado) {
        try {
            // Validar que la descripción no exista (es unique)
            if (estadoRepository.findByDescripcion(estado.getDescripcion()) != null) {
                throw new RuntimeException("Ya existe un estado con la descripción: " + estado.getDescripcion());
            }

            return estadoRepository.save(estado);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar estado: " + e.getMessage());
        }
    }

    /**
     * Actualizar estado existente
     */
    public Estado actualizar(Estado estado) {
        try {
            if (estado.getId() != null && estadoRepository.existsById(estado.getId())) {
                return estadoRepository.save(estado);
            }
            throw new RuntimeException("Estado no encontrado para actualizar");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar estado: " + e.getMessage());
        }
    }

    /**
     * Eliminar estado
     */
    public void eliminar(Integer id) {
        try {
            if (estadoRepository.existsById(id)) {
                estadoRepository.deleteById(id);
            } else {
                throw new RuntimeException("Estado no encontrado para eliminar");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar estado: " + e.getMessage());
        }
    }

    /**
     * Verificar si un estado existe
     */
    public boolean existeEstado(Integer id) {
        try {
            return estadoRepository.existsById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verificar si existe estado por descripción
     */
    public boolean existeEstadoPorDescripcion(String descripcion) {
        try {
            return estadoRepository.findByDescripcion(descripcion) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtener estado PENDIENTE
     */
    public Estado obtenerEstadoPendiente() {
        return buscarPorDescripcion("Pendiente");
    }

    /**
     * Obtener estado ATENDIDO
     */
    public Estado obtenerEstadoAtendido() {
        return buscarPorDescripcion("Atendido");
    }

    /**
     * Contar total de estados
     */
    public long contarTotalEstados() {
        try {
            return estadoRepository.count();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Verificar si es estado válido para transición
     */
    public boolean esTransicionValida(Integer estadoActualId, Integer nuevoEstadoId) {
        try {
            Estado estadoActual = buscarPorID(estadoActualId);
            Estado nuevoEstado = buscarPorID(nuevoEstadoId);
            
            if (estadoActual == null || nuevoEstado == null) {
                return false;
            }
            
            // Lógica de negocio para transiciones válidas
            // Por ejemplo: de Pendiente (1) a Atendido (2) es válido
            // pero de Atendido (2) a Pendiente (1) podría no ser válido
            
            String descripcionActual = estadoActual.getDescripcion();
            String descripcionNueva = nuevoEstado.getDescripcion();
            
            // Permitir transición de Pendiente a Atendido
            if ("Pendiente".equals(descripcionActual) && "Atendido".equals(descripcionNueva)) {
                return true;
            }
            
            // Permitir transición de Atendido a Pendiente (para correcciones)
            if ("Atendido".equals(descripcionActual) && "Pendiente".equals(descripcionNueva)) {
                return true;
            }
            
            // Permitir mantener el mismo estado
            if (estadoActualId.equals(nuevoEstadoId)) {
                return true;
            }
            
            // Por defecto, rechazar otras transiciones
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}