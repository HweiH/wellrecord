package com.togogo.annotation;

import org.aspectj.lang.JoinPoint;

/**
 *  ���ǹ����Ĳ����ӿڣ��û�����ͨ��ʵ������ӿ������һЩ�Զ���Ĳ���
 * @author well
 * @version 1.0
 *  2015��12��12��19:28:22
 */
public interface IRecordFunction {

	// �����Ĳ������������
	public void baseFunction(JoinPoint joinPoint);
	
}
