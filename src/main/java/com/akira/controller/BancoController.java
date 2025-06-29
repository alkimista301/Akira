package com.akira.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.akira.model.Banco;
import com.akira.services.BancoServices;

@Controller
@RequestMapping("/banco")
public class BancoController {
    
    @Autowired
    private BancoServices bancoService;
    
    //API PARA OBTENER TODOS LOS BANCOS
    @GetMapping("/api/todos")
    @ResponseBody
    public ResponseEntity<List<Banco>> obtenerTodosLosBancos() {
        try {
            List<Banco> bancos = bancoService.listarTodos();
            System.out.println("API: Devolviendo " + bancos.size() + " bancos");
            return ResponseEntity.ok(bancos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    //API PARA BUSCAR BANCO POR ID
    @GetMapping("/api/buscar/{id}")
    @ResponseBody
    public ResponseEntity<Banco> buscarBancoPorId(@PathVariable Integer id) {
        try {
            System.out.println("API: Buscando banco con ID: " + id);
            Banco banco = bancoService.buscarPorID(id);
            if (banco == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(banco);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<List<Banco>> listarBancos() {
        try {
            List<Banco> bancos = bancoService.listarTodos();
            System.out.println("API: Listando " + bancos.size() + " bancos disponibles");
            return ResponseEntity.ok(bancos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}