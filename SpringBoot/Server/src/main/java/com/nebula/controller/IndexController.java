package com.nebula.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 代码领取微信公众号【程序员Mars】
 *
 * @className: IndexController
 * @author: Nebula-Hash
 * @date: 2025/11/25 21:25
 */

@RestController
@AllArgsConstructor
public class IndexController {


    private final StringRedisTemplate redisTemplate;


    @GetMapping("/")
    public String index() {
        Long var = redisTemplate.opsForValue().increment("nebula");

        return "Hello World:" + var;
    }

}
