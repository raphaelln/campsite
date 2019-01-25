package com.upgrade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
public class EmbededRedisConfiguration
{

    @Value("${spring.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        redisServer = new RedisServer(redisPort);
        try {
            System.out.println("*************** STARTING REDIS");
            redisServer.start();
        } catch (Exception e ) {
            e.printStackTrace();
            throw  e;
        }
    }

    @PreDestroy
    public void stopRedis() {
        System.out.println("*************** STOPING REDIS");
        redisServer.stop();
    }
}