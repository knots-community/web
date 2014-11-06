/**
 * Created by anton on 10/9/14.
 */

angular.module('Knots')
    .config(function ($routeProvider, userResolve) {
        $routeProvider
            .when('/', {templateUrl: '/assets/javascripts/partials/home.html', controller: 'HomeCtrl'})
            .when('/login', {templateUrl: '/assets/javascripts/partials/login.html', controller: 'LoginCtrl'})
            .when('/signup/:companyKey', {templateUrl: '/assets/javascripts/partials/signup.html', controller: 'SignUpCtrl'})
            .when('/dashboard', {templateUrl: '/assets/javascripts/partials/reservation.html', controller: 'ReservationCtrl', resolve: userResolve})
            .when('/confirmation', { templateUrl: '/assets/javascripts/partials/confirmation.html', controller: 'ConfirmationCtrl', resolve: userResolve})
            .otherwise({templateUrl: '/assets/javascripts/partials/home.html'});
        //.when('/users', {templateUrl:'/assets/templates/user/users.html', controller:controllers.UserCtrl})
        //.when('/users/:id', {templateUrl:'/assets/templates/user/editUser.html', controller:controllers.UserCtrl});
    })
    .config(function ($locationProvider) {
        return $locationProvider.html5Mode(true).hashPrefix("!");
    })

/** Controls the index page */
    .controller('HomeCtrl', ['$scope', '$rootScope', function ($scope, $rootScope) {
        $rootScope.pageTitle = 'Welcome';
    }])

/** Controls the header */
    .controller('HeaderCtrl', function ($scope, $rootScope, userService, $location, $modal) {
        $scope.credentials = {};
        $scope.$watch(function () {
                return userService.getUser();
            },
            function (user) {
                $scope.user = user;
            });

        $scope.logout = function () {
            userService.logout();
            $scope.user = undefined;
            $location.path('/');
        };

        $rootScope.$on('$routeChangeError', function (/*e, next, current*/) {
            $location.path('/');
        });

        $scope.register = function () {
            $modal.open({
                templateUrl: '/assets/javascripts/partials/signup.html',
                controller: 'RegistrationDialogCtrl'
            });
        };
    })

    .controller('SignUpCtrl', function ($scope, userService, $location, $log, $routeParams) {
        $scope.companyRequest = {};
        $scope.companyRequest.companyKey = $routeParams.companyKey;
        $scope.companyInfo = {};

        userService.clear();

        $log.info("REQUEST FOR COMPANY");
        userService.queryCompanyInfo($scope.companyRequest).then(function (response) {
            $log.info("GOT THE COMPANY!");
            $scope.companyInfo = response.data.company;
            $log.info(response);
        });

        $scope.signUp = function (credentials) {
            credentials.companyName = $scope.companyInfo.name;
            userService.signup(credentials).then(function (/*user*/) {
                $log.log("Registration success");
                $location.path('/dashboard');
            });
        };
    })

    .controller('LoginCtrl', function ($scope, userService, $log, $location, bookingService) {
        userService.clear();
        $scope.login = function (credentials) {
            userService.loginUser(credentials).then(function (/*user*/) {
                $log.log("Login success");
                $location.path('/dashboard');
                bookingService.queryTimeSlots();
            });
        };
    })

    .controller('ReservationCtrl', function ($scope, $log, userService, bookingService) {
        $scope.user = {};
        $scope.events = [];
        $scope.dates = [];
        $scope.selectedDate = "";
        $scope.selectedMasseur = {};
        $scope.selectedTime = "";
        $scope.company = userService.getCompanyInfo();

        if(!angular.isDefined($scope.company)) {
            userService.queryCompanyInfo().onsucc
        }

        (function () {

            bookingService.queryTimeSlots().then(function () {
                $scope.events = bookingService.getTimeSlots();
                $scope.user = userService.getUser();
                angular.forEach($scope.events, function (value, key) {
                    $scope.dates.push(value.date);
                });
            });
        })();

        $scope.makeReservation = function () {
            var reservation = {};
            angular.forEach($scope.events, function (e) {
                if (e.date == $scope.selectedDate) {
                }
                angular.forEach(e.masseurSlots, function (ms) {
                    if (ms.masseurInfo.name == $scope.selectedMasseur) {
                        reservation.masseurId = ms.masseurInfo.masseurId;
                    }
                });
            });
            reservation.slotId = $scope.selectedTime.id;
            $log.error(reservation);
            bookingService.makeReservation(reservation);
        };

    })

    .controller('ConfirmationCtrl', function ($scope, $location, bookingService) {
        $scope.bookingInfo = bookingService.getBookingInfo();
    })

/** Controls the footer */
    .controller('FooterCtrl', [function (/*$scope*/) {
    }]);



