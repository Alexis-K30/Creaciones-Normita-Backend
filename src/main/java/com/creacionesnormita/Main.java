package com.creacionesnormita;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@SpringBootApplication
public class Main {

	@RequestMapping("/")
	String home() {
		return "<!doctype html>\n" +
				"<html lang=\"en\">\n" +
				"  <head>\n" +
				"    <meta charset=\"utf-8\">\n" +
				"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
				"    <title>API Spring</title>\n" +
				"    <link rel=\"icon\" href=\"https://docs.spring.io/spring-boot/_/img/favicon.ico\" type=\"image/vnd.microsoft.icon\">\n"+
				"    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB\" crossorigin=\"anonymous\">\n" +
				"  </head>\n" +
				"  <body>\n" +
				"    <a href=\"http://localhost:8082/api/productos\" type=\"button\" class=\"btn btn-outline-success my-4 mx-2\" > <h2>200 OK</h1> </a> \n" +
				"	<a href=\"http://localhost:8082/swagger-ui/index.html\">http://localhost:8082/swagger-ui/index.html</a>					"+
				"    <script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js\" integrity=\"sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI\" crossorigin=\"anonymous\"></script>\n" +
				"  </body>\n" +
				"</html>";
	}



	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

}
