package com.togogo.annotation;

import org.aspectj.lang.JoinPoint;

/**
 *  这是公共的操作接口，用户可以通过实现这个接口来完成一些自定义的操作
 * @author well
 * @version 1.0
 *  2015年12月12日19:28:22
 */
public interface IRecordFunction {

	// 基本的操作：切面操作
	public void baseFunction(JoinPoint joinPoint);
	
}
