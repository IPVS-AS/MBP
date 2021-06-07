/* global app */

'use strict';

/**
 * Directive which allows the user to sketch locations on a map.
 */
app.directive('locationSketcherMap', ['$interval', function ($interval) {
    //Constants
    const ADDRESS_TYPE = 'nominatim';
    const ADDRESS_DATA_PROVIDER = 'osm';
    const ADDRESS_DATA_LOCALE = 'en';
    const ADDRESS_SUGGESTIONS = 5;

    //CSS classes of interest
    const CLASS_MAP_CONTAINER = 'ol-map';

    //Keys of interest
    const KEY_ID = 'id';


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
            vectorLayer = new ol.layer.Vector({source: vectorSource});

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
         * Creates a draw interaction for a given geometry type and adds it to the map. In case another
         * draw interaction was previously added to the map, this will be removed before creating the new one.
         *
         * @param type The geometry type to draw
         */
        function addDrawInteraction(type) {
            //Remove previous draw interaction if existing
            if (drawInteraction != null) {
                map.removeInteraction(drawInteraction);
            }

            //Create draw interaction
            drawInteraction = new ol.interaction.Draw({
                source: vectorSource,
                type: type || 'point'
            });

            //Add interaction to map
            map.addInteraction(drawInteraction);
        }

        /*
        Define the exposed API.
         */
        scope.api = {
            'updateMapSize': function () {
                map.updateSize();
            },
            'enableDrawing': addDrawInteraction
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
            initCenter: '=initCenter',
            initZoom: '=initZoom',
            api: '=api'
        }
    };
}
]);