var userService = angular.module("userServices",["ng"]);

/*
 this is what the server-side object that is serialized into JSON looks like
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

userService.service("$user",["$http", "$q", function($http, $q) {

    var userInfo;
    var userInfoPromise;

    this.setUserInfo = function( accessToken, userId, email, userName, tokenExpiraion )
    {
        this.userInfo.access_token = accessToken;
        this.userInfo.user_id = userId;
        this.userInfo.user_email = email;
        this.userInfo.user_name = userName;
        this.userInfo.token_expiration = tokenExpiraion;

        window.sessionStorage.setItem("access_token", this.user_info.access_token);
        window.sessionStorage.setItem("expires_in", this.user_info.token_expiration);
        window.sessionStorage.setItem("user_id", userId);
        window.sessionStorage.setItem("user_email", this.user_info.user_email);
        window.sessionStorage.setItem("user_name",this.user_info.user_name);
    };

    this.handleLoginResponse = function( data, status, headers, config ) {
        var acesssToken = data.accessToken;
        var userId = data.idTokenBody.sub;
        var email = data.idTokenBody.email;
        var userName = data.idTokenBody.email.split('@')[0];
        var expiration = data.expiry;

        this.setUserInfo(accessToken, userId, email, userName, expiration);
        this.userInfoPromise.resolve(this.userInfo);
    };

    //while tempting to return cached user, what if we want to login a new user?
    /**
     * This handles logins that were conducted directly from the site,
     * using the back-end access_token api to handle all the oauth stuff
     * @param credentials
     * @returns {*}
     */
    this.loginUser = function(credentials) {
        this.userInfoPromise = $q.deferred();

        $http.post("api/access_token", null,
            {
                headers: {
                    'Authorization': btoa(credentials.username + ":" + credentials.password)
                }
            }
        ).success(
            this.handleLoginResponse( data, status, headers, config )
        ).error( function( data, status, headers, config ) {
            alert( status + " " + data );
        });

        return this.userInfoPromise.promise;
    };

    /**
     * This handles the information returned from a Oauth provider,
     * when someone logs in via the provider.
     * @param accessToken
     * @param expiresIn
     * @param idToken
     */
    this.handleOauth = function( accessToken, expiresIn, idToken ) {
        //TODO: pull logic from if statement from login_controller that parses Oauth response
    };

    this.logout = function() {
        this.userInfo = undefined;
        window.sessionStorage.removeItem('user_id');
        window.sessionStorage.removeItem('access_token');
        window.sessionStorage.removeItem('auth_code');
        window.sessionStorage.removeItem('user_email');
        window.sessionStorage.removeItem('user_name');
    };

    this.isValidToken = function() {
        return true;
    };

    this.getCurrentUser = function() {
        //TODO: Validate token, otherwise this logic fails messily when things timeout
        if( typeof this.userInfo === "undefined" ) {
            this.userInfo = {};
            this.userInfo.access_token = window.sessionStorage.getItem("access_token");
            this.userInfo.token_expiration = window.sessionStorage.getItem("expires_in");
            if( !this.isValidToken() ) {
                this.loginUser();
                return undefined;
            }
            this.userInfo.user_id = window.sessionStorage.getItem("user_id");
            //for now, don't clear this, so we can try to auto-fill later
            //$scope.user_info.user_email = window.sessionStorage.getItem("user_email");
            this.userInfo.user_name = window.sessionStorage.getItem("user_name");

            if( this.userInfo.access_token != undefined &&
                this.userInfo.access_token != null &&
                this.userInfo.user_name != undefined &&
                this.userInfo.user_name != null ) {
                return this.userInfo;
            } else {
                this.logout();
                return undefined;
            }
        }
    };

}]);