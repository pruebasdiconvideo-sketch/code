package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;

@Entity
@Table(name = "material")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material")
    private Long idMaterial;

    @Column(name = "codigo", unique = true, nullable = false, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 50)
    private String categoria;

    @Column(length = 50)
    private String marca; // <--- Este campo soluciona el error en la tabla

    @Column(name = "unidad_medida", length = 20)
    private String unidadMedida;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(length = 100)
    private String modelo;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Column(length = 100)
    private String mlfb;

    @Column(length = 20)
    private String pasillo;

    @Column(length = 20)
    private String estante;

    @Column(length = 20)
    private String nivel;

    @Column(length = 20)
    private String estado;

    public Material() {
        this.estado = "ACTIVO";
    }

    // Getters y Setters
    public Long getIdMaterial() { return idMaterial; }
    public void setIdMaterial(Long idMaterial) { this.idMaterial = idMaterial; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getMarca() { return marca; } // Getter de marca
    public void setMarca(String marca) { this.marca = marca; } // Setter de marca

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public String getMlfb() { return mlfb; }
    public void setMlfb(String mlfb) { this.mlfb = mlfb; }

    public String getPasillo() { return pasillo; }
    public void setPasillo(String pasillo) { this.pasillo = pasillo; }

    public String getEstante() { return estante; }
    public void setEstante(String estante) { this.estante = estante; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}