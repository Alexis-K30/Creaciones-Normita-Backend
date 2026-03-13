package com.creacionesnormita.repositories;


import com.creacionesnormita.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    Page<Producto> findByActivoTrue(Pageable pageable);
}