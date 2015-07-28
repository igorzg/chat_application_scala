/**
 * Created by igi on 28/07/15.
 */
/**
 * Created by igi on 18/07/15.
 */

(function () {

    var module = ngAngular.define('at-translate');

    /**
     * Translation logic should go here
     */
    module.filter('atTranslate', [
        function atTranslateFilterFactory() {

            return function atTranslate(value) {
                return value;
            };
        }
    ])
}());
