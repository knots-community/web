/**
 * Created by anton on 10/9/14.
 */
angular.module('Knots')
    .factory('userService', function ($http, $q, $cookies, $log, playRoutes, $location) {
        var user, token = $cookies['XSRF-TOKEN'];
        var companyInfo = "";

        /* If the token is assigned, check that the token is still valid on the server */
        if (token) {
            $log.info('Restoring user from cookie...');
            playRoutes.controllers.UsersController.authUser().get()
                .success(function (data) {
                    $log.info('Welcome back, ' + data.name);
                    user = data;
                    $log.log(data);
                    $location.path('/dashboard');
                })
                .error(function () {
                    $log.info('Token no longer valid, please log in.');
                    token = undefined;
                    delete $cookies['XSRF-TOKEN'];
                    return $q.reject("Token invalid");
                });
        }

        return {
            loginUser: function (credentials) {
                return playRoutes.controllers.ApplicationController.signin().post(credentials).then(function (response) {
                    // return promise so we can chain easily
                    token = response.data.token;
                    return playRoutes.controllers.UsersController.authUser().get();
                }).then(function (response) {
                    user = response.data;
                    return user;
                });
            },
            signup: function (credentials) {
                return playRoutes.controllers.ApplicationController.signup().post(credentials).then(function (response) {
                    token = response.data.token;
                    return playRoutes.controllers.UsersController.authUser().get();
                }).then(function (response) {
                    user = response.data;
                    return user;
                });
            },
            logout: function () {
                // Logout on server in a real app
                delete $cookies['XSRF-TOKEN'];
                token = undefined;
                user = undefined;
//                return playRoutes.controllers.ApplicationController.logout().post().then(function () {
                $log.info("Good bye ");
                $location.path('/');
//                });
            },
            queryCompanyInfo: function (companyKey) {
                $log.info(companyKey);
                return playRoutes.controllers.ApplicationController.getCompanyInfo().post(companyKey)
                    .success(function (result) {
                        companyInfo = result.company;
                    })
                    .error(function (err) {
                        $log.error(err);
                    });
            },
            getUser: function () {
                return user;
            },
            getCompanyInfo: function () {
                return companyInfo;
            }
        };
    })
/**
 * Add this object to a route definition to only allow resolving the route if the user is
 * logged in. This also adds the contents of the objects as a dependency of the controller.
 */
    .constant('userResolve', {
        user: ['$q', 'userService', function ($q, userService) {
            var deferred = $q.defer();
            var user = userService.getUser();
            if (user) {
                deferred.resolve(user);
            } else {
                deferred.reject();
            }
            return deferred.promise;
        }]
    })


    .factory('bookingService', function ($http, $q, $cookies, $log, playRoutes, userService, $location) {
        var bookingInfo = {};
        var timeSlots = {};

        return {
            makeReservation: function (reservation) {
                return playRoutes.controllers.BookingController.performBooking().post(reservation)
                    .success(function (result) {
                        bookingInfo = result.bookingInfo;
                        $location.path('/confirmation');
                    })
                    .error(function (error) {
                        $log.error(error);
                    })
                    .then(function (response) {
                    });
            },

            queryTimeSlots: function () {
                return playRoutes.controllers.BookingController.timeSlots().get().then(function (response) {
                    timeSlots = response.data.slots.events;
                    $log.info(timeSlots);
                    $location.path('/dashboard');
                });
            },

            getTimeSlots: function () {
                return timeSlots;
            },

            getBookingInfo: function () {
                return bookingInfo;
            }
        };
    });