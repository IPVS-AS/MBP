<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<h1>Scripts</h1>

<p class="lead">
    The <em>Script</em> holds a set of files that determine how a sensor
    is going to work. Which means, a file to set up the <em>Service</em> and a
    set of files with the runnable <em>Routine</em> (aka the code).
    <br>
    Files can be added once the <em>Script</em> is registered.
    <br>
</p>

<hr>

<div class="row">    
    <div class="col-lg-6">
        <h3>List of Scripts</h3>
        <table width="100%" class="table table-striped table-bordered table-hover" id="dataTables-script">
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Description</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="script" items="${scripts}">
                    <tr>
                        <td><a href="
                               <c:out value="${uriScript}"/>/<c:out value="${script.id}"/>
                               ">
                                <c:out value="${script.name}" />
                            </a>
                        </td>
                        <td><c:out value="${script.description}"/></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
        <!-- /.table-responsive -->
    </div>
    <!-- /.col-lg-6 -->
    <div class="col-lg-6">
        <h3>Register Script</h3>
        <form:form action="${uriScript}" method="POST" modelAttribute="scriptForm">
            <spring:bind path="name">
                <div class="form-group ${status.error ? 'has-error' : ''}" id="script-form-name">
                    <form:label path="name">Name</form:label>
                    <form:input path="name" type="text" cssClass="form-control" required="required"/>
                    <form:errors path="name" cssClass="text-danger" />
                </div>
            </spring:bind>
            <div class="form-group" id="script-form-description">
                <form:label path="description">Description</form:label>
                <form:textarea path="description" rows="3" cols="20" class="form-control" />
            </div>                        
            <input type="submit" value="Register" class="btn btn-default" />
        </form:form>
    </div>
    <!-- /.col-lg-6 -->
</div>