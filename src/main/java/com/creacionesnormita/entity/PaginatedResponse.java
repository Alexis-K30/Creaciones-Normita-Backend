package com.creacionesnormita.entity;

import lombok.Getter;
import java.util.List;

@Getter
public class PaginatedResponse<T> {
    private final List<T> items;
    private final int totalItems;
    private final int pagina;
    private final int porPagina;
    private final int totalPaginas;

    public PaginatedResponse(List<T> items, int totalItems, int pagina, int porPagina) {
        this.items = items;
        this.totalItems = totalItems;
        this.pagina = pagina;
        this.porPagina = porPagina;
        this.totalPaginas = (int) Math.ceil((double) totalItems / porPagina);
    }
}