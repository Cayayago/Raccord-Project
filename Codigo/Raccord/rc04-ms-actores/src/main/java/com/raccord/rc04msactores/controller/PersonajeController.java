package com.raccord.rc04msactores.controller;

import com.raccord.rc04msactores.entity.Personaje;
import com.raccord.rc04msactores.exception.RecursoNoEncontradoException;
import com.raccord.rc04msactores.repository.PersonajeRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/personajes")
@CrossOrigin(origins = "*")
public class PersonajeController {

    private final PersonajeRepository personajeRepository;

    public PersonajeController(PersonajeRepository personajeRepository) {
        this.personajeRepository = personajeRepository;
    }

    @GetMapping("/test")
    public String test() { return "✅ RC04 ms-actores/personajes - funcionando!"; }

    @PostMapping
    public ResponseEntity<Personaje> registrar(@Valid @RequestBody Personaje personaje) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personajeRepository.save(personaje));
    }

    @GetMapping
    public ResponseEntity<List<Personaje>> listar() {
        return ResponseEntity.ok(personajeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Personaje> buscarPorId(@PathVariable Long id) {
        Personaje p = personajeRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Personaje con ID " + id + " no encontrado"));
        return ResponseEntity.ok(p);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        if (!personajeRepository.existsById(id))
            throw new RecursoNoEncontradoException("Personaje con ID " + id + " no encontrado");
        personajeRepository.deleteById(id);
        return ResponseEntity.ok("Personaje con ID " + id + " eliminado");
    }
}
