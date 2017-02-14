/* global app */

'use strict';

app.factory('SessionService', ['$cookieStore', function ($cookieStore) {
        var cookieName = 'expert-mode';
        
        return {
            isExpert: function() {
                return $cookieStore.get(cookieName) === 'true';
            },
            
            goExpert: function() {
                $cookieStore.put(cookieName, 'true');
            },
            
            leaveExpert: function() {
                $cookieStore.remove(cookieName);
            }
        };
}]);