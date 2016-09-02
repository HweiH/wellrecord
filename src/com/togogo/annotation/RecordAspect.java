package com.togogo.annotation;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 *  RecordAnno.class注解的驱动
 *  这是与Spring协作的切面类，用于记录日志等需要实现的切面功能
 * @author well 
 * @version 1.0
 * 2015年12月12日00:02:02
 */
@Aspect
@Component
public class RecordAspect {
	
	// 接口，提供给外部的操作规范
	private IRecordFunction recordFunction;

	@Resource(name="recordFunction", type=IRecordFunction.class)
	public void setRecordFunction(IRecordFunction recordFunction) {
		this.recordFunction = recordFunction;
	}

	// EO-Controller层切入点
	@Pointcut("@annotation(com.togogo.annotation.RecordAnno)")
	public void testControllerAspect() {
	}

	/**
	 * 前置通知：用于拦截Controller层记录用户的操作
	 * 
	 * @param joinPoint 切入点，即拦截到的Method
	 */
	@Before("testControllerAspect()")
	public void doBefore(JoinPoint joinPoint) {
		if (null == joinPoint) {
			throw new NullPointerException("RecordAspect-doBefore arg's joinPoint could not be null !!");
		}
		// 通过接口调用(回调)
		if(null != recordFunction) {
			recordFunction.baseFunction(joinPoint);
		}
	}

	
}
