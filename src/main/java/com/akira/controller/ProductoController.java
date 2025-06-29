package com.akira.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.akira.model.Producto;
import com.akira.model.Categoria;
import com.akira.model.Marca;
import com.akira.services.ProductoServices;
import com.akira.services.CategoriaServices;
import com.akira.services.MarcaServices;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/producto")
public class ProductoController {
    
    @Autowired
    private ProductoServices productoService;
    
    @Autowired
    private CategoriaServices categoriaService;
    
    @Autowired
    private MarcaServices marcaService;
    
    //GUARDAR PRODUCTO NUEVO(AJAX)
    @PostMapping("/guardar")
    @ResponseBody
    public ResponseEntity<String> guardarProducto(@ModelAttribute Producto producto) {
        try {
            System.out.println("Guardando producto: " + producto.getNombre());
            System.out.println("Cantidad: " + producto.getCantidadStock());
            System.out.println("Precio: " + producto.getPrecio());
            System.out.println("Modelo: " + producto.getModelo());
            
            //VALIDACIONES
            if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre del producto es requerido");
            }
            
            if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
                return ResponseEntity.badRequest().body("El precio debe ser mayor a 0");
            }
            
            if (producto.getCantidadStock() == null || producto.getCantidadStock() < 0) {
                return ResponseEntity.badRequest().body("La cantidad debe ser mayor o igual a 0");
            }
            
            //SI VIENE EL ID DE CATEGORIA, BUSCAR Y ASIGNARLA
            if (producto.getCategoria() != null && producto.getCategoria().getId() != null) {
                System.out.println("Buscando categoría ID: " + producto.getCategoria().getId());
                Categoria categoria = categoriaService.buscarPorID(producto.getCategoria().getId());
                if (categoria != null) {
                    producto.setCategoria(categoria);
                    System.out.println("Categoría asignada: " + categoria.getNombre());
                } else {
                    System.out.println("Categoría no encontrada, guardando sin categoría");
                    producto.setCategoria(null);
                }
            }
            
            //SI VIENE EL ID DE MARCA, BUSCAR Y ASIGNARLA
            if (producto.getMarca() != null && producto.getMarca().getIdMarca() != null) {
                System.out.println("Buscando marca ID: " + producto.getMarca().getIdMarca());
                Marca marca = marcaService.buscarPorID(producto.getMarca().getIdMarca());
                if (marca != null) {
                    producto.setMarca(marca);
                    System.out.println("Marca asignada: " + marca.getNombre());
                } else {
                    System.out.println("Marca no encontrada, guardando sin marca");
                    producto.setMarca(null);
                }
            }
            
            Producto productoGuardado = productoService.registrar(producto);
            System.out.println("Producto guardado con ID: " + productoGuardado.getIdProducto());
            
