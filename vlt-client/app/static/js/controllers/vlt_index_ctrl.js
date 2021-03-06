app.controller('IndexCtrl', function ($scope, store, $location, IndexService) {
  $scope.vlCollection = [];
  $scope.studentCollection = [];
  $scope.vl = {name: "", dirName: ""};
  $scope.hide = {addForm: true, settingsForm: true, importForm: true, runningForm: true, servers: true, history: true};
  $scope.currentDir = "";
  $scope.selectedStudent = "";
  $scope.servers = [];
  $scope.frameCollection = [];
  $scope.session;
  $scope.selectedHistory = [];

  //init vlt
  IndexService.getVlList()
    .then(res => {
      $scope.vlCollection = res;
    });

  $scope.getOtherVl = function(){
    return IndexService.getOtherVl();
  }

  $scope.logout = function () {
    store.remove('role');
    store.remove('token');
    store.remove('refreshJwtToken');
    $location.path('/login');
  };

  $scope.closeForm = function (type) {
    switch (type) {
      case "all-form":
        $scope.hide.addForm = true;
        $scope.hide.settingsForm = true;
        $scope.hide.importForm = true;
        $scope.hide.runningForm = true;
        $scope.hide.history = true;
        $scope.vl = {};
        $scope.selectedHistory = [];
        break;
      case "add-form":
        $scope.hide.addForm = true;
        $scope.vl = {};
        break;
      case "settings-form":
        $scope.hide.settingsForm = true;
        $scope.vl = {};
        break;
      case "import-form":
        $scope.hide.importForm = true;
        $scope.vl = {};
        break;
      case "running-form":
        $scope.hide.runningForm = true;
        $scope.vl = {};
        break;
      case "settings-servers":
        $scope.hide.servers = true;
        break;
      case "history-vl":
        $scope.hide.history = true;
        $scope.selectedHistory = [];
        break;
    }
  };

  $scope.showAddForm = function () {
    $scope.closeForm('all-form');
    $scope.hide.addForm = false;
    $scope.vl = {name: "", dirName: ""};
  };

  $scope.addVl = function (name) {
    IndexService.addVl(name)
      .then(res => {
        $scope.vlCollection.push(res);
        angular.element($('#name-vl')).val("");
        $scope.hide.addForm = true;
      });
  };

  $scope.getPropertyVl = function (vl) {
    $scope.closeForm('all-form');
    $scope.hide.settingsForm = false;
    $scope.vl = vl;

    IndexService.getPropertyVl(vl.dirName)
      .then(res => {
        $scope.vl = {name: res.name, dirName: res.dirName, curName: res.name, width: res.width, height: res.height, public: res.public};

      });
  };

  $scope.savePropertyVl = function (vl) {
    vl.name = vl.curName;
    IndexService.savePropertyVl(vl.dirName, vl)
      .then(res => {
        var i = _.findIndex($scope.vlCollection, vl => (vl.dirName == res.dirName));
        $scope.vlCollection[i].name = res.name;
        $scope.closeForm('settings-form');
      });
  };

  $scope.showImportForm = function (vl) {
    $scope.closeForm('all-form');
    $scope.hide.importForm = false;
    $scope.vl = vl;
  };

  $scope.showRunningForm = function (vl) {
    $scope.closeForm('all-form');
    $scope.hide.runningForm = false;
    $scope.vl = vl;
    $scope.frameCollection = [];
    IndexService.getLabratoryFame(vl.dirName)
      .then(res => {
        $scope.frameCollection = res;
        $scope.currentDir = vl.dirName;
      });
  };

  $scope.getServersList = function () {
    $scope.servers = [];
    IndexService.getServersList()
      .then(res => {
        _.each(res, (val, key) => {
          $scope.servers.push({'url': key, 'name': val})
        });
      });
  };

  $scope.showServerList = function () {
    if ($scope.hide.servers) {
      $scope.getServersList();
      $scope.hide.servers = false;
    } else {
      $scope.closeForm('settings-servers');
    }
  };

  $scope.stopServer = function (url) {
    IndexService.stopServer(url)
      .then(res => {
        _.remove($scope.servers, server => (server.url == res));
      });
  };

  $scope.startVl = function (dir, frameId) {
    $location.path('/start_vl/' + dir + '/' + frameId);
  }

  $scope.showHistory = function(student, listSession){
    $scope.hide.history = false;
    $scope.selectedStudent = student;
    $scope.selectedHistory = listSession;
  }

  $scope.showSession = function(dir, session){
    $location.path('/show/' + session);
  }

  $scope.sort = function (keyname) {
    $scope.sortKey = keyname;
    $scope.reverse = !$scope.reverse;
  }

});