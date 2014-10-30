/**
 * Created by anton on 10/9/14.
 */

angular.module('Knots')

    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {templateUrl: '/assets/javascripts/partials/home.html', controller: 'HomeCtrl'})
            .when('/login', {templateUrl: '/assets/javascripts/partials/home.html', controller: 'HomeCtrl'})
            .when('/signup/:companyKey', {templateUrl: '/assets/javascripts/partials/home.html', controller: 'SignUpCtrl'})
            .when('/dashboard', {templateUrl: '/assets/javascripts/partials/dashboard.html', controller: 'DashboardCtrl'})
            .when('/confirmation', { templateUrl: '/assets/javascripts/partials/confirmation.html', controller: 'ConfirmationCtrl'})
            .otherwise({templateUrl: '/assets/javascripts/partials/home.html'});
        //.when('/users', {templateUrl:'/assets/templates/user/users.html', controller:controllers.UserCtrl})
        //.when('/users/:id', {templateUrl:'/assets/templates/user/editUser.html', controller:controllers.UserCtrl});
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

        $scope.login = function () {
            $modal.open({
                templateUrl: '/assets/javascripts/partials/login.html',
                controller: 'LoginDialogCtrl'
            });
        };
    })

    .controller('SignUpCtrl', function ($scope, userService, $location, $modal, $routeParams) {
        $scope.companyRequest = {};
        $scope.companyRequest.companyKey = $routeParams.companyKey;
        $scope.companyInfo = "";
        userService.queryCompanyInfo($scope.companyRequest).then(function (response) {
            $scope.companyInfo = response.company;

            $modal.open({
                templateUrl: '/assets/javascripts/partials/signup.html',
                controller: 'RegistrationDialogCtrl',
                keyboard: false
            });
        });
    })

    .controller('RegistrationDialogCtrl', function ($scope, $modalInstance, userService, $log, $location) {
        $scope.companyInfo = userService.getCompanyInfo();
        $scope.signUp = function (credentials) {
            $modalInstance.close();
            credentials.companyName = $scope.companyInfo.name;
            userService.signup(credentials).then(function (/*user*/) {
                $log.log("Registration success");
                $location.path('/dashboard');
            });
        };
    })

    .controller('LoginDialogCtrl', function ($scope, $modalInstance, userService, $log, $location, bookingService) {
        $scope.login = function (credentials) {
            $modalInstance.close();
            userService.loginUser(credentials).then(function (/*user*/) {
                $log.log("Login success");
                $location.path('/dashboard');
                bookingService.queryTimeSlots();
            });
        };
    })


    .controller('DashboardCtrl', function ($scope, $log, userService, bookingService) {
        $scope.user = {};
        $scope.events = [];
        $scope.dates = [];
        $scope.reservation = {};
        $scope.selectedDate = "";
        $scope.selectedMasseur = {};
        $scope.selectedTime = "";
        $scope.company = userService.getCompanyInfo();

        (function() {
            bookingService.queryTimeSlots().then(function() {
                $scope.events = bookingService.getTimeSlots();
                $scope.user = userService.getUser();
                angular.forEach($scope.events, function(value, key) {
                    $scope.dates.push(value.date);
                });
            });
        })();

        $scope.makeReservation = function () {
            $scope.reservation.masseurId = $scope.selectedMasseur.masseurId;
            $scope.reservation.slotId = $scope.selectedTime.startTime;
            $log.error("Booking with " + $scope.reservation);
            bookingService.makeReservation($scope.reservation);
        };
    })

    .controller('ConfirmationCtrl', function ($scope, $location, bookingService) {
        $scope.bookingInfo = bookingService.getBookingInfo();
    })

/** Controls the footer */
    .controller('FooterCtrl', [function (/*$scope*/) {
    }]);


