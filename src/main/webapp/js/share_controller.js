var shareAppControllers = angular.module('shareAppControllers',[]);

var user_id_global = "";

shareAppControllers.controller("ShareIntroCtrl", ['$scope', '$http', 
    function( $scope, $http ) {
        $http.get("test_data/share_sample.json").success( function(data) {
            $scope.share_data = data;
        })
    }
])

shareAppControllers.controller("ShareMyStuffCtrl", ['$scope', '$routeParams',
    function( $scope, $routeParams ) {
        $scope.user_name = window.localStorage.getItem("user_name");
        $scope.user_cred = window.sessionStorage.getItem("user_cred");
    }
])

shareAppControllers.controller("LoginCtrl",['$scope','$routeParams','$http',
    function( $scope, $routeParams, $http ) {
        $scope.user_info = {};
        $scope.user_info.user_name = window.localStorage.getItem("user_name");
        $scope.user_info.user_cred = window.sessionStorage.getItem("user_cred");
        
        /**
         * Sample returned URL:
        * http://www.shareplaylearn.com/SharePlayLearn2/api/oauth2callback?
        * state=insecure_test_token
        * &code=4/1Oxqgx2PRd8y4YxC7ByfJOLNiN-2.4hTyImQWEVMREnp6UAPFm0EEMmr5kAI
        * &authuser=0&num_sessions=1
        * &prompt=consent
        * &session_state=3dd372aa714b1b2313a838f8c4a4145b928da51f..8b83
         * @returns {undefined}
         */
        if( "client_state" in $routeParams &&
            "access_token" in $routeParams &&
            "expires_in" in $routeParams &&
            "id_token" in $routeParams ) {
            if( $routeParams["client_state"] === "insecure_test_token" ) { 
                $scope.user_info.client_state = $routeParams["session_state"];
                $scope.user_info.access_token = $routeParams["access_token"];
                $scope.user_info.token_expiration = $routeParams["expires_in"];
                $scope.user_info.id_token = $routeParams["id_token"];
                window.sessionStorage.setItem("auth_code",$scope.user_info.auth_code);
                //window.sessionStorage.setItem("client_state",$scope.user_info.client_state);
                window.sessionStorage.setItem("access_token", $scope.user_info.access_token);
                window.sessionStorage.setItem("expires_in", $scope.user_info.token_expiration);
                /**
                 * Parse jwt in id_token to get user info
                 */
                window.sessionStorage.setItem("id_token", $scope.user_info.id_token);
            }
        }
        
        $scope.saveUser = function () {
            window.localStorage.setItem("user_name",$scope.user_info.user_name);
            $scope.user_info.user_cred = $scope.user_info.user_name + "'s super secret token thing";
            window.sessionStorage.setItem("user_cred",$scope.user_info.user_cred);
        }
    }
])