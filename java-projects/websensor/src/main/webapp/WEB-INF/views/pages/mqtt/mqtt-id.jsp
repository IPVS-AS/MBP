<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>

<table width="100%" id="mqtt-table-id"
       class="table table-striped table-bordered table-hover">
    <thead>
        <tr>
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
                       <c:url value="/message/" /><c:out value="${mqttEntry.id}"/>
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
        $('#mqtt-table-id').DataTable({
            responsive: true,
            order: [[2, 'desc']]
        });
    });
</script>