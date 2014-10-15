/**
 * Created by anton on 10/9/14.
 */

angular.module('Knots')

    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {templateUrl: '/assets/javascripts/partials/home.html', controller: 'LoginCtrl'})
            .when('/login', {templateUrl: '/assets/javascripts/partials/home.html', controller: 'HomeCtrl'})
            .when('/signup', {templateUrl: '/assets/javascripts/partials/home.html', controller: 'LoginCtrl'})
            .when('/dashboard', {templateUrl: '/assets/javascripts/partials/dashboard.html', controller: 'DashboardCtrl'})
            .when('/confirmation', { templateUrl: '/assets/javascripts/partials/confirmation.html', controller: 'ConfirmationCtrl'})
            .otherwise({templateUrl: '/assets/javascripts/partials/home.html'});
        //.when('/users', {templateUrl:'/assets/templates/user/users.html', controller:controllers.UserCtrl})
        //.when('/users/:id', {templateUrl:'/assets/templates/user/editUser.html', controller:controllers.UserCtrl});
    })

    .controller('LoginCtrl', function ($scope, $location, $modal, $log, userService) {
        $scope.$log = $log;

        $scope.login = function (credentials) {
            userService.loginUser(credentials).then(function (/*user*/) {
                $log.log("Login success");
                $location.path('/dashboard');
            });
        };

        $scope.openLogin = function () {
            $modal.open({
                templateUrl: '/assets/javascripts/partials/login.html',
                controller: 'LoginDialogCtrl'
            });
            $log.log($scope.authDialog);
        };

        $scope.register = function () {
            $modal.open({
                templateUrl: '/assets/javascripts/partials/signup.html',
                controller: 'RegistrationDialogCtrl'
            });
        };
    })

/** Controls the index page */
    .controller('HomeCtrl', ['$scope', '$rootScope', function ($scope, $rootScope) {
        $rootScope.pageTitle = 'Welcome';
    }])

/** Controls the header */
    .controller('HeaderCtrl', function ($scope, $rootScope, userService, $location) {
        $scope.credentials = {};
        $scope.$watch(function () {
            var user = userService.getUser();
            return user;
        }, function (user) {
            $scope.user = user;
        }, true);

        $scope.logout = function () {
            userService.logout();
            $scope.user = undefined;
            $location.path('/');
        };

        $rootScope.$on('$routeChangeError', function (/*e, next, current*/) {
            $location.path('/');
        });
    })

    .controller('RegistrationDialogCtrl', function($scope, $modalInstance, userService, $log, $location) {
        $scope.signUp = function(credentials) {
            $modalInstance.close();
            userService.signup(credentials).then(function (/*user*/) {
                $log.log("Registration success");
                $location.path('/dashboard');
            });

        };
    })

    .controller('LoginDialogCtrl', function($scope, $modalInstance, userService, $log) {
        $scope.login = function(credentials) {
            $modalInstance.close();
            userService.loginUser(credentials).then(function (/*user*/) {
                $log.log("Login success");
                $location.path('/dashboard');
            });

        };
    })


    .controller('DashboardCtrl', function ($scope, $log, userService, bookingService) {
        $scope.user = userService.getUser();
        $log.log($scope.user);

        $scope.reservation = {};

        $scope.company = { name: "AutoCad Inc", location: "10 rue Duke, Montreal, Québec H3C 2L7"};
        $scope.masseurs = [
            { id: '1', name: "John", sex: "m"},
            { id: '2', name: "Barbara", sex: "f"},
            { id: '3', name: "Jennifer", sex: "f"},
            { id: '4', name: "Amy", sex: "f"}];

        $scope.timeSlotsAvailable = [
            { masseur: "John", times: [ "11:20", "13:30", "16:40"] },
            { masseur : "Barbara", times : [ "9:45", "10:00", "14:30"]},
            { masseur : "Jennifer", times : [ "9:45", "10:00", "15:00", "15:30"] },
            { masseur : "Amy", times : [ "14:00, 14:45", "15:00", "15:30"] }
        ];

        $scope.selectedMasseur = undefined;

        $scope.makeReservation = function(reservation) {
            bookingService.makeReservation(reservation);
        };
    })

    .controller('ConfirmationCtrl', function($scope, $location, bookingService) {
        $scope.bookingInfo = bookingService;
        $scope.company = { name: "AutoCad Inc", location: "10 rue Duke, Montreal, Québec H3C 2L7"};
    })

/** Controls the footer */
    .controller('FooterCtrl', [function (/*$scope*/) {
    }]);


