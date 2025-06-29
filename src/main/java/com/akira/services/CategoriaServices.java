package com.akira.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.akira.model.Categoria;
import com.akira.repository.CategoriaRepository;
import java.util.List;

@Service
public class CategoriaServices {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Listar todas las categorías
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    // Buscar categoría por ID
    public Categoria buscarPorID(Integer id) {
        return categoriaRepository.findById(id).orElse(null);
    }
}