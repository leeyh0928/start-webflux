package com.example.startwebflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@RestController
@EnableAsync
@Slf4j
public class StartWebfluxApplication {
    static final String URL1 = "http://localhost:8081/service?req={req}";
    static final String URL2 = "http://localhost:8081/service2?req={req}";

    @Autowired
    MyService myService;

    WebClient client = WebClient.create();

    @GetMapping("rest")
    public Mono<String> rest(int idx) {
        return client.get().uri(URL1, idx).retrieve()
                .bodyToMono(String.class)
                .doOnNext(log::info)
                .flatMap(res1 -> client.get().uri(URL2, res1).retrieve()
                        .bodyToMono(String.class))
                .doOnNext(log::info)
                .flatMap(res2 -> Mono.fromCompletionStage(myService.workAsync(res2)))   // CompletableFuture<String> -> Mono<String>
                .doOnNext(log::info);
    }

    @GetMapping("rest2")
    public Flux<String> rest2(int idx) {
        client.get().uri(URL1, idx).retrieve().bodyToMono(String.class)
                .flatMap(res1 -> client.get().uri(URL2, res1).retrieve().bodyToMono(String.class))
                .subscribe();

        return Flux.fromIterable(getThreads());
    }

    private List<String> getThreads() {
        return Thread.getAllStackTraces()
                .keySet()
                .stream()
                .map(t -> String.format("%-20s \t %s \t %d \t %s\n", t.getName(), t.getState(), t.getPriority(), t.isDaemon() ? "Daemon" : "Normal"))
                .toList();
    }

    public static void main(String[] args) {
//        System.setProperty("reactor.netty.ioWorkerCount", "100");
        SpringApplication.run(StartWebfluxApplication.class, args);
    }

    @Service
    public static class MyService {
        public String work(String req) {
            return req + "/asyncwork";
        }

        @Async
        public CompletableFuture<String> workAsync(String req) {
            return CompletableFuture.completedFuture(req + "/asyncwork");
        }
    }
}
