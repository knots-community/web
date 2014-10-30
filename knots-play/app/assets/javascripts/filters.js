/**
 * Created by anton on 10/13/14.
 */

angular.module("Knots")
    .filter("availableTimeSlots", function ($log) {
        return function (items, selectedDate, masseur) {
            $log.info("Maybe filterin ya time slots?? Date = " + selectedDate + " and masseur = " + masseur);
            var resultArr = [];
            if (masseur !== undefined && selectedDate !== undefined) {
                $log.info("Filtering ya slots on date = " + selectedDate + " and masseur = " + masseur);
                angular.forEach(items, function (item) {
                    if (item.date == selectedDate) {
                        $log.info("Found date!");
                        angular.forEach(item.masseurSlots, function (m) {
                            $log.info("Comparing info on " + m.masseurInfo.name + " and " + masseur);
                            if (m.masseurInfo.name == masseur.name) {
                                $log.info("Found masseur!");
                                angular.forEach(m.slots, function (s) {
                                    resultArr.push(s);
                                });
                            }
                        });
                    }
                });
            }
            $log.info("Filtered + " + resultArr.length + " slots!");
            return resultArr;
        };
    })
    .filter("availableMasseurs", function () {
        return function (items, selectedDate) {
            var resultArr = [];
            if (selectedDate !== undefined) {
                angular.forEach(items, function (item) {
                    if (item.date == selectedDate) {
                        angular.forEach(item.masseurSlots, function (s) {
                            resultArr.push(s.masseurInfo);
                        });
                    }
                });
            }
            return resultArr;
        };
    });