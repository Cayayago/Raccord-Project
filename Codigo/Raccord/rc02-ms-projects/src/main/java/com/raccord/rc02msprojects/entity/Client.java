package com.raccord.rc02msprojects.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "Client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;  // ✅ Integer porque es serial/int en Supabase

    // Enum: tipo_documento → CC, TI, NIT, PA, CE
    @Column(name = "Document", columnDefinition = "tipo_documento")
    private String document;

    @NotBlank(message = "La razón social es obligatoria")
    @Column(name = "razon_social", length = 50)
    private String razonSocial;

    @NotBlank(message = "El representante legal es obligatorio")
    @Column(name = "Representante_Legal", length = 100)
    private String representanteLegal;

    @Email(message = "El email debe tener formato válido")
    @NotBlank(message = "El email es obligatorio")
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", length = 100)
    private String address;

    @Column(name = "Telephone", length = 15)
    private String telephone;

    @Column(name = "number_cellphone", length = 20)
    private String numberCellphone;

    public Client() {}

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getRepresentanteLegal() { return representanteLegal; }
    public void setRepresentanteLegal(String representanteLegal) { this.representanteLegal = representanteLegal; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getNumberCellphone() { return numberCellphone; }
    public void setNumberCellphone(String numberCellphone) { this.numberCellphone = numberCellphone; }
}
