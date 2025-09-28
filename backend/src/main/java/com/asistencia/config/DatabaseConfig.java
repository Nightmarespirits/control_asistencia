package com.asistencia.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan(basePackages = "com.asistencia.entity")
@EnableJpaRepositories(basePackages = "com.asistencia.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    
    // Esta clase configura el escaneo de entidades JPA y repositorios
    // También habilita el manejo de transacciones
    
}