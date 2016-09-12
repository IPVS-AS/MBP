<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<div class="row">
    <div class="col-lg-4">
        <h1>Device</h1>
        <table width="100%" class="table" id="shown">
            <tr>
                <th>MAC Address</th>
                <td><c:out value="${device.macAddress}"/></td>
            </tr>
            <tr>
                <th>Location</th>
                <td><c:out value="${device.location.name}" default="" /></td>
            </tr>
            <tr>
                <th></th>
                <td> 
                    <button id="show" type="button" title="Edit values" class="btn btn-warning btn-circle"><i class="fa fa-pencil"></i></button>
                    <a href="<c:out value="${uriDelete}" />" title="Delete location" class="btn btn-danger btn-circle"><i class="fa fa-trash-o"></i></a>
                </td>
            </tr>
        </table>
    </div>
    <!-- /.col-lg-12 -->
</div>
<!-- /.row -->