package com.togogo.annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.lang.JoinPoint;

public class RecordDemo implements IRecordFunction {

	// Model�ĸ���·��
	private String modelBasePackage = "";
	// �ı�����������ģʽ
	private String textArgsRegex = "\\{.*?\\}";

	public void setModelBasePackage(String modelBasePackage) {
		this.modelBasePackage = modelBasePackage;
	}

	public void setTextArgsRegex(String textArgsRegex) {
		this.textArgsRegex = textArgsRegex;
	}

	@Override
	public void baseFunction(JoinPoint joinPoint) {
		try {
			// ��¼��Ϣ
			recordInfo(joinPoint);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��¼��Ϣ
	 * 
	 * @param joinPoint
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void recordInfo(JoinPoint joinPoint) throws ClassNotFoundException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// ��ʽ��ʼ��־
		String annoMethodName = joinPoint.getSignature().getName(); // ������
		String[] sArgs = getArgsTypeString(joinPoint.getStaticPart().toLongString()); // ��������ʽ��������ȫ��
		Class<?>[] args = getArgsType(sArgs); // �����Ĳ�������

		Class<?> targetClass = Class.forName(joinPoint.getTarget().getClass().getName()); // �����е����ڵ�Controller
		Method method = null;
		if(null == args || 0 == args.length) {
			method = targetClass.getMethod(annoMethodName); 		// ��ȡ����ǰע��ķ���
		} else {
			 method = targetClass.getMethod(annoMethodName, args); // ��ȡ����ǰע��ķ���
		}

		String finalText = getFinalText(joinPoint, method, args);

		// �����������
		System.out.println(finalText);

	}

	/**
	 * ��ȡ�����ս���֮����ı�
	 * 
	 * @param joinPoint
	 * @param method
	 * @param args
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private String getFinalText(JoinPoint joinPoint, Method method, Class<?>[] args) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		StringBuilder sb = new StringBuilder("�������ܣ�");
		// ��ȡע����������
		RecordAnno recordAnno = method.getAnnotation(RecordAnno.class);
		boolean isParsing = recordAnno.isParsing();
		String title = recordAnno.title();
		String text = recordAnno.text();
		// ƴ��
		sb.append(title);
		if (isParsing) {
			// ����
			text = getParsedText(joinPoint, args, text);
		}
		sb.append(", ����������");
		sb.append(text);
		return sb.toString();
	}

	/**
	 * ��ȡ������֮����ı�
	 * 
	 * @param joinPoint
	 * @param args
	 * @param text
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private String getParsedText(JoinPoint joinPoint, Class<?>[] args, String text) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (null == joinPoint || null == args || 1 > args.length || null == text) {
			return text;
		}
		// ÿ��������ֵ, args��ÿ������������
		Object[] argVals = joinPoint.getArgs();
		// ��������ͬʱ�ǻ�������ʱ�������� % ����
		if (1 == args.length & isJavaBaseType(args[0])) {
			String textArgVal = String.valueOf(argVals[0]);
			if (null != textArgVal & text.indexOf("%") != -1) {
				text = text.replaceAll("%", textArgVal);
				return text;
			}
		}
		// ���������߶����
		// 1����ȡ text �����в�ͬ����
		List<String> textArgs = getAllTextArgs(text);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < textArgs.size(); ++i) {
			String textArgPattern = textArgs.get(i); // {��ͷ��}��β
			String textArg = textArgPattern.substring(1, textArgPattern.length() - 1); // ȥ��{}
			int argIndex = getArgIndex(textArg, args.length);
			// ����������ʽ�е�{ }�����������ַ���ʹ��ת��
			sb.append(textArgPattern).insert(0, "\\").insert(textArgPattern.length(), "\\");
			textArgPattern = sb.toString();
			if (textArg.indexOf("@") == -1) { // ��ʵ��
				if (isJavaBaseType(args[argIndex])) {
					String textArgVal = String.valueOf(argVals[argIndex]);
					if (null != textArgVal) {
						text = text.replaceAll(textArgPattern, textArgVal);
					}
				}
			} else { // ʵ��
				if (null == modelBasePackage || "".equals(modelBasePackage)) {
					throw new RuntimeException("You should regist the modelBasePackage in RecordDemo !!");
				} else if (isJavaBaseType(args[argIndex])) {
					throw new RuntimeException("Maybe the args you pointed is not suitable !!");
				} else if (args[argIndex].getName().startsWith(modelBasePackage)) {
					// ��ȡʵ����ֶ�
					String objFiledStr = getObjFiledStr(textArg, argIndex);
					String getterName = getPropertyName(objFiledStr);
					Method getterMethod = args[argIndex].getMethod(getterName);
					String textArgVal = String.valueOf(getterMethod.invoke(argVals[argIndex]));
					if (null != textArgVal) {
						text = text.replaceAll(textArgPattern, textArgVal);
					}
				}
			}
			sb.delete(0, sb.length());
		}
		return text;
	}

	/**
	 * ����Ӧ����Ķ������ֶ�ת���ɶ�Ӧ��������
	 * 
	 * @param objFiledStr
	 * @return
	 */
	private String getPropertyName(String objFiledStr) {
		StringBuilder sb = new StringBuilder("get");
		char[] chr = objFiledStr.toCharArray();
		if (chr[0] >= 97 && chr[0] < 123) {
			chr[0] -= 32;
		}
		return sb.append(chr).toString();
	}

	/**
	 * ��ȡ��Ӧ��������ֶ�
	 * 
	 * @param textArg
	 * @param argIndex
	 * @return
	 */
	private String getObjFiledStr(String textArg, int argIndex) {
		String objFiledStr = null;
		String argIndexStr = String.valueOf(argIndex);
		if (textArg.endsWith(argIndexStr)) {
			objFiledStr = textArg.substring(0, textArg.indexOf("@"));
		} else {
			objFiledStr = textArg.substring(textArg.lastIndexOf("@"));
		}

		if (null == objFiledStr || "".equals(objFiledStr)) {
			throw new RuntimeException("There are not field or property in the OBJ !!");
		}
		return objFiledStr;
	}

	/**
	 * ��ȡ�û�ָ���Ĳ������(��0��ʼ)
	 * 
	 * @param textArg
	 * @param argsTotalNum
	 * @return
	 */
	private int getArgIndex(String textArg, int argsTotalNum) {
		int argIndex = -1;
		int chrStart = textArg.charAt(0);
		int chrEnd = textArg.charAt(textArg.length() - 1);

		if (chrStart >= 48 && chrStart < 58) {
			argIndex = chrStart - 48;
		}
		if (chrEnd >= 48 && chrEnd < 58) {
			argIndex = chrEnd - 48;
		}
		if (-1 == argIndex || argIndex >= argsTotalNum) {
			throw new IndexOutOfBoundsException("There are not the param you pointed in method's formal parameters !!");
		}
		return argIndex;
	}

	/**
	 * ���ݴ���������û������text����ȡ�����ض�ģʽ�Ĳ���
	 * 
	 * @param text
	 * @return
	 */
	private List<String> getAllTextArgs(String text) {
		List<String> tmp = new ArrayList<String>();
		Pattern pattern = Pattern.compile(textArgsRegex);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String str = matcher.group();
			if (!tmp.contains(str)) {
				tmp.add(matcher.group());
			}
		}
		return tmp;
	}

