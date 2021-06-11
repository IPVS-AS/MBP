/* global app */

'use strict';

/**
 * Directive which allows the user to sketch locations on a map.
 */
app.directive('locationDrawerMap', ['BASE_URI', '$interval', function (BASE_URI, $interval) {
    //Constants
    const ADDRESS_TYPE = 'nominatim';
    const ADDRESS_DATA_PROVIDER = 'osm';
    const ADDRESS_DATA_LOCALE = 'en';
    const ADDRESS_SUGGESTIONS = 5;

    //CSS classes of interest
    const CLASS_MAP_CONTAINER = 'ol-map';

    /**
     * Initializes the directive by passing references to its scope, DOM elements and attributes.
     * @param scope The scope in which the directive is used
     * @param elements The DOM elements of the directive
     * @param attributes The attributes of the directive
     */
    function init(scope, elements, attributes) {
        //Extract relevant DOM elements from directive
        const ELEMENT_MAP = $('.' + CLASS_MAP_CONTAINER, elements);

        //Global references
        let map = null;
        let tileSource = null;
        let vectorSource = null;
        let tileLayer = null;
        let vectorLayer = null;
        let geocoder = null;
        let drawInteraction = null;

        //Initialize everything
        (function () {
            initMap();
        })();

        /**
         * [Public]
         * Creates a draw interaction for a given geometry type and adds it to the map. In case another
         * draw interaction was previously added to the map, this will be removed before creating the new one.
         *
         * @param type The geometry type to draw
         */
        function addDrawInteraction(type) {
            //Remove previous draw interaction if existing
            removeDrawInteraction();

            //Create draw interaction
            drawInteraction = new ol.interaction.Draw({
                source: vectorSource,
                type: type || 'point'
            });

            //Register event when drawing ends and geometry is added
            drawInteraction.on('drawend', onDrawEnd);

            //Add interaction to map
            map.addInteraction(drawInteraction);
        }

        /**
         * [Public]
         * Removes the currently active draw interaction from the map.
         */
        function removeDrawInteraction() {
            //Check if draw interaction exists
            if (drawInteraction == null) {
                return;
            }

            //Remove draw interaction
            map.removeInteraction(drawInteraction);
            drawInteraction = null;
        }

        /**
         * [Public]
         * Returns the geometries that are currently displayed on the map.
         * @return The displayed geometries
         */
        function getGeometries() {
            return vectorSource.getFeatures();
        }

        /**
         * [Public]
         * Returns the number of geometries that are currently displayed on the map.
         * @return The number of geometries
         */
        function getGeometriesCount() {
            return vectorSource.getFeatures().length || 0;
        }

        /**
         * [Public]
         * Removes the geometries that are currently displayed on the map.
         */
        function removeGeometries() {
            //Clear vector source
            vectorSource.clear();
        }

        /**
         * [Public]
         * Wraps a given geometry as feature and adds it to the map.
         * @param geometry The geometry to add
         */
        function addGeometry(geometry) {
            //Wrap geometry into feature
            let feature = new ol.Feature(geometry);

            //Add feature to vector source
            vectorSource.addFeature(feature)
        }

        /**
         * [Public]
         * Moves the view of the map in order to show a given geometry.
         * @param geometry The geometry to show
         */
        function viewGeometry(geometry) {
            //Move view of the map to the extent of the geometry
            map.getView().fit(geometry, {
                'size': map.getSize(),
                'maxZoom': 18,
                'duration': 1000
            });
        }


        /**
         * Converts the unit of a given distance, measured around a given position, from the projection unit to meters.
         * @param distance The distance to convert
         * @param position The coordinates of the position around which the distance was measured
         * @return {number} The converted distance in meters
         */
        function distanceToMeters(distance, position) {
            //Get view of map and its projection
            let view = map.getView();
            let projection = view.getProjection();

            //Determine resolution at equator and resolution at the given position
            let resolutionAtEquator = view.getResolution();
            let pointResolution = ol.proj.getPointResolution(projection, resolutionAtEquator, position);

            //Transform distance from projection unit to meters
            return (distance * projection.getMetersPerUnit() * pointResolution) / resolutionAtEquator;
        }

        /**
         * Converts the unit of a given distance, measured around a given position, from meters to the projection unit.
         * @param distance The distance to convert
         * @param position The coordinates of the position around which the distance was measured
         * @return {number} The converted distance in projection unit
         */
        function distanceFromMeters(position, distance) {
            //Get view of map and its projection
            let view = map.getView();
            let projection = view.getProjection();

            //Determine resolution at equator and resolution at the given position
            let resolutionAtEquator = view.getResolution();
            let pointResolution = ol.proj.getPointResolution(projection, resolutionAtEquator, position);

            //Transform distance from meters to projection unit
            return (distance * resolutionAtEquator) / (projection.getMetersPerUnit() * pointResolution);
        }


        /**
         * [Private]
         * Initializes the map.
         */
        function initMap() {
            //Resize map container
            ELEMENT_MAP.css({'width': '100%', 'height': attributes.height + 'px'});

            //Define style for vector layer
            let vectorStyle = new ol.style.Style({
                image: new ol.style.Icon({
                    anchor: [0.5, 62],
                    anchorXUnits: 'fraction',
                    anchorYUnits: 'pixels',
                    scale: 0.7,
                    src: BASE_URI + 'images/discovery/pin.png'
                }),
                fill: new ol.style.Fill({color: 'rgba(255, 255, 255, 0.4)'}),
                stroke: new ol.style.Stroke({color: '#3399CC', width: 1.25})
            });

            //Create data sources
            tileSource = new ol.source.OSM();
            vectorSource = new ol.source.Vector({wrapX: false});

            //Create layers
            tileLayer = new ol.layer.Tile({source: tileSource});
            vectorLayer = new ol.layer.Vector({source: vectorSource, style: vectorStyle});

            //Create map
            map = new ol.Map({
                target: ELEMENT_MAP[0],
                view: new ol.View({
                    center: ol.proj.fromLonLat(scope.initCenter),
                    zoom: scope.initZoom
                }),
                layers: [tileLayer, vectorLayer]
            });

            //Create geocoder
            geocoder = new Geocoder(ADDRESS_TYPE, {
                provider: ADDRESS_DATA_PROVIDER,
                lang: ADDRESS_DATA_LOCALE,
                placeholder: 'Search for...',
                targetType: 'glass-button',
                limit: ADDRESS_SUGGESTIONS,
                autoComplete: true,
                keepOpen: true,
                preventDefault: true
            });

            //Align button appropriately
            geocoder.element.style.top = '265px';

            //Register listener for address search
            geocoder.on('addresschosen', function (event) {
                //Move view to target location
                map.getView().setCenter(event.coordinate);
            });

            //Add controls to map
            map.addControl(new ol.control.ZoomSlider());
            map.addControl(new ol.control.ScaleLine());
            map.addControl(geocoder);
        }

        /**
         * [Private]
         * Handles draw end events of the draw interaction.
         *
         * @param e The event to handle
         */
        function onDrawEnd(e) {
            //Get geometry and trigger finish event
            scope.drawingFinished({'geometry': e.feature.getGeometry()});
        }

        /*
        Define the exposed API.
         */
        scope.api = {
            'updateMapSize': function () {
                map.updateSize();
            },
            'enableDrawing': addDrawInteraction,
            'disableDrawing': removeDrawInteraction,
            'getGeometries': getGeometries,
            'getGeometriesCount': getGeometriesCount,
            'removeGeometries': removeGeometries,
            'addGeometry': addGeometry,
            'viewGeometry': viewGeometry,
            'distanceToMeters': distanceToMeters,
            'distanceFromMeters': distanceFromMeters
        };
    }

    /**
     * Linking function of the directive.
     *
     * @param scope Scope of the directive
     * @param elements Elements of the directive
     * @param attributes Attributes of the directive
     */
    let link = function (scope, elements, attributes) {
        $(document).ready(function () {
            init(scope, elements, attributes);
        });
    };

    //Configure the directive and expose its API
    return {
        restrict: 'E', //Elements only
        template:
            '<div class="' + CLASS_MAP_CONTAINER + '"></div>'
        ,
        link: link,
        scope: {
            api: '=api',
            initCenter: '=initCenter',
            initZoom: '=initZoom',
            drawingFinished: '&drawingFinished'
        }
    };
}
]);