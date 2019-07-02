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
    const CLASS_OPTIONS_CONTAINER = 'options-container';

    const CONDITION_PICKER_EXTENSION_NAME = 'cep-query-extension';

    const KEY_ID = 'id';
    const KEY_DETAILS_REF = 'details_ref';
    const KEY_OPERATOR_PRECEDENCE = 'operator_precedence';
    const KEY_SOURCE_COMPONENT_DATA = 'source_data';
    const KEY_ELEMENT_KEY = 'element_key';

    const OPERATOR_TYPES_LIST = [{
        name: 'Before',
        description: 'Specifies that first the expression on the left hand must turn true and only then ' +
            'the right hand expression is evaluated for matching events. Optionally, a time interval may be' +
            'specified during which the second expression must turn true after the first one.',
        cssClass: 'before',
        precedence: 1,
        key: 'before',
        createForm: (form, element) => {
            let withinTimeSwitchWrapper
                = $('<div class="switch"><label>Off<input type="checkbox"><span class="lever"></span>On</label></div>');
            let withinTimeSwitch = withinTimeSwitchWrapper.find('input');
            let withinTimeInput = $('<input class="form-control" type="number" placeholder="Time in seconds" min="0">');
            form.append($('<div class="form-group"><div class="form-line"></div></div>').children()
                .append('<label>Within time interval:</label>').append('<br/>')
                .append(withinTimeSwitchWrapper)
                .append(withinTimeInput));

            withinTimeInput.prop('disabled', true);

            withinTimeSwitch.on('change', function () {
                let switchValue = withinTimeSwitch.prop('checked');
                withinTimeInput.prop('disabled', !switchValue);
            });
        },
        querify: function () {
        }
    }, {
        name: 'Or',
        description: 'Requires either one of the expressions on the left hand and on the right hand to be true ' +
            'in order to turn true itself.',
        cssClass: 'or',
        precedence: 2,
        key: 'or',
        createForm: null,
        querify: function () {
        }
    }, {
        name: 'And',
        description: 'Requires the expressions on the left hand and on the right hand to be true in order to' +
            ' turn true itself.',
        cssClass: 'and',
        precedence: 3,
        key: 'and',
        createForm: null,
        querify: function () {
        }
    }];

    const COMPONENT_TYPES_LIST = [{
        name: 'Wait',
        description: 'Waits for the defined time until its truth value turns into true.',
        icon: 'hourglass_empty',
        key: 'wait',
        createForm: (form, element) => {
            let withinTimeInput = $('<input class="form-control" type="number" placeholder="Time in seconds" min="0">');
            form.append($('<div class="form-group"><div class="form-line"></div></div>').children()
                .append('<label>Wait time:</label>').append('<br/>').append(withinTimeInput));
        },
        querify: function () {
        }
    }, {
        name: 'Points in time',
        description: 'Turns true at specified points in time.',
        icon: 'date_range',
        key: 'timestamp',
        createForm: (form, element) => {
            const inputs = ['Minutes', 'Hours', 'Days of month', 'Months', 'Days of week'];
            const explaination = $('<p>').html('The time points during which this pattern element is supposed to turn true' +
                ' are specified using a syntax that is similar to the one of the Unix command' +
                ' <a target="_blank" href="https://en.wikipedia.org/wiki/Cron#CRON_expression">&ldquo;crontab&rdquo;</a>.' +
                ' The following syntactical elements are available:');

            const cheatsheet = $('<ul>');
            cheatsheet.append($('<li>').html('<code>*</code>: Wildcard, selects all possible values'));
            cheatsheet.append($('<li>').html('<code>x:y</code>: Specifies a range with <i>x</i> as upper' +
                ' and <i>y</i> as lower bound'));
            cheatsheet.append($('<li>').html('<code>*/x</code>: Selects every <i>x</i>-th value'));
            cheatsheet.append($('<li>').html('<code>[x, y]</code>: Combination of the expressions <i>x</i> and <i>y</i>'));

            const example = $('<code>*/15  8:17  [*/2,1]  *  *</code>');
            const exampleDescription = $('<p>').html('This specification matches every 15 minutes from 8am to 5pm' +
                ' on even numbered days of each month, as well as on the first day of the month.');

            let inputTable = $('<table>');
            let tableHeadRow = $('<tr>');
            let tableInputRow = $('<tr>');

            for (let i = 0; i < inputs.length; i++) {
                let input = $('<input class="form-control" type="text" placeholder="' + inputs[i] + '" maxlength="10">');
                input.val('*').css({
                    'width': '95px',
                    'margin-left': '5px',
                    'margin-right': '5px',
                    'text-align': 'center'
                });
                tableHeadRow.append($('<th>').addClass('align-center').append(inputs[i]));
                tableInputRow.append($('<td>').append(input))
            }

            inputTable.append($('<thead>').append(tableHeadRow));
            inputTable.append($('<tbody>').append(tableInputRow));


            form.append($('<div class="form-group"><div class="form-line"></div></div>').children()
                .append('<label>Explaination:</label>')
                .append(explaination)
                .append(cheatsheet)
                .append('<br/>')
                .append('<label>Example:</label>')
                .append('<br/>')
                .append(example)
                .append(exampleDescription)
                .append('<label>Time points specification:</label>')
                .append($('<p>').html('Please specify the desired points in time below.'))
                .append(inputTable));
        },
        querify: function () {
        }
    }];

    const SOURCE_COMPONENT_TYPE_PROTOTYPE = {
        //name and icon: Dynamically assigned
        description: 'Turns true if an event (i.e. a value) of this component was received.',
        key: 'source',
        createForm: (form, element) => {
            const defaultAlias = 'event' + element.data(KEY_ID);
            let aliasInput = $('<input class="form-control" type="text" placeholder="Alias" maxlength="50">');
            aliasInput.val(defaultAlias);

            form.append($('<div class="form-group"><div class="form-line"></div></div>').children()
                .append('<label>Alias:</label>')
                .append('<br/>')
                .append(aliasInput));
        },
        querify: function () {
        }
    };

    //List of all available types (shortcut)
    const ALL_ELEMENT_TYPES = OPERATOR_TYPES_LIST.concat(COMPONENT_TYPES_LIST, [SOURCE_COMPONENT_TYPE_PROTOTYPE]);

    function init(scope, element, attrs) {

        const mainContainer = $('.' + CLASS_MAIN_CONTAINER);
        const patternContainer = $('.' + CLASS_PATTERN_CONTAINER);
        const deletionArea = $('.' + CLASS_DELETION_AREA);
        const componentsCategoryContainer = $('.' + CLASS_CATEGORY_CONTAINER);
        const detailsContainer = $('.' + CLASS_DETAILS_CONTAINER);
        const optionsContainer = $('.' + CLASS_OPTIONS_CONTAINER);
        const conditionsPicker = $('<div>');

        let categoryIdCounter = 0;
        let elementIdCounter = 0;
        let isDragging = false;

        function getElementType(element) {
            let key = element.data(KEY_ELEMENT_KEY);

            for (let i = 0; i < ALL_ELEMENT_TYPES.length; i++) {
                let type = ALL_ELEMENT_TYPES[i];

                if (key === type.key) {
                    return type;
                }
            }

            return null;
        }

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
            let newType = Object.assign({}, SOURCE_COMPONENT_TYPE_PROTOTYPE);
            newType.icon = icon;
            newType.name = component.name;

            let element = createComponentType(newType);
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

            let titleContent = $('<a class="clickable collapsed" data-toggle="collapse" ' +
                'data-target="#category-' + categoryId + '-list">' + categoryName +
                '<i class="material-icons" style="float: right;">keyboard_arrow_down</i></a>');

            title.append(titleContent);
            heading.append(title);

            let body = $('<div class="panel-collapse collapse">').attr('id', 'category-' + categoryId + '-list')
                .append($('<div class="panel-body">').addClass(CLASS_CATEGORY_LIST));

            element.append(heading);
            element.append(body);

            return element;
        }

        function createPatternElementDetails(element) {
            let elementType = getElementType(element);

            let elementId = element.data(KEY_ID);

            //Build title string to display, depending on the element type
            let title = "Details: ";

            if (elementType.key === SOURCE_COMPONENT_TYPE_PROTOTYPE.key) {
                let sourceData = element.data(KEY_SOURCE_COMPONENT_DATA);
                title += "Event of " + sourceData.name;
            } else if (element.hasClass(CLASS_OPERATOR)) {
                title += "<i>" + elementType.name + "</i> Operator";
            } else {
                title += "<i>" + elementType.name + "</i> Component";
            }

            let container = $('<div class="panel panel-default">');
            let heading = $('<div class="panel-heading">');
            let titleBar = $('<h4 class="panel-title">');
            let titleContent = $('<a class="clickable">' + title +
                '<i class="material-icons" style="float: right;">close</i></a>')
                .on('click', function () {
                    hideElementDetails();
                });

            heading.append(titleBar);
            titleBar.append(titleContent);

            let body = $('<div class="panel-body">');
            let bodyHeader = $('<div>').addClass('details-header');
            let description = $('<span></span>').html(elementType.description);
            let form = $('<form>');

            bodyHeader.append('<i class="material-icons">info_outline</i>').append(description);
            body.append(bodyHeader);

            if (typeof elementType.createForm === "function") {
                elementType.createForm(form, element);
                body.append(form);
            }

            container.append(heading).append($('<div>').append(body));
            container.data(KEY_DETAILS_REF, elementId);
            container.slideUp(0);
            detailsContainer.append(container);

            element.on('click', function () {
                toggleElementDetails(elementId);
            });
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

        function hideElementDetails() {
            patternContainer.children().removeClass(CLASS_ELEMENT_SELECTED);
            return detailsContainer.children().slideUp();
        }

        function highlightPrecedence(operator) {
            function parseElementList(list, minPrecedence, style) {
                list.each(function () {
                    let currentElement = $(this);
                    let operatorPrecedence = currentElement.data(KEY_OPERATOR_PRECEDENCE) || Number.MAX_SAFE_INTEGER;

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

            //Create details panel
            createPatternElementDetails(element);
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

            hideElementDetails();
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

        function initPattern() {
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
        }

        function initDeletionArea() {
            deletionArea.hide().droppable({
                accept: '.' + CLASS_PATTERN_CONTAINER + ' > .' + CLASS_PATTERN_ELEMENT,
                hoverClass: CLASS_DELETION_AREA_ACCEPTING,
                drop: removeElement
            });
        }


        function initOptions() {
            let conditionsPanel = $('<div class="panel panel-default">')
                .append($('<div class="panel-heading">')
                    .append($('<h4 class="panel-title">')
                        .append($('<a class="clickable" data-toggle="collapse" ' +
                            'data-target="#options-conditions-body">Conditions' +
                            '<i class="material-icons" style="float: right;">keyboard_arrow_down</i></a>'))));

            let conditionsPanelBody = $('<div id="options-conditions-body" class="panel-collapse collapse in">')
                .append($('<div class="panel-body">').append(conditionsPicker));

            conditionsPanel.append(conditionsPanelBody);
            optionsContainer.append(conditionsPanel);

            initConditionsPicker();
        }

        function initConditionsPicker() {
            //List of all available operators
            const OPERATORS_LIST = [{
                'id': 'equal',
                'sign': '='
            }, {
                'id': 'not_equal',
                'sign': '&ne;'
            }, {
                'id': 'less',
                'sign': '&lt;'
            }, {
                'id': 'less_or_equal',
                'sign': '&le;'
            }, {
                'id': 'greater',
                'sign': '&gt;'
            }, {
                'id': 'greater_or_equal',
                'sign': '&ge;'
            }, {
                'id': 'between',
                'sign': '&hArr;'
            }, {
                'id': 'not_between',
                'sign': '&nhArr;'
            }];

            $.fn.queryBuilder.define(CONDITION_PICKER_EXTENSION_NAME, function (options) {

                this.on('afterCreateRuleOperators.queryBuilder', function (event, rule) {
                    const KEY_OPERATOR_INDEX = 'current_index';

                    //Apply styling
                    $('.query-builder .rule-value-container').css({'border-left': 'none', 'padding-left': '0px'});

                    let ruleElement = rule.$el;
                    let operatorContainer = $(ruleElement.find('div.rule-operator-container'));
                    let operatorSelect = $(operatorContainer.find('select'));

                    let operatorStartIndex = 0;
                    let alternativeOperatorChooser = $('<span>').addClass('clickable').css({
                        'font-size': '30px',
                        'font-weight': 'bold',
                        'vertical-align': 'middle',
                    }).html(OPERATORS_LIST[operatorStartIndex].sign).data(KEY_OPERATOR_INDEX, operatorStartIndex)
                        .on('click', function () {
                            let element = $(this);
                            let currentIndex = element.data(KEY_OPERATOR_INDEX) || 0;
                            let nextIndex = (currentIndex + 1) % OPERATORS_LIST.length;
                            element.data(KEY_OPERATOR_INDEX, nextIndex);
                            element.html(OPERATORS_LIST[nextIndex].sign);
                            operatorSelect.val(OPERATORS_LIST[nextIndex].id).trigger('change');
                        });

                    //Place alternative operator chooser before select and hide select
                    operatorSelect.before(alternativeOperatorChooser).hide();
                });

                this.on('afterUpdateGroupCondition.queryBuilder', function (event, rule) {
                    $('div.group-conditions > label').each(function () {
                        var _this = $(this);
                        if (_this.hasClass('active')) {
                            _this.css('opacity', '1');
                        } else {
                            _this.css('opacity', '0.7');
                        }
                    });
                });

                this.on('afterCreateRuleFilters.queryBuilder', function (event, rule) {
                    function onTypeChoose() {
                        filterSelect.show();
                        if (lastTypeChoice !== filterSelect.val()) {
                            filterSelect.val('-1');
                            filterSelect.trigger('change');
                        }
                        lastTypeChoice = filterSelect.val();
                    }

                    //Apply styling
                    $('button[data-add="rule"]').html('<i class="material-icons">add</i> Add rule');
                    $('button[data-add="group"]').html('<i class="material-icons">add_circle_outline</i> Add group');
                    $('button[data-delete="rule"]').html('<i class="material-icons">delete</i>');
                    $('button[data-delete="group"]').html('<i class="material-icons">delete_forever</i> Delete group');

                    let ruleElement = rule.$el;
                    let filterContainer = $(ruleElement.find('div.rule-filter-container'));
                    let filterSelect = $(filterContainer.find('select'));

                    let lastTypeChoice = null;

                    let dropdownButton = $('<button type="button" class="btn bg-primary dropdown-toggle" ' +
                        'data-toggle="dropdown">Select type <span class="caret"/></button>');
                    let dropdown = $('<ul class="dropdown-menu">');
                    let buttonSingleEvent = $('<li>').append($('<a class="waves-effect waves-block">Single event</a>')
                        .on('click', () => {
                            dropdownButton.html('Event <span class="caret"/>');
                            onTypeChoose();
                        }));
                    let buttonAggregation = $('<li>').append($('<a class="waves-effect waves-block">Aggregation</a>')
                        .on('click', () => {
                            dropdownButton.html('Aggreg. <span class="caret"/>');
                            onTypeChoose(this);
                        }));
                    dropdown.append(buttonSingleEvent).append(buttonAggregation);

                    let dropdownContainer = $('<div class="btn-group">').append(dropdownButton).append(dropdown)
                        .css({
                            'box-shadow': 'none',
                            'margin-right': '5px'
                        });

                    filterContainer.prepend(dropdownContainer);
                    filterSelect.hide();
                });
            }, {});

            let predefinedConditionsFilter = [
                {
                    id: 'event1',
                    label: 'event1',
                    type: 'double',
                    operators: OPERATORS_LIST.map(x => x.id)
                }
            ];

            let pluginsObject = {};
            pluginsObject[CONDITION_PICKER_EXTENSION_NAME] = {};

            conditionsPicker.queryBuilder({
                filters: predefinedConditionsFilter,
                plugins: pluginsObject
            });


            //conditionsPicker.queryBuilder('setFilters', true, filterArray);
        }

        (function () {
            initOperators();
            initSourceComponents(scope.componentList);
            initAdditionalComponents();

            initPattern();
            initDeletionArea();

            initOptions();
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
            '<div class="panel-group ' + CLASS_OPTIONS_CONTAINER + '"></div>' +
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
}]);