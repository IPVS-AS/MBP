<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<div class="row">
    <div class="col-lg-4">
        <h1>Location</h1>
        <table width="100%" class="table" id="shown">
            <tr>
                <th>Name</th>
                <td><c:out value="${location.name}"/></td>
            </tr>
            <tr>
                <th>Description</th>
                <td><c:out value="${location.description}" default="" /></td>
            </tr>
            <tr>
                <th></th>
                <td> 
                    <button onclick="show()" title="Edit values" class="btn btn-warning btn-circle"><i class="fa fa-pencil"></i></button>
                    <a href="<c:out value="${uriDelete}" />" title="Delete location" class="btn btn-danger btn-circle"><i class="fa fa-trash-o"></i></a>
                </td>
            </tr>
        </table>
        <table width="100%" class="table hidden" id="hidden">
            <form:form action="${uriEdit}" method="POST" modelAttribute="locationForm">
                <form:hidden path="id" />
                <tr>
                    <th>Name</th>
                    <td>
                        <div class="form-group" id="location-form-name">
                            <form:input path="name"  type="text" class="form-control" />
                        </div>
                    </td>
                </tr>
                <tr>
                    <th>Description</th>
                    <td>
                        <div class="form-group" id="location-form-description">
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
    <!-- /.col-lg-12 -->
</div>
<!-- /.row -->