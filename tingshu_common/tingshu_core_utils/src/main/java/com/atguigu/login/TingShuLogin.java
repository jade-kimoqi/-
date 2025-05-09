package com.atguigu.login;

import java.lang.annotation.*;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TingShuLogin {
    boolean required() default true;
}
