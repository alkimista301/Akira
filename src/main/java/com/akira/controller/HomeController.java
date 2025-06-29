package com.akira.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.akira.model.Categoria;
import com.akira.model.Producto;
import com.akira.model.Cliente;
import com.akira.services.CategoriaServices;
import com.akira.services.ProductoServices;
import com.akira.services.ClienteServices;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
public class HomeController {
	
    @Autowired
    private CategoriaServices categoriaServices;
    
    @Autowired
    private ProductoServices productoServices;
    
    @Autowired
    private ClienteServices clienteServices;
    

    /**
     * Página principal / Home
     */
    @GetMapping({"", "/"})
    public String home(Model model, HttpSession session) {
        try {
            // Obtener categorías principales para mostrar en home
            List<Categoria> categorias = categoriaServices.listarTodas();
            model.addAttribute("categorias", categorias);
            
            // Verificar si hay cliente logueado
            String clienteId = (String) session.getAttribute("clienteId");
            if (clienteId != null) {
                Cliente cliente = clienteServices.buscarPorDni(clienteId);
                model.addAttribute("clienteLogueado", cliente);
            }
            
            return "index"; // home page
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la página: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Página de categorías
     */
    @GetMapping("/categorias")
    public String categorias(Model model) {
        try {
            List<Categoria> categorias = categoriaServices.listarTodas();
            model.addAttribute("categorias", categorias);
            return "categoria"; // página de categorías
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar categorías: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Productos por categoría
     */
    @GetMapping("/categoria/{id}")
    public String productosPorCategoria(@PathVariable Integer id, Model model) {
        try {
            Categoria categoria = categoriaServices.buscarPorID(id);
            if (categoria == null) {
                model.addAttribute("error", "Categoría no encontrada");
                return "error";
            }
            
            List<Producto> productos = productoServices.buscarPorCategoria(id);
            
            model.addAttribute("categoria", categoria);
            model.addAttribute("productos", productos);
            return "productos-categoria"; // página de productos por categoría
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar productos: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Página para armar PC (con acordeón)
     */
    @GetMapping("/armar-pc")
    public String armarPC(Model model, HttpSession session) {
        try {
            // Verificar si cliente está logueado
            String clienteId = (String) session.getAttribute("clienteId");
            Cliente clienteLogueado = null;
            
            if (clienteId != null) {
                clienteLogueado = clienteServices.buscarPorDni(clienteId);
                model.addAttribute("clienteLogueado", clienteLogueado);
            }
            
            // Cargar categorías para el acordeón
            List<Categoria> categorias = categoriaServices.listarTodas();
            model.addAttribute("categorias", categorias);
            
            return "armar-pc"; // página de armar PC
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar página de armar PC: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Página de productos general
     */
    @GetMapping("/productos")
    public String productos(@RequestParam(required = false) String categoria, Model model) {
        try {
            List<Producto> productos;
            
            if (categoria != null && !categoria.isEmpty()) {
                productos = productoServices.buscarPorCategoria(Integer.valueOf(categoria));
            } else {
                productos = productoServices.listarTodos();
            }
            
            List<Categoria> categorias = categoriaServices.listarTodas();
            
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            return "productos"; // página de productos
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar productos: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/nosotros")
    public String nosotros() {
        return "nosotros";
    }
    

    
    @GetMapping("/contacto")
    public String contacto() {
        return "contacto";
    }
    
    @GetMapping("/producto")
    public String producto() {
        return "producto";
    }
    
    @GetMapping("/detalleProducto")
    public String detalleProducto() {
        return "detalleProducto";
    }
    
    /**
     * Página de login para clientes
     */
    @GetMapping("/auth/login-cliente")
    public String loginCliente(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        return "redirect:/?login=true"; // Redirigir al home con modal de login
    }

    /**
     * Verificar si el cliente ya está logueado y redirigir a su dashboard
     */
    @GetMapping("/mi-dashboard")
    public String miDashboard(HttpSession session) {
        String clienteId = (String) session.getAttribute("clienteId");
        if (clienteId != null) {
            return "redirect:/cliente-dashboard/" + clienteId;
        } else {
            return "redirect:/?login=true";
        }
    }

}