/**
 * Created by igi on 19/07/15.
 */
(function () {
    var module = ngAngular.define('at-loader');

    module.directive('atLoader', [
        '$compile', '$http', '$controller',
        function ($compile, $http, $controller) {
            return {
                restrict: 'A',
                scope: {
                    atLoader: '='
                },
                link: function (scope, element, attrs, ctrl) {
                    var cScope;
                    /**
                     * Package watcher
                     */
                    scope.$watch('atLoader', function (nVal, oVal) {
                        if (nVal !== oVal) {
                            cScope = compile(nVal, scope, cScope, element);
                        }
                    });

                    /**
                     * Compile first view
                     */
                    cScope = compile(scope.atLoader, scope, cScope, element);
                }
            };


            /**
             * Compile scope
             * @param name
             * @param scope
             * @param cScope
             * @param element
             */
            function compile(config, scope, cScope, element) {
                var name = config.name,
                    templateUrl = ngAngular.getTemplate(name),
                    controller = ngAngular.getController(name);

                ngAngular.require(name, function atLoaderLoad() {
                    if (templateUrl) {
                        $http.get(templateUrl, {cache: true}).then(function getTemplate(template) {
                            element.html(template.data);
                            createNewScope(function (newScope) {
                                var link, ctrl;
                                config.locals.$scope = newScope;
                                link = $compile(element.contents());
                                ctrl = $controller(controller, config.locals);
                                element.data('$ngControllerController', ctrl);
                                element.children().data('$ngControllerController', ctrl);
                                link(newScope);
                            });
                        });
                    } else {
                        createNewScope();
                    }
                });

                return cScope;


                function createNewScope(callback) {
                    if (cScope) {
                        element.empty();
                        cScope.destroy();
                    }
                    cScope = scope.$new();
                    if (angular.isFunction(callback)) {
                        callback(cScope);
                    }
                }


            }
        }
    ]);

}());