package com.raccord.rc04msactores.controller;

import com.raccord.rc04msactores.entity.Actor;
import com.raccord.rc04msactores.exception.RecursoNoEncontradoException;
import com.raccord.rc04msactores.repository.ActorRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/actores")
@CrossOrigin(origins = "*")
public class ActorController {

    private final ActorRepository actorRepository;

    public ActorController(ActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    @GetMapping("/test")
    public String test() { return "✅ RC04 ms-actores - Puerto 8084 funcionando!"; }

    // 🔐 ADMIN - Registrar actor
    @PostMapping
    public ResponseEntity<Actor> registrar(@Valid @RequestBody Actor actor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(actorRepository.save(actor));
    }

    // 🔐 USER/ADMIN - Listar todos
    @GetMapping
    public ResponseEntity<List<Actor>> listar() {
        return ResponseEntity.ok(actorRepository.findAll());
    }

    // 🔐 USER/ADMIN - Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Actor> buscarPorId(@PathVariable Long id) {
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actor con ID " + id + " no encontrado"));
        return ResponseEntity.ok(actor);
    }

    // 🔐 USER/ADMIN - Buscar por nacionalidad
    @GetMapping("/nacionalidad/{nacionalidad}")
    public ResponseEntity<List<Actor>> porNacionalidad(@PathVariable String nacionalidad) {
        return ResponseEntity.ok(actorRepository.findByNacionalidad(nacionalidad));
    }

    // 🔐 USER/ADMIN - Buscar actores de doble de riesgo
    @GetMapping("/doble-riesgo")
    public ResponseEntity<List<Actor>> doblesDeRiesgo() {
        return ResponseEntity.ok(actorRepository.findByDobleRiesgo(true));
    }

    // 🔐 ADMIN - Eliminar actor
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        if (!actorRepository.existsById(id))
            throw new RecursoNoEncontradoException("Actor con ID " + id + " no encontrado");
        actorRepository.deleteById(id);
        return ResponseEntity.ok("Actor con ID " + id + " eliminado");
    }
}
