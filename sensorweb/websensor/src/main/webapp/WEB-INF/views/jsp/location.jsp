<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/jsp/include.jsp" %> 
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Register Location</title>
    </head>
    <body>
        <div align="center">
            <form:form action="location" method="POST" commandName="locationForm">
                <table>
                    <tr>
                        <td><form:label path="name">name: </form:label></td>
                            <td>
                            <form:input path="name" />
                        </td>
                    </tr>
                    <tr>
                        <td><form:label path="description">description: </form:label></td>
                            <td>
                            <form:input path="description" />
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input type="submit" value="Register" />
                        </td>
                    </tr>
                </table>
            </form:form>
        </div>
        <div align="center">
            <table>
                <tr>
                    <td>
                        <c:forEach var="location" items="${locationList}">
                            <c:out value="${location.name}"/><p>
                        </c:forEach>
                    </td>
                </tr>
            </table>
        </div>
    </body>
</html>
