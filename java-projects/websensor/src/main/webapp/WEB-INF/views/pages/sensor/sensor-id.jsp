<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<div class="row">
    <div class="col-lg-4">
        <h1>Sensor</h1>
        <table width="100%" class="table" id="shown">
            <tr>
                <th>Name</th>
                <td><c:out value="${sensor.name}"/></td>
            </tr>
            <tr>
                <th>Device</th>
                <td>
                    <a href="
                       <c:out value="${uriDevice}"/>/<c:out value="${sensor.device.id}"/>
                       ">
                        <c:out value="${sensor.device.macAddress}" default="" />
                    </a>
                </td>
            </tr>
            <tr>
                <th>Script</th>
                <td>
                    <a href="
                       <c:out value="${uriScript}"/>/<c:out value="${sensor.script.id}"/>
                       ">
                        <c:out value="${sensor.script.name}" default="" />
                    </a>
                </td>
            </tr>
            <tr>
                <th></th>
                <td> 
                    <button id="show" type="button" title="Edit values" class="btn btn-warning btn-circle"><i class="fa fa-pencil"></i></button>
                    <a href="<c:out value="${uriDelete}" />" title="Delete sensor" class="btn btn-danger btn-circle"><i class="fa fa-trash-o"></i></a>
                </td>
            </tr>
        </table>
        <table width="100%" class="table hidden" id="hidden">
            <form:form action="${uriEdit}" method="POST" modelAttribute="sensorForm">
                <form:hidden path="id" />
                <tr>
                    <th>Name</th>
                    <td>
                        <div class="form-group" id="sensor-form-name">
                            <form:input path="name"  type="text" class="form-control" />
                            <form:errors path="name" />
                        </div>
                    </td>
                </tr>
                <tr>
                    <th>Device</th>
                    <td>
                        <div class="form-group" id="sensor-form-device">
                            <form:select path="device">
                                <form:options items="${devices}" itemLabel="macAddress" itemValue="id" />
                            </form:select>
                        </div>
                    </td>
                </tr>
                <tr>
                    <th>Script</th>
                    <td>
                        <div class="form-group" id="sensor-form-script">
                            <form:select path="script">
                                <form:options items="${scripts}" itemLabel="name" itemValue="id" />
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