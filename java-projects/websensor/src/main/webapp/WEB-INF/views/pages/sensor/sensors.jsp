<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<h1>Sensors</h1>
<div class="panel-group" id="accordion">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">
                <a data-toggle="collapse" data-parent="#accordion" aria-expanded="true" href="#collapseOne">List of Sensors</a>
            </h4>
        </div>
        <!-- /.panel-heading -->
        <div id="collapseOne" class="panel-collapse collapse in">
            <div class="panel-body">
                <table width="100%" class="table table-striped table-bordered table-hover" id="dataTables-sensor">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Device</th>
                            <th>Script</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="sensor" items="${sensors}">
                            <tr>
                                <td><a href="
                                       <c:out value="${uriSensor}"/>/<c:out value="${sensor.id}"/>
                                       ">
                                        <c:out value="${sensor.name}" />
                                    </a>
                                </td>
                                <td>
                                    <a href="
                                       <c:out value="${uriDevice}"/>/<c:out value="${sensor.device.id}"/>
                                       ">
                                        <c:out value="${sensor.device.macAddress}"/>
                                    </a>
                                </td>
                                <td>
                                    <a href="
                                       <c:out value="${uriScript}"/>/<c:out value="${sensor.script.id}"/>
                                       ">
                                        <c:out value="${sensor.script.name}"/>
                                    </a>
                                </td>
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
                <a data-toggle="collapse" data-parent="#accordion" href="#collapseTwo">Register Sensor</a>   
            </h4>
        </div>
        <!-- /.panel-heading -->
        <div id="collapseTwo" class="panel-collapse collapse">
            <div class="panel-body">
                <div class="col-lg-6">
                    <form:form action="${uriSensor}" method="POST" modelAttribute="sensorForm">
                        <div class="form-group" id="sensor-form-name">
                            <form:label path="name">Name</form:label>
                            <form:input path="name"  type="text" class="form-control" required="required"/>
                            <form:errors path="name" />
                        </div>
                        <div class="form-group" id="sensor-form-device">
                            <form:label path="device">Device</form:label>
                            <form:select path="device">
                                <form:option label="-- SELECT --" value="${null}"/>
                                <form:options items="${devices}" itemLabel="macAddress" itemValue="id" />
                            </form:select>
                        </div>
                        <div class="form-group" id="sensor-form-script">
                            <form:label path="script">Script</form:label>
                            <form:select path="script">
                                <form:option label="-- SELECT --" value="${null}"/>
                                <form:options items="${scripts}" itemLabel="name" itemValue="id" />
                            </form:select>
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