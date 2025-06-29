package com.akira.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.akira.model.Marca;
import com.akira.repository.MarcaRepository;

@Service
public class MarcaServices {
    
    @Autowired
    private MarcaRepository marcaRepository;
    
    /**
     * Listar todas las marcas
     */
    public List<Marca> listarTodas() {
        try {
            return marcaRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar marcas: " + e.getMessage());
        }
    }
    
    /**
     * Buscar marca por ID
     */
    public Marca buscarPorID(Integer id) {
        try {
            return marcaRepository.findById(id).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Registrar nueva marca
     */
    public Marca registrar(Marca marca) {
        try {
            return marcaRepository.save(marca);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar marca: " + e.getMessage());
        }
    }
    
    /**
     * Actualizar marca
     */
    public Marca actualizar(Marca marca) {
        try {
            return marcaRepository.save(marca);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar marca: " + e.getMessage());
        }
    }
    
    /**
     * Eliminar marca
     */
    public void eliminar(Integer id) {
        try {
            marcaRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar marca: " + e.getMessage());
        }
    }
}