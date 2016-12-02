app.controller('UserCtrl', function ($scope, $cookieStore, UserService) {
    $scope.isLogin = true;
    $scope.errors = {
      loginIsError: '',
      passwordIsError: '',
      registerRole: ''
    };
    $scope.roles = [
      {name: 'Студент'},
      {name: 'Разработчик'}
    ];
    $scope.user = {
      login: '',
      password: ''
    };
    $scope.role = "Выберите роль";

    $scope.resetVariables = function () {
      $scope.user.login = '';
      $scope.user.password = '';
      $scope.errors.loginIsError = '';
      $scope.errors.passwordIsError = '';
      $scope.errors.registerRole = '';
    }

    $scope.login = function () {
      if ($scope.user.login != '' && $scope.user.password != '') {
        UserService.login($scope.user)
          .then(res => {
              $cookieStore.put('refreshJwtToken', res.refreshJwtToken);
              $cookieStore.put('token', res.token);
              $scope.isAuthorized = true;

            }, err => {
            $scope.errors.loginIsError='Некорректные логин или пароль';
            $scope.errors.passwordIsError='Некорректные логин или пароль';
          }
          );
      } else {
        if ($scope.user.login == '') {
          $scope.errors.loginIsError = "Заполните поле"
        }
        if ($scope.user.password == '') {
          $scope.errors.passwordIsError = "Заполните поле"
        }
      }
    }

    $scope.register = function () {
      if ($scope.user.login != '' && $scope.user.password != '') {
        UserService.register($scope.user);
      } else {
        if ($scope.user.login == '') {
          $scope.errors.loginIsError = "Заполните поле"
        }
        if ($scope.user.password == '') {
          $scope.errors.passwordIsError = "Заполните поле"
        }
      }
    }

  });