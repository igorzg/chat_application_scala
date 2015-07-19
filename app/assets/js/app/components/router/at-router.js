/**
 * Created by igi on 18/07/15.
 */

(function () {

    var module = ngAngular.define('at-router');
    /**
     * Custom modules
     */
    module.controller('atRouteController', [
        "$scope",
        function (scope) {
            scope.component = {
                name: 'at-login',
                locals: {}
            };
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
