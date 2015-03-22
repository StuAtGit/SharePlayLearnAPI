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
        $scope.user_name = window.sessionStorage.getItem("user_name");
        $scope.user_id = window.sessionStorage.getItem("user_id");
        Parse.initialize("JCcUz8bxlZDvXozzQ7EnSNSDptquCwWSz16BRuW3", "N8bTlY2KKP4dE98QHx3YGzEiCFPwtiTb3t7tCd2A");
        var UserObject = Parse.Object.extend("UserObject");
        var userQuery = new Parse.Query(UserObject);
        var userObject;
        userQuery.equalTo("userId",$scope.user_id);
        userQuery.find({
            success: function( results ) {
                if( results.length > 1 ) {
                    alert("More than one user with your user id! " + results[0].userId);
                }
                else if( results.length == 1 ){
                    userObject = results[0];
                    alert("Retrieved user " + userObject.userName);
                }
                else if( results.length == 0 ) {
                    userObject = new UserObject();
                    userObject.save({userId:$scope.user_id, userName:$scope.user_name},
                        {
                            success: function( userObject ) {
                                alert( "Saved user object " + $scope.user_name );
                            },
                            error: function( userObject, error ) {
                                alert( "Failed to save user information " + error.code + " " + error.message );
                            }
                        });
                }
            },
            error: function( error ) {
                alert( "Failed to save user information " + error.code + " " + error.message );
            }
        });
    }
])


/**
 * 
 * @param {type} str
 * @returns {unresolved}
 *
 **/
function base64urlDecode(str) {
  return atob(str.replace(/\-/g, '+').replace(/_/g, '/'));
};

shareAppControllers.controller("LoginCtrl",['$scope','$routeParams','$http',
    function( $scope, $routeParams, $http ) {
        $scope.user_info = {};

        /**
         * Sample returned URL:
        * http://www.shareplaylearn.com/SharePlayLearn2/api/oauth2callback?
        * state=insecure_test_token
        * &code=4/1Oxqgx2PRd8y4YxC7ByfJOLNiN-2.4hTyImQWEVMREnp6UAPFm0EEMmr5kAI
        * &authuser=0&num_sessions=1
        * &prompt=consent
        * &session_state=3dd372aa714b1b2313a838f8c4a4145b928da51f..8b83
         * @returns {undefined}
         * 
         * eyJhbGciOiJSUzI1NiIsImtpZCI6IjljNjMxNDFjMzAzNjkyY2E3Y2Q4MDAxZTUxNmNhNDVhZDdlNTJiZTIifQ.
         * eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMTEwODMxNjM0MzU1MjI2MzY0OTQwIiwiYXpwIjoiNzI2ODM3ODY1MzU3LXRxczIwdTZsdXFjOW9hdjFicDN2YjhuZGdhdmpucmtmLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1haWwiOiJzdHUyNmNvZGVAZ21haWwuY29tIiwiYXRfaGFzaCI6Iml3NWg3NUlnZlJzdkdKLUdDcTJNQWciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXVkIjoiNzI2ODM3ODY1MzU3LXRxczIwdTZsdXFjOW9hdjFicDN2YjhuZGdhdmpucmtmLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiaWF0IjoxNDEyNTQ5NTIzLCJleHAiOjE0MTI1NTM0MjN9.
         * iMrciLGvWA__B-PY_1_POk1eus4C5W7K4LdOzZ4DNa3Fi2HaD5t8Wg9usq1-MzswZG4um55abkzlZ6IlmWNc-sJ_wwXXdO-cK4Bj8ucdBjCYWOCnZwx1akjH8Ettv3MGTa76mh7CuipTYpes8Ka_Wn2SPH7mmD1PK-asuj1t8U8
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
                 * Pulled in jwsjs library so I could do this...
                 * I have just added header, payload, signature to scope so I can print them in the logged in template..
                 * I vaguely recall that I may have already confirmed this? But adding it back in for debugging while I decode jwt payload.
                 * Soo.. just FYI: the payload will decode to the JSON with the info I need - the jsjws library will just verify
                 * the signature of the js passed.
                 * The access token (not the id_token) is what is used to authorize with google.
                 * If we talk https to the oauth endpoint (we need SSL for the site, then!), and the secret comes back OK,
                 * it should be secure-ish (secure, according to google).. so I suppose we can delay verification for a little bit???
                 * We may not even need all this info.. mebbe just use email.
                 */
                var id_token_elements = $scope.user_info.id_token.split('.');
                var header = base64urlDecode(id_token_elements[0]);
                var payload = JSON.parse(base64urlDecode(id_token_elements[1]));
                //do we need to escape this? Gibberish either way.. (coz binary sig)
                var signature = base64urlDecode(id_token_elements[2]);
              
                $scope.user_info.id_token_header = header;
                $scope.user_info.id_token_payload = payload;
                $scope.user_info.id_token_signature = signature;
                $scope.user_info.user_name = payload.email.split('@')[0];
                $scope.user_info.user_id = payload.sub;
                
                window.sessionStorage.setItem("id_token", $scope.user_info.id_token);
                window.sessionStorage.setItem("id_token_header", header);
                window.sessionStorage.setItem("id_token_payload", payload);
                window.sessionStorage.setItem("user_id", payload.sub);
                window.sessionStorage.setItem("user_email", payload.email);
                window.sessionStorage.setItem("user_name",$scope.user_info.user_name)
                window.sessionStorage.setItem("id_token_signature", signature);
            }
        }
        else {
            //TODO: try to retrieve user_id, user email, and token from session storage, and validate it
            //if it's all there and valid, you're still logged in.
            //need some kind of marker in the model that you're logged in, and token is confirmed.
        }
    }
])