/* global app */

'use strict';

/**
 * Directive which allows the user to define event patterns based on components
 */
app.directive('eventPatternCreator', ['$interval', function ($interval) {

    function init(scope, element, attrs) {
        function dragStart() {
            $('.pattern-container').addClass("dragging");
        }

        function dragStop() {
            $('.pattern-container').removeClass("dragging");
        }

        function sortReceive(event, ui) {
            ui.helper.hide().fadeIn();

            if (ui.helper.is(':only-child')) {
                return;
            }

            var operator = $('<div class="operator"/>').addClass('and').data('type', 0).click(function () {
                var classes = ['and', 'or', 'after'];
                var _this = $(this);
                var newIndex = (_this.data('type') + 1) % classes.length;
                _this.removeClass("and or after");
                _this.addClass(classes[newIndex]);
                _this.data('type', newIndex);
            });

            var previousSibling = ui.helper.prev();
            var nextSibling = ui.helper.next();

            if ((previousSibling.length > 0) && (!previousSibling.hasClass('operator'))) {
                ui.helper.before(operator);
            } else if ((nextSibling.length > 0) && (!nextSibling.hasClass('operator'))) {
                ui.helper.after(operator);
            }
        }

        function sortUpdate(event, ui) {

            console.log("change");

            var patternContainer = $('.pattern-creator .pattern-container');
            var children = patternContainer.children();

            var wasOperator = true;
            var moveLeft = false;
            var currentElement = null;

            children.each(function (index) {
                currentElement = $(this);

                if((!wasOperator) && (!currentElement.hasClass("operator"))){
                    moveLeft = true;
                }

                if(wasOperator && currentElement.hasClass("operator")){
                    if(moveLeft){
                        var firstOperator = currentElement.prev();
                        firstOperator.insertBefore(firstOperator.prev());
                    }else{
                        currentElement.insertAfter(currentElement.next());
                    }

                    sortUpdate(event, ui);
                    return false;
                }

                if((index === (children.length - 1)) && currentElement.hasClass("operator")){
                    currentElement.insertBefore(currentElement.prev());
                    sortUpdate(event, ui);
                    return false;
                }

                wasOperator = currentElement.hasClass("operator");
            });
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
                    helper: 'clone',
                    revert: 'invalid',
                    snap: '.component-placeholder',
                    snapMode: 'inner',
                    start: dragStart,
                    stop: dragStop
                });
        }

        $(".pattern-creator .pattern-container").sortable({
            items: '> .component',
            placeholder: 'component-placeholder',
            update: sortUpdate,
            receive: sortReceive
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