            return ResponseEntity.ok("Producto guardado exitosamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al guardar el producto: " + e.getMessage());
        }
    }
    
    //ACTUALIZA PRODUCTOS EXISTENTE (AJAX)
    @PostMapping("/actualizar")
    @ResponseBody
    public ResponseEntity<String> actualizarProducto(@ModelAttribute Producto producto) {
        try {
            System.out.println("Actualizando producto código: " + producto.getCodigo());
            
            if (producto.getCodigo() == null) {
                return ResponseEntity.badRequest()
                        .body("El código del producto es requerido para actualizar");
            }
            
            //VERIFICAR QUE EL PRODUCTO EXISTE
            Producto productoExistente = productoService.buscarPorID(producto.getCodigo());
            if (productoExistente == null) {
                return ResponseEntity.badRequest()
                        .body("Producto no encontrado");
            }
            
            //ASIGNAR EL ID CORRECTO
            producto.setIdProducto(producto.getCodigo());
            
            //VALIDACIONES
            if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre del producto es requerido");
            }
            
            if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
                return ResponseEntity.badRequest().body("El precio debe ser mayor a 0");
            }
            
            if (producto.getCantidadStock() == null || producto.getCantidadStock() < 0) {
                return ResponseEntity.badRequest().body("La cantidad debe ser mayor o igual a 0");
            }
            
            //SI VIENE EL ID DE CATEGORIA, BUSCAR Y ASIGNARLA
            if (producto.getCategoria() != null && producto.getCategoria().getId() != null) {
                Categoria categoria = categoriaService.buscarPorID(producto.getCategoria().getId());
                if (categoria != null) {
                    producto.setCategoria(categoria);
                } else {
                    producto.setCategoria(null);
                }
            } else {
                producto.setCategoria(null);
            }
            
            //SI VIENE EL ID DE MARCA, BUSCAR Y ASIGNARLA
            if (producto.getMarca() != null && producto.getMarca().getIdMarca() != null) {
                Marca marca = marcaService.buscarPorID(producto.getMarca().getIdMarca());
                if (marca != null) {
                    producto.setMarca(marca);
                } else {
                    producto.setMarca(null);
                }
            } else {
                producto.setMarca(null);
            }
            
            productoService.actualizar(producto);
            return ResponseEntity.ok("Producto actualizado exitosamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al actualizar el producto: " + e.getMessage());
        }
    }
    
    
    
 // Agregar estos métodos al ProductoController existente

  //API PARA BUSCAR PRODUCTO POR CÓDIGO
  @GetMapping("/api/buscar/{codigo}")
  @ResponseBody
  public ResponseEntity<Producto> buscarProductoPorCodigo(@PathVariable Integer codigo) {
      try {
          System.out.println("API: Buscando producto con código: " + codigo);
          Producto producto = productoService.buscarPorID(codigo);
          if (producto == null) {
              return ResponseEntity.notFound().build();
          }
          return ResponseEntity.ok(producto);
      } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.internalServerError().build();
      }
  }

  //API PARA BUSCAR PRODUCTOS POR NOMBRE
  @GetMapping("/api/buscarPorNombre")
  @ResponseBody
  public ResponseEntity<List<Producto>> buscarProductosPorNombre(@RequestParam String nombre) {
      try {
          System.out.println("API: Buscando productos con nombre: " + nombre);
          List<Producto> productos = productoService.buscarPorNombre(nombre);
          return ResponseEntity.ok(productos);
      } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.internalServerError().build();
      }
  }

  //API PARA OBTENER PRODUCTOS POR CATEGORÍA
  @GetMapping("/api/categoria/{categoriaId}")
  @ResponseBody
  public ResponseEntity<List<Producto>> obtenerProductosPorCategoria(@PathVariable Integer categoriaId) {
      try {
          System.out.println("API: Obteniendo productos de categoría: " + categoriaId);
          List<Producto> productos = productoService.buscarPorCategoria(categoriaId);
          return ResponseEntity.ok(productos);
      } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.internalServerError().build();
      }
  }
  
		//API para listar todos los productos
		@GetMapping("/api/listar")
		@ResponseBody
		public ResponseEntity<List<Producto>> listarProductosAPI() {
		   try {
		       List<Producto> productos = productoService.listarTodos();
		       return ResponseEntity.ok(productos);
		   } catch (Exception e) {
		       e.printStackTrace();
		       return ResponseEntity.internalServerError().build();
		   }
		}
		
		//API para eliminar producto
		@DeleteMapping("/eliminar/{id}")
		@ResponseBody
		public ResponseEntity<String> eliminarProducto(@PathVariable Integer id) {
		   try {
		       productoService.eliminar(id);
		       return ResponseEntity.ok("Producto eliminado exitosamente");
		   } catch (Exception e) {
		       e.printStackTrace();
		       return ResponseEntity.internalServerError()
		               .body("Error al eliminar producto: " + e.getMessage());
		   }
		}
		
		//API para obtener categorías
		@GetMapping("/api/categorias")
		@ResponseBody
		public ResponseEntity<List<Categoria>> obtenerCategoriasAPI() {
		   try {
		       List<Categoria> categorias = categoriaService.listarTodas();
		       return ResponseEntity.ok(categorias);
		   } catch (Exception e) {
		       e.printStackTrace();
		       return ResponseEntity.internalServerError().build();
		   }
		}
		
		// Endpoint para descargar reporte de productos
		@GetMapping("/pdf/descargar")
		public ResponseEntity<String> descargarReporte(HttpServletResponse response) {
		    try {
		        List<Producto> productos = productoService.listarTodos();
		        byte[] htmlBytes = productoService.generarPDFProductos(productos);
		        String htmlContent = new String(htmlBytes, java.nio.charset.StandardCharsets.UTF_8);
		        
		        response.setContentType("text/html");
		        response.setHeader("Content-Disposition", "inline; filename=productos-akira.html");
		        
		        return ResponseEntity.ok(htmlContent);
		                
		    } catch (Exception e) {
		        e.printStackTrace();
		        return ResponseEntity.internalServerError().body("Error al generar reporte");
		    }
		}
  
}