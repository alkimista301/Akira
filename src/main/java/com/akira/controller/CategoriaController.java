package com.akira.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.akira.model.Categoria;
import com.akira.services.CategoriaServices;

@Controller
@RequestMapping("/categoria")
public class CategoriaController {
    
    @Autowired
    private CategoriaServices categoriaService;
    
    //API PARA OBTERNER TODAS LAS CATEGOGIAS
    @GetMapping("/api/todas")
    @ResponseBody
    public ResponseEntity<List<Categoria>> obtenerTodasLasCategorias() {
        try {
            List<Categoria> categorias = categoriaService.listarTodas();
            System.out.println("API: Devolviendo " + categorias.size() + " categorías");
            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}