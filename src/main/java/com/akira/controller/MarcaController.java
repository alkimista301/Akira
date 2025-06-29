package com.akira.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.akira.model.Marca;
import com.akira.services.MarcaServices;

@Controller
@RequestMapping("/marca")
public class MarcaController {
    
    @Autowired
    private MarcaServices marcaService;
   
    // API PARA OBTENER TODAS LAS MARCAS (YA TIENES ESTE)
    @GetMapping("/api/todas")
    @ResponseBody
    public ResponseEntity<List<Marca>> obtenerTodasLasMarcas() {
        try {
            List<Marca> marcas = marcaService.listarTodas();
            System.out.println("API: Devolviendo " + marcas.size() + " marcas");
            return ResponseEntity.ok(marcas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
   
    // API PARA OBTENER MARCAS POR ID (YA TIENES ESTE)
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Marca> obtenerMarca(@PathVariable Integer id) {
        try {
            Marca marca = marcaService.buscarPorID(id);
            if (marca == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(marca);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // NUEVO: API ALTERNATIVA (para compatibilidad futura)
    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<List<Marca>> listarMarcasAPI() {
        try {
            List<Marca> marcas = marcaService.listarTodas();
            return ResponseEntity.ok(marcas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}