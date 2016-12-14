angular.module('shawa-core', [])
.controller('QueryResultController', function QueryResultController($scope, $http) {
  var result = "";

  $scope.query = {
    text: ""
  };

  this.getResult = function () {
    return result;
  };

  this.sendQuery = function () {
    $http({
      method: 'GET',
      url: 'http://localhost:8090/query?text=' + $scope.query.text,
      responseType: 'text',
      transformResponse: undefined
    }).then(function successCallback(response) {
      result = response.data.split("<br>")
    }, function errorCallback(response) {
      result = response.statusText;
    });
  }
});
