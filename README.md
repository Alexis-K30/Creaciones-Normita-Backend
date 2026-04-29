# Productos API — Java Spring Boot

Conversión de una API REST de productos desde **C# (.NET)** a **Java (Spring Boot)** usando **Lombok**, **JPA** e **Hibernate**.

---

## 📁 Estructura de archivos

```
src/main/java/com/example/
├── models/
│   ├── Producto.java
│   └── PaginatedResponse.java
├── repositories/
│   └── ProductoRepository.java
└── controllers/
    └── ProductosController.java
```

---

## 📄 Descripción de cada archivo

### `Producto.java`
Entidad JPA que representa un producto en la base de datos.

| Anotación | Propósito |
|---|---|
| `@Entity` / `@Table` | Mapea la clase a la tabla `productos` en la BD |
| `@Id` + `@GeneratedValue` | Clave primaria autoincremental (equivale a `[Key]` + `DatabaseGeneratedOption.Identity`) |
| `@NotBlank` / `@Size` | Validaciones de campo (equivalen a `[Required]` y `[StringLength]`) |
| `@DecimalMin` / `@DecimalMax` | Rango de precio (equivale a `[Range]`) |
| `@JdbcTypeCode(SqlTypes.JSON)` | Almacena `imagenUrls` como `jsonb` en PostgreSQL (equivale a `[Column(TypeName = "jsonb")]`) |
| `@Data` | Lombok genera getters, setters, `equals`, `hashCode` y `toString` |
| `@Builder` + `@Builder.Default` | Permite construcción fluida con valores por defecto |

**Campos:**
- `id` — Identificador único autoincremental
- `nombre` — Nombre del producto (máx. 100 caracteres, obligatorio)
- `descripcion` — Descripción del producto (máx. 500 caracteres, obligatoria)
- `precio` — Precio con precisión decimal (`BigDecimal`)
- `imagenUrls` — Lista de hasta 3 URLs de imágenes, guardada como JSON en la BD
- `activo` — Indica si el producto está visible (por defecto `true`)
- `fechaCreacion` — Fecha de registro en el sistema (se asigna en el servidor)

---

### `PaginatedResponse.java`
Clase genérica de respuesta paginada, equivalente a `PaginatedResponse<T>` en C#.

Contiene:
- `items` — Lista de elementos de la página actual
- `totalItems` — Total de registros en la BD
- `pagina` — Página actual (base 1)
- `porPagina` — Elementos por página
- `totalPaginas` — Calculado automáticamente como `ceil(totalItems / porPagina)`

Usa `@Getter` de Lombok para exponer todos los campos en la respuesta JSON.

---

### `ProductoRepository.java`
Interfaz de acceso a datos que extiende `JpaRepository`. Spring Data JPA genera la implementación en tiempo de ejecución.

```java
Page<Producto> findByActivoTrue(Pageable pageable);
```

Este método equivale al `Where(p => p.Activo)` + `.Skip().Take()` de Entity Framework Core, pero con paginación y ordenamiento integrados a través del objeto `Pageable`.

---

### `ProductosController.java`
Controlador REST que expone los endpoints de la API bajo `/api/productos`.

| Método   | Endpoint              | Acceso  | Descripción                            |
|----------|-----------------------|---------|----------------------------------------|
| `GET`    | `/api/productos`      | Público | Lista productos activos con paginación |
| `GET`    | `/api/productos/{id}` | Público | Obtiene un producto por ID             |
| `POST`   | `/api/productos`      | Admin   | Crea un nuevo producto                 |
| `PUT`    | `/api/productos/{id}` | Admin   | Actualiza un producto existente        |
| `DELETE` | `/api/productos/{id}` | Admin   | Elimina un producto                    |

**Detalles de implementación:**
- `@PreAuthorize("hasRole('Administrador')")` reemplaza a `[Authorize(Roles = "Administrador")]` de ASP.NET.
- La fecha de creación siempre se asigna en el servidor (`LocalDateTime.now()`), nunca desde el cliente.
- En el `PUT`, se recupera la `fechaCreacion` original para evitar que sea sobreescrita.
- `@RequiredArgsConstructor` de Lombok genera el constructor con inyección de dependencias automáticamente.

---

## ⚙️ Dependencias requeridas (`pom.xml`)

```xml
<!-- Spring Boot Starter Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## 🔐 Configuración de seguridad requerida

Para que `@PreAuthorize` funcione, debes habilitar la seguridad a nivel de método en tu clase de configuración:

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig { ... }
```

---

## 🔄 Tabla de equivalencias C# → Java

| C# / .NET                                    | Java / Spring                       |
|----------------------------------------------|-------------------------------------|
| `[Key]` + `DatabaseGeneratedOption.Identity` | `@Id` + `@GeneratedValue(IDENTITY)` |
| `[Required]`                                 | `@NotBlank` / `@NotNull`            |
| `[StringLength(N)]`                          | `@Size(max = N)`                    |
| `[Range(min, max)]`                          | `@DecimalMin` + `@DecimalMax`       |
| `[Column(TypeName = "jsonb")]`               | `@JdbcTypeCode(SqlTypes.JSON)`      |
| `decimal`                                    | `BigDecimal`                        |
| `DateTime`                                   | `LocalDateTime`                     |
| `DateTime.UtcNow`                            | `LocalDateTime.now()`               |
| `IActionResult`                              | `ResponseEntity<Void>`              |
| `[Authorize(Roles = "X")]`                   | `@PreAuthorize("hasRole('X')")`     |
| `AsNoTracking()`                             | No necesario en JPA                 |
| `.Skip().Take()`                             | `Pageable` con `PageRequest.of()`   |
| `CreatedAtAction(...)`                       | `ResponseEntity.created(URI)`       |


____
## Por si da error el correr (es para borrar el BOM el UTF-8)
```aiexclude
Get-ChildItem -Recurse -Filter *.java | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $content = $content -replace '^\xEF\xBB\xBF', ''
    Set-Content $_.FullName $content -NoNewline
}
```