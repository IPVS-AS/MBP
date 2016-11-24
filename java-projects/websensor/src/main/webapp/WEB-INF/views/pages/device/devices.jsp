<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<h1>Devices</h1>

<p>
    The <em>Device</em> addresses a real one and is able to monitor the state
    of the same.
</p>

<div class="row">
    <div class="col-lg-6">
        <h3>Register Device</h3>
        <form:form action="${uriDevice}" method="POST" modelAttribute="deviceForm">
            <div class="form-group" id="device-form-name">
                <form:label path="macAddress">MAC Address</form:label>
                <form:input path="macAddress"  type="text" class="form-control" required="required"/>
                <form:errors path="macAddress" />
            </div>
            <div class="form-group" id="device-form-description">
                <form:label path="location">Location</form:label>
                <form:select path="location">
                    <form:option label="-- SELECT --" value="${null}"/>
                    <form:options items="${locations}" itemLabel="name" itemValue="id" />
                </form:select>
            </div>
            <input type="submit" value="Register" class="btn btn-default" />
        </form:form>
    </div>
    <!-- /.col-lg-6 -->    
    <div class="col-lg-6">
        <h3>List of Devices</h3>
        <table width="100%" class="table table-striped table-bordered table-hover" id="device-table">
            <thead>
                <tr>
                    <th>MAC Address</th>
                    <th>Location</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="device" items="${devices}">
                    <tr>
                        <td><a href="
                               <c:out value="${uriDevice}"/>/<c:out value="${device.id}"/>
                               ">
                                <c:out value="${device.macAddress}" />
                            </a>
                        </td>
                        <td>
                            <a href="
                               <c:out value="${uriLocation}"/>/<c:out value="${device.location.id}"/>
                               ">
                                <c:out value="${device.location.name}"/>
                            </a>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
        <!-- /.table-responsive -->
    </div>
    <!-- /.col-lg-6 -->
</div>
<!-- /.row -->

<script>
    $(document).ready(function () {
        $('#device-table').DataTable({
            responsive: true,
            order: [[0, 'desc']]
        });
    });
</script>