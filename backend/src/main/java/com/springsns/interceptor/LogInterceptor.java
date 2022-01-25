package com.springsns.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Slf4j
public class LogInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String traceId = UUID.randomUUID().toString();

        log.info("[{}] start====================",traceId);
        MDC.put("traceId",traceId);
        MDC.put("requestURI",requestURI);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        String traceId = MDC.get("traceId");

        MDC.clear();
        log.info("[{}] end====================",traceId);
    }
}
