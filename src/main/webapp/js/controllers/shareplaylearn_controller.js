var shareAppControllers = angular.module('shareAppControllers',[]).config(function($sceProvider) {
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
    window.sessionStorage.removeItem('user_id');
    window.sessionStorage.removeItem('access_token');
    window.sessionStorage.removeItem('auth_code');
    window.sessionStorage.removeItem('user_email');
    window.sessionStorage.removeItem('user_name');

    document.getElementById("login-control").style.display = "block";
    document.getElementById("logout-control").style.display = "none";
};

var checkLoginStatus = function( $scope, document ) {

    //TODO: OR invalid token
    if( $scope.user_info == undefined ||
        $scope.user_info == null ) {
        $scope.user_info = {};
    }

    //TODO: Validate token, otherwise this logic fails messily when things timeout
    $scope.user_info.access_token = window.sessionStorage.getItem("access_token");
    $scope.user_info.token_expiration = window.sessionStorage.getItem("expires_in");
    $scope.user_info.user_id = window.sessionStorage.getItem("user_id");
    //for now, don't clear this, so we can try to auto-fill later
    //$scope.user_info.user_email = window.sessionStorage.getItem("user_email");
    $scope.user_info.user_name = window.sessionStorage.getItem("user_name");

    if( $scope.user_info.access_token != undefined &&
        $scope.user_info.access_token != null &&
        $scope.user_info.user_name != undefined &&
        $scope.user_info.user_name != null ) {
        setCurrentUser($scope.user_info.user_name, document);
    } else {
        logout($scope, document);
    }

};