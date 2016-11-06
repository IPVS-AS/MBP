<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page isELIgnored="false" %>
<%@ page session="false"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>

<!DOCTYPE html>
<html> 
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title><tiles:getAsString name="title" /></title>

        <!-- stylesheets -->
        <tiles:importAttribute name="globalcss"/>
        <c:forEach var="css" items="${globalcss}">
            <link rel="stylesheet" type="text/css" href="<c:url value="${css}"/>">
        </c:forEach>
        <!-- end stylesheets -->
        
        <!-- scripts -->
        <tiles:importAttribute name="topglobaljs"/>
        <c:forEach var="script" items="${topglobaljs}">
            <script src="<c:url value="${script}"/>"></script>
        </c:forEach>
        <!-- end scripts -->

        <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
        <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
        <!--[if lt IE 9]>
            <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
            <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
        <![endif]-->
    </head>

    <body>
        <nav>
            <header id="header">
                <tiles:insertAttribute name="header" />
            </header>

            <section id="topmenu">
                <tiles:insertAttribute name="topmenu" />
            </section>

            <section id="sidemenu">
                <tiles:insertAttribute name="sidemenu" />
            </section>
        </nav>

        <section id="site-content">
            <div id="page-wrapper">
                <div class="container-fluid">
                    <div class="row">
                        <div class="col-xs-12">
                            <c:if test="${not empty msgSuccess}">
                                <div class="alert alert-success alert-dismissible fade in" role="alert">
                                    <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                                        <span aria-hidden="true">&times;</span>
                                    </button>
                                    <c:out value="${msgSuccess}" />
                                </div>
                            </c:if>
                            <c:if test="${not empty msgError}">
                                <div class="alert alert-danger alert-dismissible fade in" role="alert">
                                    <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                                        <span aria-hidden="true">&times;</span>
                                    </button>
                                    <c:out value="${msgError}" />
                                </div>
                            </c:if>
                            <tiles:insertAttribute name="body" />
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <footer id="footer">
            <tiles:insertAttribute name="footer" />
        </footer>

        <!-- scripts -->
        <tiles:importAttribute name="bottomglobaljs"/>
        <c:forEach var="script" items="${bottomglobaljs}">
            <script src="<c:url value="${script}"/>"></script>
        </c:forEach>
        <!-- end scripts -->
    </body>
</html>