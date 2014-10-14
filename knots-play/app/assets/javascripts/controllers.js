/**
 * Created by anton on 10/9/14.
 */

angular.module('Knots')

    .config(function ($routeProvider, $locationProvider) {
        $routeProvider
            .when('/', {templateUrl: '/assets/javascripts/partials/home.html', controller: 'LoginCtrl'})
            .when('/login', {templateUrl: '/assets/javascripts/partials/home.html', controller: 'HomeCtrl'})
            .when('/signup', {templateUrl: '/assets/javascripts/partials/home.html', controller: 'LoginCtrl'})
            .when('/dashboard', {templateUrl: '/assets/javascripts/partials/home.html', controller: 'DashboardCtrl'})
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
            $scope.authDialog = $modal.open({
                templateUrl: '/assets/javascripts/partials/login.html',
                controller: 'LoginCtrl'
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
    .controller('HeaderCtrl', ['$scope', 'userService', '$location', function ($scope, userService, $location) {
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
    }])

    .controller('RegistrationDialogCtrl', function($scope, $modalInstance, userService) {
        $scope.signUp = function(credentials) {
            $modalInstance.close();
            userService.signup(credentials).then(function (/*user*/) {
                $log.log("Registration success");
                $location.path('/dashboard');
            });

        };
    })


    .controller('DashboardCtrl', function ($scope, $location, userService) {

    })

    .controller('BookingCtrl', function($scope, $location, bookingService) {

    })

/** Controls the footer */
    .controller('FooterCtrl', [function (/*$scope*/) {
    }]);


