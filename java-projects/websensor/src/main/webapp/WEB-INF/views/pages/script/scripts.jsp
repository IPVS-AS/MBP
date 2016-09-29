<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<h1>Scripts</h1>
<div class="panel-group" id="accordion">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">
                <a data-toggle="collapse" data-parent="#accordion" aria-expanded="true" href="#collapseOne">List of Scripts</a>
            </h4>
        </div>
        <!-- /.panel-heading -->
        <div id="collapseOne" class="panel-collapse collapse in">
            <div class="panel-body">
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
            <!-- /.panel-body -->
        </div>
        <!-- /.collapseOne -->    
    </div>
    <!-- /.panel -->
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">
                <a data-toggle="collapse" data-parent="#accordion" href="#collapseTwo">Register Script</a>   
            </h4>
        </div>
        <!-- /.panel-heading -->
        <div id="collapseTwo" class="panel-collapse collapse">
            <div class="panel-body">
                <div class="col-lg-6">
                    <form:form action="${uriScript}" method="POST" modelAttribute="scriptForm">
                        <div class="form-group" id="script-form-name">
                            <form:label path="name">Name</form:label>
                            <form:input path="name"  type="text" class="form-control" required="required"/>
                            <form:errors path="name" />
                        </div>
                        <div class="form-group" id="script-form-description">
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