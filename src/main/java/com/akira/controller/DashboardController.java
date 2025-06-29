// CREAR UN NUEVO ARCHIVO: DashboardController.java

package com.akira.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.akira.model.OrdenPedido;
import com.akira.model.Usuario;
import com.akira.services.OrdenPedidoServices;
import com.akira.services.UsuarioServices;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/")  // ⬅️ Raíz para que el dashboard sea /dashboard/{id}
public class DashboardController {

    @Autowired
    private OrdenPedidoServices ordenPedidoServices;
    
    @Autowired
    private UsuarioServices usuarioServices;

    @GetMapping("/dashboard/{usuarioId}")
    public String dashboard(@PathVariable String usuarioId, Model model, HttpSession session) {
        try {
            String sessionUsuarioId = (String) session.getAttribute("usuarioId");
            
            if (sessionUsuarioId == null || !sessionUsuarioId.equals(usuarioId)) {
                return "redirect:/auth/login";
            }
            
            Usuario usuario = usuarioServices.buscarPorId(usuarioId);
            if (usuario == null) {
                model.addAttribute("error", "Usuario no encontrado");
                return "error";
            }
            
            model.addAttribute("usuario", usuario);
            
            if (usuario.esDueno()) {
                List<OrdenPedido> pedidosPendientes = ordenPedidoServices.listarPendientesSinAsignar();
                List<Usuario> tecnicos = usuarioServices.listarTecnicos();
                List<Usuario> vendedores = usuarioServices.listarVendedores();
                model.addAttribute("pedidosPendientes", pedidosPendientes);
                model.addAttribute("tecnicos", tecnicos);
                model.addAttribute("vendedores", vendedores);
                return "dashboard/dueno";
            } else if (usuario.esTecnico()) {
                List<OrdenPedido> misAsignados = ordenPedidoServices.listarAsignadosATecnico(usuarioId);
                model.addAttribute("misAsignados", misAsignados);
                return "dashboard/tecnico";
            } else if (usuario.esVendedor()) {
                List<OrdenPedido> atendidos = ordenPedidoServices.listarAtendidosDeVendedor(usuarioId);
                List<OrdenPedido> cerrados = ordenPedidoServices.listarCerradosDeVendedor(usuarioId);
                model.addAttribute("atendidos", atendidos);
                model.addAttribute("cerrados", cerrados);
                return "dashboard/vendedor";
            } else if (usuario.esCliente()) {
                List<OrdenPedido> misPedidos = ordenPedidoServices.listarPorCliente(usuarioId);
                model.addAttribute("misPedidos", misPedidos);
                return "dashboard/cliente";
            }
            
            return "dashboard/general";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar dashboard: " + e.getMessage());
            return "error";
        }
    }
}