/**
 * Created by igi on 26/07/15.
 */
(function () {
    var module = ngAngular.define('at-transport');
    /**
     * Custom modules
     */
    module.service('atTransport', [
        "$q", "atLocalEventHandler", "$rootScope",
        function ($q, events, $rootScope) {
            var atTransportSocket = new WebSocket(AT_TRANSPORT_WS_URL),
                isConnected = false,
                lazyConnectEvents = [];

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
                console.log('transport connected', lazyConnectEvents);
                while (lazyConnectEvents.length) {
                    event = lazyConnectEvents.shift();
                    console.log('sending lazy', event);
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
             * Crete user event
             * @param name
             * @returns {*}
             */
            function createEvent(name) {
                return function processEvent(params) {
                    var deferred = $q.defer(),
                        id = name + "_" + btoa(new Date().getTime() + "_" + Math.random());

                    events.trigger("send", {
                        event: name,
                        id: id,
                        params: params || {}
                    });

                    events.add("receive", function (response) {
                        if (response.event === name && response.id === id) {
                            deferred.notify(angular.copy(response.data));
                            $rootScope.$apply();
                        }
                    });

                    return deferred.promise;
                }
            }

            /**
             * Return api
             */
            return {
                isUserLoggedIn: createEvent("isUserLoggedIn")
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