package com.togogo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 	记录标记
 * @author well
 * 2015年12月11日22:40:39
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RecordAnno {
	// 特殊功能标记，true：开启；false：关闭
	boolean isParsing() default false;
	// 标题
	String title() default "";
	// 详情
	String text() default "";
}