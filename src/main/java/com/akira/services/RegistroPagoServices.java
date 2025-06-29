package com.akira.services;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.akira.model.RegistroPago;
import com.akira.repository.RegistroPagoRepository;
@Service
public class RegistroPagoServices {
    
    @Autowired
    private RegistroPagoRepository registroPagoRepository;
    
    /**
     * Obtener información de pago por orden
     */
    public Map<String, Object> obtenerInfoPagoPorOrden(Integer idOrden) {
        try {
            List<RegistroPago> pagos = registroPagoRepository.findByOrdenId(idOrden); // CORREGIDO
            Map<String, Object> infoPago = new HashMap<>();
            
            if (!pagos.isEmpty()) {
                // Tomar el primer pago (o el más reciente)
                RegistroPago pago = pagos.get(0);
                
                infoPago.put("fechaPago", pago.getFechaPago());
                infoPago.put("nroOperacion", pago.getNroOperacion());
                infoPago.put("banco", pago.getBanco() != null ? pago.getBanco().getNombre() : "Sin banco");
            } else {
                // No hay registro de pago
                infoPago.put("fechaPago", null);
                infoPago.put("nroOperacion", "Sin registro");
                infoPago.put("banco", "Sin banco");
            }
            
            return infoPago;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener información de pago: " + e.getMessage());
        }
    }
    
    /**
     * Listar todos los registros de pago
     */
    public List<RegistroPago> listarTodos() {
        try {
            return registroPagoRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar registros de pago: " + e.getMessage());
        }
    }
    
    /**
     * Buscar registro por ID
     */
    public RegistroPago buscarPorID(Integer id) {
        try {
            return registroPagoRepository.findById(id).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Registrar nuevo pago
     */
    public RegistroPago registrar(RegistroPago registroPago) {
        try {
            return registroPagoRepository.save(registroPago);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar pago: " + e.getMessage());
        }
    }
    
    /**
     * Actualizar registro existente
     */
    public RegistroPago actualizar(RegistroPago registroPago) {
        try {
            return registroPagoRepository.save(registroPago);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar registro de pago: " + e.getMessage());
        }
    }
    
    /**
     * Eliminar registro
     */
    public void eliminar(Integer id) {
        try {
            registroPagoRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar registro de pago: " + e.getMessage());
        }
    }
}