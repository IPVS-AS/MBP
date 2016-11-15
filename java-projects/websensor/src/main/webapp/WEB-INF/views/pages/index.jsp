<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>

<h1>Welcome!</h1>

<div class="panel-group" id="accordion" 
     role="tablist" aria-multiselectable="true">
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
                <div id="mqtt-table-loader">
                    <img src="
                         <c:url value="/resources/image/ajax-loader.gif" />
                         " />
                </div>
                <div id="mqtt-table-container">                    
                </div>
            </div>
            <!-- /.panel-body -->
        </div>
    </div>
    <!-- /.panel -->
    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">
                <a data-toggle="collapse" data-parent="#accordion" href="#collapseTwo">ARP Table</a>   
            </h4>
        </div>
        <!-- /.panel-heading -->
        <div id="collapseTwo" class="panel-collapse collapse">
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
        $.ajax({url: "<c:url value="/mqtt" />",
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