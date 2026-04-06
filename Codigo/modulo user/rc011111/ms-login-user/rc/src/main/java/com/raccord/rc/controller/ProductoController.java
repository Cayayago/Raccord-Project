package com.raccord.rc.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")

public class ProductoController {

    // 🔓 Público
    @GetMapping("/test")
    public String testPublico() {
        return "Productos público funcionando";
    }

    // 🔐 Protegido
    @GetMapping
    public String listarProductos() {
        return "Lista de productos";
    }
}