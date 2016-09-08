<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<div class="row">
    <div class="col-lg-4">
        <h1>Script</h1>
        <table width="100%" class="table" id="shown">
            <tr>
                <th>Name</th>
                <td><c:out value="${script.name}"/></td>
            </tr>
            <tr>
                <th>Description</th>
                <td><c:out value="${script.description}" default="" /></td>
            </tr>            
            <tr>
                <th></th>
                <td> 
                    <button onclick="show()" title="Edit values" class="btn btn-warning btn-circle"><i class="fa fa-pencil"></i></button>
                    <a href="<c:out value="${uriDelete}" />" title="Delete script" class="btn btn-danger btn-circle"><i class="fa fa-trash-o"></i></a>
                </td>
            </tr>
        </table>
        <table width="100%" class="table hidden" id="hidden">
            <form:form action="${uriEdit}" method="POST" modelAttribute="scriptForm">
                <form:hidden path="id" />
                <tr>
                    <th>Name</th>
                    <td>
                        <div class="form-group" id="script-form-name">
                            <form:input path="name"  type="text" class="form-control" />
                        </div>
                    </td>
                </tr>
                <tr>
                    <th>Description</th>
                    <td>
                        <div class="form-group" id="script-form-description">
                            <form:textarea path="description" rows="3" cols="20" class="form-control" />
                        </div>
                    </td>
                </tr>
                <tr>
                    <th></th>
                    <td> 
                        <button type="submit" title="Save" class="btn btn-success btn-circle" /><i class="fa fa-check"></i></button>
                        <button type="button" onclick="hide()" title="Cancel" class="btn btn-danger btn-circle">
                            <i class="fa fa-times"></i>
                        </button>
                    </td>
                </tr>
            </form:form>
        </table>
    </div>
</div>
<div class="row">
    <div class="col-lg-4">
        <table width="100%" class="table" id="shown2">
            <tr>
                <th>Service</th>
                <td>
                    <c:choose>
                        <c:when test="${script.service.name}">
                            <c:out value="${script.service.name}" />
                        </c:when>
                        <c:otherwise>
                            Empty File
                        </c:otherwise>
                    </c:choose>
                </td>
                <td> 
                    <button onclick="show2()" title="Edit values" class="btn btn-warning btn-circle"><i class="fa fa-pencil"></i></button>
                    <a href="<c:out value="${uriDeleteService}" />" title="Delete script" class="btn btn-danger btn-circle"><i class="fa fa-trash-o"></i></a>
                </td>
            </tr>
        </table>
        <table width="100%" class="table hidden" id="hidden2">
            <form:form action="${uriEditService}" enctype="multipart/form-data" method="POST" modelAttribute="fileBucket">           
                <tr>
                    <th>Service</th>
                    <td>
                        <div class="form-group" id="script-form-description">
                            <input type="file" name="file" />
                        </div>
                    </td>
                    <td> 
                        <button type="submit" title="Save" class="btn btn-success btn-circle" /><i class="fa fa-check"></i></button>
                        <button type="button" onclick="hide2()" title="Cancel" class="btn btn-danger btn-circle">
                            <i class="fa fa-times"></i>
                        </button>
                    </td>
                </tr>
            </form:form>
        </table>
    </div>
</div>
<div class="row">
    <div class="col-lg-4">
        <table width="100%" class="table" id="shown3">
            <tr>
                <th>Routine</th>
                <td>
                    <c:choose>
                        <c:when test="${script.routine.name}">
                            <c:out value="${script.routine.name}" />
                        </c:when>
                        <c:otherwise>
                            Empty File
                        </c:otherwise>
                    </c:choose>
                </td>
                <td> 
                    <button onclick="show3()" title="Edit values" class="btn btn-warning btn-circle"><i class="fa fa-pencil"></i></button>
                    <a href="<c:out value="${uriDeleteRoutine}" />" title="Delete script" class="btn btn-danger btn-circle"><i class="fa fa-trash-o"></i></a>
                </td>
            </tr>
        </table>
        <table width="100%" class="table hidden" id="hidden3">
            <form:form action="${uriEditRoutine}" enctype="multipart/form-data" method="POST" modelAttribute="fileBucket">       
                <tr>
                    <th>Routine</th>
                    <td>
                        <div class="form-group" id="script-form-description">
                            <input type="file" name="file" />
                        </div>
                    </td>
                    <td> 
                        <button type="submit" title="Save" class="btn btn-success btn-circle" /><i class="fa fa-check"></i></button>
                        <button type="button" onclick="hide3()" title="Cancel" class="btn btn-danger btn-circle">
                            <i class="fa fa-times"></i>
                        </button>
                    </td>
                </tr>
            </form:form>
        </table>
    </div>
    <!-- /.col-lg-4 -->
</div>
<!-- /.row -->