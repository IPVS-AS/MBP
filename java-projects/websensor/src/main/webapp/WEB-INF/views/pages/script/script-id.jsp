<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<div class="row">
    <div class="col-lg-6">
        <div class="row">
            <div class="col-lg-12">
                <h1>Script</h1>

                <p class="lead">
                    A script needs a .conf file as the <em>Service</em>. That
                    Service is a set of instructions to run the Script.
                    <br>
                    A script also needs the code, which should be provided on
                    the <em>Routines</em> part. Multiple files may be used for
                    that.
                    <br>
                </p>

                <h2>Script Data</h2>
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
                            <button id="show" type="button" title="Edit values" class="btn btn-warning btn-circle btn-lg">
                                <i class="fa fa-pencil"></i>
                            </button>
                            <form:form action="${uriId}" method="DELETE" modelAttribute="scriptForm" style="display: inline;">
                                <button id="show" type="submit" title="Delete Script" class="btn btn-danger btn-circle btn-lg">
                                    <i class="fa fa-trash-o"></i>
                                </button>
                            </form:form>
                        </td>
                    </tr>
                </table>

                <table width="100%" class="table hidden" id="hidden">
                    <form:form action="${uriId}" method="PUT" modelAttribute="scriptForm">
                        <form:hidden path="id" />
                        <tr>
                            <spring:bind path="name">
                                <th>
                                    <form:label path="name">
                                        Name
                                    </form:label>
                                </th>
                                <td>
                                    <div class="form-group ${status.error ? 'has-error' : ''}" id="script-form-name">
                                        <form:input path="name"  type="text" class="form-control" />
                                        <form:errors path="name" cssClass="text-danger" />
                                    </div>
                                </td>
                            </spring:bind>
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
                                <button type="submit" title="Save" class="btn btn-success btn-circle btn-lg">
                                    <i class="fa fa-check"></i>
                                </button>
                                <button id="hide" type="button" title="Cancel" class="btn btn-danger btn-circle btn-lg">
                                    <i class="fa fa-times"></i>
                                </button>
                            </td>
                        </tr>
                    </form:form>
                </table>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-6">
                <h3>Service</h3>
                <table width="100%" class="table">
                    <c:choose>
                        <c:when test="${script.service == null or empty script.service}">
                            <form:form action="${uriService}" enctype="multipart/form-data" method="POST" modelAttribute="fileBucket"> 
                                <tr>
                                    <td class="vert-align">
                                        <div class="form-group" id="script-form-description">
                                            <label class="btn btn-default btn-file">
                                                Choose File <input type="file" id="file" name="file" style="display: none;">
                                            </label>
                                        </div>
                                    </td>
                                    <td>
                                        <button type="submit" title="Set Service" class="btn btn-success btn-circle btn-lg">
                                            <i class="fa fa-plus"></i>
                                        </button>
                                    </td>
                                </tr>
                            </form:form>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td>
                                    <a href="<c:out value="${uriService}" />" title="View raw content" target="blank">
                                        <c:out value="${script.service.name}" />
                                    </a>
                                </td>
                                <td> 
                                    <c:if test="${script.service != null}">
                                        <form:form action="${uriService}" method="DELETE" modelAttribute="fileBucket" style="display: inline;">
                                            <button type="submit" title="Delete Service" class="btn btn-danger btn-circle btn-lg">
                                                <i class="fa fa-trash-o"></i>
                                            </button>
                                        </form:form>                 
                                    </c:if>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </table>
            </div>
            <div class="col-lg-6">
                <h3>Routines</h3>
                <table width="100%" class="table">                    
                    <form:form action="${uriRoutine}" enctype="multipart/form-data" method="POST" modelAttribute="fileBucket">       
                        <tr>
                            <td class="vert-align">
                                <div class="form-group" id="script-form-description">
                                    <label class="btn btn-default btn-file">
                                        Choose File <input type="file" id="file" name="file"  style="display: none;" />
                                    </label>
                                </div>
                            </td>
                            <td> 
                                <button type="submit" title="Add Routine" class="btn btn-success btn-circle btn-lg">
                                    <i class="fa fa-plus"></i>
                                </button>
                            </td>
                        </tr>
                    </form:form>

                    <c:choose>
                        <c:when test="${script.routines == null or empty script.routines}">
                            <!-- Empty Routine List -->
                            <tr>
                                <td>
                                    No routines added yet.
                                </td>
                                <td>

                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <!-- List of Routines -->
                            <c:forEach var="routine" items="${script.routines}">
                                <tr>
                                    <td>
                                        <a href="<c:out value="${uriRoutine}/${routine.name}" />" title="View raw content" target="blank">
                                            <c:out value="${routine.name}" />
                                        </a>
                                    </td>
                                    <td>
                                        <form:form action="${uriRoutine}/${routine.name}" method="DELETE" modelAttribute="fileBucket" style="display: inline;">
                                            <button id="show3" type="submit" title="Delete Routine" class="btn btn-danger btn-circle btn-lg">
                                                <i class="fa fa-trash-o"></i>
                                            </button>
                                        </form:form>

                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </table>
            </div>
            <!-- /.col-lg-6 -->
        </div>
        <!-- /.row -->
    </div>
    <!-- /.col-lg-6 -->
</div>
<!-- /.row -->

<script>
    <c:if test="${showForm}">
        $("#hidden").removeClass('hidden');
        $("#shown").addClass('hidden');
    </c:if>
</script>