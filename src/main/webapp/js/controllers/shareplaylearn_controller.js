var shareAppControllers = angular.module('shareAppControllers',["userService"]).config(function($sceProvider) {
    //completely disable SCE because it sanitizes data that is *not*
    //user-provided, and is *not* cross domain.
    //Security through uselessness will always be disabled.
    //TODO: eventually figure out how to work with angular's stupidity.
    $sceProvider.enabled(false);
});

shareAppControllers.controller("PlayCtrl", ['$scope', '$routeParams',
    function( $scope, $routeParams ) {
        checkLoginStatus($scope, document);
    }
]);

/**
 * 
 * @param {type} str
 * @returns {unresolved}
 *
 **/
var base64urlDecode  = function(str) {
  return atob(str.replace(/\-/g, '+').replace(/_/g, '/'));
};

var setCurrentUser = function ( username, document ) {
    if (document.getElementById("current-user") != null &&
        document.getElementById("current-user") != undefined) {

        document.getElementById("current-user")
            .appendChild(
            document.createTextNode("Logged in as: " + username )
        );
    }

    document.getElementById("login-control").style.display = "none";
    document.getElementById("logout-control").style.display = "block";
};

var logout = function( $scope, document ) {
    document.getElementById("login-control").style.display = "block";
    document.getElementById("logout-control").style.display = "none";
};