	/**
	 * �жϴ��������Class�Ƿ�Java�Ļ�������
	 * 
	 * @param arg
	 * @return
	 */
	private boolean isJavaBaseType(Class<?> arg) {
		if (null == arg) {
			return false;
		}
		// ��������
		String name = arg.getName();
		if ("java.lang.Byte".equals(name) || "java.lang.Short".equals(name) || "java.lang.Integer".equals(name)
				|| "java.lang.Long".equals(name) || "java.lang.Float".equals(name) || "java.lang.Double".equals(name)
				|| "java.lang.Character".equals(name) || "java.lang.Boolean".equals(name)
				|| "java.lang.String".equals(name) || "java.util.Date".equals(name)) {
			return true;
		}
		return false;
	}

	/**
	 * ���ݴ��������ȫ��������ȡ��Ӧ��Class
	 * 
	 * @param sArgs
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class<?>[] getArgsType(String[] sArgs) throws ClassNotFoundException {
		// �쳣
		if(null == sArgs || 0 == sArgs.length) {
			return null;
		}
		
		Class<?>[] args = new Class<?>[sArgs.length]; // ������������
		for (int i = 0; i < args.length; ++i) {
			if (sArgs[i].indexOf(".") == -1) { // �����ǻ�������
				if ("byte".equals(sArgs[i])) {
					args[i] = byte.class;
				} else if ("short".equals(sArgs[i])) {
					args[i] = short.class;
				} else if ("int".equals(sArgs[i])) {
					args[i] = int.class;
				} else if ("long".equals(sArgs[i])) {
					args[i] = long.class;
				} else if ("float".equals(sArgs[i])) {
					args[i] = float.class;
				} else if ("double".equals(sArgs[i])) {
					args[i] = double.class;
				} else if ("char".equals(sArgs[i])) {
					args[i] = char.class;
				} else if ("boolean".equals(sArgs[i])) {
					args[i] = boolean.class;
				}
			} else if (sArgs[i].endsWith("[]")) { // ��������������
				throw new RuntimeException("The array's type of args are not supported now !!");
			} else { // �����Ƕ�����������
				args[i] = Class.forName(sArgs[i]);
			}
		}
		return args;
	}

	/**
	 * ��ȡ�������ڷ����Ĳ�����ȫ������
	 * 
	 * @param longTemp
	 * @return
	 */
	private String[] getArgsTypeString(String longTemp) {
		if (null == longTemp || "".equals(longTemp)) {
			return new String[0];
		}
		String tmp = longTemp.substring(longTemp.lastIndexOf("(") + 1, longTemp.length() - 2);
		return "".equals(tmp) ? new String[0] : tmp.split(",");
	}
}
