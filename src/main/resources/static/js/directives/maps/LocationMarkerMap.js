/* global app */

'use strict';

/**
 * Directive which allows to display and mark multiple locations on a map.
 */
app.directive('locationMarkerMap', ['BASE_URI', function (BASE_URI) {
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

        //Style function for vector layer
        const vectorStyleFunction = function (feature) {
            return new ol.style.Style({
                image: new ol.style.Icon({
                    anchor: [0.5, 62],
                    anchorXUnits: 'fraction',
                    anchorYUnits: 'pixels',
                    scale: 0.7,
                    src: BASE_URI + 'images/discovery/pin.png'
                }),
                fill: new ol.style.Fill({color: 'rgba(255, 255, 255, 0.4)'}),
                stroke: new ol.style.Stroke({color: '#3399CC', width: 1.25}),
                text: new ol.style.Text({
                    font: '13px sans-serif',
                    fill: new ol.style.Fill({color: 'black'}), //#00BEFF
                    stroke: new ol.style.Stroke({color: 'white', width: 4}),
                    offsetY: 10,
                    text: feature.get("name"),
                })
            });
        };

        //Initialize everything
        (function () {
            initMap();
        })();

        /**
         * [Public]
         * Adds a location with a certain name, given as longitude and latitude values, to the map.
         * @param lon The longitude of the location
         * @param lat The latitude of the location
         * @param name The name of the location
         */
        function addLocation(lon, lat, name) {
            //Create point geometry
            let point = new ol.geom.Point(ol.proj.fromLonLat([lon, lat]));

            //Wrap point into feature and set the name
            let feature = new ol.Feature(point);
            feature.set('name', name);

            //Add feature to vector source
            vectorSource.addFeature(feature)
        }

        /**
         * [Public]
         * Removes all previously added locations from the map.
         */
        function clearLocations() {
            //Remove all locations
            vectorSource.clear();
        }

        /**
         * [Public]
         * Moves the center of the map's view to a certain location, given as longitude and latitude values.
         * @param lon The longitude value of the location to view
         * @param lat The latitude value of the location to view
         */
        function viewLocation(lon, lat) {
            //Move view of the map to the given location and adjust zoom
            map.getView().animate({
                center: ol.proj.fromLonLat([lon, lat]),
                zoom: 15,
                duration: 1000
            });
        }

        /**
         * Moves the center of the map's view such that all added locations are visible at the same time.
         */
        function viewAllLocations() {
            //Sanity check
            if (vectorSource.getFeatures().length < 1) {
                return;
            }

            //Get extent of vector source
            let extent = vectorSource.getExtent();

            //Fit map view to the extent
            map.getView().fit(extent, {
                'size': map.getSize(),
                'padding': [45, 30, 30, 20],
                'maxZoom': 18,
                'duration': 1000
            });
        }

        /**
         * [Private]
         * Initializes the map.
         */
        function initMap() {
            //Resize map container
            ELEMENT_MAP.css({'width': '100%', 'height': attributes.height + 'px'});

            //Create data sources
            tileSource = new ol.source.OSM();
            vectorSource = new ol.source.Vector({wrapX: false});

            //Create layers
            tileLayer = new ol.layer.Tile({source: tileSource});
            vectorLayer = new ol.layer.Vector({source: vectorSource, style: vectorStyleFunction});

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

        /*
        Define the exposed API.
         */
        scope.api = {
            'updateMapSize': function () {
                map.updateSize();
            },
            'addLocation': addLocation,
            'clearLocations': clearLocations,
            'viewLocation': viewLocation,
            'viewAllLocations': viewAllLocations,
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
            initZoom: '=initZoom'
        }
    };
}
]);