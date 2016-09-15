<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<h1>Devices</h1>
<div class="panel-group" id="accordion">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">
                <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne">List of Devices</a>
            </h4>
        </div>
        <!-- /.panel-heading -->
        <div id="collapseOne" class="panel-collapse collapse">
            <div class="panel-body">
                <table width="100%" class="table table-striped table-bordered table-hover" id="dataTables-device">
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
            <!-- /.panel-body -->
        </div>
        <!-- /.collapseOne -->    
    </div>
    <!-- /.panel -->
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">
                <a data-toggle="collapse" data-parent="#accordion" href="#collapseTwo">Register Device</a>   
            </h4>
        </div>
        <!-- /.panel-heading -->
        <div id="collapseTwo" class="panel-collapse collapse">
            <div class="panel-body">
                <div class="col-lg-6">
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
                <!-- /.col-lg-12 -->
            </div>
            <!-- /.panel-body -->
        </div>
        <!-- /.collapseTwo -->    
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
            </div>
            <!-- /.panel-body -->
        </div>
        <!-- /.collapseThree -->    
    </div>
    <!-- /.panel -->
</div>
<!-- /.panel-group -->