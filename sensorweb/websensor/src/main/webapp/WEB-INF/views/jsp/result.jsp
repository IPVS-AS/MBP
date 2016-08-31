<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/jsp/include.jsp" %> 
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Result</title>
    </head>
    <body>
        <p>
            <c:if test="${success}">
                <c:out value="${operation}" /> succeeded.
            </c:if>
            <c:if test="${fail}">
                <c:out value="${operation}" /> failed.
            </c:if>
        </p>

        <c:if test="${success}">
            <p>
                Result: <c:out value="${result}" />
            </p>
        </c:if>

        <p><a href="<c:out value="${pageContext.request.contextPath}${view}" />">Back</a></p>
    </body>
</html>
