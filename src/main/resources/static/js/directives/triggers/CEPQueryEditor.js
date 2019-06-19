/* global app */

'use strict';

/**
 * Directive which allows the user to define event patterns based on components.
 */
app.directive('cepQueryEditor', ['$interval', function ($interval) {

    const OPERATOR_LIST = [{
        name: 'Before',
        cssClass: 'before',
        precedence: 1
    }, {
        name: 'Or',
        cssClass: 'or',
        precedence: 2
    }, {
        name: 'And',
        cssClass: 'and',
        precedence: 3
    }];

    const CLASS_MAIN_CONTAINER = 'cep-query-editor';
    const CLASS_PATTERN_CONTAINER = 'pattern-container';
    const CLASS_DELETION_AREA = 'deletion-area';
    const CLASS_DELETION_AREA_ACCEPTING = 'accepting';
    const CLASS_PATTERN_ELEMENT = 'pattern-element';
    const CLASS_CATEGORY_CONTAINER = 'category-container';
    const CLASS_CATEGORY = 'category';
    const CLASS_CATEGORY_LIST = 'category-list';
    const CLASS_COMPONENT = 'component';
    const CLASS_OPERATOR = 'operator';
    const CLASS_ELEMENT_SELECTED = 'element-selected';
    const CLASS_PLACEHOLDER = 'placeholder';
    const CLASS_STUB = 'stub';
    const CLASS_HIGHLIGHT_PRECEDENCE = 'highlight-precedence';
    const CLASS_DETAILS_CONTAINER = 'details-container';

    const DATA_KEY_ID = 'id';
    const DATA_KEY_DETAILS_REF = 'details_ref';
    const DATA_KEY_ELEMENT_DATA = 'element_data';

    function init(scope, element, attrs) {

        const mainContainer = $('.' + CLASS_MAIN_CONTAINER);
        const patternContainer = $('.' + CLASS_PATTERN_CONTAINER);
        const deletionArea = $('.' + CLASS_DELETION_AREA);
        const componentsCategoryContainer = $('.' + CLASS_CATEGORY_CONTAINER);
        const detailsContainer = $('.' + CLASS_DETAILS_CONTAINER);

        var idCounter = 0;
        var isDragging = false;

        function createComponent(component, icon) {
            var nameSpan = $('<span href="#">' + component.name + '</span>')
                .attr('title', component.name)
                .tooltip({
                    container: 'body',
                    delay: {"show": 1000, "hide": 100},
                    placement: 'bottom'
                });

            var element = $('<div><i class="material-icons">' + icon + '</i></div>')
                .addClass(CLASS_COMPONENT)
                .append(nameSpan);

            //Provide component data
            element.data(DATA_KEY_ELEMENT_DATA, component);

            makePatternElement(element);

            return element;
        }

        function createComponentStub() {

            return $('<div>')
                .addClass(CLASS_PATTERN_ELEMENT)
                .addClass(CLASS_COMPONENT)
                .addClass(CLASS_STUB);
        }

        function createOperator(operator) {
            var element = $('<div>').addClass(CLASS_OPERATOR);

            element.html(operator.name).addClass(operator.cssClass);

            makePatternElement(element);

            //Add operator data
            element.data(DATA_KEY_ELEMENT_DATA, operator);

            return element;
        }

        function createOperatorStub() {
            var element = $('<div>')
                .addClass(CLASS_PATTERN_ELEMENT)
                .addClass(CLASS_OPERATOR)
                .addClass(CLASS_STUB);

            return element;
        }

        function generateId() {
            return idCounter++;
        }

        function makePatternElement(element) {
            element.addClass(CLASS_PATTERN_ELEMENT);
            element.draggable({
                containment: 'document',
                connectToSortable: patternContainer,
                stack: '.' + CLASS_PATTERN_ELEMENT,
                cursor: 'move',
                helper: 'clone',
                revert: 'invalid',
                start: dragStart,
                stop: dragStop,
                drag: updatePattern
            });
        }

        function createCategory(categoryName) {

            var element = $('<div class="panel panel-default">').addClass(CLASS_CATEGORY);
            var heading = $('<div class="panel-heading">');
            var title = $('<h4 class="panel-title">');

            var titleContent = $('<a class="clickable" data-toggle="collapse" ' +
                'data-target="#category-' + categoryName + '-list" aria-expanded="true">' + categoryName +
                '<i class="material-icons" style="float: right;">keyboard_arrow_down</i></a>');

            title.append(titleContent);
            heading.append(title);

            var body = $('<div class="panel-collapse collapse in">').attr('id', 'category-' + categoryName + '-list')
                .append($('<div class="panel-body">').addClass(CLASS_CATEGORY_LIST));

            body.collapse('hide');

            element.append(heading);
            element.append(body);

            return element;
        }

        function createPatternElementDetails(element) {

            var elementId = element.data(DATA_KEY_ID);
            var elementData = element.data(DATA_KEY_ELEMENT_DATA);

            var container = $('<div class="panel panel-default">');

            var heading = $('<div class="panel-heading">');
            var title = $('<h4 class="panel-title">');

            var titleContent = $('<a class="clickable">Details' +
                '<i class="material-icons" style="float: right;">close</i></a>');
            titleContent.on('click', function () {
                hideElementDetailsPanels();
            });

            title.append(titleContent);
            heading.append(title);

            var body = $('<div class="panel-body">');

            //TODO content
            body.append("<p>asdfasdfasdf</p>");

            container.append(heading).append($('<div>').append(body)).slideUp(0);
            container.data(DATA_KEY_DETAILS_REF, elementId);

            detailsContainer.append(container);
        }

        function showElementDetailsPanel(elementId) {
            //Get all detail panels
            var panels = detailsContainer.children();

            //Iterate over all panels
            panels.each(function () {
                var panel = $(this);

                if (panel.data(DATA_KEY_DETAILS_REF) === elementId) {
                    panel.slideDown();
                } else {
                    panel.slideUp();
                }
            });

            //Get all pattern elements
            var elements = patternContainer.children();

            //Iterate over all pattern elements
            elements.each(function () {
                var element = $(this);

                if(element.data(DATA_KEY_ID) === elementId){
                    element.addClass(CLASS_ELEMENT_SELECTED);
                }else{
                    element.removeClass(CLASS_ELEMENT_SELECTED);
                }
            });
        }

        function hideElementDetailsPanels(){
            detailsContainer.children().slideUp();
            patternContainer.children().removeClass(CLASS_ELEMENT_SELECTED);
        }

        function highlightPrecedence(operator) {
            function parseElementList(list, minPrecedence, style) {
                list.each(function () {
                    var currentElement = $(this);
                    var elementData = currentElement.data(DATA_KEY_ELEMENT_DATA);

                    if (currentElement.hasClass(CLASS_OPERATOR)
                        && (currentElement.hasClass(CLASS_STUB) || (elementData.precedence <= minPrecedence))) {
                        return false;
                    }

                    currentElement.addClass(CLASS_HIGHLIGHT_PRECEDENCE);
                    currentElement.css(style);
                });
            }

            var operatorPrecedence = operator.data(DATA_KEY_ELEMENT_DATA).precedence;
            var operatorStyle = {
                'border-top-color': operator.css('background-color')
            };

            //Consider pattern elements left from the operator
            var previous = operator.prevAll();
            parseElementList(previous, operatorPrecedence, operatorStyle);

            //Consider pattern elements left from the operator
            var next = operator.nextAll();
            parseElementList(next, operatorPrecedence, operatorStyle);
        }

        function unhighlightPrecedence() {
            patternContainer.children().each(function () {
                $(this).removeClass(CLASS_HIGHLIGHT_PRECEDENCE).css('border-color', '');
            });
        }

        function prepareAddedPatternElement(element, prototype) {
            //Give element an id
            var elementId = generateId();
            element.data(DATA_KEY_ID, elementId);

            //Copy element data
            var elementData = prototype.data(DATA_KEY_ELEMENT_DATA);
            element.data(DATA_KEY_ELEMENT_DATA, elementData);

            //Type-specific preparations
            if (element.hasClass(CLASS_COMPONENT)) {
                //Enable tooltip
                element.children('span').tooltip({
                    container: 'body',
                    delay: {"show": 500, "hide": 100},
                    placement: 'bottom'
                });
            } else if (element.hasClass(CLASS_OPERATOR)) {
                element.on({
                    mouseenter: function () {
                        if (!isDragging) {
                            highlightPrecedence($(this));
                        }
                    },
                    mouseleave: unhighlightPrecedence
                });
            }

            //Create details panel and show it
            createPatternElementDetails(element);

            element.on('click', function () {
                showElementDetailsPanel(elementId);
            });
        }

        function dragStart() {
            isDragging = true;
            unhighlightPrecedence();
            patternContainer.addClass("dragging");
        }

        function dragStop() {
            isDragging = false;
            patternContainer.removeClass("dragging");
        }

        function patternElementAdded(event, ui) {
            prepareAddedPatternElement(ui.helper, ui.item);
        }

        function sortingStart(event, ui) {
            isDragging = true;
            unhighlightPrecedence();
            ui.placeholder.removeClass([CLASS_COMPONENT, CLASS_OPERATOR].join(" "));

            if (ui.helper.hasClass(CLASS_COMPONENT)) {
                ui.placeholder.addClass(CLASS_COMPONENT);
            } else if (ui.helper.hasClass(CLASS_OPERATOR)) {
                ui.placeholder.addClass(CLASS_OPERATOR);
            }

            if (!ui.helper.hasClass('ui-draggable-dragging')) {
                deletionArea.show();
            }
        }

        function sortingStop(event, ui) {
            isDragging = false;
            deletionArea.hide();
        }

        function updatePattern(event, ui) {
            var children = patternContainer.children().not('.ui-sortable-helper');

            var wasOperator = true;

            children.each(function (index) {
                    var currentElement = $(this);
                    var previousElement = null;
                    if (index > 0) {
                        previousElement = $(children[index - 1]);
                    }

                    if ((!wasOperator) && (!currentElement.hasClass(CLASS_OPERATOR))) {
                        if (currentElement.hasClass(CLASS_STUB)) {
                            currentElement.remove();
                        } else if (previousElement && previousElement.hasClass(CLASS_STUB)) {
                            previousElement.remove();
                        } else {
                            currentElement.before(createOperatorStub());
                        }

                        updatePattern(event, ui);
                        return false;
                    }

                    if (wasOperator && currentElement.hasClass(CLASS_OPERATOR)) {
                        if (currentElement.hasClass(CLASS_STUB)) {
                            currentElement.remove();
                        } else if (previousElement && previousElement.hasClass(CLASS_STUB)) {
                            previousElement.remove();
                        } else {
                            currentElement.before(createComponentStub());
                        }

                        updatePattern(event, ui);
                        return false;
                    }

                    if ((index === (children.length - 1)) && currentElement.hasClass(CLASS_OPERATOR)) {
                        if (currentElement.hasClass(CLASS_STUB)) {
                            currentElement.remove();
                        } else {
                            currentElement.after(createComponentStub());
                        }

                        updatePattern(event, ui);
                        return false;
                    }

                    wasOperator = currentElement.hasClass(CLASS_OPERATOR);
                }
            );

            patternContainer.sortable("refresh");
        }

        function sortPattern(event, ui) {
            if (ui.helper.hasClass('ui-draggable-dragging')) {
                return;
            }

            var children = patternContainer.children().not('.ui-sortable-helper');
            var wasOperator = true;

            children.each(function () {
                    var currentElement = $(this);

                    if ((!wasOperator && !currentElement.hasClass(CLASS_OPERATOR))
                        || (wasOperator && currentElement.hasClass(CLASS_OPERATOR))) {
                        currentElement.insertAfter(currentElement.next());
                        sortPattern(event, ui);
                        return false;
                    }

                    wasOperator = currentElement.hasClass(CLASS_OPERATOR);
                }
            );

            patternContainer.sortable("refresh");
        }

        function removeElement(event, ui) {

            function getPatternElements() {
                return patternContainer.children().not('.' + CLASS_PLACEHOLDER).not('.ui-sortable-helper');
            }

            function simplify() {
                var children = getPatternElements();

                children.each(function (index) {
                    var currentElement = $(this);
                    var previousElement = null;
                    if (index > 0) {
                        previousElement = $(children[index - 1]);
                    }

                    if (previousElement && previousElement.hasClass(CLASS_STUB)
                        && currentElement.hasClass(CLASS_STUB)) {

                        previousElement.remove();
                        currentElement.remove();

                        simplify();
                        return false;
                    }

                    if ((children.length === 1) && children.hasClass(CLASS_STUB)) {
                        children.remove();
                    }
                });
            }

            var element = ui.draggable;

            if (element.hasClass(CLASS_COMPONENT)) {
                element.replaceWith(createComponentStub());
            } else if (element.hasClass(CLASS_OPERATOR)) {
                element.replaceWith(createOperatorStub());
            }

            simplify();
        }

        function initOperators(operatorList) {
            //Create category for operators
            var categoryElement = createCategory("Operators");
            var categoryContent = categoryElement.find('.' + CLASS_CATEGORY_LIST);

            //Iterate over operators and add them to the category
            for (var i = 0; i < operatorList.length; i++) {
                var operator = operatorList[i];

                var element = createOperator(operator);
                categoryContent.append(element);
            }

            //Append category
            componentsCategoryContainer.append(categoryElement);
        }

        function initComponents(componentList) {
            //Iterate over all component categories
            for (var i = 0; i < componentList.length; i++) {

                var category = componentList[i];

                var categoryElement = createCategory(category.name);
                var categoryContent = categoryElement.find('.' + CLASS_CATEGORY_LIST);

                for (var j = 0; j < category.list.length; j++) {
                    var componentElem = createComponent(category.list[j], category.icon);
                    categoryContent.append(componentElem);
                }

                componentsCategoryContainer.append(categoryElement);
            }
        }

        (function () {
            initOperators(OPERATOR_LIST);
            initComponents(scope.componentList);

            patternContainer.sortable({
                cancel: '.' + CLASS_STUB,
                cursor: 'move',
                items: '> .' + CLASS_PATTERN_ELEMENT,
                placeholder: [CLASS_PATTERN_ELEMENT, CLASS_PLACEHOLDER].join(" "),
                tolerance: 'pointer',
                start: sortingStart,
                stop: sortingStop,
                change: sortPattern,
                receive: patternElementAdded
            });

            deletionArea.hide().droppable({
                accept: '.' + CLASS_PATTERN_CONTAINER + ' > .' + CLASS_PATTERN_ELEMENT,
                hoverClass: CLASS_DELETION_AREA_ACCEPTING,
                drop: removeElement
            });
        })();
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
            '<div class="' + CLASS_MAIN_CONTAINER + '">' +
            '<div class="col-lg-9">' +
            '<div class="' + CLASS_PATTERN_CONTAINER + '"></div>' +
            '<div class="' + CLASS_DELETION_AREA + '"></div>' +
            '<div class="panel-group ' + CLASS_DETAILS_CONTAINER + '"></div>' +
            '</div>' +
            '<div class="col-lg-3">' +
            '<div class="panel-group ' + CLASS_CATEGORY_CONTAINER + '"></div>' +
            '</div>' +
            '</div>'
        ,
        link: link,
        scope: {
            componentList: '=componentList'
        }
    };
}])
;