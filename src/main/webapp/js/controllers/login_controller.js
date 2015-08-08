shareAppControllers.controller("LogoutCtrl", ['$scope', '$routeParams',
    function( $scope ) {
        logout( $scope, document );
    }
]);

shareAppControllers.controller("LoginCtrl",['$scope', '$http', '$routeParams',
    function( $scope, $http, $routeParams ) {
        $scope.credentials = {};
        checkLoginStatus($scope, document);
        if( $scope.user_info.user_email != null &&
            $scope.user_info.user_email != undefined ) {
            $scope.credentials.username = $scope.user_info.user_email;
        }

        $scope.submitLogin = function(credentials) {
            $http.post("api/access_token", null,
                {
                    headers: {
                        'Authorization': btoa($scope.credentials.username + ":" + $scope.credentials.password)
                    }
                }
                /*
                 public static class OauthJwt {
                 public String iss;
                 public String sub;
                 public String azp;
                 public String email;
                 public String at_hash;
                 public String email_verified;
                 public String aud;
                 public String iat;
                 public String exp;
                 }

                 public static class LoginInfo {
                 public String accessToken;
                 public String expiry;
                 public String idToken;
                 public OauthJwt idTokenBody;
                 public String id;
                 }
                 */
            ).success( function( data, status, headers, config ) {
                    //TODO: pull setting session storage & login controls into function
                    $scope.user_info.access_token = data.accessToken;
                    $scope.user_info.user_id = data.idTokenBody.sub;
                    $scope.user_info.user_email = data.idTokenBody.email;
                    $scope.user_info.user_name = data.idTokenBody.email.split('@')[0];
                    $scope.user_info.token_expiration = data.expiry;

                    window.sessionStorage.setItem("access_token", $scope.user_info.access_token);
                    window.sessionStorage.setItem("expires_in", $scope.user_info.token_expiration);
                    window.sessionStorage.setItem("user_id", data.idTokenBody.sub);
                    window.sessionStorage.setItem("user_email", $scope.user_info.user_email);
                    window.sessionStorage.setItem("user_name",$scope.user_info.user_name)

                    setCurrentUser($scope.user_info.user_name, document);
                }).error( function( data, status, headers, config ) {
                    alert( status + " " + data );
                })
        };
        /**
         * Sample User token (see jwt):
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
                //might want to calculate expiration as soon as it gets back, so I can have it anchored to a time?
                //will need to be UTC, etc.
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

                //window.sessionStorage.setItem("id_token", $scope.user_info.id_token);
                //window.sessionStorage.setItem("id_token_header", header);
                //window.sessionStorage.setItem("id_token_payload", payload);
                window.sessionStorage.setItem("user_id", payload.sub);
                window.sessionStorage.setItem("user_email", payload.email);
                window.sessionStorage.setItem("user_name",$scope.user_info.user_name)
                //window.sessionStorage.setItem("id_token_signature", signature);

                setCurrentUser($scope.user_info.user_name, document);
            }
        }
    }
]);