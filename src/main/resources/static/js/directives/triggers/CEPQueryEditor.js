/* global app */

'use strict';

/**
 * Directive which allows the user to define event patterns based on components.
 */
app.directive('cepQueryEditor', ['$compile', function ($compile) {

    const CLASS_MAIN_CONTAINER = 'cep-query-editor';
    const CLASS_PATTERN_CONTAINER = 'pattern-container';
    const CLASS_DELETION_AREA = 'deletion-area';
    const CLASS_DELETION_AREA_ACCEPTING = 'accepting';
    const CLASS_ERROR_AREA = 'error-area';
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
    const KEY_SOURCE_RESOURCE_NAME = "source_resource";
    const KEY_ELEMENT_KEY = 'element_key';
    const KEY_OPERATOR_INDEX = 'op_index';

    const KEY_EVENT_FILTER_CONDITION = "filter_condition_bindings";

    //List of all available operators for the condition picker
    const CONDITION_PICKER_OPERATORS = [{
        'id': 'equal',
        'operator': '=',
        'sign': '='
    }, {
        'id': 'not_equal',
        'operator': '!=',
        'sign': '&ne;'
    }, {
        'id': 'less',
        'operator': '<',
        'sign': '&lt;'
    }, {
        'id': 'less_or_equal',
        'operator': '<=',
        'sign': '&le;'
    }, {
        'id': 'greater',
        'operator': '>',
        'sign': '&gt;'
    }, {
        'id': 'greater_or_equal',
        'operator': '>=',
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
        'func': 'avg',
        'prefix': 'avg_'
    }, {
        'name': 'Minimum',
        'short': 'Minimum',
        'func': 'min',
        'prefix': 'min_'
    }, {
        'name': 'Maximum',
        'short': 'Maximum',
        'func': 'max',
        'prefix': 'max_'
    }];

    const CONDITIONS_PICKER_ALL_FILTER_TYPES = CONDITIONS_PICKER_AGGREGATION_FILTERS.concat([CONDITIONS_PICKER_SOURCE_FILTER]);

    const CONDITIONS_PICKER_WINDOW_UNITS = [{short: 'seconds', querySymbol: 'sec'},
        {short: 'minutes', querySymbol: 'min'}, {short: 'hours', querySymbol: 'hour'}, {
            short: 'days',
            querySymbol: 'day'
        }];

    function init(scope, element, attrs) {
        // Map for accessing the rule objects provided by the query builder for the angular bindings of the json path input
        scope.ruleJsonPathBindings = new Map();
        // Map for accessing component infos by their corresponding event name (filter name). Needed for the json path input
        scope.eventComponentMapping = new Map();

        scope.sourcesMapForFilterConditions = new Map();

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
                    = $('<div class="switch"><label>Off<input type="checkbox" name="withinSwitch"><span class="lever"></span>On</label></div>');
                let withinTimeSwitch = withinTimeSwitchWrapper.find('input');
                let withinTimeInput = $('<input class="form-control" type="number" name="withinInput" placeholder="Time in seconds" min="0">');
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
            querify: (element, detailsPage, leftSide, rightSide) => {
                let withinSwitch = detailsPage.find('input[name="withinSwitch"]');

                if (withinSwitch.prop('checked')) {
                    let withinInput = detailsPage.find('input[name="withinInput"]');
                    let withinTime = withinInput.val();

                    if (withinTime === "") {
                        withinTime = 0;
                    } else {
                        withinTime = parseInt(withinTime) * 1000;
                    }

                    return "(" + leftSide + " -> " + rightSide + " WHERE timer:within(" + withinTime + "))";
                }

                return leftSide + " -> " + rightSide;
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
            querify: (element, detailsPage, leftSide, rightSide) => {
                return leftSide + " or " + rightSide;
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
            querify: (element, detailsPage, leftSide, rightSide) => {
                return leftSide + " and " + rightSide;
            }
        }];

        const COMPONENT_TYPES_LIST = [{
            name: 'Wait',
            description: 'Waits for the defined time until its truth value turns into true.',
            icon: 'hourglass_empty',
            key: 'wait',
            init: null,
            createForm: (form, element) => {
                let waitTimeInput = $('<input class="form-control" type="number" name="waitTime" placeholder="Time in seconds" min="0">');
                form.append($('<div class="form-group"><div class="form-line"></div></div>').children()
                    .append('<label>Wait time:</label>').append('<br/>').append(waitTimeInput));
            },
            querify: (element, detailsPage) => {
                let waitTimeInput = detailsPage.find('input[name="waitTime"]');
                let waitTime = waitTimeInput.val();

                if (!waitTime) {
                    waitTime = 0;
                } else {
                    waitTime = parseInt(waitTime);
                }

                return "timer:interval(" + waitTime + " sec)";
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
                    let input = $('<input class="form-control" type="text" name="timestamp[' + i + ']" placeholder="' + inputs[i] + '" maxlength="10">');
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
            querify: (element, detailsPage) => {
                let timeParameters = [];

                detailsPage.find('[name^=timestamp]').each(function (index) {
                    let value = $(this).val();
                    timeParameters.push(value);
                });

                return "timer:at(" + timeParameters.join(", ") + ")";
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
                    console.log("ON CHANGE");

                    let newAliasValue = aliasInput.val();

                    //Do not allow alias with less than 3 chars or other alias that start with the alias prefix
                    if ((newAliasValue.length < 3) || (newAliasValue.startsWith(SOURCE_ALIAS_PREFIX))) {
                        newAliasValue = SOURCE_ALIAS_PREFIX + eventId;
                        aliasInput.val(newAliasValue);
                    }

                    //Write new value
                    element.data(KEY_SOURCE_ALIAS, newAliasValue);

                    console.log(newAliasValue);

                    //Find the corresponding filter
                    for (let i = 0; i < conditionsPickerFilters.length; i++) {
                        let currentFilter = conditionsPickerFilters[i];

                        //Check if current filter is the desired one
                        if (currentFilter.id === (CONDITIONS_PICKER_SOURCE_FILTER.prefix + eventId)) {
                            //Update filter label to new alias
                            currentFilter.label = newAliasValue;

                            //Update conditions picker accordingly
                            conditionsPicker.queryBuilder('setFilters', conditionsPickerFilters);

                            break;
                        }
                    }
                });

                let operatorStartIndex = 0;
                let deleteValueInput = $();

                // Init the data structures of the element to organize the angular bindings. has jsonPath object and value
                element.data(KEY_EVENT_FILTER_CONDITION, []);
                scope.sourcesMapForFilterConditions.set(element.data(KEY_ID), new Map());

                var countOfFilterConditionForms = 0;

                let addFilterConditionButton = $('<button type="button" class="btn btn-xs btn-success" ' +
                    'data-add="rule"><i class="material-icons">add</i> Condition</button>').on('click', function () {

                    // Generate id
                    var id = "" + element.data(KEY_ID) + "+" + countOfFilterConditionForms;

                    // Add a new map entry for storing the ui filter condition contents in bindings properly
                    scope.sourcesMapForFilterConditions.get(element.data(KEY_ID)).set(id,
                        {
                            jsonPath: {},
                            operator: CONDITION_PICKER_OPERATORS[0],
                            value: ""
                        }
                    );

                    appendFilterConditionToForm("" + element.data(KEY_ID) + "+" + countOfFilterConditionForms++);
                    scope.$apply();
                }).css({
                    'margin-top': '10px'
                });
                ;

                /**
                 * Adds a condition element for the filter of event types in their details view.
                 *
                 * @param index Index of the condition to append in the format "<source_id>+<conditionIdWithinSourceFilter">
                 */
                var appendFilterConditionToForm = function (index) {
                    var filterConditionForm = $('<div class="filter-condition-form"' + '>');

                    var operatorStartIndex = 0;
                    var conditionOperatorSelect = $('<span class="clickable" name="conditionOperator">').css({
                        'font-size': '30px',
                        'font-weight': 'bold',
                        'vertical-align': 'middle',
                        'margin-left': '10px',
                        'margin-right': '10px'
                    }).html(CONDITION_PICKER_OPERATORS[operatorStartIndex].sign).data(KEY_OPERATOR_INDEX, operatorStartIndex)
                        .on('click', function () {
                            let thisElement = $(this);
                            let currentIndex = thisElement.data(KEY_OPERATOR_INDEX) || 0;
                            let nextIndex = (currentIndex + 1) % CONDITION_PICKER_OPERATORS.length;
                            thisElement.data(KEY_OPERATOR_INDEX, nextIndex);
                            thisElement.html(CONDITION_PICKER_OPERATORS[nextIndex].sign);
                            // Update the bindings map
                            scope.sourcesMapForFilterConditions.get(element.data(KEY_ID)).get(index).operator = CONDITION_PICKER_OPERATORS[nextIndex];
                        });

                    var conditionValueInput = $('<input class="form-control" name="conditionValue">').css({
                        'display': 'inline-block',
                        'width': '100px',
                        'text-align': 'center'
                    }).on('change', function () {
                        // Update the value in the bindings object
                        scope.sourcesMapForFilterConditions.get(element.data(KEY_ID)).get(index).value = $(this).val();
                    });

                    var conditionRemoveButton = $('<button type="button" class="btn btn-xs btn-danger"' +
                        ' data-delete="rule"><i class="material-icons">delete</i></button>').on('click', function () {
                        scope.sourcesMapForFilterConditions.get(element.data(KEY_ID)).delete(index);
                        filterConditionForm.remove();
                        console.log(scope.sourcesMapForFilterConditions);
                    }).css({
                        'display': 'inline-block',
                        'margin-left': '10px',
                        'margin-right': '10px'
                    });

                    // Prepare the jsonPath input with all needed bindings
                    var jsonFiltInpHtml =
                        "<ng-container><json-path-input style=\"display: inline-block;\" ng-model=\"sourcesMapForFilterConditions.get(" + element.data(KEY_ID) + ").get(\'" + index + "\').jsonPath\" " +
                        "json-path-list=\"eventComponentMapping.get(\'" + element.data(KEY_SOURCE_ALIAS) + "\').operator.dataModel.jsonPathsToLeafNodes\"" +
                        "number-of-needed-wildcards=\"0\"" +
                        "</json-path-input></ng-container>";


                    var jsonPathFilterInput = $compile(jsonFiltInpHtml)(scope);

                    // TODO Add AND between inputs
                    // $('<div class="filter-condition-form"' + '>');
                    var andLabelBetweenInputs = $('<div class="filter-condition-form">').text("AND");

                    filterConditionForm.append(jsonPathFilterInput)
                        .append(conditionOperatorSelect)
                        .append(conditionValueInput)
                        .append(conditionRemoveButton);
                    conditionOptionsContainer.append(filterConditionForm);

                }

                var conditionOptionsContainer = $('<div class="filter-condition-option-container">');

                var conditionContainer = $('<div class="filter-condition-container">')
                    .append(conditionOptionsContainer)
                    .append(addFilterConditionButton)
                    .hide();

                scope.$apply();

                let conditionSwitch = $('<div class="switch"><label>Off<input type="checkbox" name="conditionSwitch"><span class="lever"></span>On</label></div>');
                conditionSwitch.find('input').on('change', function () {
                    if ($(this).prop('checked')) {
                        conditionContainer.slideDown();
                    } else {
                        conditionContainer.slideUp();
                    }
                });

                form.append($('<div class="form-group"><div class="form-line"></div></div>').children()
                    .append('<label>Alias:</label>')
                    .append('<br/>')
                    .append(aliasInput)
                    .append('<br/>')
                    .append('<label>Filter condition:</label>')
                    .append('<br/>')
                    .append(conditionSwitch)
                    .append(conditionContainer));
            },
            querify: (element, detailsPage) => {
                let resourceName = element.data(KEY_SOURCE_RESOURCE_NAME);
                let componentData = element.data(KEY_SOURCE_COMPONENT_DATA);
                let alias = element.data(KEY_SOURCE_ALIAS);

                let conditionSwitch = detailsPage.find('input[name="conditionSwitch"]');

                let conditionString = "";

                if (conditionSwitch.prop('checked')) {

                    conditionString += "(";

                    var forEachCount = 0;
                    // Iterate over all map entries for the filter conditions of this event type and adapt the conditionString
                    scope.sourcesMapForFilterConditions.get(element.data(KEY_ID)).forEach(function (value, key, map) {
                        if (!value.jsonPath.path) {
                            return;
                        }

                        var condValue = value.value;
                        if (value.jsonPath.type === "string" || value.jsonPath.type === "binary") {
                            condValue = "\"" + condValue + "\"";
                        }

                        conditionString += "`" + value.jsonPath.path.substring(1) + "` " + value.operator.operator + " " + condValue;

                        // Add the AND for consecutive conditions
                        if (map.size > 0 && forEachCount < map.size - 1) {
                            conditionString += " AND ";
                        }

                        forEachCount++;
                    });
                    conditionString += ")";
                }
                return alias + "=" + resourceName + "_" + componentData.id + conditionString;

            }
        };

        //List of all available types (shortcut)
        const ALL_ELEMENT_TYPES = OPERATOR_TYPES_LIST.concat(COMPONENT_TYPES_LIST, [SOURCE_COMPONENT_TYPE_PROTOTYPE]);

        const mainContainer = $('.' + CLASS_MAIN_CONTAINER);
        const patternContainer = $('.' + CLASS_PATTERN_CONTAINER);
        const deletionArea = $('.' + CLASS_DELETION_AREA);
        const errorArea = $('.' + CLASS_ERROR_AREA);
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
            operators: CONDITIONS_PICKER_OPERATORS_PLAIN,
            data: {},
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

        function getAliasForPatternEvent(eventId) {
            let component = null;

            //Iterate over all pattern elements
            patternContainer.children('.' + CLASS_COMPONENT).each(function () {
                let element = $(this);
                let fullId = CONDITIONS_PICKER_SOURCE_FILTER.prefix + element.data(KEY_ID);

                //Check if pattern element id matches event id
                if (fullId === eventId) {
                    component = element;
                    return false;
                }
            });

            //Sanity check
            if (component == null) {
                return null;
            }

            //Return alias
            return component.data(KEY_SOURCE_ALIAS);
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

        function createSourceComponentType(component, resourceName, icon) {
            let newType = Object.assign({}, SOURCE_COMPONENT_TYPE_PROTOTYPE);
            newType.icon = icon;
            newType.name = component.name;

            let element = createComponentType(newType);
            element.data(KEY_SOURCE_COMPONENT_DATA, component);
            element.data(KEY_SOURCE_RESOURCE_NAME, resourceName);

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

        function findElementDetails(elementId) {
            //Get all detail panels
            let panels = detailsContainer.children();

            //References found panels
            let foundPanel = null;

            //Iterate over all panels
            panels.each(function () {
                let panel = $(this);

                if (panel.data(KEY_DETAILS_REF) === elementId) {
                    foundPanel = panel.find('.panel-body');
                    return false;
                }
            });

            return foundPanel;
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
            let eventResourceName = element.data(KEY_SOURCE_RESOURCE_NAME);
            let eventAlias = element.data(KEY_SOURCE_ALIAS);

            // Update the maps holding the mapping information: event-name --> component infos
            scope.eventComponentMapping.set(eventAlias, element.data(KEY_SOURCE_COMPONENT_DATA));
            scope.eventComponentMapping.set(element.data(KEY_SOURCE_COMPONENT_DATA).name, element.data(KEY_SOURCE_COMPONENT_DATA));

            //Iterate over all available filter types and create corresponding filter objects
            for (let i = 0; i < CONDITIONS_PICKER_ALL_FILTER_TYPES.length; i++) {
                let filterType = CONDITIONS_PICKER_ALL_FILTER_TYPES[i];

                //Determine id and name to use for this filter
                let filterId = filterType.prefix + eventId;
                let filterLabel = eventAlias;
                if (filterType !== CONDITIONS_PICKER_SOURCE_FILTER) {
                    let sourceComponentData = element.data(KEY_SOURCE_COMPONENT_DATA);
                    filterId = filterType.prefix + eventResourceName + "_" + sourceComponentData.id;
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
                    'type': 'string',
                    'operators': CONDITIONS_PICKER_OPERATORS_PLAIN
                };

                //Add filter to global filters list
                conditionsPickerFilters.push(filterObject);
            }

            //Update conditions picker
            conditionsPicker.queryBuilder('setFilters', conditionsPickerFilters);
        }

        function removeSourceFromConditionsPicker(element) {
            let eventId = element.data(KEY_ID);
            let eventResourceName = element.data(KEY_SOURCE_RESOURCE_NAME);

            //Stores the ids of filters that are supposed to be deleted
            let deleteFiltersList = [];

            //Iterate over all available filter types and remove the corresponding filters
            for (let i = 0; i < CONDITIONS_PICKER_ALL_FILTER_TYPES.length; i++) {
                let filterType = CONDITIONS_PICKER_ALL_FILTER_TYPES[i];

                //Concat id of the filter to remove for the current filter type
                let filterId = filterType.prefix + eventId;
                if (filterType !== CONDITIONS_PICKER_SOURCE_FILTER) {
                    filterId = filterType.prefix + eventResourceName + "_" + element.data(KEY_SOURCE_COMPONENT_DATA).id;
                }

                //Add id to list of filters that are supposed to be deleted
                deleteFiltersList.push(filterId);

                //Delete from internal filter list
                let filterIndex = conditionsPickerFilters.map(filter => filter.id).indexOf(filterId);
                conditionsPickerFilters.splice(filterIndex, 1);
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
        }

        function sortingStop(event, ui) {
            isDragging = false;
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
                    let componentElement = createSourceComponentType(category.list[j], category.resourceName, category.icon);
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
            deletionArea.droppable({
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
                            'data-target="#options-conditions-body">Conditions & Aggregations' +
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
                    let element = group.$el;

                    //Apply styling
                    element.css({
                        'background': 'none',
                        'border': '1px solid grey'
                    });
                });

                this.on('afterCreateRuleOperators.queryBuilder', function (event, rule) {
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

                this.on('validateValue.queryBuilder.filter', function (event, value, rule) {

                    function addValidationError(messageId) {
                        if (Array.isArray(validationResult)) {
                            validationResult.push(messageId);
                        } else {
                            validationResult = [messageId];
                        }
                    }

                    /**
                     * Validates the types of the value input of conditions.
                     *
                     * @param value The value which should be validated against the expected type
                     * @param type The data type which is expected to the data type of value.
                     */
                    function validateTypes(value, type) {
                        console.log(typeof value);
                        switch (type) {
                            case "int":
                            // Fallthrough
                            case "long":
                                var x = parseFloat(value);
                                if ((isNaN(value) || (x | 0) !== x)) {
                                    addValidationError('Value must be a ' + type + "!");
                                }
                                break;
                            case "boolean":
                                if (!(value == "true" || value == "false" || typeof value === "boolean")) {
                                    addValidationError("Value must be either true or false!");
                                }
                                break;
                            case "double":
                            // Fallthrough
                            case "decimal128":
                                if (isNaN(value)) {
                                    addValidationError("Value must be a number!");
                                }
                                break;
                            case "date":
                                // TODO: Check if the date format is valid
                                break;
                            case "binary":
                                // TODO: Check if the date format is valid
                                break;
                            default:
                            // Case for strings etc.
                        }
                    }

                    let validationResult = event.value;

                    // Check whether a json path was selected
                    if (!rule.data.jsonPath.type) {
                        addValidationError('No json path selected!');
                        event.value = validationResult;
                        return;
                    }

                    validateTypes(value, rule.data.jsonPath.type);

                    // Validation for time windows
                    if (typeof (aggregationWindowOptions[rule.id]) !== 'undefined') {
                        if (aggregationWindowOptions[rule.id].type == null) {
                            addValidationError('No aggregation window selected');
                        } else if (aggregationWindowOptions[rule.id].size < 1) {
                            addValidationError('No aggregation window size provided');
                        }
                    }

                    event.value = validationResult;
                });

                this.on('ruleToJson.queryBuilder.filter', function (event, rule) {

                    let jsonObject = event.value;

                    //Add condition type description to the json object
                    jsonObject.conditionType = filterTypeChoices[rule.id];

                    //Check if the rule applies to a single event
                    if (typeof (aggregationWindowOptions[rule.id]) === "undefined") {
                        //No need to change something
                        return jsonObject;
                    }

                    //Add aggregation window options to the json object
                    jsonObject.aggregationWindow = aggregationWindowOptions[rule.id];

                    return jsonObject;
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
                    $('button[data-add="rule"]').html('<i class="material-icons">add</i> Condition')
                        .removeClass("btn-success").addClass("bg-mbp-blue").css('color', 'white');
                    $('button[data-add="group"]').html('<i class="material-icons">add_circle_outline</i> Group')
                        .removeClass("btn-success").addClass("bg-mbp-blue").css('color', 'white');
                    $('button[data-delete="rule"]').html('<i class="material-icons">delete</i>');
                    $('button[data-delete="group"]').html('<i class="material-icons">delete_forever</i> Group');

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

                    let ruleElement = rule.$el;
                    let filterContainer = $(ruleElement.find('div.rule-filter-container'));
                    let filterSelect = $(filterContainer.find('select.form-control:not(.json-path-select)'));
                    let valueContainer = $(ruleElement.find('div.rule-value-container'));

                    //Apply styling
                    $('.rule-value-container input:first').css({
                        'width': '70px',
                        'text-align': 'center'
                    });

                    if (typeof (filterTypeChoices[rule.id]) === 'undefined') {
                        return;
                    }

                    rule.data = {
                        jsonPath: {
                            path: "",
                            type: "",
                        }
                    };
                    scope.ruleJsonPathBindings.set(rule.id, rule);

                    // Get the name of the event type (filter) which is currently selected for this rule
                    let selectedFilter = filterSelect.find('option:selected').text();

                    // Prepare the jsonPath input with all needed bindings
                    var jsonPathInput = "<json-path-input ng-model=\"ruleJsonPathBindings.get(\'" + rule.id + "\').data.jsonPath\"" +
                        "json-path-list=\"eventComponentMapping.get(\'" + selectedFilter + "\').operator.dataModel.jsonPathsToLeafNodes\"" +
                        "number-of-needed-wildcards=\"0\"" +
                        "</json-path-input>";

                    // Compile the html so that angular can apply the bindings properly
                    var compiledJsonPathInput = $compile(jsonPathInput)(scope);

                    // Add a the jsonPath input within a new container to the rule element
                    var componentFieldContainer = $('<div class="btn-group json-path-input-container" style="margin-left: 5px">').append(compiledJsonPathInput);
                    if (filterContainer.has("json-path-input").length) {
                        // Remove old json-path-inputs
                        filterContainer.find(".json-path-input-container").remove();
                    }
                    if (filterSelect.val() != '-1') {
                        filterContainer.append(componentFieldContainer);
                        scope.$apply();
                    }
                    console.log("filter select:", filterSelect.val());

                    if ((filterSelect.val() === '-1')) {
                        delete aggregationWindowOptions[rule.id];
                        return;
                    }


                    //Get filter type
                    let filterType = filterTypeChoices[rule.id];
                    if (filterType === CONDITIONS_PICKER_SOURCE_FILTER) {
                        return;
                    }


                    if (typeof (aggregationWindowOptions[rule.id]) !== 'undefined' &&
                        valueContainer.find('button.dropdown-toggle').length) {
                        return;
                    }

                    aggregationWindowOptions[rule.id] = {
                        'type': null,
                        'size': -1,
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

                    let windowSizeInput = $('<input type="number" placeholder="Size" min="0" class="form-control">').css({
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

                    let lengthWindowSizeUnitDisplay = $('<span>').html('events').css({
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
                'allow_empty': true,
                'filters': conditionsPickerFilters,
                'plugins': pluginsObject
            });
        }

        function parseConditions(conditionsObject) {
            if (conditionsObject == null) {
                return null;
            }

            if (typeof (conditionsObject['rules']) !== 'undefined') {
                //Object is a conditions group
                let conditionsArray = conditionsObject.rules;

                if (conditionsArray.length < 1) {
                    return null;
                }

                let parsedConditions = [];

                for (let i = 0; i < conditionsArray.length; i++) {
                    let parsed = parseConditions(conditionsArray[i]);
                    parsedConditions.push(parsed)
                }

                return "(" + parsedConditions.join(" " + conditionsObject.condition + " ") + ")";
            }

            var value = conditionsObject.value;
            // Add "" for string types
            if (conditionsObject.data.jsonPath.type === "string" || conditionsObject.data.jsonPath.type === "binary" ) {
                value = "\"" + conditionsObject.value + "\"";
            }

            //Object is a single condition
            let conditionType = conditionsObject.conditionType;

            let operatorIndex = CONDITIONS_PICKER_OPERATORS_PLAIN.indexOf(conditionsObject.operator);
            let operator = CONDITION_PICKER_OPERATORS[operatorIndex].operator;

            if (conditionType === CONDITIONS_PICKER_SOURCE_FILTER) {
                //Condition is a single event, get event alias
                let eventAlias = getAliasForPatternEvent(conditionsObject.id);

                return "(" + eventAlias + ".`" + conditionsObject.data.jsonPath.path.substring(1) + "`" + operator + " " + value + ")";
            }

            //Condition is an aggregation
            let sourceReference = conditionsObject.id.replace(conditionType.prefix, "");
            let aggregationFunction = conditionType.func;
            let windowOptions = conditionsObject.aggregationWindow;

            let windowUnit = "";

            if (windowOptions.type === 'time') {
                windowUnit = " " + CONDITIONS_PICKER_WINDOW_UNITS[windowOptions.unit].querySymbol;
            }

            return "((SELECT " + aggregationFunction + "(`" + conditionsObject.data.jsonPath.path.substring(1) +
                "`) FROM " + sourceReference +
                ".win:" + windowOptions.type + "(" + windowOptions.size + windowUnit + ")) " +
                operator + " " + value + ")";
        }

        function initErrorArea() {
            errorArea.children("button").on('click', function () {
                errorArea.hide();
            });

            clearErrorArea();
        }

        function pushToErrorArea(errorMessage) {
            //Push error
            errorArea.children('ul').append($('<li>').html(errorMessage));

            //Show area if hidden
            if (!errorArea.is(':visible')) {
                errorArea.slideDown();
            }
        }

        function clearErrorArea() {
            //Hide area
            errorArea.hide();

            //Remove all error items
            errorArea.children('ul').children().remove();
        }

        function getQueryString() {

            function parsePattern(elementList) {
                if (elementList.length < 1) {
                    return "";
                } else if (elementList.length === 1) {
                    let element = $(elementList[0]);
                    let elementId = element.data(KEY_ID);
                    let detailsPage = findElementDetails(elementId);
                    let elementType = getElementType(element);

                    return elementType.querify(element, detailsPage);
                }

                let lowestPrecedence = Number.MAX_SAFE_INTEGER;

                elementList.filter('.' + CLASS_OPERATOR).each(function () {
                    let precedence = $(this).data(KEY_OPERATOR_PRECEDENCE);

                    if (precedence < lowestPrecedence) {
                        lowestPrecedence = precedence;
                    }
                });

                //Now split the pattern at the operators with lowest precedence
                let previousOperator = null;
                let currentSplit = [];

                //Stores the result of the pattern that has already been processed
                let incrementalPattern = "";

                elementList.each(function (index) {
                        let element = $(this);
                        if (element.hasClass(CLASS_COMPONENT)) {
                            currentSplit.push(this);

                            //Return if this component is not the last pattern element
                            if (index < (elementList.length - 1)) {
                                return;
                            }
                        }

                        //Current element is either an operator or the last pattern element
                        let precedence = $(this).data(KEY_OPERATOR_PRECEDENCE);

                        if (element.hasClass(CLASS_OPERATOR) && (precedence > lowestPrecedence)) {
                            currentSplit.push(this);
                            return;
                        }

                        if (previousOperator == null) {
                            incrementalPattern = parsePattern($(currentSplit));
                        } else {
                            let operatorId = previousOperator.data(KEY_ID);
                            let operatorType = getElementType(previousOperator);
                            let detailsPage = findElementDetails(operatorId);

                            let rightSubPattern = parsePattern($(currentSplit));

                            incrementalPattern = operatorType.querify(previousOperator, detailsPage, incrementalPattern, rightSubPattern);
                        }

                        previousOperator = element;
                        currentSplit = [];
                    }
                );

                return incrementalPattern;
            }

            //Reset error area
            clearErrorArea();

            //Remembers if at least one error occurred
            let hasError = false;

            //Get current pattern elements
            let patternElements = patternContainer.children();

            //Validate pattern elements
            if (patternElements.length < 1) {
                pushToErrorArea("The pattern must not be empty.");
                hasError = true;
            } else if (patternElements.hasClass(CLASS_STUB)) {
                pushToErrorArea("All stubs within the pattern need to be replaced");
                hasError = true;
            }

            let conditionsObject = conditionsPicker.queryBuilder('getRules', {
                'skip_empty': true
            });

            if (conditionsObject == null) {
                //Validation error in conditions picker
                pushToErrorArea("There are invalid conditions specified");
                hasError = true;
            }

            //Check if an error occurred and abort in this case
            if (hasError) {
                return null;
            }

            let patternString = parsePattern(patternElements);
            let conditionsString = parseConditions(conditionsObject);

            let queryString = "SELECT * FROM pattern [every(" + patternString + ")]";

            if (conditionsString != null) {
                queryString += " WHERE " + conditionsString;
            }

            return queryString;
        }

        /**
         * Resets the whole CEP query editor to its initial state.
         */
        function reset() {
            //Get all pattern elements
            let patternElements = patternContainer.children();

            //Iterate over all pattern elements
            patternElements.each(function () {
                //Get current element
                let element = $(this);

                //Update conditions picker if element is a source component
                if (element.data(KEY_ELEMENT_KEY) === SOURCE_COMPONENT_TYPE_PROTOTYPE.key) {
                    removeSourceFromConditionsPicker(element);
                }

                //Remove element
                element.remove();
            });

            //Reset id counters
            categoryIdCounter = 0;
            elementIdCounter = 0;

            //Hide element details if displayed
            hideElementDetails();

            //Empty details container
            detailsContainer.empty();

            //Reset conditions picker
            conditionsPicker.queryBuilder('reset');
        }


        (function () {
            initOperators();
            initSourceComponents(scope.componentList);
            initAdditionalComponents();

            initPattern();
            initDeletionArea();
            initErrorArea();

            initOptions();
        })();

        /*
        Defines the exposed API.
         */
        scope.api = {
            'getQueryString': getQueryString,
            'reset': reset
        };
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
            '<div class="' + CLASS_ERROR_AREA + ' alert alert-danger alert-dismissible"><button type="button" class="close"><span aria-hidden="true">&times;</span></button><p>Errors occurred:</p><ul></ul></div>' +
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
            api: '=api',
            componentList: '=componentList'
        }
    };
}

]);