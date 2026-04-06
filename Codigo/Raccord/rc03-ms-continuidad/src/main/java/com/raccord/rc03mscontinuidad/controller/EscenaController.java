package com.raccord.rc03mscontinuidad.controller;

import com.raccord.rc03mscontinuidad.entity.Escena;
import com.raccord.rc03mscontinuidad.exception.RecursoNoEncontradoException;
import com.raccord.rc03mscontinuidad.repository.EscenaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/escenas")
@CrossOrigin(origins = "*")
public class EscenaController {

    private final EscenaRepository escenaRepository;

    public EscenaController(EscenaRepository escenaRepository) {
        this.escenaRepository = escenaRepository;
    }

    @PostMapping
    public ResponseEntity<Escena> registrar(@Valid @RequestBody Escena escena) {
        return ResponseEntity.status(HttpStatus.CREATED).body(escenaRepository.save(escena));
    }

    @GetMapping
    public ResponseEntity<List<Escena>> listar() {
        return ResponseEntity.ok(escenaRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Escena> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(escenaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Escena '" + id + "' no encontrada")));
    }

    @GetMapping("/guion/{idGuion}")
    public ResponseEntity<List<Escena>> buscarPorGuion(@PathVariable Long idGuion) {
        return ResponseEntity.ok(escenaRepository.findByIdGuion(idGuion));
    }

    @GetMapping("/momento/{momentoDia}")
    public ResponseEntity<List<Escena>> buscarPorMomento(@PathVariable String momentoDia) {
        return ResponseEntity.ok(escenaRepository.findByMomentoDia(momentoDia));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable String id) {
        if (!escenaRepository.existsById(id))
            throw new RecursoNoEncontradoException("Escena '" + id + "' no encontrada");
        escenaRepository.deleteById(id);
        return ResponseEntity.ok("Escena '" + id + "' eliminada correctamente");
    }
}
