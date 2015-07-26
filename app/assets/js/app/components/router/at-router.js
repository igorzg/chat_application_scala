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

            scope.component = {
                name: 'at-login',
                locals: {}
            };

            atTransport.isUserLoggedIn().then(null, null, function (data) {
                if (data.isLoggedIn) {
                    scope.component = {
                        name: 'at-user',
                        locals: {}
                    };
                } else {
                    scope.component = {
                        name: 'at-login',
                        locals: {}
                    };
                }
            }).catch(function (error) {

            })
        }
    ]);

    module.run([
        '$q',
        function ($q) {
            $q.resolve('test').then(function(data) {
                console.log(data);
            });
        }
    ])
}());
