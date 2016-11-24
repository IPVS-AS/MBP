<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<h1>Sensors</h1>

<p>
    The <em>Sensor</em> is holds all the data necessary for deploying and 
    monitoring a sensor, whice is the hosting <em>Device</em> and the running 
    <em>Script</em>.
</p>
<div class="row">
    <div class="col-lg-6">
        <h3>Register Sensor</h3>
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
    <!-- /.col-lg-6 -->    
    <div class="col-lg-6">
        <h3>
            List of Sensors
        </h3>
        <table width="100%" class="table table-striped table-bordered table-hover" id="sensor-table">
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
</div>
<!-- /.row -->

<script>
    $(document).ready(function () {
        $('#sensor-table').DataTable({
            responsive: true,
            order: [[0, 'desc']]
        });
    });
</script>