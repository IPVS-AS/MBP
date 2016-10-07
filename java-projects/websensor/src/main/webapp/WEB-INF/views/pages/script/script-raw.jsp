<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h3>
    <a href="<c:out value="${uriId}" default="#" />">
        <c:out value="${title}" default="No file name" />
    </a>
</h3>
<pre>
    <c:out value="${content}" default="No content" />
</pre>
