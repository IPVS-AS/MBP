/* global app */

'use strict';

/**
 * Directive which allows the user to define event patterns based on components
 */
app.directive('eventPatternCreator', ['$interval', function ($interval) {

    function init(scope, element, attrs) {
        function handleCardDrop(event, ui) {
            console.log(ui);
            //var slotNumber = $(this).data( 'number' );
            //var cardNumber = ui.draggable.data( 'number' );

            //$(this).before(ui.draggable.clone())
        }

        //Add components to component container
        for (var i = 0; i < scope.componentList.length; i++) {
            var component = scope.componentList[i];

            $('<div class="component"><i class="material-icons">settings_remote</i><span>' + component.name + '</span></div>')
                .appendTo('#sensor-container .content')
                .draggable({
                    containment: '.pattern-creator',
                    connectToSortable: ".pattern-creator .pattern-container",
                    stack: '.pattern-creator .components-container .component',
                    cursor: 'move',
                    helper: "clone",
                    revert: true,
                    snap: "invalid",
                    snapMode: "inner"
                });
        }

        $(".pattern-creator .pattern-container").sortable({
            //forcePlaceholderSize: false,
            items: '> .component',
            placeholder: 'component-placeholder'
        });
    }

    /**
     * Linking function, glue code.
     *
     * @param scope Scope of the directive
     * @param element Elements of the directive
     * @param attrs Attributes of the directive
     */
    var link = function (scope, element, attrs) {
        $(document).ready(function () {
            init(scope, element, attrs);
        });
    };

    //Configure and expose the directive
    return {
        restrict: 'E', //Elements only
        template:
            '<div class="pattern-creator">' +
            '<div class="pattern-container">' +
            '</div>' +
            '<div id="sensor-container" class="components-container">' +
            '<div class="title"><span>Sensors</span></div>' +
            '<div class="content">' +
            '</div>' +
            '</div>' +
            '</div>'
        ,
        link: link,
        scope: {
            componentList: '=componentList'
        }
    };
}]);