package com.akira.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
public class Cliente {
    
    @Id
    @Column(name = "dni", length = 11)
    private String dni; // DNI (8 dígitos) o RUC (11 dígitos)
    
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
    
    @Column(name = "apellido", length = 100, nullable = false)
    private String apellido;
    
    @Column(name = "correo", length = 100)
    private String correo;
    
    @Column(name = "celular", length = 15)
    private String celular;
    
    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;
    
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
    
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
    
    @Column(name = "usuario", unique = true, length = 50)
    private String usuario; // Para login de clientes

    @Column(name = "password", length = 255)
    private String password; // Para autenticación de clientes
    
    // CONSTRUCTORES
    public Cliente() {
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
    }
    
    public Cliente(String dni, String nombre, String apellido) {
        this();
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
    }
    
    public Cliente(String dni, String nombre, String apellido, String correo, String celular) {
        this(dni, nombre, apellido);
        this.correo = correo;
        this.celular = celular;
    }
    
    // GETTERS Y SETTERS
    public String getDni() {
        return dni;
    }
    
    public void setDni(String dni) {
        this.dni = dni;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getApellido() {
        return apellido;
    }
    
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    
    public String getCorreo() {
        return correo;
    }
    
    public void setCorreo(String correo) {
        this.correo = correo;
    }
    
    public String getCelular() {
        return celular;
    }
    
    public void setCelular(String celular) {
        this.celular = celular;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    // MÉTODOS ÚTILES
    
    /**
     * Obtener nombre completo
     */
    public String getNombreCompleto() {
        StringBuilder nombreCompleto = new StringBuilder();
        if (nombre != null) {
            nombreCompleto.append(nombre);
        }
        if (apellido != null && !apellido.trim().isEmpty()) {
            if (nombreCompleto.length() > 0) {
                nombreCompleto.append(" ");
            }
            nombreCompleto.append(apellido);
        }
        return nombreCompleto.toString();
    }
    
    /**
     * Determinar si es RUC (11 dígitos) o DNI (8 dígitos)
     */
    public boolean esRuc() {
        return dni != null && dni.length() == 11;
    }
    
    /**
     * Obtener tipo de documento
     */
    public String getTipoDocumento() {
        if (dni == null) return "Sin documento";
        return dni.length() == 11 ? "RUC" : "DNI";
    }
    
    /**
     * Verificar si tiene información de contacto completa
     */
    public boolean tieneContactoCompleto() {
        return correo != null && !correo.trim().isEmpty() && 
               celular != null && !celular.trim().isEmpty();
    }
    
    /**
     * Verificar si es un cliente válido para hacer pedidos
     */
    public boolean esValidoParaPedidos() {
        return activo && dni != null && !dni.trim().isEmpty() &&
               nombre != null && !nombre.trim().isEmpty();
    }
    
    /**
     * Obtener información resumida del cliente
     */
    public String getInfoResumida() {
        return String.format("%s - %s (%s)", 
            getNombreCompleto(), 
            getTipoDocumento() + ": " + dni,
            activo ? "Activo" : "Inactivo");
    }
    
    @Override
    public String toString() {
        return "Cliente{" +
                "dni='" + dni + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", correo='" + correo + '\'' +
                ", celular='" + celular + '\'' +
                ", tipoDocumento='" + getTipoDocumento() + '\'' +
                ", activo=" + activo +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return dni != null ? dni.equals(cliente.dni) : cliente.dni == null;
    }
    
    @Override
    public int hashCode() {
        return dni != null ? dni.hashCode() : 0;
    }
    

    
    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // AGREGAR ESTOS MÉTODOS ÚTILES

    /**
     * Verificar si tiene credenciales de login
     */
    public boolean tieneCredenciales() {
        return usuario != null && !usuario.trim().isEmpty() && 
               password != null && !password.trim().isEmpty();
    }

    /**
     * Verificar si puede hacer login
     */
    public boolean puedeLogin() {
        return tieneCredenciales() && activo;
    }
}