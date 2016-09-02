<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<c:choose>
		<c:when test="${false == isLogin}">
			RecordAnno注解测试：<br/>
			tailNo: ${tailNo} <br/>
			tailNos: ${tailNos} <br/>
			TailDto.tailNo: ${tailNoINTailDto} <br/>
			TailDto.tailNos: ${tailNosINTailDto} <br/>
		</c:when>
		<c:otherwise>
			正在登录...
		</c:otherwise>
	</c:choose>
</body>
</html>