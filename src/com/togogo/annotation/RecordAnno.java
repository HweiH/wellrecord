package com.togogo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 	��¼���
 * @author well
 * 2015��12��11��22:40:39
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RecordAnno {
	// ���⹦�ܱ�ǣ�true��������false���ر�
	boolean isParsing() default false;
	// ����
	String title() default "";
	// ����
	String text() default "";
}