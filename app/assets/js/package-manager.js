/**
 * Created by igi on 18/07/15.
 */
define(function () {


    /**
     * Package manager
     * @param config
     * @constructor
     */
    function PackageManager(config) {
        this.config = config;
        this.packages = transform(config);
        this.requireConfig = requireConfig(this.packages);
        require.config(this.requireConfig);
        console.log(this);
    }

    /**
     * Return packagename
     * @param name
     * @returns {T|*}
     */
    PackageManager.prototype.getPackage = function (name) {
        return this.packages.filter(function (item) {
            return item.name === name;
        }).shift();
    };

    /**
     * Create require config
     * @param pckgs
     */
    function requireConfig(pckgs) {
        var vendors = pckgs.filter(function (item) {
           return item.isVendor;
        });
        var config = {
            paths: {},
            shim: {}
        };
        pckgs.forEach(function (item) {
            config.paths[item.name] = item.filePath + (item.file ? item.file : item.name);
        });
        vendors.forEach(function (item) {
            var obj = config.shim[item.name] = {};
            if (item.varName) {
                obj.init = function() {
                    return window[item.varName];
                }
            }
        });
        return config;
    }

    /**
     * Extend package
     * @param destination
     * @param source
     */
    function extendPackage(destination, source) {
        var skip = ['packages'];
        Object.keys(source).forEach(function (key) {
            if (skip.indexOf(key) === -1)  {
                if (!destination.hasOwnProperty(key)) {
                    destination[key] = source[key];
                }
            }
            destination.parent = source;
        });
    }
    /**
     * Transform configuration
     * @param config
     */
    function transform(config, parent) {
        var packages = [];
        config.forEach(function (pckg) {
            var havePackages = Array.isArray(pckg.packages) && pckg.packages.length > 0,
                pckgList = pckg.packages;

            if (!!parent) {
                extendPackage(pckg, parent);
            }

            delete pckg.packages;

            if (havePackages) {
                packages = packages.concat(transform(pckgList, pckg));
            }

            if (havePackages && pckg.isVendor) {
                return false;
            }
            packages.push(pckg);
        });
        return packages;
    }

    return PackageManager
});