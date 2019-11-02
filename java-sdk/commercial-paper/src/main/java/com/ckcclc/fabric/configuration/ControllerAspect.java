package com.ckcclc.fabric.configuration;


import com.alibaba.fastjson.JSON;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.http.HttpServletRequest;


@Aspect
@Order(1)
@Configuration
public class ControllerAspect {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAspect.class);

    private static final AtomicLong idGenerator = new AtomicLong();

    /** * 监控com.meitu.mlink.backend.controller包public方法 */
    @Pointcut("execution(* com.ckcclc.fabric.controller.*.*(..))")
    private void api() {
    }

    @Around("api()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        String method = signature.toString();
        Object[] arguments = joinPoint.getArgs();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String requestMethod = request.getMethod();
        String uri = request.getRequestURI();
        String host = request.getRemoteHost();
        Long id = idGenerator.getAndIncrement();
        Long start = System.currentTimeMillis();
        logger.info("[Request] id:{}. uri:{} {} from host:{}. Invoke method:[{}] with arguments:{}.",
                id, requestMethod, uri, host, method, arguments);

        Object response = joinPoint.proceed();
        Long end = System.currentTimeMillis();
        logger.info("[Response] id:{}, time elapsed:{} ms. uri:{} {} from host:{}. Response:{}",
                id, end - start, requestMethod, uri, host, JSON.toJSONString(response));
        return response;
    }
}
