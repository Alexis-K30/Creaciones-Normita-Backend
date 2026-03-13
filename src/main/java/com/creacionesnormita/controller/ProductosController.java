package com.creacionesnormita.controller;


import com.creacionesnormita.entity.Producto;
import com.creacionesnormita.entity.PaginatedResponse;
import com.creacionesnormita.repositories.ProductoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controlador REST para gestionar productos.
 * Proporciona endpoints para crear, leer, actualizar y eliminar productos.
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductosController {

    private final ProductoRepository productoRepository;

    /**
     * Obtiene una lista paginada de productos activos.
     *
     * @param pagina Número de página (por defecto 1).
     * @param porPagina Cantidad de productos por página (por defecto 9).
     * @return Respuesta con la lista de productos y metadatos de paginación.
     */
    // GET: /api/productos?pagina=1&porPagina=9
    @GetMapping
    public ResponseEntity<PaginatedResponse<Producto>> getProductos(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "9") int porPagina) {

        Pageable pageable = PageRequest.of(pagina - 1, porPagina, Sort.by("fechaCreacion").descending());
        Page<Producto> page = productoRepository.findByActivoTrue(pageable);

        PaginatedResponse<Producto> response = new PaginatedResponse<>(
                page.getContent(),
                (int) page.getTotalElements(),
                pagina,
                porPagina
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un producto específico por su ID.
     *
     * @param id ID del producto.
     * @return El producto si existe y está activo, o 404 Not Found.
     */
    // GET: /api/productos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Producto> getProducto(@PathVariable Integer id) {
        return productoRepository.findById(id)
                .filter(Producto::isActivo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo producto.
     * Requiere rol de Administrador.
     *
     * @param producto Datos del producto a crear.
     * @return El producto creado con código 201 Created.
     */
    // POST: /api/productos
    @PostMapping
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Producto> postProducto(@Valid @RequestBody Producto producto) {
        producto.setFechaCreacion(LocalDateTime.now());
        Producto saved = productoRepository.save(producto);
        return ResponseEntity.created(
                java.net.URI.create("/api/productos/" + saved.getId())
        ).body(saved);
    }

    /**
     * Actualiza un producto existente.
     * Requiere rol de Administrador.
     *
     * @param id ID del producto a actualizar.
     * @param producto Datos actualizados del producto.
     * @return 204 No Content si se actualizó correctamente, o error correspondiente.
     */
    // PUT: /api/productos/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Void> putProducto(@PathVariable Integer id,
                                            @Valid @RequestBody Producto producto) {
        if (!id.equals(producto.getId())) {
            return ResponseEntity.badRequest().build();
        }

        return productoRepository.findById(id)
                .map(existing -> {
                    producto.setFechaCreacion(existing.getFechaCreacion());
                    productoRepository.save(producto);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().<Void>build());
    }

    /**
     * Elimina un producto por su ID.
     * Requiere rol de Administrador.
     *
     * @param id ID del producto a eliminar.
     * @return 204 No Content si se eliminó correctamente, o 404 Not Found.
     */
    // DELETE: /api/productos/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Void> deleteProducto(@PathVariable Integer id) {
        return productoRepository.findById(id)
                .map(producto -> {
                    productoRepository.delete(producto);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().<Void>build());
    }
}