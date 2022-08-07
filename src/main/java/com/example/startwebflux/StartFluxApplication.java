package com.example.startwebflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

@SpringBootApplication
@RestController
@Slf4j
public class StartFluxApplication {
    @GetMapping("/flux")
    Flux<String> hello() {
        Flux<String> f1 = Flux.just("Hello Flux!").log();
        Flux<String> f2 = Flux.just("Second Flux!").log();
        Flux<Tuple2<String, String>> zip = Flux.zip(f1, f2);
        Flux<String> f = zip.flatMap(it -> Flux.just(it.getT1() + " / " + it.getT2())).log();

        return f;
    }

    public static void main(String[] args) {
        SpringApplication.run(StartFluxApplication.class, args);
    }
}
