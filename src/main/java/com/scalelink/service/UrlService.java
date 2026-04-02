package com.scalelink.service;

import com.scalelink.model.UrlMapping;
import com.scalelink.repository.UrlRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String CACHE_PREFIX = "url:";
    private static final String KAFKA_TOPIC = "click-events";

    public UrlService(UrlRepository urlRepository,
                      RedisTemplate<String, String> redisTemplate,
                      KafkaTemplate<String, String> kafkaTemplate) {
        this.urlRepository = urlRepository;
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    public UrlMapping createShortUrl(String originalUrl) {
        String shortCode = generateShortCode();
        UrlMapping mapping = new UrlMapping(shortCode, originalUrl);
        urlRepository.save(mapping);
        redisTemplate.opsForValue().set(CACHE_PREFIX + shortCode, originalUrl, 30, TimeUnit.DAYS);
        return mapping;
    }

    public String getOriginalUrl(String shortCode) {
        String cached = redisTemplate.opsForValue().get(CACHE_PREFIX + shortCode);
        if (cached != null) {
            kafkaTemplate.send(KAFKA_TOPIC, shortCode, "click");
            return cached;
        }
        return urlRepository.findByShortCode(shortCode)
                .map(mapping -> {
                    redisTemplate.opsForValue().set(CACHE_PREFIX + shortCode,
                            mapping.getOriginalUrl(), 30, TimeUnit.DAYS);
                    kafkaTemplate.send(KAFKA_TOPIC, shortCode, "click");
                    return mapping.getOriginalUrl();
                })
                .orElseThrow(() -> new RuntimeException("Short URL not found: " + shortCode));
    }

    private String generateShortCode() {
        long timestamp = System.currentTimeMillis();
        long machineId = 1L;
        long sequence = timestamp % 1000;
        long id = (timestamp << 12) | (machineId << 10) | sequence;
        return Long.toString(Math.abs(id), 36).substring(0, 7).toUpperCase();
    }
}
