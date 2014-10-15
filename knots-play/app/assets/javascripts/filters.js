/**
 * Created by anton on 10/13/14.
 */

angular.module("Knots")
    .filter("availableTimeSlots", function($log) {
        return function(items, masseur) {
            var resultArr = [];
            angular.forEach(items, function(item) {
                if(item.masseur == masseur.name) {
                    resultArr = item.times;
                }
            });
            $log.log("Passed masseur name = " + masseur.name);
            $log.log("Result: " + resultArr);
            return resultArr;
        };
    });