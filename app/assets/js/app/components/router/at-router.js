/**
 * Created by igi on 18/07/15.
 */

(function () {

    var module = ngAngular.define('at-router');
    /**
     * Custom modules
     */
    module.controller('atRouteController', [
        "$scope", "atTransport",
        function (scope, atTransport) {

            atTransport.isUserLoggedIn().then(null, null, function (data) {
                if (data.isLoggedIn) {
                    scope.component = {
                        name: 'at-chat',
                        locals: {
                            nick: data.name
                        }
                    };
                } else {
                    scope.component = {
                        name: 'at-login',
                        locals: {}
                    };
                }
            }).catch(function (error) {
                scope.component = {
                    name: 'at-login',
                    locals: {}
                };
            })
        }
    ]);
}());
