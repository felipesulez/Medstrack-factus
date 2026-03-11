package com.felipesulez.reto_facturacion.util;

public class NitUtils {
    public static String calcularDV(String nit) {
        if (nit == null || nit.trim().isEmpty()) return null;

        int[] pesos = {3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71};
        String nitLimpio = nit.replaceAll("[^0-9]", "");
        int suma = 0;

        for (int i = 0; i < nitLimpio.length(); i++) {
            // Tomamos el dígito de derecha a izquierda
            int digito = Character.getNumericValue(nitLimpio.charAt(nitLimpio.length() - 1 - i));
            suma += (digito * pesos[i]);
        }

        int residuo = suma % 11;
        System.out.println("DEBUG MEDSTRACK - Suma: " + suma + " para NIT: " + nitLimpio);
        return (residuo < 2) ? String.valueOf(residuo) : String.valueOf(11 - residuo);
    }
}