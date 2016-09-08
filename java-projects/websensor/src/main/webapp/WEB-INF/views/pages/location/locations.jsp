<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<h1>Locations</h1>
<div class="panel-group" id="accordion">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">
                <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne">List of Locations</a>
            </h4>
        </div>
        <!-- /.panel-heading -->
        <div id="collapseOne" class="panel-collapse collapse">
            <div class="panel-body">
                <table width="100%" class="table table-striped table-bordered table-hover" id="dataTables-location">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Description</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="location" items="${locations}">
                            <tr>
                                <td><a href="
                                       <c:out value="${uriLocation}"/>/<c:out value="${location.id}"/>
                                       ">
                                        <c:out value="${location.name}" />
                                    </a>
                                </td>
                                <td><c:out value="${location.description}"/></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                <!-- /.table-responsive -->
            </div>
            <!-- /.panel-body -->
        </div>
        <!-- /.collapseOne -->    
    </div>
    <!-- /.panel -->
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">
                <a data-toggle="collapse" data-parent="#accordion" href="#collapseTwo">Register Location</a>   
            </h4>
        </div>
        <!-- /.panel-heading -->
        <div id="collapseTwo" class="panel-collapse collapse">
            <div class="panel-body">
                <div class="col-lg-6">
                    <form:form action="location" method="POST" commandName="locationForm">
                        <div class="form-group" id="location-form-name">
                            <form:label path="name">Name</form:label>
                            <form:input path="name"  type="text" class="form-control" required="required"/>
                            <form:errors path="name" />
                        </div>
                        <div class="form-group" id="location-form-description">
                            <form:label path="description">Description</form:label>
                            <form:textarea path="description" rows="3" cols="20" class="form-control" />
                        </div>
                        <input type="submit" value="Register" class="btn btn-default" />
                    </form:form>
                </div>
                <!-- /.col-lg-12 -->
            </div>
            <!-- /.panel-body -->
        </div>
        <!-- /.collapseTwo -->    
    </div>
    <!-- /.panel -->
</div>
<!-- /.panel-group -->