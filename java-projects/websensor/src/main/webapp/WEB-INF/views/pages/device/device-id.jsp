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
                <td>
                    <a href="
                       <c:out value="${uriLocation}"/>/<c:out value="${device.location.id}"/>
                       ">
                        <c:out value="${device.location.name}" default="" />
                    </a>
                </td>
            </tr>
            <c:if test="${not empty heartbeatResult}">
                <tr>
                    <th>Status</th>
                    <td class="
                        <c:if test="${heartbeatResult.status eq 'REACHABLE'}"> text-success </c:if>
                        <c:if test="${heartbeatResult.status eq 'UNREACHABLE'}"> text-warning </c:if>
                        ">
                        <c:out value="${heartbeatResult.status}" default="UNDEFINED" />
                    </td>
                </tr>    
            </c:if>
            <tr>
                <th></th>
                <td> 
                    <button id="show" type="button" title="Edit values" class="btn btn-warning btn-circle"><i class="fa fa-pencil"></i></button>
                    <a href="<c:out value="${uriDelete}" />" title="Delete device" class="btn btn-danger btn-circle"><i class="fa fa-trash-o"></i></a>
                    <a href="<c:out value="${uriHeartbeat}" />" title="Register on Heartbeat" class="btn btn-success btn-circle
                       <c:if test="${hasHeartbeat}">disabled</c:if>">
                           <i class="fa fa-medkit"></i>
                       </a>
                    </td>
                </tr>
            </table>
            <table width="100%" class="table hidden" id="hidden">
            <form:form action="${uriEdit}" method="POST" modelAttribute="deviceForm">
                <form:hidden path="id" />
                <tr>
                    <th>MAC Address</th>
                    <td>
                        <div class="form-group" id="device-form-mac-address">
                            <form:input path="macAddress"  type="text" class="form-control" />
                            <form:errors path="macAddress" />
                        </div>
                    </td>
                </tr>
                <tr>
                    <th>Location</th>
                    <td>
                        <div class="form-group" id="device-form-location">
                            <form:select path="location">
                                <form:options items="${locations}" itemLabel="name" itemValue="id" />
                            </form:select>
                        </div>
                    </td>
                </tr>
                <tr>
                    <th></th>
                    <td> 
                        <button type="submit" title="Save" class="btn btn-success btn-circle" /><i class="fa fa-check"></i></button>
                        <button id="hide" type="button"  title="Cancel" class="btn btn-danger btn-circle">
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