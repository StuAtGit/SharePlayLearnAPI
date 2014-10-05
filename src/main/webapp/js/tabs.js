/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var currentTab = 0; 

function openTab(clickedTab) {
	var thisTab = $(".tab").index(clickedTab);
	$(".tab a").removeClass("active");
	$(".tab a:eq("+thisTab+")").addClass("active");
	$(".tab-content").hide();
	$(".tab-content:eq("+thisTab+")").show();
	currentTab = thisTab;
}

$(document).ready(function() {
   $(".tab:eq(0) a").css("border-left", "none");
   
   var tab_boxes = $(".tab");
   tab_boxes.click(function() { 
      openTab($(this)); return false; 
   });
   
   $(".tab:eq(0)").click();
});

