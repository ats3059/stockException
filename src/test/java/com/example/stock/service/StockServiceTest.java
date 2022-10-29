package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.reopsitory.StockRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before() {
        Stock stock = new Stock(1L,100L);
        stockRepository.saveAndFlush(stock);
    }

    @Test
    public void stock_decrease() {
        stockService.decrease(1L,1L);
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertThat(99L).isEqualTo(stock.getQuantity());
    }

    @Test
    public void 동시에_100개의_요청() throws InterruptedException{
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(()->{
                try{
                    stockService.decrease(1L,1L);
                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertThat(stock.getQuantity()).isEqualTo(0);

    }

}