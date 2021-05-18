class Sheepfold extends GameObject {
  PShape sheepfold, heatArea, boundary;

  Sheepfold() { 
    sheepfold = loadShape("sheepfold.svg");
    
    collider  = createShape(RECT, 0, 0, 100, 10);
    collider.setStroke(false);
    collider.setFill(#00ffff);
     
    boundary = createShape(RECT, 0, 0, 10, sheepfold.height);
    boundary.setStroke(false);
    boundary.setFill(#000000);
    
    heatArea = createShape(GROUP);
    initTemperature();
  }
  
  void display() { 
    shape(sheepfold, width/2, sheepfold.height/2); 
  }

  void drawOnObstaclesMap() {
    initDrawAction(obstacles);
    obstacles.shape(collider, width/2-50, 0);
    obstacles.shape(boundary, width/2-sheepfold.width/2, 0);
    obstacles.shape(boundary, width/2+sheepfold.width/2-10, 0);
    endDrawAction(obstacles);
  }

  void drawHeat() {
    initDrawAction(heatmap);
    heatmap.shape(heatArea, width/2-50, 0);
    endDrawAction(heatmap);
  }
  
  void initTemperature(){
    PShape diameter;
    for(int i = 0; i < 6; i++){
      diameter = createShape(RECT, 0, 20*i, 100, 20);
      diameter.setStroke(false);
      diameter.setFill(color(#ffffff, 255-i*3));
      heatArea.addChild(diameter);
    }
  }
}
