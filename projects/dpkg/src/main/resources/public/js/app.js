var dpkgStatus = angular.module('dpkgStatus', ['ngResource']);

dpkgStatus.controller('DpkgResourceCtrl', [ '$scope', 'DpkgResourceService', function($scope, DpkgResourceService) {
	$scope.searchString = '';
	$scope.selectedPackage = null;

    $scope.updatePackageSelection = function(newPackage) {
    	$scope.selectedPackage = $scope.dpkg[newPackage];
    };

	DpkgResourceService.all.query().$promise.then(function (result) {
		$scope.dpkg = JSON.parse(angular.toJson(result));
		$scope.packages = _.values($scope.dpkg);
	});
}]);

dpkgStatus.factory('DpkgResourceService', ['$resource', function($resource) {
	return {
		all: $resource('/all', {}, {
			query: {method: 'GET',  params: {}, isArray: false}
		}),
	};
}]);