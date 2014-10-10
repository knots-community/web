/**
 * Created by anton on 10/9/14.
 */

angular.module('Knots')

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/', {templateUrl: '/assets/javascripts/partials/home.html', controller: 'LoginCtrl'})
            .when('/login', {templateUrl: '/assets/javascripts/partials/home.html', controller: 'HomeCtrl'})
            .when('/signup', {templateUrl: '/assets/javascripts/partials/signup.html', controller: 'SignUpCtrl'})
            .otherwise({templateUrl: '/assets/javascripts/partials/notFound.html'});
        //.when('/users', {templateUrl:'/assets/templates/user/users.html', controller:controllers.UserCtrl})
        //.when('/users/:id', {templateUrl:'/assets/templates/user/editUser.html', controller:controllers.UserCtrl});
    }])

    .controller('LoginCtrl', ['$scope', '$location', '$modal', '$log', function ($scope, $location, $modal, $log) {
        $scope.$log = $log;
        $scope.message = 'Hello World!';
        $log.log("Inside LoginCtrl");
        $scope.credentials = {};
        $scope.login = function (credentials) {
            userService.loginUser(credentials).then(function (/*user*/) {
                $location.path('/dashboard');
            });
        };

        $scope.openLogin = function() {
            $log.log("⁄!!!!!!!!!!!");
            $modal.open({
                templateUrl: '/assets/javascripts/partials/login.html',
                controller: 'LoginCtrl'
            });
        };

    }])

    .controller('SignUpCtrl', ['$scope', '$location', '$userService', function ($scope, $location, userService) {
        $scope.credentials = {};

        $scope.signUp = function (credentials) {
            userService.signUp(credentials).then(function (/*user*/) {
                $location.path('/dashboard');
            });
        };
    }])

/** Controls the index page */
    .controller('HomeCtrl', ['$scope', '$rootScope', function ($scope, $rootScope) {
        $rootScope.pageTitle = 'Welcome';
    }])

/** Controls the header */
    .controller('HeaderCtrl', ['$scope', 'userService', '$location', function ($scope, userService, $location) {
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

/** Controls the footer */
    .controller('FooterCtrl', [function (/*$scope*/) {
    }]);


