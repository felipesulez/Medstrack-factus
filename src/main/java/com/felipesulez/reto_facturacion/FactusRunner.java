package com.felipesulez.reto_facturacion;

import com.felipesulez.reto_facturacion.dto.CustomerDTO;
import com.felipesulez.reto_facturacion.dto.InvoiceRequest;
import com.felipesulez.reto_facturacion.dto.ItemDTO;
import com.felipesulez.reto_facturacion.service.FactusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FactusRunner implements CommandLineRunner {

    private final FactusService factusService;

    public FactusRunner(FactusService factusService) {
        this.factusService = factusService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("--- 🏁 INICIANDO RETO DE FACTURACIÓN (MODO SENIOR INTEGRADO) ---");

        // 1. Configuración del Cliente
        CustomerDTO cliente = new CustomerDTO();
        cliente.setIdentification("123456789");
        cliente.setNames("Felipe Sulez - Final Refactor");
        cliente.setEmail("felipe@ejemplo.com");

        // 2. Configuración del Producto (Aseguramos el Nombre)
        ItemDTO producto = new ItemDTO();
        producto.setCodeReference("PROD-FINAL");
        producto.setName("Producto Reto Spring"); // <-- El campo que la API reclama
        producto.setPrice(75000.0);
        producto.setQuantity(2);

        // 3. Construcción de la Factura con asignación explícita de lista
        InvoiceRequest factura = new InvoiceRequest();
        factura.setReferenceCode("RETO_FINAL_" + System.currentTimeMillis());
        factura.setObservation("Factura integrada con logs y solución de mapeo");
        factura.setCustomer(cliente);

        // Creamos la lista manualmente para asegurar que Jackson la detecte
        List<ItemDTO> listaItems = new ArrayList<>();
        listaItems.add(producto);
        factura.setItems(listaItems);

        // 4. Envío y manejo de respuesta
        try {
            log.info("📡 Conectando con el servicio de validación Factus...");
            Map<String, Object> respuesta = factusService.enviarFactura(factura);

            // Verificamos si la respuesta contiene el estado esperado
            if (respuesta != null && "Created".equals(respuesta.get("status"))) {
                log.info("✅ ÉXITO TOTAL: {}", respuesta.get("message"));
            } else {
                log.warn("⚠️ RESPUESTA DE LA API: {}", respuesta);
            }

        } catch (Exception e) {
            // El log.error con tres parámetros imprime el mensaje y el error completo
            log.error("❌ ERROR EN EL PROCESO: {}", e.getMessage(), e);
        }
    }
}