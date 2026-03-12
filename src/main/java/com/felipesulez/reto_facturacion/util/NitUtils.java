package com.felipesulez.reto_facturacion.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j // ✅ Agrega el logger de Lombok
public class NitUtils {

    public static String calcularDV(String nit) {
        if (nit == null || nit.trim().isEmpty()) return null;

        int[] pesos = {3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71};
        String nitLimpio = nit.replaceAll("[^0-9]", "");
        int suma = 0;

        for (int i = 0; i < nitLimpio.length(); i++) {
            int digito = Character.getNumericValue(nitLimpio.charAt(nitLimpio.length() - 1 - i));
            suma += (digito * pesos[i]);
        }

        int residuo = suma % 11;
        // ✅ log.debug solo aparece si el nivel de log está en DEBUG
        // En producción (WARN/INFO) esta línea no genera ninguna salida
        log.debug("Cálculo DV - Suma: {} para NIT: {}", suma, nitLimpio);
        return (residuo < 2) ? String.valueOf(residuo) : String.valueOf(11 - residuo);
    }
}