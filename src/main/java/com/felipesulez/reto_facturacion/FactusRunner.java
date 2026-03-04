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

        // 1. Configuración del Cliente
        CustomerDTO cliente = new CustomerDTO();
        cliente.setIdentification("123456789");
        cliente.setNames("Felipe Sulez - Refresh Test");
        cliente.setEmail("felipe@ejemplo.com");

        // 2. Configuración del Producto
        ItemDTO producto = new ItemDTO();
        producto.setCodeReference("PROD-REFRESH");
        producto.setName("Producto Prueba Resiliencia");
        producto.setPrice(75000.0);
        producto.setQuantity(2);

        // 3. Construcción de la Factura
        InvoiceRequest factura = new InvoiceRequest();
        factura.setReferenceCode("RETO_REFRESH_" + System.currentTimeMillis());
        factura.setCustomer(cliente);

        List<ItemDTO> listaItems = new ArrayList<>();
        listaItems.add(producto);
        factura.setItems(listaItems);

        // 4. Envío (Aquí es donde el Interceptor debe salvar el día)
        try {
            log.info("3️⃣ Intentando enviar factura con token dañado...");
            Map<String, Object> respuesta = factusService.enviarFactura(factura);

            if (respuesta != null && "Created".equals(respuesta.get("status"))) {
                log.info("✅ ¡PRUEBA SUPERADA! La factura se envió tras el Refresh automático.");
                log.info("Mensaje API: {}", respuesta.get("message"));
            } else {
                log.warn("⚠️ RESPUESTA INESPERADA: {}", respuesta);
            }

        } catch (Exception e) {
            log.error("❌ FALLO LA PRUEBA: {}", e.getMessage());
        }
    }
}