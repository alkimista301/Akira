package com.akira.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "marca")
public class Marca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marca")
    private Integer idMarca;

    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    //CONSTRUCTORES
    public Marca() {}

    public Marca(String nombre) {
        this.nombre = nombre;
    }

    //GETTERS Y SETTERS
    public Integer getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Integer idMarca) {
        this.idMarca = idMarca;
    }

    //METODO DE COMPATIBILIDAD PARA FORMULARIO HTML
    @JsonProperty("id")
    public Integer getId() {
        return idMarca;
    }

    public void setId(Integer id) {
        this.idMarca = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Marca{" +
                "idMarca=" + idMarca +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}