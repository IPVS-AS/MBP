<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page isELIgnored="false" %>
<%@ page session="false"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>

<tiles:importAttribute name="globalcss"/>
<tiles:importAttribute name="topglobaljs"/>
<c:forEach var="script" items="${topglobaljs}">
    <script src="<c:url value="${script}"/>"></script>
</c:forEach>

<tiles:insertAttribute name="body" />

<tiles:importAttribute name="bottomglobaljs"/>
<c:forEach var="script" items="${bottomglobaljs}">
    <script src="<c:url value="${script}"/>"></script>
</c:forEach>