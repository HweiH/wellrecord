package com.togogo.annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.lang.JoinPoint;

public class RecordDemo implements IRecordFunction {

	// Model的根包路径
	private String modelBasePackage = "";
	// 文本参数的正则模式
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
			// 记录信息
			recordInfo(joinPoint);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 记录信息
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
		// 正式开始日志
		String annoMethodName = joinPoint.getSignature().getName(); // 方法名
		String[] sArgs = getArgsTypeString(joinPoint.getStaticPart().toLongString()); // 方法的形式参数类型全名
		Class<?>[] args = getArgsType(sArgs); // 方法的参数类型

		Class<?> targetClass = Class.forName(joinPoint.getTarget().getClass().getName()); // 加载切点所在的Controller
		Method method = null;
		if(null == args || 0 == args.length) {
			method = targetClass.getMethod(annoMethodName); 		// 获取被当前注解的方法
		} else {
			 method = targetClass.getMethod(annoMethodName, args); // 获取被当前注解的方法
		}

		String finalText = getFinalText(joinPoint, method, args);

		// 输出最新描述
		System.out.println(finalText);

	}

	/**
	 * 获取到最终解析之后的文本
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
		StringBuilder sb = new StringBuilder("操作功能：");
		// 获取注解的三个标记
		RecordAnno recordAnno = method.getAnnotation(RecordAnno.class);
		boolean isParsing = recordAnno.isParsing();
		String title = recordAnno.title();
		String text = recordAnno.text();
		// 拼凑
		sb.append(title);
		if (isParsing) {
			// 解析
			text = getParsedText(joinPoint, args, text);
		}
		sb.append(", 功能描述：");
		sb.append(text);
		return sb.toString();
	}

	/**
	 * 获取到解析之后的文本
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
		// 每个参数的值, args是每个参数的类型
		Object[] argVals = joinPoint.getArgs();
		// 单参数，同时是基本类型时，可以用 % 代替
		if (1 == args.length & isJavaBaseType(args[0])) {
			String textArgVal = String.valueOf(argVals[0]);
			if (null != textArgVal & text.indexOf("%") != -1) {
				text = text.replaceAll("%", textArgVal);
				return text;
			}
		}
		// 单参数或者多参数
		// 1、获取 text 中所有不同变量
		List<String> textArgs = getAllTextArgs(text);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < textArgs.size(); ++i) {
			String textArgPattern = textArgs.get(i); // {开头，}结尾
			String textArg = textArgPattern.substring(1, textArgPattern.length() - 1); // 去掉{}
			int argIndex = getArgIndex(textArg, args.length);
			// 调整正则表达式中的{ }这两个特殊字符，使用转义
			sb.append(textArgPattern).insert(0, "\\").insert(textArgPattern.length(), "\\");
			textArgPattern = sb.toString();
			if (textArg.indexOf("@") == -1) { // 非实体
				if (isJavaBaseType(args[argIndex])) {
					String textArgVal = String.valueOf(argVals[argIndex]);
					if (null != textArgVal) {
						text = text.replaceAll(textArgPattern, textArgVal);
					}
				}
			} else { // 实体
				if (null == modelBasePackage || "".equals(modelBasePackage)) {
					throw new RuntimeException("You should regist the modelBasePackage in RecordDemo !!");
				} else if (isJavaBaseType(args[argIndex])) {
					throw new RuntimeException("Maybe the args you pointed is not suitable !!");
				} else if (args[argIndex].getName().startsWith(modelBasePackage)) {
					// 获取实体的字段
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
	 * 将对应传入的对象域字段转换成对应的属性名
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
	 * 获取对应对象的域字段
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
	 * 获取用户指定的参数序号(从0开始)
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
	 * 根据传入进来的用户输入的text，获取其中特定模式的参数
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
	 * 判断传入进来的Class是否Java的基本类型
	 * 
	 * @param arg
	 * @return
	 */
	private boolean isJavaBaseType(Class<?> arg) {
		if (null == arg) {
			return false;
		}
		// 基本类型
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
	 * 根据传入进来的全类型名获取对应的Class
	 * 
	 * @param sArgs
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class<?>[] getArgsType(String[] sArgs) throws ClassNotFoundException {
		// 异常
		if(null == sArgs || 0 == sArgs.length) {
			return null;
		}
		
		Class<?>[] args = new Class<?>[sArgs.length]; // 参数类型容器
		for (int i = 0; i < args.length; ++i) {
			if (sArgs[i].indexOf(".") == -1) { // 参数是基本类型
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
			} else if (sArgs[i].endsWith("[]")) { // 参数是数组类型
				throw new RuntimeException("The array's type of args are not supported now !!");
			} else { // 参数是对象引用类型
				args[i] = Class.forName(sArgs[i]);
			}
		}
		return args;
	}

	/**
	 * 获取切面所在方法的参数的全类型名
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
