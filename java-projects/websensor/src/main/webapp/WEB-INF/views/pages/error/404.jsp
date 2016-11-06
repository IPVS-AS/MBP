<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%> 
<h1>Page Not Found</h1>
<em><c:out value="${exception.message}" /></em>

<!--
  Failed URL: ${url}
  Exception:  ${exception.message}
<c:forEach items="${exception.stackTrace}" var="ste">    ${ste} 
</c:forEach>
-->