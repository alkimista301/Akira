package com.akira.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orden_pedido")
public class OrdenPedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Integer id;
    
    // CAMBIADO: Ahora usa Cliente en lugar de Usuario
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_dni", nullable = false)
    private Cliente cliente;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tecnico_asignado_id")
    private Usuario tecnicoAsignado;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendedor_asignado_id")
    private Usuario vendedorAsignado;
    
    @Column(name = "fecha_orden", nullable = false)
    private LocalDateTime fecha;
    
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado", nullable = false, referencedColumnName = "id_estado")
    private Estado estado;
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
    
    @Column(name = "tipo_pedido", length = 20, nullable = false)
    private String tipoPedido; // "PRODUCTO_COMPLETO" o "ARMAR_PC"
    
    @Column(name = "detalles_componentes", columnDefinition = "TEXT")
    private String detallesComponentes;
    
    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;
    
    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;
    
    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;
    
    // CONSTRUCTORES
    public OrdenPedido() {}
    
    public OrdenPedido(Cliente cliente, BigDecimal total, Estado estado, String tipoPedido) {
        this.cliente = cliente;
        this.total = total;
        this.estado = estado;
        this.tipoPedido = tipoPedido;
        this.fecha = LocalDateTime.now();
    }
    
    // GETTERS Y SETTERS
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public Usuario getTecnicoAsignado() {
        return tecnicoAsignado;
    }
    
    public void setTecnicoAsignado(Usuario tecnicoAsignado) {
        this.tecnicoAsignado = tecnicoAsignado;
    }
    
    public Usuario getVendedorAsignado() {
        return vendedorAsignado;
    }
    
    public void setVendedorAsignado(Usuario vendedorAsignado) {
        this.vendedorAsignado = vendedorAsignado;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public Estado getEstado() {
        return estado;
    }
    
    public void setEstado(Estado estado) {
        this.estado = estado;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public String getTipoPedido() {
        return tipoPedido;
    }
    
    public void setTipoPedido(String tipoPedido) {
        this.tipoPedido = tipoPedido;
    }
    
    public String getDetallesComponentes() {
        return detallesComponentes;
    }
    
    public void setDetallesComponentes(String detallesComponentes) {
        this.detallesComponentes = detallesComponentes;
    }
    
    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }
    
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }
    
    public LocalDateTime getFechaAtencion() {
        return fechaAtencion;
    }
    
    public void setFechaAtencion(LocalDateTime fechaAtencion) {
        this.fechaAtencion = fechaAtencion;
    }
    
    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }
    
    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }
    
    // MÉTODOS ÚTILES
    
    /**
     * Obtener código de la orden (alias para getId())
     */
    public Integer getCodigo() {
        return this.id;
    }
    
    /**
     * Verificar si es un pedido de producto completo
     */
    public boolean esProductoCompleto() {
        return "PRODUCTO_COMPLETO".equalsIgnoreCase(tipoPedido);
    }
    
    /**
     * Verificar si es un pedido de armar PC
     */
    public boolean esArmarPC() {
        return "ARMAR_PC".equalsIgnoreCase(tipoPedido);
    }
    
    /**
     * Verificar si la orden está pendiente
     */
    public boolean estaPendiente() {
        return estado != null && "PENDIENTE".equalsIgnoreCase(estado.getDescripcion());
    }
    
    /**
     * Verificar si la orden está asignada
     */
    public boolean estaAsignada() {
        return estado != null && "ASIGNADO".equalsIgnoreCase(estado.getDescripcion());
    }
    
    /**
     * Verificar si la orden está atendida
     */
    public boolean estaAtendida() {
        return estado != null && "ATENDIDO".equalsIgnoreCase(estado.getDescripcion());
    }
    
    /**
     * Verificar si la orden está cerrada
     */
    public boolean estaCerrada() {
        return estado != null && "CERRADO".equalsIgnoreCase(estado.getDescripcion());
    }
    
    /**
     * Asignar técnico y cambiar estado a ASIGNADO
     */
    public void asignarTecnico(Usuario tecnico, Estado estadoAsignado) {
        this.tecnicoAsignado = tecnico;
        this.estado = estadoAsignado;
        this.fechaAsignacion = LocalDateTime.now();
    }
    
    /**
     * Marcar como atendida
     */
    public void marcarComoAtendida(Estado estadoAtendido) {
        this.estado = estadoAtendido;
        this.fechaAtencion = LocalDateTime.now();
    }
    
    /**
     * Cerrar la orden
     */
    public void cerrarOrden(Estado estadoCerrado) {
        this.estado = estadoCerrado;
        this.fechaCierre = LocalDateTime.now();
    }
    
    /**
     * Obtener información del cliente de forma segura
     */
    public String getClienteInfo() {
        if (cliente == null) return "Sin cliente";
        return cliente.getNombreCompleto() + " (" + cliente.getDni() + ")";
    }
    
    /**
     * Obtener descripción del tipo de pedido
     */
    public String getTipoPedidoDescripcion() {
        if (tipoPedido == null) return "Sin tipo";
        
        switch (tipoPedido.toUpperCase()) {
            case "PRODUCTO_COMPLETO":
                return "Producto Completo";
            case "ARMAR_PC":
                return "Armar PC";
            default:
                return tipoPedido;
        }
    }
    
    /**
     * Verificar si tiene técnico asignado
     */
    public boolean tieneTecnicoAsignado() {
        return tecnicoAsignado != null;
    }
    
    /**
     * Verificar si tiene vendedor asignado
     */
    public boolean tieneVendedorAsignado() {
        return vendedorAsignado != null;
    }
    
    /**
     * Obtener días transcurridos desde la creación
     */
    public long getDiasTranscurridos() {
        if (fecha == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(fecha.toLocalDate(), LocalDateTime.now().toLocalDate());
    }
    
    /**
     * Verificar si es una orden antigua (más de 30 días)
     */
    public boolean esOrdenAntigua() {
        return getDiasTranscurridos() > 30;
    }
    
    @Override
    public String toString() {
        return "OrdenPedido{" +
                "id=" + id +
                ", cliente=" + (cliente != null ? cliente.getNombreCompleto() : "null") +
                ", tecnico=" + (tecnicoAsignado != null ? tecnicoAsignado.getNombre() : "null") +
                ", vendedor=" + (vendedorAsignado != null ? vendedorAsignado.getNombre() : "null") +
                ", fecha=" + fecha +
                ", total=" + total +
                ", estado=" + (estado != null ? estado.getDescripcion() : "null") +
                ", tipoPedido='" + tipoPedido + '\'' +
                '}';
    }
}