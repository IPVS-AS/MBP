(function() {
  'use strict';

  angular
    .module('app')
    .controller('GamificationController', GamificationController);

  GamificationController.$inject = ['$location', '$scope', 'GamificationUserService', 'FlashService'];

  function GamificationController($location, $scope, GamificationUserService, FlashService) {
    var vm = this;
    vm.activities = [];
    vm.levels = [];
	vm.GFUser = {};
	vm.storeItemList= [];
	vm.selectedActivity = {};
	vm.modalContentTitle = "";
	vm.modalContent = "";
	vm.minimumGrade = 50.0;
	
    vm.loadGamificationContent = loadGamificationContent;
    vm.loadUserGamificationData = loadUserGamificationData;
    vm.setGamificationContentProgress = setGamificationContentProgress; 
    vm.loadStoreData = loadStoreData;
    vm.openActivityContent = openActivityContent;
    vm.completeActivity = completeActivity;
    vm.submitQuizAnswers = submitQuizAnswers;
    vm.testUpdUserGF = testUpdUserGF;

    (function initController() {
      loadUserGamificationData();
      loadGamificationContent();
      
      loadStoreData();
      
      $scope.$on('$viewContentLoaded', function() {
    		loadUserGamificationData();
		});
    })();

	 function loadUserGamificationData() {
      vm.dataLoading = true;
      
      GamificationUserService.getGamificationUserByUser()
      .then(function(response) {
          if (response.success) {
          	  //JSON.stringify(response)
          	  //FlashService.Success(JSON.stringify(response))
              vm.GFUser.id = response.data.id;
              vm.GFUser.user = response.data.user;
              vm.GFUser.points = response.data.points;
              vm.GFUser.progress = response.data.progress;
              vm.GFUser.progressLevel = getGamificationUserProgress(vm.GFUser.progress);
              vm.GFUser.progressLastSequence = getGamificationUserLastSequence(vm.GFUser.progress);
              
              setGamificationContentProgress();
          } else {
            if (response.status === 403) {
              FlashService.Error("Authorization error!");
            } else {
              FlashService.Error(response.message);
            }
          }
        });
      
      vm.dataLoading = false;
    };
	
	function getGamificationUserProgress(progressStr) {
		return progressStr.substring(0, progressStr.indexOf("("));
	};
	
	function getGamificationUserLastSequence(progressStr) {
		return progressStr.substring(progressStr.indexOf("(")+1, progressStr.indexOf(")"));
	};
	
    function loadGamificationContent() {
      vm.dataLoading = true;
      
      GamificationUserService.getGamificationQuestDB()
      .then(function(response) {
          if (response.success) {
          	  //FlashService.Success(JSON.stringify(response));
          	  vm.activities = response.data.quests;
          	  
          	  vm.levels = new Array();
          	  var previousLevel = "";
          	  var previousLevelIdxStart = 0;
              for (var i = 0; i < vm.activities.length; i++) {
	              if (vm.activities[i].level != previousLevel) {
	           		  vm.activities[i].firstOfLevel = true;
	           		  vm.levels.push(vm.activities[i].level);
	           		  /*if (i > 0) { 
	           		  	vm.levelCount[vm.activities[i-1].level] = i - previousLevelIdxStart; 
	           		  }*/
	           		  previousLevel = vm.activities[i].level;
	           		  previousLevelIdxStart = i;  
	              }
	              
	              //Last item - count for last level
	              /*if (i == vm.activities.length-1) {
	                  vm.levelCount[vm.activities[i].level] = i - previousLevelIdxStart + 1;
	              }*/
              }
          } else {
              FlashService.Error(response.message);
          }
        }); 
      
      vm.dataLoading = false;
    };
    
    function countLevelActivities(p_level) {
    	var count = 0;
    	for (var i = 0; i <= vm.activities.length; i++) {
          if (vm.activities[i].level == p_level) {
       		  count++;
          }
      	}
      	
      	return count;
    };
    
    function setGamificationContentProgress() {
    	for (var i = 0; i < vm.activities.length; i++) {
    		if (vm.activities[i].sequence < vm.GFUser.progressLastSequence) {
    			vm.activities[i].status = "done";
    		}
    		else { 
    			vm.activities[i].status = "open"
    		}
    	}
    };
    
    function loadStoreData() {
      vm.dataLoading = true;
      
      var item1 = {id:1, name:'5€ to Greenpeace', price:4300};
      var item2 = {id:2, name:'Surprise', price:4300};
      var item3 = {id:3, name:'1 Green Chocolate', price:2500};
      var item4 = {id:4, name:'1€ to Project Tres', price:1000};
      var item5 = {id:5, name:'1€ to Greenpeace', price:1000};
      var item6 = {id:6, name:'1 Fruit/Vegetable Bag', price:1000};
      var item7 = {id:7, name:'2 Eco Trash Bags', price:500};
      var item8 = {id:8, name:'1 Bio Vegetable', price:500};
      var item9 = {id:9, name:'1 Eco Trash Bag', price:300};
      var item10 = {id:10, name:'1 Bio Apple', price:300};
      var item11 = {id:11, name:'1 Coffee', price:100};
      var item12 = {id:12, name:'1 Tea', price:100};
      vm.storeItemList = new Array();
      vm.storeItemList.push(item1);
      vm.storeItemList.push(item2);
      vm.storeItemList.push(item3);
      vm.storeItemList.push(item4);
      vm.storeItemList.push(item5);
      vm.storeItemList.push(item6);
      vm.storeItemList.push(item7);
      vm.storeItemList.push(item8);
      vm.storeItemList.push(item9);
      vm.storeItemList.push(item10);
      vm.storeItemList.push(item11);
      vm.storeItemList.push(item12);
      
      vm.dataLoading = false;
    };
	
	function openActivityContent(activityId) {
	  var activityType = "";
	  for (var i = 0; i < vm.activities.length; i++) {
	  	if (vm.activities[i].id == activityId) {
	  	  vm.selectedActivity = vm.activities[i];
	  	  vm.modalContentTitle = vm.activities[i].title + " - " + vm.activities[i].description;
	  	  vm.modalContent = vm.activities[i].content;
	  	  activityType = vm.activities[i].type;
	  	}
	  }
	  
	  if (activityType == "quest") {
	  	$("#gamificationActivityContentModal").modal("show");
	  } else {
	  	$("#gamificationActivityQuizModal").modal("show");
	  }
	};
	
	function completeActivity(p_addPoints) {
		if (p_addPoints) {
			vm.GFUser.points = vm.GFUser.points + vm.selectedActivity.points;
		}
		var newSequence = vm.selectedActivity.sequence + 1;
		vm.GFUser.progress = vm.selectedActivity.level + "(" + newSequence + ")";
		GamificationUserService.updateGamificationUser(vm.GFUser)
      	.then(function(response) {
            if (!response.success) {
              FlashService.Error(response.message);
            }
            
            if (vm.selectedActivity.type == "quest") {
              $("#gamificationActivityContentModal").modal("hide");
            }
            
            loadUserGamificationData();
          });
	};
	
	function submitQuizAnswers() {
		var questionCount = 0;
		var correctQuestionCount = 0;
		for (var i = 0; i < vm.selectedActivity.questions.length; i++) {
			questionCount++;
			var questionID = "#question_" + vm.selectedActivity.questions[i].id;  
			$(questionID).find("input.form-check-input").each(function(){
				if (this.checked && ($(this).attr("is_correct") == "true")) {
					correctQuestionCount++;
				}
				
				if ($(this).attr("is_correct") == "true") {
					$(this).before('<span class="glyphicon glyphicon-ok text-success"></span>');
				}
			});
		}
		
		var grade = Math.round((correctQuestionCount/questionCount)*100);
		if (grade > vm.minimumGrade) {
			alert("Congratulations! Your reached mark (" + grade + "%) was over the minimum of " + vm.minimumGrade + "%");
			completeActivity(true);
		}
		else {
			alert("Oops! Your reached mark (" + grade + ") was below the minimum of " + vm.minimumGrade);
			completeActivity(false);
		}
		
		vm.selectedActivity.status = "done";		
	};
	
	function testUpdUserGF() {
	  vm.GFUser.points = vm.GFUser.points + 1;
      GamificationUserService.updateGamificationUser(vm.GFUser)
      	.then(function(response) {
            if (response.success) {
              FlashService.Success(response.message, false);
            } else {
              FlashService.Error(response.message);
            }
          });
	};
  }

})();