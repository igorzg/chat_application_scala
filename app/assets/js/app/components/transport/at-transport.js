/**
 * Created by igi on 26/07/15.
 */
(function () {
    var module = ngAngular.define('at-transport');

    /**
     * Custom modules
     */
    module.service('atTransport', [
        "$q", "atLocalEventHandler", "$rootScope", "$timeout",
        function ($q, events, $rootScope, $timeout) {
            var atTransportSocket = new WebSocket(AT_TRANSPORT_WS_URL),
                isConnected = false,
                lazyConnectEvents = [];

            /**
             * On receive fire event
             * @param event
             */
            atTransportSocket.onclose = function atTransportSocketClose(event) {
                console.log('close', event);
                //events.trigger("close", JSON.parse(event.data));
            };

            /**
             * On receive fire event
             * @param event
             */
            atTransportSocket.onerror = function atTransportSocketError(event) {
                events.trigger("error", JSON.parse(event.data));
            };
            /**
             * On receive fire event
             * @param event
             */
            atTransportSocket.onmessage = function atTransportSocketMessage(event) {
                events.trigger("receive", JSON.parse(event.data));
            };
            /**
             * On open process lazy events
             */
            atTransportSocket.onopen = function atTransportSocketOpen() {
                var event;
                isConnected = true;
                while (lazyConnectEvents.length) {
                    event = lazyConnectEvents.shift();
                    atTransportSocket.send(JSON.stringify(event));
                }
            };
            /**
             * Register send event
             */
            events.add("send", function (event) {
                if (isConnected) {
                    atTransportSocket.send(JSON.stringify(event));
                } else {
                    lazyConnectEvents.push(event);
                }
            });

            /**
             * On unload terminate
             */
            angular.element(window).bind('unload', function () {
                events.trigger("send", {
                    name: "unload",
                    id: "unload",
                    terminate: false,
                    session_id: SESSION_ID,
                    params: {}
                });
            });

            /**
             * Crete user event
             * @param name
             * @returns {*}
             */
            function createEvent(name) {
                return function processEvent(params, fireEvents) {
                    var deferred = $q.defer(),
                        id = name + "_" + btoa(new Date().getTime() + "_" + Math.random()),
                        timeout = $timeout(function () {
                            deferred.reject("no_response");
                            events.remove("receive", receiveEvent);
                        }, 5000);

                    events.trigger("send", {
                        name: name,
                        id: id,
                        terminate: false,
                        session_id: SESSION_ID,
                        params: params || {}
                    });

                    console.log("client_send", {
                        name: name,
                        id: id,
                        session_id: SESSION_ID,
                        params: params || {}
                    });

                    events.add("receive", receiveEvent);

                    /**
                     * Terminate event
                     */
                    deferred.promise.terminate = function terminate() {
                        events.trigger("send", {
                            name: name,
                            id: id,
                            terminate: true,
                            params: {}
                        });
                        deferred.reject("terminated");
                    };

                    return deferred.promise;

                    function receiveEvent(response) {
                        if (response.name === name && response.id === id) {
                            console.log("client_received", response);
                            deferred.notify(angular.copy(response.data));
                            $timeout.cancel(timeout);
                            $rootScope.$apply();
                        }
                    }
                }
            }

            /**
             * Return api
             */
            return {
                isUserLoggedIn: createEvent("isUserLoggedIn"),
                logIn: createEvent("logIn"),
                logOut: createEvent("logOut")
            };
        }
    ]);
    /**
     * Local event handler
     */
    module.factory('atLocalEventHandler', function () {
        return new EventHandler();
    });
    /**
     * Global event handler
     */
    module.service('atGlobalEventHandler', EventHandler);

    /**
     * Event handler
     * @constructor
     */
    function EventHandler() {
        this.events = [];
    }

    /**
     * Remove all events by name
     * @param name
     * @constructor
     */
    EventHandler.prototype.removeAllByName = function EventHandler_removeAllByName(name) {
        this.events.filter(function (item) {
            return item.name === name;
        }).forEach(function (event) {
            this.events.splice(this.events.indexOf(event), 1);
        }.bind(this));
    };
    /**
     * Remove event
     * @param name
     * @param callback
     * @constructor
     */
    EventHandler.prototype.remove = function EventHandler_remove(name, callback) {
        var item = this.events.filter(function (item) {
            return item.name === name && item.callback === callback
        }).shift();
        if (item) {
            this.events.splice(this.events.indexOf(item), 1);
        }
    };

    /**
     * Trigger event
     * @param name
     * @constructor
     */
    EventHandler.prototype.trigger = function EventHandler_trigger(name) {
        var args = Array.prototype.slice.call(arguments, 1);
        this.events.forEach(function (item) {
            if (item.name === name) {
                item.callback.apply({}, args);
            }
        });
    };
    /**
     * Add event
     * @param name
     * @param callback
     * @constructor
     */
    EventHandler.prototype.add = function EventHandler_add(name, callback) {
        this.events.push({
            name: name,
            callback: callback
        });
    };

}());