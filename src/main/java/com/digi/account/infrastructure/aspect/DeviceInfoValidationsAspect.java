package com.digi.account.infrastructure.aspect;

import java.util.Objects;

import com.digi.account.domain.model.dto.AccountStatementRequest;
import com.digi.account.domain.model.dto.ApiResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DeviceInfoValidationsAspect {

    @Around("@within(com.digi.dashboard.infrastructure.annotation.RequireDeviceInfo)")
    public Object validateDeviceInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof AccountStatementRequest request && Objects.isNull(request.getDeviceInfo())) {
                return ResponseEntity.badRequest().body(ApiResponse.deviceInfoNotFound());
            }
        }

        return joinPoint.proceed();
    }
}