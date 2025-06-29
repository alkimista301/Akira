package com.akira.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "producto")
public class Producto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;
    
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "precio", nullable = false, precision = 10)
    private Double precio;
    
    @Column(name = "cantidad_stock")
    private Integer cantidadStock;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_marca")
    private Marca marca;
    
    @Column(name = "modelo", length = 100)
    private String modelo;
    
    //CONSTRUCTORES
    public Producto() {}
    
    public Producto(String nombre, String descripcion, Double precio, Integer cantidadStock) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidadStock = cantidadStock;
    }
    
    //GETTERS Y SETTERS
    public Integer getIdProducto() {
        return idProducto;
    }
    
    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }
    
    //METODO DE COMPATIBILIDAD PARA EL FRONTEND
    @JsonProperty("codigo")
    public Integer getCodigo() {
        return idProducto;
    }
    
    public void setCodigo(Integer codigo) {
        this.idProducto = codigo;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public Double getPrecio() {
        return precio;
    }
    
    public void setPrecio(Double precio) {
        this.precio = precio;
    }
    
    public Integer getCantidadStock() {
        return cantidadStock;
    }
    
    public void setCantidadStock(Integer cantidadStock) {
        this.cantidadStock = cantidadStock;
    }
    
    //METODO DE COMPATIBILIDAD PARA EL FRONTEND
    @JsonProperty("cantidad")
    public Integer getCantidad() {
        return cantidadStock;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidadStock = cantidad;
    }
    
    public Categoria getCategoria() {
        return categoria;
    }
    
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
    
    public Marca getMarca() {
        return marca;
    }
    
    public void setMarca(Marca marca) {
        this.marca = marca;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    @Override
    public String toString() {
        return "Producto{" +
                "idProducto=" + idProducto +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", cantidadStock=" + cantidadStock +
                ", categoria=" + (categoria != null ? categoria.getNombre() : "Sin categoría") +
                '}';
    }
}