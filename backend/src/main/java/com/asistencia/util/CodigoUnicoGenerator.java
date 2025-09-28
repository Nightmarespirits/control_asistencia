package com.asistencia.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class CodigoUnicoGenerator {
    
    private static final AtomicInteger counter = new AtomicInteger(1);
    private static final String PREFIX = "EMP";
    
    /**
     * Genera un código único para empleado en formato EMP + número secuencial de 3 dígitos
     * Ejemplo: EMP001, EMP002, etc.
     */
    public static String generarCodigoEmpleado() {
        int numero = counter.getAndIncrement();
        return String.format("%s%03d", PREFIX, numero);
    }
    
    /**
     * Genera un código único basado en timestamp para casos especiales
     */
    public static String generarCodigoConTimestamp() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return PREFIX + timestamp.substring(timestamp.length() - 6);
    }
    
    /**
     * Reinicia el contador (útil para testing)
     */
    public static void reiniciarContador() {
        counter.set(1);
    }
    
    /**
     * Establece el siguiente número del contador
     */
    public static void establecerSiguienteNumero(int numero) {
        counter.set(numero);
    }
}