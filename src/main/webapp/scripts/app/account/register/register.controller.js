'use strict';

angular.module('nummapApp')
    .controller('RegisterController', function ($scope, $translate, $timeout, Auth, Domains, Competencies) {
        $scope.success = null;
        $scope.error = null;
        $scope.doNotMatch = null;
        $scope.errorUserExists = null;
        $scope.registerAccount = {};
        $timeout(function (){angular.element('[ng-model="registerAccount.login"]').focus();});

        // Nécessaire pour le formulaire
        $scope.personSocialNetworkList = [];
        $scope.companySocialNetworkList = [];
        $scope.domainsSelected = [];
        $scope.competenciesSelected = [];

        // Récupère la liste de secteurs et de compétences
        Domains.query(function(result) {
            $scope.sectors = result;
            console.log($scope.sectors);
        });
        Competencies.query(function(result) {
            $scope.competencies = result;
            console.log($scope.competencies);
        });

        // Valeurs pour le questionnaire
        $scope.fields = [
            {name: 'Outsourcing'},
            {name: 'Consulting'},
            {name: 'System Integration'}
        ];
        $scope.categories = [
            {value: 'STUDENT', translationKey: 'register.form.category.student'},
            {value: 'PROFESSOR', translationKey: 'register.form.category.professor'},
            {value: 'FREELANCE', translationKey: 'register.form.category.freelance'},
            {value: 'COMPANY', translationKey: 'register.form.category.company'},
            {value: 'ASSOCIATION', translationKey: 'register.form.category.association'}
        ];
        
        $scope.tags = [
            { text: 'just' },
            { text: 'some' },
            { text: 'cool' },
            { text: 'tags' }
          ];
          $scope.loadTags = function(query) {
            var res = [];
            $scope.competencies.forEach(function(element) {
                if (element.name.includes(query)) {
                    res.push(element);
                }
            });
            return res;
          };

        $scope.addElement = function(list) {
            list.push({});
            console.log(list);
        };

        $scope.register = function () {
            if ($scope.registerAccount.password !== $scope.confirmPassword) {
                $scope.doNotMatch = 'ERROR';
            } else {
                $scope.registerAccount.langKey = $translate.use();
                $scope.doNotMatch = null;
                $scope.error = null;
                $scope.errorUserExists = null;
                $scope.errorEmailExists = null;

                // re

                // Ajout de la liste des domaines
                // $scope.registerAccount.sectors = [];
                // $scope.sectors.forEach(function(element) {
                //     if (element.checked) {
                //         $scope.registerAccount.sectors.push({name : element.name});
                //     }
                // });

                // Ajout de la liste des domaines
                // $scope.registerAccount.fields = [];
                // $scope.fields.forEach(function(element) {
                //     if (element.checked) {
                //         $scope.registerAccount.sectors.push({name : element.name});
                //     }
                // });

                // console.log($scope.registerAccount.sectors);
                // console.log($scope.registerAccount.fields);
                // console.log($scope.registerAccount.competencies);
                console.log($scope.competenciesSelected);

                // Auth.createAccount($scope.registerAccount).then(function () {
                //     $scope.success = 'OK';
                // }).catch(function (response) {
                //     $scope.success = null;
                //     if (response.status === 400 && response.data === 'login already in use') {
                //         $scope.errorUserExists = 'ERROR';
                //     } else if (response.status === 400 && response.data === 'e-mail address already in use') {
                //         $scope.errorEmailExists = 'ERROR';
                //     } else {
                //         $scope.error = 'ERROR';
                //     }
                // });
            }
        };
    });
