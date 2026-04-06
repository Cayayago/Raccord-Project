package com.raccord.rc03mscontinuidad.controller;

import com.raccord.rc03mscontinuidad.entity.Guion;
import com.raccord.rc03mscontinuidad.exception.RecursoNoEncontradoException;
import com.raccord.rc03mscontinuidad.repository.GuionRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/guiones")
@CrossOrigin(origins = "*")
public class GuionController {

    private final GuionRepository guionRepository;

    public GuionController(GuionRepository guionRepository) {
        this.guionRepository = guionRepository;
    }

    @GetMapping("/test")
    public String test() { return "✅ RC03 ms-continuidad - Puerto 8083 funcionando!"; }

    @PostMapping
    public ResponseEntity<Guion> registrar(@Valid @RequestBody Guion guion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guionRepository.save(guion));
    }

    @GetMapping
    public ResponseEntity<List<Guion>> listar() {
        return ResponseEntity.ok(guionRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Guion> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(guionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Guion con ID " + id + " no encontrado")));
    }

    @GetMapping("/proyecto/{idProject}")
    public ResponseEntity<List<Guion>> buscarPorProyecto(@PathVariable String idProject) {
        return ResponseEntity.ok(guionRepository.findByIdProject(idProject));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        if (!guionRepository.existsById(id))
            throw new RecursoNoEncontradoException("Guion con ID " + id + " no encontrado");
        guionRepository.deleteById(id);
        return ResponseEntity.ok("Guion con ID " + id + " eliminado correctamente");
    }
}
