<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h3>
    <c:out value="${title}" default="" />
</h3>
<pre>
    <c:out value="${content}" default="No content" />
</pre>
