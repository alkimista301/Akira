package com.akira.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "categoria_producto")
public class Categoria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;
    
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "estante", length = 20)
    private String estante;
    
    // Campo para la imagen - ESTO AGREGUE
    @Column(name = "imagen_url")
    private String imagenUrl;
    
    //CONSTRUCTORES
    public Categoria() {}
    
    public Categoria(String nombre, String descripcion, String estante, String imagenUrl) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estante = estante;
        this.imagenUrl = imagenUrl;
    }
    
    //GETTER Y SETTERS
    public Integer getIdCategoria() {
        return idCategoria;
    }
    
    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }
    
    //METODO DE COMPATIBILIDAD PARA EL FRONTEND
    @JsonProperty("id")
    public Integer getId() {
        return idCategoria;
    }
    
    public void setId(Integer id) {
        this.idCategoria = id;
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
    
    public String getEstante() {
        return estante;
    }
    
    public void setEstante(String estante) {
        this.estante = estante;
    }
    
    public String getImagenUrl() {
        return imagenUrl;
    }
    
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
    
    @Override
    public String toString() {
        return "Categoria{" +
                "idCategoria=" + idCategoria +
                ", nombre='" + nombre + '\'' +
                ", estante='" + estante + '\'' +
                '}';
    }
}