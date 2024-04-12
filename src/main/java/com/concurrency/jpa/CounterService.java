package com.concurrency.jpa;


import org.springframework.stereotype.Service;

@Service
public class CounterService {

    public CounterService(CounterRepository counterRepository) {
    }
}
