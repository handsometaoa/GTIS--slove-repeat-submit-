package com.tao.anno;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Gridsum
 */

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatSubmit {

    /**
     * 时间间隔
     */
    long interval() default 5000;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 重复提交时显示的提示语
     */
    String message() default "重复提交";
}
