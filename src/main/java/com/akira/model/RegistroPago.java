package com.akira.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "registroPago")
public class RegistroPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer id;

    @Column(name = "nro_operacion", nullable = false, unique = true, length = 50)
    private String nroOperacion;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @ManyToOne
    @JoinColumn(name = "id_orden", nullable = false, referencedColumnName = "id_orden")
    private OrdenPedido orden;

    @ManyToOne
    @JoinColumn(name = "id_banco", nullable = false)
    private Banco banco;

    @ManyToOne
    @JoinColumn(name = "id_estado", nullable = false, referencedColumnName = "id_estado")
    private Estado estado;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNroOperacion() {
        return nroOperacion;
    }

    public void setNroOperacion(String nroOperacion) {
        this.nroOperacion = nroOperacion;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public OrdenPedido getOrden() {
        return orden;
    }

    public void setOrden(OrdenPedido orden) {
        this.orden = orden;
    }

    public Banco getBanco() {
        return banco;
    }

    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}