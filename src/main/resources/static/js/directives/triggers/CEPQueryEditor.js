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
    const KEY_SOURCE_ALIAS = 'source_alias';
    const KEY_SOURCE_COMPONENT_DATA = 'source_data';
    const KEY_ELEMENT_KEY = 'element_key';

    //List of all available operators for the condition picker
    const CONDITION_PICKER_OPERATORS = [{
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
    }];

    const CONDITIONS_PICKER_OPERATORS_PLAIN = CONDITION_PICKER_OPERATORS.map(operator => operator.id);
    const CONDITIONS_PICKER_SOURCE_FILTER = {
        'name': 'Single event',
        'short': 'Event',
        'prefix': 'event_'
    };
    const CONDITIONS_PICKER_AGGREGATION_FILTERS = [{
        'name': 'Average',
        'short': 'Average',
        'prefix': 'avg_'
    }, {
        'name': 'Minimum',
        'short': 'Minimum',
        'prefix': 'min_'
    }, {
        'name': 'Maximum',
        'short': 'Maximum',
        'prefix': 'max_'
    }];

    const CONDITIONS_PICKER_ALL_FILTER_TYPES = CONDITIONS_PICKER_AGGREGATION_FILTERS.concat([CONDITIONS_PICKER_SOURCE_FILTER]);

    const CONDITIONS_PICKER_WINDOW_UNITS = [{short: 'seconds', querySymbol: 's'},
        {short: 'minutes', querySymbol: 'm'}, {short: 'hours', querySymbol: 'h'}, {short: 'days', querySymbol: 'd'}];


    function init(scope, element, attrs) {

        const OPERATOR_TYPES_LIST = [{
            name: 'Before',
            description: 'Specifies that first the expression on the left hand must turn true and only then ' +
                'the right hand expression is evaluated for matching events. Optionally, a time interval may be' +
                'specified during which the second expression must turn true after the first one.',
            cssClass: 'before',
            precedence: 1,
            key: 'before',
            init: null,
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
            init: null,
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
            init: null,
            createForm: null,
            querify: function () {
            }
        }];

        const COMPONENT_TYPES_LIST = [{
            name: 'Wait',
            description: 'Waits for the defined time until its truth value turns into true.',
            icon: 'hourglass_empty',
            key: 'wait',
            init: null,
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
            init: null,
            createForm: (form, element) => {
                const inputs = ['Minutes', 'Hours', 'Days of month', 'Months', 'Days of week'];
                const explanation = $('<p>').html('The time points during which this pattern element is supposed to turn true' +
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
                    .append(explanation)
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
        const SOURCE_ALIAS_PREFIX = 'event_';
        const SOURCE_COMPONENT_TYPE_PROTOTYPE = {
            //name and icon: Dynamically assigned
            description: 'Turns true if an event (i.e. a value) of this component was received.',
            key: 'source',
            init: (element) => {
                //Generate first alias for this source event
                let defaultAlias = SOURCE_ALIAS_PREFIX + element.data(KEY_ID);
                element.data(KEY_SOURCE_ALIAS, defaultAlias);
            },
            createForm: (form, element) => {
                let eventId = element.data(KEY_ID);
                let alias = element.data(KEY_SOURCE_ALIAS);
                let aliasInput = $('<input class="form-control" type="text" placeholder="Alias" maxlength="50">');
                aliasInput.val(alias);
                aliasInput.on('change', function () {
                    let newAliasValue = aliasInput.val();

                    //Do not allow alias with less than 3 chars or other alias that start with the alias prefix
                    if ((newAliasValue.length < 3) || (newAliasValue.startsWith(SOURCE_ALIAS_PREFIX))) {
                        newAliasValue = SOURCE_ALIAS_PREFIX + eventId;
                        aliasInput.val(newAliasValue);
                    }

                    //Write new value
                    element.data(KEY_SOURCE_ALIAS, newAliasValue);

                    //Find the corresponding filter
                    for (let i = 0; i < conditionsPickerFilters.length; i++) {
                        let currentFilter = conditionsPickerFilters[i];

                        //Check if current filter is the wanted one
                        if (currentFilter.id === (CONDITIONS_PICKER_SOURCE_FILTER.prefix + eventId)) {
                            //Update filter label to new alias
                            currentFilter.label = newAliasValue;

                            //Update conditions picker accordingly
                            conditionsPicker.queryBuilder('setFilters', conditionsPickerFilters);

                            break;
                        }
                    }
                });

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

        let conditionsPickerFilters = [{
            //Null object filter that will be hidden, only needed to make the builder work at the beginning
            id: 'null',
            label: 'None',
            type: 'double',
            operators: CONDITIONS_PICKER_OPERATORS_PLAIN
        }];

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

        function patternElementAdded(event, ui) {
            let element = ui.helper;
            let prototype = ui.item;

            //Copy data from prototype to new pattern element and give new element id
            let elementData = prototype.data();
            elementData[KEY_ID] = generateElementId();
            element.data(elementData);

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

            //Determine element type
            let elementType = getElementType(element);
            //Call init function (if available) as defined by the type of this event
            if (typeof elementType.init === "function") {
                elementType.init(element);
            }

            //Check if element is a source component, update the conditions picker accordingly
            if (elementData[KEY_ELEMENT_KEY] === SOURCE_COMPONENT_TYPE_PROTOTYPE.key) {
                addSourceToConditionsPicker(element);
            }

            //Create details panel
            createPatternElementDetails(element);
        }

        function addSourceToConditionsPicker(element) {
            let eventId = element.data(KEY_ID);
            let eventAlias = element.data(KEY_SOURCE_ALIAS);

            //List of filters to add
            let addFiltersList = [];

            //Iterate over all available filter types and create corresponding filter objects
            for (let i = 0; i < CONDITIONS_PICKER_ALL_FILTER_TYPES.length; i++) {
                let filterType = CONDITIONS_PICKER_ALL_FILTER_TYPES[i];

                //Determine id and name to use for this filter
                let filterId = filterType.prefix + eventId;
                let filterLabel = eventAlias;
                if (filterType !== CONDITIONS_PICKER_SOURCE_FILTER) {
                    let sourceComponentData = element.data(KEY_SOURCE_COMPONENT_DATA);
                    filterId = filterType.prefix + sourceComponentData.id;
                    filterLabel = sourceComponentData.name;
                }

                //Do not add the new filter if already one with this id exists
                if (conditionsPickerFilters.map(filter => filter.id).indexOf(filterId) !== -1) {
                    continue;
                }

                //Create filter object for this filter type
                let filterObject = {
                    'id': filterId,
                    'label': filterLabel,
                    'type': 'double',
                    'operators': CONDITIONS_PICKER_OPERATORS_PLAIN
                };

                //Add object to list of filters that are supposed to be added
                addFiltersList.push(filterObject);

                //Add filter to global filters list
                conditionsPickerFilters.push(filterObject);
            }

            //Update conditions picker
            conditionsPicker.queryBuilder('addFilter', addFiltersList, conditionsPickerFilters.length);
        }

        function removeSourceFromConditionsPicker(element) {
            let eventId = element.data(KEY_ID);

            //Stores the ids of filters that are supposed to be deleted
            let deleteFiltersList = [];

            //Iterate over all available filter types and remove the corresponding filters
            for (let i = 0; i < CONDITIONS_PICKER_ALL_FILTER_TYPES.length; i++) {
                let filterType = CONDITIONS_PICKER_ALL_FILTER_TYPES[i];

                //Concat id of the filter to remove for the current filter type
                let filterId = filterType.prefix + eventId;
                if (filterType !== CONDITIONS_PICKER_SOURCE_FILTER) {
                    filterId = filterType.prefix + element.data(KEY_SOURCE_COMPONENT_DATA).id;
                }

                //Add id to list of filters that are supposed to be deleted
                deleteFiltersList.push(filterId);
            }

            //Update conditions picker
            conditionsPicker.queryBuilder('removeFilter', deleteFiltersList, true);
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

            if (element.data(KEY_ELEMENT_KEY) === SOURCE_COMPONENT_TYPE_PROTOTYPE.key) {
                removeSourceFromConditionsPicker(element);
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
            //Remembers the filter type choices for each rule
            let filterTypeChoices = {};
            //Remembers user options regarding aggregation windows
            let aggregationWindowOptions = {};

            $.fn.queryBuilder.define(CONDITION_PICKER_EXTENSION_NAME, function (options) {

                this.on('afterAddGroup.queryBuilder', function (event, group) {
                    console.log("hier!!");
                    console.log(group);
                    let element = group.$el;

                    //Apply styling
                    element.css({
                        'background': 'none',
                        'border': '1px solid grey'
                    });
                });

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
                    }).html(CONDITION_PICKER_OPERATORS[operatorStartIndex].sign).data(KEY_OPERATOR_INDEX, operatorStartIndex)
                        .on('click', function () {
                            let element = $(this);
                            let currentIndex = element.data(KEY_OPERATOR_INDEX) || 0;
                            let nextIndex = (currentIndex + 1) % CONDITION_PICKER_OPERATORS.length;
                            element.data(KEY_OPERATOR_INDEX, nextIndex);
                            element.html(CONDITION_PICKER_OPERATORS[nextIndex].sign);
                            operatorSelect.val(CONDITION_PICKER_OPERATORS[nextIndex].id).trigger('change');
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
                    function setFilterDropDownText(filterType) {
                        dropdownButton.html(filterType.short + ' <span class="caret"/>');
                    }

                    function updateAvailableFilters(filterType) {
                        filterSelect.children('option').each(function () {
                            let option = $(this);
                            let value = option.val();
                            //Hide select options that do not match the filter prefix
                            if ((value.startsWith(filterType.prefix)) || (value === '-1')) {
                                option.show();
                            } else {
                                option.hide();
                            }
                        });
                    }

                    function onTypeChoose(filterType) {
                        filterSelect.show();

                        if (filterTypeChoices[rule.id] !== filterType) {
                            filterSelect.val('-1');
                            filterSelect.trigger('change');
                        }

                        //Update last filter type choice for this rule
                        filterTypeChoices[rule.id] = filterType;

                        setFilterDropDownText(filterType);
                        updateAvailableFilters(filterType);
                    }

                    function createFilterDropDownItem(filterType) {
                        return $('<li>').append($('<a class="waves-effect waves-block"></a>').html(filterType.name)
                            .on('click', () => {
                                onTypeChoose(filterType);
                            }));
                    }

                    //Apply styling
                    $('button[data-add="rule"]').html('<i class="material-icons">add</i> Add condition');
                    $('button[data-add="group"]').html('<i class="material-icons">add_circle_outline</i> Add group');
                    $('button[data-delete="rule"]').html('<i class="material-icons">delete</i>');
                    $('button[data-delete="group"]').html('<i class="material-icons">delete_forever</i> Delete group');

                    let ruleElement = rule.$el;
                    let filterContainer = $(ruleElement.find('div.rule-filter-container'));
                    let filterSelect = $(filterContainer.find('select'));

                    let dropdownButton = $('<button type="button" class="btn bg-primary dropdown-toggle" ' +
                        'data-toggle="dropdown">Select type <span class="caret"/></button>');
                    let dropdown = $('<ul class="dropdown-menu">');
                    dropdown.append(createFilterDropDownItem(CONDITIONS_PICKER_SOURCE_FILTER));
                    dropdown.append($('<div class="divider">'));

                    //Add all aggregation filters
                    for (let i = 0; i < CONDITIONS_PICKER_AGGREGATION_FILTERS.length; i++) {
                        dropdown.append(createFilterDropDownItem(CONDITIONS_PICKER_AGGREGATION_FILTERS[i]))
                    }

                    let dropdownContainer = $('<div class="btn-group">').append(dropdownButton).append(dropdown)
                        .css({
                            'box-shadow': 'none',
                            'margin-right': '5px'
                        });

                    filterContainer.prepend(dropdownContainer);

                    if (typeof (filterTypeChoices[rule.id]) === 'undefined') {
                        filterSelect.hide();
                    } else {
                        setFilterDropDownText(filterTypeChoices[rule.id]);
                        updateAvailableFilters(filterTypeChoices[rule.id]);
                    }
                });

                this.on('afterUpdateRuleFilter.queryBuilder', function (event, rule) {
                    function setWindowTypeDropdownText(text) {
                        windowTypeDropdownButton.html(text + ' <span class="caret"/>');
                    }

                    //Apply styling
                    $('.rule-value-container input:first').css({
                        'width': '70px',
                        'text-align': 'center'
                    });

                    if (typeof (filterTypeChoices[rule.id]) === 'undefined') {
                        return;
                    }

                    //Get filter type
                    let filterType = filterTypeChoices[rule.id];
                    if (filterType === CONDITIONS_PICKER_SOURCE_FILTER) {
                        return;
                    }

                    let ruleElement = rule.$el;
                    let filterContainer = $(ruleElement.find('div.rule-filter-container'));
                    let filterSelect = $(filterContainer.find('select'));
                    let valueContainer = $(ruleElement.find('div.rule-value-container'));

                    if ((filterSelect.val() === '-1')) {
                        delete aggregationWindowOptions[rule.id];
                        return;
                    }

                    if (typeof (aggregationWindowOptions[rule.id]) !== 'undefined' &&
                        valueContainer.find('button.dropdown-toggle').length) {
                        return;
                    }

                    aggregationWindowOptions[rule.id] = {
                        'type': null,
                        'size': 1,
                        'unit': 0
                    };

                    let windowTypeDropdownButton = $('<button type="button" class="btn btn-warning dropdown-toggle" ' +
                        'data-toggle="dropdown">Select window <span class="caret"/></button>');
                    let windowTypeDropdown = $('<ul class="dropdown-menu">');
                    let dropdownLengthWindowItem = $('<li>').append($('<a class="waves-effect waves-block"></a>')
                        .html('Length window').on('click', () => {
                            aggregationWindowOptions[rule.id].type = 'length';
                            setWindowTypeDropdownText('Length');
                            windowSizeInput.show();
                            lengthWindowSizeUnitDisplay.show();
                            timeWindowSizeUnitDisplay.hide();
                        }));
                    let dropdownTimeWindowItem = $('<li>').append($('<a class="waves-effect waves-block"></a>')
                        .html('Time window').on('click', () => {
                            aggregationWindowOptions[rule.id].type = 'time';
                            setWindowTypeDropdownText('Time');
                            windowSizeInput.show();
                            lengthWindowSizeUnitDisplay.hide();
                            timeWindowSizeUnitDisplay.show();
                        }));

                    windowTypeDropdown.append(dropdownLengthWindowItem).append(dropdownTimeWindowItem);

                    let windowTypeDropdownContainer = $('<div class="btn-group">')
                        .append(windowTypeDropdownButton).append(windowTypeDropdown)
                        .css({
                            'box-shadow': 'none',
                            'margin-left': '10px',
                            'margin-right': '2px'
                        });

                    let windowSizeInput = $('<input type="number" placeholder="Size" min="1" class="form-control">').css({
                        'width': '60px',
                        'text-align': 'center'
                    }).hide().on('change', function () {
                        aggregationWindowOptions[rule.id].size = parseInt(windowSizeInput.val());
                    });

                    let defaultUnit = CONDITIONS_PICKER_WINDOW_UNITS[aggregationWindowOptions[rule.id].unit].short;
                    let timeWindowSizeUnitDisplay = $('<span>').addClass('clickable').html(defaultUnit).css({
                        'font-size': '14px',
                        'font-weight': 'bold',
                        'margin-left': '5px'
                    }).hide().on('click', function () {
                        let currentIndex = aggregationWindowOptions[rule.id].unit;
                        let newIndex = (currentIndex + 1) % CONDITIONS_PICKER_WINDOW_UNITS.length;
                        $(this).html(CONDITIONS_PICKER_WINDOW_UNITS[newIndex].short);
                        aggregationWindowOptions[rule.id].unit = newIndex;
                    });

                    let lengthWindowSizeUnitDisplay = $('<span>').addClass('clickable').html('events').css({
                        'font-size': '14px',
                        'font-weight': 'bold',
                        'margin-left': '5px'
                    }).hide();

                    valueContainer.append(windowTypeDropdownContainer)
                        .append(windowSizeInput)
                        .append(timeWindowSizeUnitDisplay)
                        .append(lengthWindowSizeUnitDisplay);
                });
            }, {});

            let pluginsObject = {};
            pluginsObject[CONDITION_PICKER_EXTENSION_NAME] = {};

            conditionsPicker.queryBuilder({
                filters: conditionsPickerFilters,
                plugins: pluginsObject
            });
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