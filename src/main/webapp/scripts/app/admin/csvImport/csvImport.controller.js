'use strict';

angular.module('nummapApp')
    .controller('csvImportController', ['$scope', '$upload', function ($scope, $upload) {
        $scope.$watch('files', function () {
            $scope.upload($scope.files);
        });
        $scope.success = false;

        $scope.upload = function (files) {
            if (files && files.length) {
                for (var i = 0; i < files.length; i++) {
                    var file = files[i];
                    $upload.upload({
                        url: '/api/uploadcsv',
                        fields: {'name': file.name},
                        file: file
                    }).progress(function (evt) {
                        var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
                        console.log('progress: ' + progressPercentage + '% ' + evt.config.file.name);
                    }).success(function (data, status, headers, config) {
                        $scope.success = true;
                        console.log('file ' + config.file.name + 'uploaded. Response: ' + data);
                    }).
                        error(function(data, status, headers, config) {
                        $scope.success = false;
                  });;
                  }
            }
        };
    }]);