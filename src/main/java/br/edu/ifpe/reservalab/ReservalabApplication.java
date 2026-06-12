package br.edu.ifpe.reservalab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ReservalabApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservalabApplication.class, args);
    }

}
