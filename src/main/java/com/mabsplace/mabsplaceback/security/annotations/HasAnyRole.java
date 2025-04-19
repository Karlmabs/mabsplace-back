package com.mabsplace.mabsplaceback.security.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, #this.getClass().getMethod(#root.method.name,#root.method.parameterTypes).getAnnotation(T(com.mabsplace.mabsplaceback.security.annotations.HasAnyRole)).value())")
public @interface HasAnyRole {
    String[] value();
}