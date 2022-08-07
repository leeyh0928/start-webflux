package com.example.startwebflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@SpringBootApplication
@RestController
@Slf4j
public class StartMonoApplication {
    @GetMapping("/")
    Mono<String> hello() {
        log.info("pos1");
        Mono<String> m = Mono.just("Hello WebFlux").log();
//        Mono<String> m = Mono.just(generateHello()).log();
//        Mono<String> m = Mono.fromSupplier(this::generateHello).log();

        // multiple
        Mono<Tuple2<String, String>> zip = Mono.zip(m, Mono.fromSupplier(this::generateHello));
//        m = zip.flatMap(tuple -> Mono.just(tuple.getT1() + " / " + tuple.getT2())).log();
//        m = zip.map(tu -> tu.getT1() + " / " + tu.getT1());
        // error
//        String msg = m.block();
        log.info("pos2");
        return m; // Publisher -> (Publisher) -> (Publisher) -> Subscriber
    }

    private String generateHello() {
        log.info("method generateHello()");
        return "Hello";
    }

    public static void main(String[] args) {
        SpringApplication.run(StartMonoApplication.class, args);
    }
}
