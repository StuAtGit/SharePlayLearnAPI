var userService = angular.module("userService",["ng","userItemModule"]);

/*
 this is what the server-side object that is serialized into JSON looks like,
 which comes from google, and is specified in the google developer openid connect docs
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

userService.service("$user",["$http", "$q", "$itemService", function($http, $q, $itemService) {
    /**
     *
     * @param {type} str
     * @returns {unresolved}
     *
     **/
    this.base64urlDecode  = function(str) {
        return atob(str.replace(/\-/g, '+').replace(/_/g, '/'));
    };

    this.handleItemListResolve = function( itemList ) {
        this.userInfo.itemListDeferred.resolve(itemList);
    };

    this.handleItemListReject = function( msg ) {
        this.userInfo.itemListDeferred.reject(msg);
    };

    this.initializeItemList = function( userId, accessToken ) {
        this.userInfo.itemListDeferred = $q.defer();

        $itemService.getItems( userId, accessToken).then(
            this.handleItemListResolve.bind(this),
            this.handleItemListReject.bind(this)
        );
    };

    this.setUserInfo = function( accessToken, userId, email, userName, tokenExpiration )
    {
        this.userInfo = {};
        this.userInfo.access_token = accessToken;
        this.userInfo.user_id = userId;
        this.userInfo.user_email = email;
        this.userInfo.user_name = userName;
        this.userInfo.token_expiration = tokenExpiration;
        this.userInfoPromise.resolve(this.userInfo);
        this.initializeItemList(this.userInfo.user_id, this.userInfo.access_token);

        sessionStorage.setItem("access_token", this.userInfo.access_token);
        sessionStorage.setItem("expires_in", this.userInfo.token_expiration);
        sessionStorage.setItem("user_id", this.userInfo.user_id);
        sessionStorage.setItem("user_email", this.userInfo.user_email);
        sessionStorage.setItem("user_name",this.userInfo.user_name);
    };

    this.handleLoginResponse = function( data, status, headers, config ) {
        var accessToken = data.accessToken;
        var userId = data.idTokenBody.sub;
        var email = data.idTokenBody.email;
        var userName = data.idTokenBody.email.split('@')[0];
        var expiration = data.expiry;

        this.setUserInfo(accessToken, userId, email, userName, expiration);
    };

    this.handleLoginReject = function( data, status, headers, config ) {
        this.userInfoPromise.reject( "User login failed due to network issue: " + status + " " + data );
    };

    //while tempting to return cached user, what if we want to login a new user?
    /**
     * This handles logins that were conducted directly from the site,
     * using the back-end access_token api to handle all the oauth stuff
     * @param credentials
     * @returns {*}
     */
    this.loginUser = function(credentials) {
        this.userInfoPromise = $q.defer();

        $http.post("api/access_token", null,
            {
                headers: {
                    'Authorization': btoa(credentials.username + ":" + credentials.password)
                }
            }
        ).success(
            this.handleLoginResponse.bind(this)
        ).error(
            this.handleLoginReject.bind(this)
        );

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
        this.userInfo.access_token = accessToken;
        this.userInfo.token_expiration = expiresIn;
        this.userInfo.id_token = idToken;

        window.sessionStorage.setItem("access_token", $scope.user_info.access_token);
        //might want to calculate expiration as soon as it gets back, so I can have it anchored to a time?
        //will need to be UTC, etc.
        window.sessionStorage.setItem("expires_in", $scope.user_info.token_expiration);
        /**
         * The access token (not the id_token) is what is used to authorize with google.
         * For, now delaying jwt authorization because we talk SSL to google.
         * But jsjws has been pulled in for that purpose.
         */
        var id_token_elements = this.userInfo.id_token.split('.');
        var header = this.base64urlDecode(id_token_elements[0]);
        /**This is turned into a JSON object that maps to the JWT fields in google**/
        var payload = JSON.parse(this.base64urlDecode(id_token_elements[1]));
        //do we need to escape this? Gibberish either way.. (coz binary sig)
        var signature = this.base64urlDecode(id_token_elements[2]);

        /**
         * The first three items are for debugging any jwt parsing issues
         */
        this.userInfo.id_token_header = header;
        this.userInfo.id_token_payload = payload;
        this.userInfo.id_token_signature = signature;
        this.userInfo.email = payload.email;
        this.userInfo.user_name = payload.email.split('@')[0];
        this.userInfo.user_id = payload.sub;

        this.setUserInfo(this.userInfo.access_token, this.userInfo.user_id,
            this.userInfo.email, this.userInfo.user_name, this.userInfo.token_expiration);
    };

    this.logout = function() {
        this.userInfo = undefined;
        window.sessionStorage.removeItem('user_id');
        window.sessionStorage.removeItem('access_token');
        window.sessionStorage.removeItem('user_email');
        window.sessionStorage.removeItem('user_name');
    };

    this.isValidToken = function() {
        return true;
    };

    this.getCurrentUser = function() {
        //TODO: Validate token, otherwise user will just get a random popup that tells them
        //that they should logout and login again
        if( typeof this.userInfo === "undefined" ) {
            this.userInfo = {};
            this.userInfo.access_token = window.sessionStorage.getItem("access_token");
            this.userInfo.token_expiration = window.sessionStorage.getItem("expires_in");
            if( !this.isValidToken() ) {
                this.logout();
                return undefined;
            }
            this.userInfo.user_id = window.sessionStorage.getItem("user_id");
            //for now, don't clear this, so we can try to auto-fill later
            //$scope.user_info.user_email = window.sessionStorage.getItem("user_email");
            this.userInfo.user_name = window.sessionStorage.getItem("user_name");

            //check to make sure the session storage had valid values
            //TODO: verify token
            if( this.userInfo.access_token != undefined &&
                this.userInfo.access_token != null &&
                this.userInfo.user_name != undefined &&
                this.userInfo.user_name != null ) {

                if( typeof this.userInfo.itemListDeferred === "undefined" ) {
                    this.initializeItemList(this.userInfo.user_id, this.userInfo.access_token);
                }
                return this.userInfo;
            } else {
                this.logout();
                return undefined;
            }
        } else {
            return this.userInfo;
        }
    };

}]);