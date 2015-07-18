/**
 * Created by igi on 15/07/15.
 */
define('ng-bootstrap', ['angularjs'], function (angular) {
    var APP_NAME = 'amd',
        isBOOTSTRAPED = false,
        app,
        _$provide,
        _$compile,
        _$filter,
        _$controller,
        _$injector,
        _vendors = {},
        _packageManager,
        api;

    app = angular.module(APP_NAME, []);


    app.config(['$provide', '$compileProvider', '$filterProvider', '$controllerProvider',
        function ($provide, $compileProvider, $filterProvider, $controllerProvider) {
            _$provide = $provide;
            _$compile = $compileProvider;
            _$filter = $filterProvider;
            _$controller = $controllerProvider;
        }
    ]);

    app.run(['$injector',
        function ($injector) {
            _$injector = $injector;
        }
    ]);



    api = {
        /**
         * Defome ,pdiöe
         * @param name
         * @returns {Module}
         */
        define: function define(name) {
            return new Module(name);
        },
        /**
         * Bootstrap an application once
         */
        bootstrap: function bootstrap(packageManager) {
            _packageManager = packageManager;
            var pckg = api.getPackage('ng-bootstrap');
            require(pckg.deps, function () {
                if (!isBOOTSTRAPED) {
                    angular.bootstrap(document, [APP_NAME]);
                    isBOOTSTRAPED = true;
                } else {
                    console.log('APPLICATION IS BOOTSTRAPED');
                }
            });
        },
        /**
         * Define package
         * @param name
         * @param callback
         */
        load: function load(name, callback) {
            var pckg = api.getPackage(name),
                args = [];

            args.push(pckg.name);

            if (Array.isArray(pckg.deps)) {
                args.push(pckg.deps);
            }

            args.push(function loadAll() {
                Array.prototype.slice.call(arguments).forEach(function (item, index) {
                    var name;
                    if (!!item) {
                        name = pckg.deps[index];
                        _vendors[name] = item;
                    }
                });
                callback();
            });

            window.define.apply(window.define, args);
        },
        /**
         * Get package
         */
        getPackage: function getPackage(name) {
            var pckg = _packageManager.getPackage(name);
            if (!pckg) {
                throw new Error('Package {0} is not registered in system'.replace('{0}', name))
            }
            return pckg;
        },
        /**
         * Get template
         */
        getTemplate: function getTemplate(name) {
            var pckg = api.getPackage(name);
            return !!pckg && pckg.template ? pckg.filePath + pckg.template : false;
        },
        /**
         * Return vendor instance
         * @param name
         */
        getVendor: function getVendor(name) {
            return _vendors[name];
        }
    };

    /**
     * Module definition
     * @param name
     * @constructor
     */
    function Module(name) {
        this.name = name;
        this.isLoaded = false;
        this.promise = null;
    }
    /**
     * Module load
     * @returns {Module}
     * @constructor
     */
    Module.prototype.load = function Module_load(callback) {
        var self = this;
        if (!this.promise) {
            this.promise = new Promise(function (resolve) {
                api.load(self.name, function Module_require_load() {
                    callback();
                    resolve();
                }.bind(this));
            });
        } else {
            this.promise = this.promise.then(callback);
        }
    };
    /**
     * Module controller
     * @returns {Module}
     * @constructor
     */
    Module.prototype.controller = function Module_controller() {
        var args = Array.prototype.slice.call(arguments);
        this.load(function Module_controller_load() {
            if (isBOOTSTRAPED) {
                _$controller.register.apply(_$controller, args);
            } else {
                app.controller.apply(app.controller, args);
            }
        });
        return this;
    };

    /**
     * Register filter
     * @returns {Module}
     * @constructor
     */
    Module.prototype.filter = function Module_filter() {
        var args = Array.prototype.slice.call(arguments);
        this.load(function Module_filter_load() {
            if (isBOOTSTRAPED) {
                _$filter.register.apply(_$controller, args);
            } else {
                app.filter.apply(app, args);
            }
        });
        return this;
    };
    /**
     * Register directive
     * @returns {Module}
     * @constructor
     */
    Module.prototype.directive = function Module_directive() {
        var args = Array.prototype.slice.call(arguments);
        this.load(function Module_directive_load() {
            if (isBOOTSTRAPED) {
                _$compile.directive.apply(_$controller, args);
            } else {
                app.directive.apply(app, args);
            }
        });
        return this;
    };

    /**
     * Register factory
     * @returns {Module}
     * @constructor
     */
    Module.prototype.factory = function Module_factory() {
        var args = Array.prototype.slice.call(arguments);
        this.load(function Module_factory_load() {
            if (isBOOTSTRAPED) {
                _$provide.factory.apply(_$provide, args);
            } else {
                app.factory.apply(app, args);
            }
        });
        return this;
    };
    /**
     * Register service
     * @returns {Module}
     * @constructor
     */
    Module.prototype.service = function Module_service() {
        var args = Array.prototype.slice.call(arguments);
        this.load(function Module_service_load() {
            if (isBOOTSTRAPED) {
                _$provide.service.apply(_$provide, args);
            } else {
                app.service.apply(app, args);
            }
        });
        return this;
    };


    /**
     * Register value
     * @returns {Module}
     * @constructor
     */
    Module.prototype.value = function Module_value() {
        var args = Array.prototype.slice.call(arguments);
        this.load(function Module_value_load() {
            if (isBOOTSTRAPED) {
                _$provide.value.apply(_$provide, args);
            } else {
                app.value.apply(app, args);
            }
        });
        return this;
    };



    /**
     * Register decorator
     * @returns {Module}
     * @constructor
     */
    Module.prototype.decorator = function Module_decorator() {
        var args = Array.prototype.slice.call(arguments);
        this.load(function Module_decorator_load() {
            if (isBOOTSTRAPED) {
                _$provide.decorator.apply(_$provide, args);
            } else {
                app.decorator.apply(app, args);
            }
        });
        return this;
    };


    /**
     * Register constant
     * @returns {Module}
     * @constructor
     */
    Module.prototype.constant = function Module_constant() {
        var args = Array.prototype.slice.call(arguments);
        this.load(function Module_constant_load() {
            if (isBOOTSTRAPED) {
                throw new Error('Constant can be registered only before bootstrap');
            } else {
                app.constant.apply(app, args);
            }
        });
        return this;
    };


    /**
     * Register constant
     * @returns {Module}
     * @constructor
     */
    Module.prototype.provider = function Module_provider() {
        var args = Array.prototype.slice.call(arguments);
        this.load(function Module_provider_load() {
            if (isBOOTSTRAPED) {
                throw new Error('Provider can be registered only before bootstrap');
            } else {
                app.provider.apply(app, args);
            }
        });
        return this;
    };

    /**
     * Run
     * @returns {Module}
     * @constructor
     */
    Module.prototype.run = function Module_run() {
        var args = Array.prototype.slice.call(arguments);
        this.load(function Module_run_load() {
            if (isBOOTSTRAPED) {
                _$injector.invoke.apply(_$injector, args);
            } else {
                app.invoke.apply(app, args);
            }
        });
        return this;
    };

    return api;
});