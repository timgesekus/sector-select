$(document).ready(function() {
    $div = $('.oversize');
    $div.children().each(function(){
      if ($div.width() < $(this).width()) {
        $(this).wrap('<marquee>');
      }
    });
});

function renameRunButton() {
    with(document) {
    
        var x = getElementsByClassName("runButton");
        console.log(x.length);
        x[0].innerHTML = 'Terminate';
        x[0].style.color = 'red';
    }
}


$(document).ready(function(){
    $(".runButton").mouseup(function(){
        $("#runtime").html("<a class=\"terminateButton\" href=\"@routes.Application.waiting()\">Terminate</a>");
        
    });
});


$(document).ready(function() {
  var click = 0;
  $('#toolPanelButton').click(function() {
      if (click === 0) {
          maximizePanel('toolPanel', 'toolTable', 20, 18);
          click = 1;
      } else {
          minimizePanel('toolPanel', 'toolTable', 0, 0);
          click = 0;
      }
  });
});

$(document).ready(function() {
  var click = 0;
  $('#simChatPanelButton').click(function() {
      if (click === 0) {
          maximizeSimChatPanel('simChatPanel', 'simChatContent', 20, 18);
          click = 1;
      } else {
          minimizeSimChatPanel('simChatPanel', 'simChatContent', 0, 0);
          click = 0;
      }
  });
});

function maximizeSimChatPanel(panel, panelContent, panelWidth, panelHeight) {
  with (document) {
    getElementById(panel).style.width = panelWidth + 'em';
    getElementById(panel).style.height = panelHeight + 'em';
    setTimeout(function() { getElementById(panelContent).style.visibility = 'visible'; }, 300);
  }
}

function minimizeSimChatPanel(panel, panelContent, panelWidth, panelHeight) {
  with (document) {
    getElementById(panel).style.width = panelWidth + 'em';
    getElementById(panelContent).style.visibility = 'hidden';
    getElementById(panel).style.height = panelHeight + 'em';
  }
}




$(document).ready(function() {
  var click = 0;
  $('#airspacePanelButton').click(function() {
      if (click === 0) {
          maximizePanel('airspacePanel', 'airspacePanelContent', 12, 12);
          click = 1;
      } else {
          minimizePanel('airspacePanel', 'airspacePanelContent', 0, 0);
          click = 0;
      }
  });
});


function maximizePanel(panel, panelContent, panelWidth, panelHeight) {
  with (document) {
    getElementById(panel).style.width = panelWidth + 'em';
    getElementById(panel).style.height = panelHeight + 'em';
    setTimeout(function() { getElementById(panelContent).style.display = 'inline'; }, 300);
  }
}

function minimizePanel(panel, panelContent, panelWidth, panelHeight) {
  with (document) {
    getElementById(panel).style.width = panelWidth + 'em';
    getElementById(panelContent).style.display = 'none';
    getElementById(panel).style.height = panelHeight + 'em';
  }
}


$(document).ready(function() {
    drawAtCanvas();
});

$(window).on('resize', function(){
    drawAtCanvas();
});

function drawAtCanvas() {
    var canvas = document.getElementById("myCanvas");
    var ctx = canvas.getContext("2d");
    
    ctx.canvas.width  = window.innerWidth;
    ctx.canvas.height = window.innerHeight;
  
    ctx.fillStyle = "#FFFFFF";
    ctx.beginPath();
    ctx.arc(95,50,40,0,2*Math.PI);
    ctx.stroke();

    var ctx = canvas.getContext("2d");
    ctx.font = "12px Arial";
    ctx.fillStyle = "#FFFFFF";
    ctx.textAlign = "center";
    ctx.fillText("Der Kreis ist rund?", canvas.width/2, canvas.height/2);
}




