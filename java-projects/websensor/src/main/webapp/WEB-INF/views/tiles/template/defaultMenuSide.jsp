<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<div class="navbar-default sidebar" role="navigation">
    <div class="sidebar-nav navbar-collapse">
        <ul class="nav" id="side-menu">
            <li>
                <a href="<c:url value="/" />"><i class="fa fa-calendar fa-fw"></i> Dashboard</a>
            </li>
            <li>
                <a href="<c:url value="/sensor" />"><i class="fa fa-tachometer fa-fw"></i> Sensors</a>
            </li>
            <li>
                <a href="<c:url value="/device" />"><i class="fa fa-power-off fa-fw"></i> Devices</a>
            </li>
            <li>
                <a href="<c:url value="/script" />"><i class="fa fa-file-text-o fa-fw"></i> Scripts</a>
            </li>
            <li>
                <a href="<c:url value="/location" />"><i class="fa fa-map-marker fa-fw"></i> Locations</a>
            </li>
        </ul>
    </div>
    <!-- /.sidebar-collapse -->
</div>
<!-- /.navbar-static-side -->