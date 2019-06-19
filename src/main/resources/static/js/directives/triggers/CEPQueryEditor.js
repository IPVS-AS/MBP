/* global app */

'use strict';

/**
 * Directive which allows the user to define event patterns based on components.
 */
app.directive('cepQueryEditor', ['$interval', function ($interval) {

    const OPERATOR_LIST = [{
        name: 'Before',
        cssClass: 'before'
    }, {
        name: 'Or',
        cssClass: 'or'
    }, {
        name: 'And',
        cssClass: 'and'
    }];

    const CLASS_MAIN_CONTAINER = 'cep-query-editor';
    const CLASS_PATTERN_CONTAINER = 'pattern-container';
    const CLASS_DELETION_AREA = 'deletion-area';
    const CLASS_DELETION_AREA_ACCEPTING = 'accepting';
    const CLASS_PATTERN_MEMBER = 'pattern-member';
    const CLASS_CATEGORY_CONTAINER = 'category-container';
    const CLASS_CATEGORY = 'category';
    const CLASS_CATEGORY_LIST = 'category-list';
    const CLASS_COMPONENT = 'component';
    const CLASS_OPERATOR = 'operator';
    const CLASS_PLACEHOLDER = 'placeholder';
    const CLASS_STUB = 'stub';
    const CLASS_DETAILS_CONTAINER = 'details-container';

    const DATA_KEY_COMPONENT_DATA = 'component_data';

    function init(scope, element, attrs) {

        const mainContainer = $('.' + CLASS_MAIN_CONTAINER);
        const patternContainer = $('.' + CLASS_PATTERN_CONTAINER);
        const deletionArea = $('.' + CLASS_DELETION_AREA);
        const componentsCategoryContainer = $('.' + CLASS_CATEGORY_CONTAINER);
        const detailsContainer = $('.' + CLASS_DETAILS_CONTAINER);

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
            element.data(DATA_KEY_COMPONENT_DATA, component);

            makePatternMember(element);

            return element;
        }

        function createComponentStub() {

            return $('<div>')
                .addClass(CLASS_PATTERN_MEMBER)
                .addClass(CLASS_COMPONENT)
                .addClass(CLASS_STUB);
        }

        function createOperator(operator) {
            var element = $('<div>').addClass(CLASS_OPERATOR);

            element.html(operator.name).addClass(operator.cssClass);

            makePatternMember(element);

            return element;
        }

        function createOperatorStub() {
            var element = $('<div>')
                .addClass(CLASS_PATTERN_MEMBER)
                .addClass(CLASS_OPERATOR)
                .addClass(CLASS_STUB);

            return element;
        }

        function makePatternMember(element) {

            element.addClass(CLASS_PATTERN_MEMBER);
            element.draggable({
                containment: 'document',
                connectToSortable: patternContainer,
                stack: '.' + CLASS_PATTERN_MEMBER,
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

            element.append(heading);
            element.append(body);

            return element;
        }

        function createComponentDetails(component) {

            var container = $('<div class="panel panel-default">');
            var heading = $('<div class="panel-heading">');
            var title = $('<h4 class="panel-title">');

            var titleContent = $('<a class="clickable">Details: Event <i>xyz</i> from ' + component.name +
                '<i class="material-icons" style="float: right;">close</i></a>');
            titleContent.on('click', function () {
                container.slideUp();
            });

            title.append(titleContent);
            heading.append(title);

            var body = $('<div class="panel-body">');

            //TODO content
            body.append("<p>asdfasdfasdf</p>");

            container.append(heading);
            container.append($('<div>').append(body));

            return container;
        }

        function showComponentDetails(detailsPanel) {
            //Hide all other details panels
            detailsContainer.children().not(detailsPanel).slideUp();

            //Show details panel for this component
            detailsPanel.slideDown();
        }

        function prepareAddedComponent(element, prototype) {
            //Enable tooltip
            element.children('span').tooltip({
                container: 'body',
                delay: {"show": 500, "hide": 100},
                placement: 'bottom'
            });

            //Copy component data
            var component = prototype.data(DATA_KEY_COMPONENT_DATA);
            element.data(DATA_KEY_COMPONENT_DATA, component);


            /*
            //Create and append details panel
            var details = createComponentDetails(component);
            details.css('height', '0px');
            detailsContainer.append(details);
            showComponentDetails(details);

            element.on('click', function () {
                showComponentDetails(details);
            });*/
        }

        function dragStart() {
            patternContainer.addClass("dragging");
        }

        function dragStop() {
            patternContainer.removeClass("dragging");
        }

        function patternMemberAdded(event, ui) {
            //prepareAddedComponent(ui.helper, ui.item);
        }

        function sortingStart(event, ui) {
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

            function getPatternMembers() {
                return patternContainer.children().not('.' + CLASS_PLACEHOLDER).not('.ui-sortable-helper');
            }

            function simplify() {
                var children = getPatternMembers();

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
                items: '> .' + CLASS_PATTERN_MEMBER,
                placeholder: [CLASS_PATTERN_MEMBER, CLASS_PLACEHOLDER].join(" "),
                tolerance: 'pointer',
                start: sortingStart,
                stop: sortingStop,
                change: sortPattern,
                receive: patternMemberAdded
            });

            deletionArea.hide().droppable({
                accept: '.' + CLASS_PATTERN_CONTAINER + ' > .' + CLASS_PATTERN_MEMBER,
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