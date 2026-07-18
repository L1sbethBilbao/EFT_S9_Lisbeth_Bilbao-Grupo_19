package com.minimarket.service;

import com.minimarket.entity.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoriaService {
    List<Categoria> findAll();
    Page<Categoria> findAll(Pageable pageable);
    Categoria findById(Long id);
    Categoria save(Categoria categoria);
    void deleteById(Long id);
}
