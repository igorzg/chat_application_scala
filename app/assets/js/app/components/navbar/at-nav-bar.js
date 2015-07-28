/**
 * Created by igi on 28/07/15.
 */

(function () {

    var module = ngAngular.define('at-nav-bar');
    /**
     * Custom modules
     */
    module.controller('atNavBar', [
        "$scope", "atTransport",
        function (scope, atTransport) {

            scope.logout = function logout($event) {
                $event.preventDefault()
                atTransport.logOut()
            }
        }
    ]);
}());
