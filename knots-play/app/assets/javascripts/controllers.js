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
        $scope.companyName = "";
        userService.queryCompanyName($scope.companyRequest).then(function (response) {
            $scope.companyName = response.companyName;

            $modal.open({
                templateUrl: '/assets/javascripts/partials/signup.html',
                controller: 'RegistrationDialogCtrl',
                keyboard: false
            });
        });
    })

    .controller('RegistrationDialogCtrl', function ($scope, $modalInstance, userService, $log, $location, bookingService) {
        $scope.companyName = userService.getCompanyName();
        $scope.signUp = function (credentials) {
            $modalInstance.close();
            credentials.companyName = $scope.companyName;
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
        $scope.selectedMasseur = "";

        (function() {
            bookingService.queryTimeSlots().then(function() {
                $scope.events = bookingService.getTimeSlots();
                $scope.user = userService.getUser();
                angular.forEach($scope.events, function(value, key) {
                    $scope.dates.push(value.date);
                });
            });
        })();

//        $scope.reservation = {};
//
//        $scope.masseurs = {};
//        angular.forEach($scope.slots, function(value, key) {
//           $scope.dates[value.date] = true;
//            if(!value.masseurId in $scope.masseurs) {
//                $scope.masseurs[value.masseurId] = true;
//                $scope.masseurs[value.masseurId].name = value.masseurName;
//                $scope.masseurs[value.masseurId].slots = [];
//            }
//
//            $scope.masseurs[value.masseurId].push({id: value.slotId, time: value.startTime});
//        });


//        $scope.company = { name: "AutoCad Inc", location: "10 rue Duke, Montreal, Québec H3C 2L7"};
//        $scope.masseurs = [
//            { id: '1', name: "John", sex: "m"},
//            { id: '2', name: "Barbara", sex: "f"},
//            { id: '3', name: "Jennifer", sex: "f"},
//            { id: '4', name: "Amy", sex: "f"}
//        ];

//        $scope.timeSlotsAvailable = [
//            { masseur: "John", times: [ "11:20", "13:30", "16:40"] },
//            { masseur: "Barbara", times: [ "9:45", "10:00", "14:30"]},
//            { masseur: "Jennifer", times: [ "9:45", "10:00", "15:00", "15:30"] },
//            { masseur: "Amy", times: [ "14:00, 14:45", "15:00", "15:30"] }
//        ];

//        $scope.selectedMasseur = undefined;

        $scope.makeReservation = function (reservation) {
            bookingService.makeReservation(reservation);
        };
    })

    .controller('ConfirmationCtrl', function ($scope, $location, bookingService) {
        $scope.bookingInfo = bookingService;
        $scope.company = { name: "AutoCad Inc", location: "10 rue Duke, Montreal, Québec H3C 2L7"};
    })

/** Controls the footer */
    .controller('FooterCtrl', [function (/*$scope*/) {
    }]);


