<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>

<table width="100%" id="mqtt-table"
       class="table table-striped table-bordered table-hover">
    <thead>
        <tr>
            <th>Sensor</th>
            <th>Value</th>
            <th>Topic</th>
            <th>Date</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="mqttEntry" items="${mqttTable}">
            <tr>
                <td>
                    <a href="
                       <c:url value="/sensor/" /><c:out value="${mqttEntry.sensorId}"/>
                       ">
                        <c:out value="${mqttEntry.sensorName}" default="${mqttEntry.sensorId}" />
                    </a>
                </td>
                <td>
                    <a href="
                       <c:url value="/message/" /><c:out value="${logEntry.id}"/>
                       ">
                        <c:out value="${mqttEntry.value}" />
                    </a>
                </td>
                <td>
                    <c:out value="${mqttEntry.topic}" />
                </td>
                <td>
                    <c:out value="${mqttEntry.date}"/>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<script>
    $(document).ready(function () {
        $('#mqtt-table').DataTable({
            responsive: true,
            order: [[3, 'desc']]
        });
    });
</script>