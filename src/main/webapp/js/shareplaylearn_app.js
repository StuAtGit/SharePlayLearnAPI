
var shareApp = angular.module( 'shareApp', ['ngRoute', 'shareAppControllers']);

shareApp.config(
        ['$routeProvider',
         function( $routeProvider ) {
             $routeProvider.
                when('/share', {
                    templateUrl: "share-my-stuff.html",
                    controller:  "ShareMyStuffCtrl"
                }).when('/share/:uploaded_flag' , {
                    templateUrl: "share-my-stuff.html",
                    controller: "ShareMyStuffCtrl"
                }).when('/login',{
                    templateUrl: "login.html",
                    controller: "LoginCtrl"
                }).when('/login_callback',{
                    templateUrl: "logged_in.html",
                    controller: "LoginCtrl"
                }).when( '/play', {
                    templateUrl: "play.html",
                    controller: "PlayCtrl"
                }).when( '/learn', {
                     templateUrl: "learn.html",
                     controller: "LearnCtrl"
                }).when( '/logout', {
                     templateUrl: "logout.html",
                     controller: "LogoutCtrl"
                }).otherwise( {
                     redirectTo: "/share"
                })
         }
        ]
 );