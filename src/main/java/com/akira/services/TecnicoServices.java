package com.akira.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.akira.model.Tecnico;
import com.akira.repository.TecnicoRepository;

@Service
public class TecnicoServices {
    
    @Autowired
    private TecnicoRepository tecnicoRepository;
    
    /**
     * Listar todos los técnicos
     */
    public List<Tecnico> listarTodos() {
        try {
            return tecnicoRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar técnicos: " + e.getMessage());
        }
    }
    
    /**
     * Buscar técnico por ID
     */
    public Tecnico buscarPorID(Integer id) {
        try {
            return tecnicoRepository.findById(id).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}