/**
 * Created by igi on 18/07/15.
 */

(function () {

    var module = ngAngular.define('ng-router');
    /**
     * Custom modules
     */
    module.controller('appController', [
        "$scope",
        function (scope) {
            scope.data = 1;

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
