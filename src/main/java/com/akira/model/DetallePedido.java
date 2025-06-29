package com.akira.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_orden", nullable = false, referencedColumnName = "id_orden")
    private OrdenPedido orden;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;
    
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
    
    public DetallePedido() {}
    
    public DetallePedido(OrdenPedido orden, Producto producto, Integer cantidad) {
        this.orden = orden;
        this.producto = producto;
        this.cantidad = cantidad;
    }
    
    public Integer getIdDetalle() {
        return idDetalle;
    }
    
    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }
    
    public OrdenPedido getOrden() {
        return orden;
    }
    
    public void setOrden(OrdenPedido orden) {
        this.orden = orden;
    }
    
    public Producto getProducto() {
        return producto;
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
    }
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public Double getSubtotal() {
        if (producto != null && cantidad != null) {
            return producto.getPrecio() * cantidad;
        }
        return 0.0;
    }
    
    public String getNombreProducto() {
        return producto != null ? producto.getNombre() : "Producto no disponible";
    }
    
    public Double getPrecioUnitario() {
        return producto != null ? producto.getPrecio() : 0.0;
    }
    
    @Override
    public String toString() {
        return "DetallePedido{" +
                "idDetalle=" + idDetalle +
                ", orden=" + (orden != null ? orden.getCodigo() : "Sin orden") +
                ", producto=" + (producto != null ? producto.getNombre() : "Sin producto") +
                ", cantidad=" + cantidad +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}