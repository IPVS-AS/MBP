/* global app */

'use strict';

/**
 * Directive which allows the user to define event patterns based on components.
 */
app.directive('cepQueryEditor', ['$interval', function ($interval) {

    const CLASS_MAIN_CONTAINER = 'cep-query-editor';
    const CLASS_PATTERN_CONTAINER = 'pattern-container';
    const CLASS_COMPONENT = 'component';
    const CLASS_COMPONENT_PLACEHOLDER = 'component-placeholder';
    const CLASS_COMPONENT_CATEGORY_CONTAINER = 'component-category-container';
    const CLASS_COMPONENT_CATEGORY = 'component-category';
    const CLASS_COMPONENT_CATEGORY_OPTIONS = 'options';
    const CLASS_COMPONENT_CATEGORY_TITLE = 'title';
    const CLASS_COMPONENT_CATEGORY_LIST = 'component-list';
    const CLASS_OPERATOR = 'operator';
    const CLASS_OPERATOR_TYPES = ['before', 'or', 'and'];
    const CLASS_OPERATOR_INTERMEDIATE = 'intermediate';

    const DATA_KEY_OPERATOR_TYPE = 'op_type';

    function init(scope, element, attrs) {

        const mainContainer = $('.' + CLASS_MAIN_CONTAINER);
        const patternContainer = $('.' + CLASS_PATTERN_CONTAINER);
        const componentsCategoryContainer = $('.' + CLASS_COMPONENT_CATEGORY_CONTAINER);

        function createComponent(component, icon) {
            var nameSpan = $('<span href="#">' + component.name + '</span>')
                .attr('title', component.name);
            var component = $('<div><i class="material-icons">' + icon + '</i></div>');
            component.append(nameSpan).addClass(CLASS_COMPONENT);
            component.draggable({
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

            //Enable tooltip
            nameSpan.tooltip({
                container: 'body',
                delay: {"show": 1000, "hide": 100},
                placement: 'bottom'
            });

            return component;
        }

        function createOperator(intermediate) {
            intermediate = intermediate || false;
            var operator = $('<div>').addClass(CLASS_OPERATOR);

            if (intermediate) {
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

        function createComponentCategory(name) {

            var category = $('<div class="panel panel-default">').addClass(CLASS_COMPONENT_CATEGORY);
            var heading = $('<div class="panel-heading">');
            var title = $('<h4 class="panel-title">');

            var title_content = $('<a class="clickable" data-toggle="collapse" ' +
                'data-target="#category-' + name + '-list" aria-expanded="true">' + name +
                '<i class="material-icons" style="float: right;">keyboard_arrow_down</i></a>');

            title.append(title_content);
            heading.append(title);

            var body = $('<div class="panel-collapse collapse in">').attr('id', 'category-' + name + '-list')
                .append($('<div class="panel-body">').addClass(CLASS_COMPONENT_CATEGORY_LIST));

            category.append(heading);
            category.append(body);

            return category;

            /*
            var title = $('<div><span>' + name + '</span></div>').addClass(CLASS_COMPONENT_CATEGORY_TITLE);
            var list = $('<div>').attr('id', 'category-' + name + '-list').addClass(CLASS_COMPONENT_CATEGORY_LIST)
                .addClass('collapse in');

            var category = $('<div>').addClass(CLASS_COMPONENT_CATEGORY);

            category.append(options);
            category.append(title);
            category.append(list);

            //Enable collapse functionality
            collapse.collapse();
            list.on('hidden.bs.collapse', function () {
                $(this).hide()
            }).on('show.bs.collapse', function () {
                $(this).show();
            });

            return category;*/
        }

        function prepareAddedComponent(component){
            //Enable tooltip
            component.children('span').tooltip({
                container: 'body',
                delay: {"show": 500, "hide": 100},
                placement: 'bottom'
            });
        }

        function dragStart() {
            patternContainer.addClass("dragging");
        }

        function dragStop() {
            patternContainer.removeClass("dragging");
        }

        function componentAdd(event, ui) {
            prepareAddedComponent(ui.helper);
            patternContainer.children().removeClass(CLASS_OPERATOR_INTERMEDIATE);
        }

        function updatePattern(event, ui) {
            var children = patternContainer.children().not('.ui-sortable-helper');

            var placeholder = children.filter('.' + CLASS_COMPONENT_PLACEHOLDER);
            var intermediate = children.filter('.' + CLASS_OPERATOR + '.' + CLASS_OPERATOR_INTERMEDIATE);

            if (placeholder.length && intermediate.length === 1) {
                intermediate.detach();

                if (placeholder.index() === (children.length - 1)) {
                    placeholder.before(intermediate);
                } else {
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

            for (var i = 0; i < children.length; i++) {
                var currentElement = $(children[i]);
                if (ui.helper.is(currentElement)) {
                    console.log("out");
                    continue;
                }

                if (currentElement.hasClass(CLASS_COMPONENT) || currentElement.hasClass(CLASS_COMPONENT_PLACEHOLDER)) {
                    numberComponents++;
                } else if (currentElement.hasClass(CLASS_OPERATOR)) {
                    numberOperators++;
                }
            }

            var balance = (numberComponents - 1) - numberOperators;

            if ((balance < 0) && intermediate.length) {
                intermediate.remove();
                renderPattern(event, ui);
            }

            var wasOperator = true;
            var moveLeft = false;

            children.each(function (index) {
                var currentElement = $(this);

                if ((!wasOperator) && (!currentElement.hasClass(CLASS_OPERATOR))) {
                    if (balance > 0) {
                        var operator = createOperator(true);
                        currentElement.before(operator);
                        renderPattern(event, ui);
                        return false;
                    } else if (balance === 0) {
                        moveLeft = true;
                    }
                }

                if (wasOperator && currentElement.hasClass(CLASS_OPERATOR)) {
                    if (balance < 0) {
                        currentElement.remove();
                        renderPattern(event, ui);
                        return false;
                    } else if (moveLeft) {
                        var firstOperator = $(children[index - 1]);
                        firstOperator.insertBefore(firstOperator.prev());
                    } else {
                        currentElement.insertAfter(currentElement.next());
                    }

                    renderPattern(event, ui);
                    return false;
                }

                if ((index === (children.length - 1)) && currentElement.hasClass(CLASS_OPERATOR)) {
                    currentElement.insertBefore(currentElement.prev());
                    renderPattern(event, ui);
                    return false;
                }

                wasOperator = currentElement.hasClass(CLASS_OPERATOR);
            });
        }

        function initComponents(componentList) {
            //Iterate over all component categories
            for (var i = 0; i < componentList.length; i++) {

                var componentCategory = componentList[i];

                var categoryElem = createComponentCategory(componentCategory.name);
                var categoryContent = categoryElem.find('.' + CLASS_COMPONENT_CATEGORY_LIST);

                for (var j = 0; j < componentCategory.list.length; j++) {
                    var componentElem = createComponent(componentCategory.list[j], componentCategory.icon);
                    categoryContent.append(componentElem);
                }

                componentsCategoryContainer.append(categoryElem);
            }
        }

        initComponents(scope.componentList);

        patternContainer.sortable({
            items: '> .' + CLASS_COMPONENT,
            placeholder: CLASS_COMPONENT_PLACEHOLDER,
            change: updatePattern,
            receive: componentAdd
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
            '<div class="' + CLASS_MAIN_CONTAINER + '">' +
            '<div class="' + CLASS_PATTERN_CONTAINER + '">' +
            '</div>' +
            '<div class="panel-group ' + CLASS_COMPONENT_CATEGORY_CONTAINER + '">' +
            '</div>' +
        '</div>'
        ,
        link: link,
        scope: {
            componentList: '=componentList'
        }
    };
}]);