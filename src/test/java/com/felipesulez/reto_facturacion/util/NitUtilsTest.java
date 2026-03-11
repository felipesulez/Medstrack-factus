package com.felipesulez.reto_facturacion.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NitUtilsTest {

    @Test
    @DisplayName("🧪 Validando algoritmo Módulo 11 para NITs Colombianos")
    void testCalcularDV() {
        // Caso 1: NIT de la DIAN
        // Entrada: 800197268 -> Esperado: 4
        assertEquals("4", NitUtils.calcularDV("800197268"), "Error en NIT DIAN");

        // Caso 2: NIT de Ecopetrol
        // Entrada: 899999068 -> Esperado: 1
        assertEquals("1", NitUtils.calcularDV("899999068"), "Error en NIT Ecopetrol");

        // Caso 3: NIT de prueba común
        // Entrada: 900700576 -> Esperado: 8
        assertEquals("9", NitUtils.calcularDV("900700576"), "Error en NIT de prueba");
        System.out.println("✅ ¡Todas las pruebas del algoritmo pasaron con éxito!");
    }
}