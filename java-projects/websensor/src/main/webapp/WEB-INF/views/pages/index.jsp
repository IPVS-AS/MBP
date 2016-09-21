<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<h1>Welcome!</h1>

<div class="panel-group" id="accordion">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">
                <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne">Last MQTT Updates</a>
            </h4>
        </div>
        <!-- /.panel-heading -->
        <div id="collapseOne" class="panel-collapse collapse">
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
                        <tr>
                            <td>Trident</td>
                            <td>1.1</td>
                            <td>/sensor/idofthesensor</td>
                            <td>30/08/2016 11:00:30</td>
                        </tr>
                        <tr>
                            <td>Trident</td>
                            <td>1</td>
                            <td>/sensor/idofthesensor</td>
                            <td>30/08/2016 11:00:00</td>
                        </tr>
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
                <a href="<c:out value="${uriReset}" />" title="Clear table" class="btn btn-danger btn-circle"><i class="fa fa-trash-o"></i></a>
            </div>
            <!-- /.panel-body -->
        </div>
        <!-- /.collapseThree -->    
    </div>
    <!-- /.panel -->
</div>