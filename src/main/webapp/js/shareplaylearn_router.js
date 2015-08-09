
var shareApp = angular.module( 'shareApp', ['ngRoute', 'shareAppControllers']);

shareApp.config(
        ['$routeProvider',
         function( $routeProvider ) {
             $routeProvider.
                when('/share', {
                    templateUrl: "templates/share-my-stuff.html",
                    controller:  "ShareMyStuffCtrl"
                }).when('/share/:uploaded_flag' , {
                    templateUrl: "templates/share-my-stuff.html",
                    controller: "ShareMyStuffCtrl"
                }).when('/login',{
                    templateUrl: "templates/login.html",
                    controller: "LoginCtrl"
                }).when('/login_callback',{
                    templateUrl: "templates/logged_in.html",
                    controller: "LoginCtrl"
                }).when( '/play', {
                    templateUrl: "templates/play.html",
                    controller: "PlayCtrl"
                }).when( '/learn', {
                     templateUrl: "templates/learn.html",
                     controller: "LearnCtrl"
                }).when( '/logout', {
                     templateUrl: "templates/logout.html",
                     controller: "LogoutCtrl"
                }).otherwise( {
                     redirectTo: "/share"
                })
         }
        ]
 );