/* global app */

'use strict';

/**
 * Directive which allows the user to define event patterns based on components.
 */
app.directive('cepQueryEditor', ['$interval', function ($interval) {

    const CLASS_COMPONENT = 'component';
    const CLASS_COMPONENT_PLACEHOLDER = 'component-placeholder';
    const CLASS_OPERATOR = 'operator';
    const CLASS_OPERATOR_TYPES = ['before', 'or', 'and'];
    const CLASS_OPERATOR_INTERMEDIATE = 'intermediate';

    const DATA_KEY_OPERATOR_TYPE = 'op_type';

    function init(scope, element, attrs) {

        const mainContainer = $(element[0]);
        const patternContainer = $('.pattern-container');

        function createOperator(intermediate){
            intermediate = intermediate || false;
            var operator = $('<div>').addClass(CLASS_OPERATOR);

            if(intermediate){
                operator.addClass(CLASS_OPERATOR_INTERMEDIATE);
            }

            operator.addClass(CLASS_OPERATOR_TYPES[0]).data(DATA_KEY_OPERATOR_TYPE, 0);
            operator.click(function () {
                var thisElem = $(this);
                var oldIndex = thisElem.data(DATA_KEY_OPERATOR_TYPE);
                var newIndex = (oldIndex + 1) % CLASS_OPERATOR_TYPES.length;
                thisElem.data(DATA_KEY_OPERATOR_TYPE, newIndex);
                thisElem.removeClass(CLASS_OPERATOR_TYPES.join(" "));
                thisElem.addClass(CLASS_OPERATOR_TYPES[newIndex]);
            });

            return operator;
        }

        function dragStart() {
            patternContainer.addClass("dragging");
        }

        function dragStop() {
            patternContainer.removeClass("dragging");
        }

        function updatePattern(event, ui){
            var children = patternContainer.children().not('.ui-sortable-helper');

            var placeholder = children.filter('.' + CLASS_COMPONENT_PLACEHOLDER);
            var intermediate = children.filter('.' + CLASS_OPERATOR + '.' + CLASS_OPERATOR_INTERMEDIATE);

            if(placeholder.length && intermediate.length === 1){
                intermediate.detach();

                if(placeholder.index() === (children.length - 1)){
                    placeholder.before(intermediate);
                }
                else{
                    placeholder.after(intermediate);
                }
            }

            renderPattern(event, ui);
        }

        function renderPattern(event, ui) {
            var children = patternContainer.children().not('.ui-sortable-helper');

            var intermediate = children.filter('.' + CLASS_OPERATOR + '.' + CLASS_OPERATOR_INTERMEDIATE);

            var numberComponents = 0;
            var numberOperators = 0;

            for(var i = 0; i < children.length; i++){
                var currentElement = $(children[i]);
                if (ui.helper.is(currentElement)) {
                    console.log("out");
                    continue;
                }

                if(currentElement.hasClass(CLASS_COMPONENT) || currentElement.hasClass(CLASS_COMPONENT_PLACEHOLDER)){
                    numberComponents++;
                }else if(currentElement.hasClass(CLASS_OPERATOR)){
                    numberOperators++;
                }
            }

            var balance = (numberComponents - 1) - numberOperators;

            if((balance < 0) && intermediate.length){
                intermediate.remove();
                renderPattern(event, ui);
            }

            var wasOperator = true;
            var moveLeft = false;

            children.each(function (index) {
                var currentElement = $(this);

                if((!wasOperator) && (!currentElement.hasClass(CLASS_OPERATOR))){
                    if(balance > 0){
                        var operator = createOperator(true);
                        currentElement.before(operator);
                        renderPattern(event, ui);
                        return false;
                    }else if(balance === 0){
                        moveLeft = true;
                    }
                }

                if(wasOperator && currentElement.hasClass(CLASS_OPERATOR)){
                    if(balance < 0){
                        currentElement.remove();
                        renderPattern(event, ui);
                        return false;
                    }else if(moveLeft){
                        var firstOperator = $(children[index - 1]);
                        firstOperator.insertBefore(firstOperator.prev());
                    }else{
                        currentElement.insertAfter(currentElement.next());
                    }

                    renderPattern(event, ui);
                    return false;
                }

                if((index === (children.length - 1)) && currentElement.hasClass(CLASS_OPERATOR)){
                    currentElement.insertBefore(currentElement.prev());
                    renderPattern(event, ui);
                    return false;
                }

                wasOperator = currentElement.hasClass(CLASS_OPERATOR);
            });
        }

        //Add components to component container
        for (var i = 0; i < scope.componentList.length; i++) {
            var component = scope.componentList[i];

            $('<div><i class="material-icons">settings_remote</i><span>' + component.name + '</span></div>')
                .addClass(CLASS_COMPONENT)
                .appendTo('#sensor-container .content')
                .draggable({
                    containment: 'document',
                    connectToSortable: patternContainer,
                    stack: '.' + CLASS_COMPONENT,
                    cursor: 'move',
                    helper: 'clone',
                    revert: 'invalid',
                    snap: '.' + CLASS_COMPONENT_PLACEHOLDER,
                    snapMode: 'inner',
                    start: dragStart,
                    stop: dragStop,
                    drag: updatePattern
                });
        }

        patternContainer.sortable({
            items: '> .component',
            placeholder: CLASS_COMPONENT_PLACEHOLDER,
            change: updatePattern,
            receive: function () {
                //TODO
                patternContainer.children().removeClass(CLASS_OPERATOR_INTERMEDIATE);
            }
        });
    }

    /**
     * Linking function.
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
            '<div class="cep-query-editor">' +
            '<div class="pattern-container">' +
            '</div>' +
            '<div id="sensor-container" class="component-container">' +
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