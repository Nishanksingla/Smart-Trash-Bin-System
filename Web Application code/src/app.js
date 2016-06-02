var app = angular.module("iotApp", ['ui.router', 'elasticsearch']);
app.service('client', function (esFactory) {
    return esFactory({
        host: '52.38.158.239:9200'
        // apiVersion: '1.2',
        // log: 'trace'
    });
});

app.config(['$stateProvider', '$urlRouterProvider', '$locationProvider', function ($stateProvider, $urlRouterProvider, $locationProvider) {
    $stateProvider
        .state("/", {
            url: '/',
            templateUrl: 'default.html'
        })
        .state("/SignIn", {
            url: '/SignIn',
            templateUrl: 'SignIn.html'
        })
        .state("dashboard", {
            url: '/dashboard',
            templateUrl: 'dashboard.html',
            controllerUrl: "dashboardController"
        })
        .state("SmartHome", {
            url: '/SmartHome',
            templateUrl: 'SmartHome.html',
            controllerUrl: "homeController"
        })
        .state("SmartApartment", {
            url: '/SmartApartment',
            templateUrl: 'SmartApartment.html',
            controllerUrl: "aptController"
        })
        .state("SmartCity", {
            url: '/SmartCity',
            templateUrl: 'SmartCity.html',
            controllerUrl: "cityController"
        })
        .state("nest", {
            url: '/nest',
            templateUrl: 'nest.html',
            controllerUrl: "nestController"
        }).state("SignUp", {
            url: '/SignUp',
            templateUrl: 'register.html',
            controllerUrl: "registerController"
        });

    $urlRouterProvider.otherwise("/");
    $locationProvider.html5Mode({
        enabled: true,
        requireBase: false
    });
}]);

app.controller('indexController', ['$scope', '$http', function ($scope, $http) {

    $scope.username = "";
    $scope.LoginData = {};
    $scope.alert = '';

    if (window.sessionStorage.login === undefined && window.location.pathname === "/dashboard") {
        window.location.assign("/");
    }
    $scope.SignIn = function () {
        debugger
        $http.post('/login', $scope.LoginData)
            .success(function (data) {
                if (data.message === 'success') {
                    window.sessionStorage.login = true;
                    window.location.assign("/nest");
                } else if (data.message === "wrong") {
                    $scope.alert = 'Invalid username or password. Please try again.';
                } else if (data.message === "NoUser") {
                    $scope.alert = 'Wrong Email Id. Please Try Again.';
                }
            })
            .error(function (data, status, headers, config) {
            });
    }
}]);
