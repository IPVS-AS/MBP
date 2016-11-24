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
            <c:if test="${not empty heartbeatResult}">
                <tr>
                    <th>Device Status</th>
                    <td class="
                        <c:if test="${heartbeatResult.status eq 'REACHABLE'}"> text-success </c:if>
                        <c:if test="${heartbeatResult.status eq 'UNREACHABLE'}"> text-warning </c:if>
                            ">
                        <c:out value="${heartbeatResult.status}" default="UNDEFINED" />
                    </td>
                </tr>
                <tr>
                    <th>Status @</th>
                    <td>
                        <c:out value="${heartbeatResult.date}" default="Not informed" />
                    </td>
                </tr>
                <c:if test="${heartbeatResult.status eq 'REACHABLE'}">
                    <tr>
                        <th>IP</th>
                        <td>
                            <c:out value="${heartbeatResult.ip}" default=""/>
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Running
                        </th>
                        <c:if test="${isRunning eq true}">
                            <td class="success">
                                <i class="fa fa-check"></i>
                            </td>
                        </c:if>
                        <c:if test="${isRunning eq false}">
                            <td class="danger">
                                <i class="fa fa-times"></i>
                            </td>
                        </c:if>                
                    </tr>
                </c:if>
            </c:if>
            <tr>
                <th></th>
                <td> 
                    <button id="show" type="button" title="Edit values" class="btn btn-warning btn-circle btn-lg"><i class="fa fa-pencil"></i></button>
                        <form:form action="${uriId}" method="DELETE" modelAttribute="sensorForm" style="display: inline;">
                        <button id="show" type="submit" title="Delete" class="btn btn-danger btn-circle btn-lg">
                            <i class="fa fa-trash-o"></i>
                        </button>
                    </form:form>
                </td>
            </tr>            
        </table>
        <table width="100%" class="table hidden" id="hidden">
            <form:form action="${uriId}" method="PUT" modelAttribute="sensorForm">
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
                        <button type="submit" title="Save" class="btn btn-success btn-circle btn-lg" /><i class="fa fa-check"></i></button>
                        <button id="hide" type="button"  title="Cancel" class="btn btn-danger btn-circle btn-lg">
                            <i class="fa fa-times"></i>
                        </button>
                    </td>
                </tr>
            </form:form>
        </table>

        <!-- Deploying -->
        <c:if test="${heartbeatResult.status eq 'REACHABLE'}">
            <h3>Deploy</h3>
            <c:if test="${isRunning eq true}">
                <form:form action="${uriDeploy}" method="DELETE" modelAttribute="sensorForm">
                    <label>Already Deployed</label>
                    <button type="submit" title="Undeploy" class="btn btn-danger btn-circle btn-lg" /><i class="fa fa-power-off"></i></button>
                </form:form>
            </c:if>
            <c:if test="${isRunning eq false}">
                <form action="${uriDeploy}" method="POST"  class="form-inline">
                    <div class="form-group">
                        <label>Pinset</label>
                        <input name="pinset"  type="text" class="form-control" placeholder="15,16,17"/>
                        <button type="submit" title="Deploy sensor" class="btn btn-success btn-circle" /><i class="fa fa-download"></i></button>
                        <p class="help-block">Pin numbers separated by commas</p>
                    </div>
                </form>
            </c:if>
        </c:if>
    </div>
    <!-- /.col-lg-4 -->
    <div class="col-lg-8">
        <div id="mqtt-table-loader">
            <img src="
                 <c:url value="/resources/image/ajax-loader.gif" />
                 " />
        </div>
        <div id="mqtt-table-container">                    
        </div>
    </div>
</div>
<!-- /.row -->

<script>
    $(document)
            .ajaxStart(function () {
                $("#mqtt-table-loader").show();
                $("#mqtt-table-container").hide();
            })
            .ajaxStop(function () {
                $("#mqtt-table-loader").hide();
                $("#mqtt-table-container").show();
                $(this).unbind("ajaxStart");
            });
    
    function loadtable() {
        $.ajax({url: "<c:url value="/mqtt/${sensor.id}" />",
            success: function (result) {
                $("#mqtt-table-container").html(result);
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                $("#mqtt-table-container").html("Couldn't load table.");
            }
        });
    }

    $(document).ready(function () {
        loadtable();
    });

    setInterval(function () {
        loadtable(); // this will run after every 5 seconds
    }, 5000);
</script>