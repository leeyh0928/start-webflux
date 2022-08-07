package com.example.startwebflux;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@RestController
@Slf4j
public class StartFlux2Application {
    @GetMapping("/event/{id}")
    Mono<Event> event(@PathVariable long id) {
        return Mono.just(new Event(id, "event" + id)).log();
    }

    @GetMapping("/events/{id}")
    Mono<List<Event>> events(@PathVariable long id) {
        List<Event> events = Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2"));
        return Mono.just(events).log();
    }

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> events() {
//        List<Event> events = Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2"));
//        return Flux.fromIterable(events);
        return Flux
//                .fromStream(Stream.generate(() -> new Event(System.currentTimeMillis(), "value")))
//                .range(1, 10)
//                .<Event>generate(sink -> sink.next(new Event(System.currentTimeMillis(), "value")))
                .<Event, Long>generate(() -> 1L, (id, sink) -> {
                    sink.next(new Event(id, "value" + id));
                    return id + 1;
                })
                .delayElements(Duration.ofSeconds(1))
                .take(10);
    }

    @GetMapping(value = "/v2/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> eventsV2() {
        Flux<Event> es = Flux.generate(() -> 1L, (id, sink) -> {
            sink.next(new Event(id, "value" + id));
            return id + 1;
        });
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

        return Flux.zip(es, interval)
                .map(Tuple2::getT1)
                .take(10);
    }

    @GetMapping(value = "/v3/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> eventsV3() {
        Flux<String> es = Flux.generate(sink -> sink.next("Value"));
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

        return Flux.zip(es, interval)
                .map(tu -> new Event(tu.getT2(), tu.getT1()))
                .take(10);
    }

    public static void main(String[] args) {
        SpringApplication.run(StartFlux2Application.class, args);
    }

    @Data @AllArgsConstructor
    public static class Event {
        long id;
        String value;
    }
}
