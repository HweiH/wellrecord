package com.togogo.annotation;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 *  RecordAnno.classע�������
 *  ������SpringЭ���������࣬���ڼ�¼��־����Ҫʵ�ֵ����湦��
 * @author well 
 * @version 1.0
 * 2015��12��12��00:02:02
 */
@Aspect
@Component
public class RecordAspect {
	
	// �ӿڣ��ṩ���ⲿ�Ĳ����淶
	private IRecordFunction recordFunction;

	@Resource(name="recordFunction", type=IRecordFunction.class)
	public void setRecordFunction(IRecordFunction recordFunction) {
		this.recordFunction = recordFunction;
	}

	// EO-Controller�������
	@Pointcut("@annotation(com.togogo.annotation.RecordAnno)")
	public void testControllerAspect() {
	}

	/**
	 * ǰ��֪ͨ����������Controller���¼�û��Ĳ���
	 * 
	 * @param joinPoint ����㣬�����ص���Method
	 */
	@Before("testControllerAspect()")
	public void doBefore(JoinPoint joinPoint) {
		if (null == joinPoint) {
			throw new NullPointerException("RecordAspect-doBefore arg's joinPoint could not be null !!");
		}
		// ͨ���ӿڵ���(�ص�)
		if(null != recordFunction) {
			recordFunction.baseFunction(joinPoint);
		}
	}

	
}
