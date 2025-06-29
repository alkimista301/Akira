package com.akira.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="vendedor")
public class Vendedor {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id_vendedor")
	private Integer codigo;
	
	@Column(name="nombre", length=100, nullable=false)
	private String nombre; // VARCHAR(100) NOT NULL
	
	@Column(name="apellido", length=100, nullable=false)
	private String apellido; // VARCHAR(100) NOT NULL
	
	@Column(name="dni", length=8, unique=true, nullable=false)
	private String dni; // CHAR(8) UNIQUE NOT NULL
	
	//GETTERS Y SETTERS
	public Integer getCodigo() {
		return codigo;
	}
	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
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
	public String getDni() {
		return dni;
	}
	public void setDni(String dni) {
		this.dni = dni;
	}
}