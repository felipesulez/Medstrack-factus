package com.felipesulez.reto_facturacion;

import com.felipesulez.reto_facturacion.dto.*;
import com.felipesulez.reto_facturacion.service.FactusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
public class FactusRunner implements CommandLineRunner {

    private final FactusService factusService;

    public FactusRunner(FactusService factusService) {
        this.factusService = factusService;
    }

    @Override
    public void run(String... args) {
        try {
            log.info("🔍 Ejecutando Smoke Test de Medstrack...");

            CustomerDTO cliente = new CustomerDTO();
            cliente.setIdentification("901234567");
            cliente.setCompany("MEDSTRACK TEST");
            cliente.setNames("Felipe Sulez");
            cliente.setAddress("La Pamba, Popayán");
            cliente.setEmail("ingenieria@medstrack.com");
            cliente.setIdentificationDocumentId("6");

            ItemDTO item = new ItemDTO();
            item.setCodeReference("SRV-001");
            item.setName("Servicio de Prueba");
            item.setQuantity(new BigDecimal("1.00"));
            item.setPrice(new BigDecimal("50000.00"));

            InvoiceRequest request = new InvoiceRequest();
            request.setReferenceCode("SMOKE-" + System.currentTimeMillis());
            request.setCustomer(cliente);
            request.setItems(List.of(item));

            factusService.enviarFactura(request);
            log.info("✅ Conexión con Factus: OK");

        } catch (Exception e) {
            log.warn("⚠️ Runner: Sistema listo, pero el Sandbox de Factus no respondió.");
        }
    }
}