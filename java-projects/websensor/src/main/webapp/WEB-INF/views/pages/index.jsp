<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<h1>Welcome!</h1>

<div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">
                <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne"
                   aria-expanded="true" aria-controls="collapseOne">
                    Last MQTT Updates
                </a>
            </h4>
        </div>
        <!-- /.panel-heading -->
        <div id="collapseOne" class="panel-collapse collapse in">
            <div class="panel-body">
                <table width="100%" class="table table-striped table-bordered table-hover" id="dataTables-index">
                    <thead>
                        <tr>
                            <th>Sensor</th>
                            <th>Value (dynamic link to raw message)</th>
                            <th>Topic</th>
                            <th>Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="logEntry" items="${logTable}">
                            <tr>
                                <td>
                                    <a href="
                                       <c:out value="${uriSensor}"/>/<c:out value="${logEntry.sensorId}"/>
                                       ">
                                        <c:out value="${logEntry.sensorName}" default="${logEntry.sensorId}" />
                                    </a>
                                </td>
                                <td>
                                    <a href="
                                       <c:out value="${uriMessage}"/>/<c:out value="${logEntry.id}"/>
                                       ">
                                        <c:out value="${logEntry.value}" />
                                    </a>
                                </td>
                                <td>
                                    <c:out value="${logEntry.topic}" />
                                </td>
                                <td>
                                    <c:out value="${logEntry.date}"/>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
            <!-- /.panel-body -->
        </div>
    </div>
    <!-- /.panel -->
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">
                <a data-toggle="collapse" data-parent="#accordion" href="#collapseThree">ARP Table</a>   
            </h4>
        </div>
        <!-- /.panel-heading -->
        <div id="collapseThree" class="panel-collapse collapse">
            <div class="panel-body">
                <table width="100%" class="table table-striped table-bordered table-hover" id="dataTables-arp">
                    <thead>
                        <tr>
                            <th>MAC Address</th>
                            <th>IP Address</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="arpEntry" items="${arpTable}">
                            <tr>
                                <td>
                                    <c:out value="${arpEntry.mac}" />
                                </td>
                                <td>
                                    <c:out value="${arpEntry.ip}"/>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                <!-- /.table-responsive -->
                <a href="<c:out value="${uriReset}" />" title="Clear table" class="btn btn-danger btn-circle btn-lg"><i class="fa fa-trash-o"></i></a>
            </div>
            <!-- /.panel-body -->
        </div>
        <!-- /.collapseThree -->    
    </div>
    <!-- /.panel -->
</div>