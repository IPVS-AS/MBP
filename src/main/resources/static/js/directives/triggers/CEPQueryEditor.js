/* global app */

'use strict';

/**
 * Directive which allows the user to define event patterns based on components.
 */
app.directive('cepQueryEditor', [function () {

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

    const KEY_ID = 'id';
    const KEY_DETAILS_REF = 'details_ref';
    const KEY_OPERATOR_PRECEDENCE = 'operator_precedence';
    const KEY_SOURCE_COMPONENT_DATA = 'source_data';
    const KEY_ELEMENT_KEY = 'element_key';

    const OPERATOR_TYPES_LIST = [{
        name: 'Before',
        cssClass: 'before',
        precedence: 1,
        key: 'before',
        hasDetails: true,
        createForm: function () {
        },
        querify: function () {
        }
    }, {
        name: 'Or',
        cssClass: 'or',
        precedence: 2,
        key: 'or',
        hasDetails: false,
        createForm: function () {
        },
        querify: function () {
        }
    }, {
        name: 'And',
        cssClass: 'and',
        precedence: 3,
        key: 'and',
        hasDetails: false,
        createForm: function () {
        },
        querify: function () {
        }
    }];

    const COMPONENT_TYPES_LIST = [{
        name: 'Wait',
        icon: 'hourglass_empty',
        key: 'wait',
        hasDetails: true,
        createForm: function () {
        },
        querify: function () {
        }
    }, {
        name: 'Timestamp',
        icon: 'date_range',
        key: 'timestamp',
        hasDetails: true,
        createForm: function () {
        },
        querify: function () {
        }
    }];

    const SOURCE_COMPONENT_TYPE_PROTOTYPE = {
        //name and icon: Dynamically assigned
        key: 'source',
        hasDetails: true,
        createForm: function () {
        },
        querify: function () {
        }
    };

    function init(scope, element, attrs) {

        const mainContainer = $('.' + CLASS_MAIN_CONTAINER);
        const patternContainer = $('.' + CLASS_PATTERN_CONTAINER);
        const deletionArea = $('.' + CLASS_DELETION_AREA);
        const componentsCategoryContainer = $('.' + CLASS_CATEGORY_CONTAINER);
        const detailsContainer = $('.' + CLASS_DETAILS_CONTAINER);

        let categoryIdCounter = 0;
        let elementIdCounter = 0;
        let isDragging = false;

        function createComponentType(componentType) {
            let nameSpan = $('<span href="#">' + componentType.name + '</span>')
                .attr('title', componentType.name)
                .tooltip({
                    container: 'body',
                    delay: {"show": 1000, "hide": 100},
                    placement: 'bottom'
                });

            let element = $('<div><i class="material-icons">' + componentType.icon + '</i></div>')
                .addClass(CLASS_COMPONENT)
                .append(nameSpan);

            makePatternElement(element, componentType.key);

            return element;
        }

        function createComponentStub() {
            return $('<div>')
                .addClass(CLASS_PATTERN_ELEMENT)
                .addClass(CLASS_COMPONENT)
                .addClass(CLASS_STUB);
        }

        function createOperatorType(operatorType) {
            let element = $('<div>').addClass(CLASS_OPERATOR).addClass(operatorType.cssClass).html(operatorType.name);

            element.data(KEY_OPERATOR_PRECEDENCE, operatorType.precedence);

            makePatternElement(element, operatorType.key);

            return element;
        }

        function createOperatorStub() {
            return $('<div>')
                .addClass(CLASS_PATTERN_ELEMENT)
                .addClass(CLASS_OPERATOR)
                .addClass(CLASS_STUB);
        }

        function createSourceComponentType(component, icon) {
            var newType = Object.assign({}, SOURCE_COMPONENT_TYPE_PROTOTYPE);
            newType.icon = icon;
            newType.name = component.name;

            var element = createComponentType(newType);
            element.data(KEY_SOURCE_COMPONENT_DATA, component);

            return element;
        }

        function generateCategoryId() {
            return categoryIdCounter++;
        }

        function generateElementId() {
            return elementIdCounter++;
        }

        function makePatternElement(element, key) {
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

            //Set element key
            element.data(KEY_ELEMENT_KEY, key);
        }

        function createCategory(categoryName) {

            let categoryId = generateCategoryId();

            let element = $('<div class="panel panel-default">').addClass(CLASS_CATEGORY);
            let heading = $('<div class="panel-heading">');
            let title = $('<h4 class="panel-title">');

            let titleContent = $('<a class="clickable" data-toggle="collapse" ' +
                'data-target="#category-' + categoryId + '-list" aria-expanded="true">' + categoryName +
                '<i class="material-icons" style="float: right;">keyboard_arrow_down</i></a>');

            title.append(titleContent);
            heading.append(title);

            let body = $('<div class="panel-collapse collapse in">').attr('id', 'category-' + categoryId + '-list')
                .append($('<div class="panel-body">').addClass(CLASS_CATEGORY_LIST));

            body.collapse('hide');

            element.append(heading);
            element.append(body);

            return element;
        }

        function createPatternElementDetails(element) {

            let elementId = element.data(KEY_ID);

            let container = $('<div class="panel panel-default">');
            let heading = $('<div class="panel-heading">');
            let title = $('<h4 class="panel-title">');

            let titleContent = $('<a class="clickable">Details' +
                '<i class="material-icons" style="float: right;">close</i></a>');
            titleContent.on('click', function () {
                hideElementDetailsPanels();
            });

            title.append(titleContent);
            heading.append(title);

            let body = $('<div class="panel-body">');

            //TODO content
            body.append("<p>asdfasdfasdf</p>");

            container.append(heading).append($('<div>').append(body)).slideUp(0);
            container.data(KEY_DETAILS_REF, elementId);

            detailsContainer.append(container);
        }

        function toggleElementDetails(elementId) {
            //Get all detail panels
            let panels = detailsContainer.children();

            //Iterate over all panels
            panels.each(function () {
                let panel = $(this);

                if (panel.data(KEY_DETAILS_REF) === elementId) {
                    panel.slideToggle();
                } else {
                    panel.slideUp();
                }
            });

            //Get all pattern elements
            let elements = patternContainer.children();

            //Iterate over all pattern elements
            elements.each(function () {
                let element = $(this);

                if (element.data(KEY_ID) === elementId) {
                    element.toggleClass(CLASS_ELEMENT_SELECTED);
                } else {
                    element.removeClass(CLASS_ELEMENT_SELECTED);
                }
            });
        }

        function hideElementDetailsPanels() {
            detailsContainer.children().slideUp();
            patternContainer.children().removeClass(CLASS_ELEMENT_SELECTED);
        }

        function highlightPrecedence(operator) {
            function parseElementList(list, minPrecedence, style) {
                list.each(function () {
                    let currentElement = $(this);
                    let operatorPrecedence = currentElement.data(KEY_OPERATOR_PRECEDENCE) || Number.MAX_SAFE_INTEGER

                    if (currentElement.hasClass(CLASS_OPERATOR)
                        && (currentElement.hasClass(CLASS_STUB) || (operatorPrecedence <= minPrecedence))) {
                        return false;
                    }

                    currentElement.addClass(CLASS_HIGHLIGHT_PRECEDENCE);
                    currentElement.css(style);
                });
            }

            let operatorPrecedence = operator.data(KEY_OPERATOR_PRECEDENCE);
            let operatorStyle = {
                'border-top-color': operator.css('background-color')
            };

            //Consider pattern elements left from the operator
            let previous = operator.prevAll();
            parseElementList(previous, operatorPrecedence, operatorStyle);

            //Consider pattern elements left from the operator
            let next = operator.nextAll();
            parseElementList(next, operatorPrecedence, operatorStyle);
        }

        function unhighlightPrecedence() {
            patternContainer.children().each(function () {
                $(this).removeClass(CLASS_HIGHLIGHT_PRECEDENCE).css('border-color', '');
            });
        }

        function prepareAddedPatternElement(element, prototype) {
            //Copy all data from prototype to new pattern element
            let elementData = prototype.data();
            element.data(elementData);

            //Give element an id
            let elementId = generateElementId();
            element.data(KEY_ID, elementId);

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
                toggleElementDetails(elementId);
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
            let children = patternContainer.children().not('.ui-sortable-helper');

            let wasOperator = true;

            children.each(function (index) {
                    let currentElement = $(this);
                    let previousElement = null;
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

            let children = patternContainer.children().not('.ui-sortable-helper');
            let wasOperator = true;

            children.each(function () {
                    let currentElement = $(this);

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
                let children = getPatternElements();

                children.each(function (index) {
                    let currentElement = $(this);
                    let previousElement = null;
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

            let placeholder = patternContainer.children('.' + CLASS_PLACEHOLDER);
            let element = ui.draggable;

            if (element.hasClass(CLASS_COMPONENT)) {
                placeholder.after(createComponentStub());
            } else if (element.hasClass(CLASS_OPERATOR)) {
                placeholder.after(createOperatorStub());
            }

            element.remove();
            simplify();
        }

        function initOperators() {
            //Create category for operators
            let categoryElement = createCategory("Operators");
            let categoryContent = categoryElement.find('.' + CLASS_CATEGORY_LIST);

            //Iterate over operators and add them to the category
            for (let i = 0; i < OPERATOR_TYPES_LIST.length; i++) {
                let operator = OPERATOR_TYPES_LIST[i];

                let element = createOperatorType(operator);
                categoryContent.append(element);
            }

            //Append category
            componentsCategoryContainer.append(categoryElement);
        }

        function initSourceComponents(componentList) {
            //Iterate over all component categories
            for (let i = 0; i < componentList.length; i++) {

                let category = componentList[i];

                let categoryElement = createCategory(category.name);
                let categoryContent = categoryElement.find('.' + CLASS_CATEGORY_LIST);

                for (let j = 0; j < category.list.length; j++) {
                    let componentElement = createSourceComponentType(category.list[j], category.icon);
                    categoryContent.append(componentElement);
                }

                componentsCategoryContainer.append(categoryElement);
            }
        }

        function initAdditionalComponents() {
            //Create category for additional components
            let categoryElement = createCategory("Additional Components");
            let categoryContent = categoryElement.find('.' + CLASS_CATEGORY_LIST);

            //Iterate over all additional components and add them to the category
            for (let i = 0; i < COMPONENT_TYPES_LIST.length; i++) {
                let componentType = COMPONENT_TYPES_LIST[i];

                let element = createComponentType(componentType);
                categoryContent.append(element);
            }

            //Append category
            componentsCategoryContainer.append(categoryElement);
        }

        (function () {
            initOperators();
            initSourceComponents(scope.componentList);
            initAdditionalComponents();

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
    let link = function (scope, element, attrs) {
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