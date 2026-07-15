package com.minimarket.service.impl;

import com.minimarket.entity.Categoria;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.service.CategoriaService;
import com.minimarket.util.InputSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private InputSanitizer inputSanitizer;

    @Override
    public List<Categoria> findAll() {
        return categoriaRepository.findAll().stream()
                .peek(this::sanitizeForOutput)
                .toList();
    }

    @Override
    public Categoria findById(Long id) {
        Categoria categoria = categoriaRepository.findById(id).orElse(null);
        if (categoria != null) {
            sanitizeForOutput(categoria);
        }
        return categoria;
    }

    @Override
    public Categoria save(Categoria categoria) {
        sanitizeForInput(categoria);
        Categoria saved = categoriaRepository.save(categoria);
        sanitizeForOutput(saved);
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        categoriaRepository.deleteById(id);
    }

    private void sanitizeForInput(Categoria categoria) {
        categoria.setNombre(inputSanitizer.sanitize(categoria.getNombre()));
    }

    private void sanitizeForOutput(Categoria categoria) {
        categoria.setNombre(inputSanitizer.sanitize(categoria.getNombre()));
    }
}
