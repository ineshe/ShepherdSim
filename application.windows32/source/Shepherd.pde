class Shepherd extends Character {
  
  PShape heatArea;
  int heatIntensity, coldIntensity;
  float speed;
  int lastStep;
  boolean moving;

  Shepherd(float xpos, float ypos) {
    
    pos = new PVector(xpos, ypos);
    dimensions = new PVector(int(shepherd1.width), int(shepherd1.height));
    rotation = 0;
    localY = 0;
    speed = 1;
    moving = false;
    heatIntensity = 40;
    coldIntensity = 55;
    heatArea = createShape(GROUP);

    collider = createShape();    
    collider.beginShape();   
    collider.vertex(0, dimensions.y/2);
    collider.vertex(dimensions.x/3, 0);
    collider.vertex(2*dimensions.x/3, 0);
    collider.vertex(dimensions.x, dimensions.y/2);
    collider.vertex(2*dimensions.x/3, dimensions.y);
    collider.vertex(dimensions.x/3, dimensions.y);
    collider.fill(#ff0000);
    collider.noStroke();
    collider.endShape();
    
    initFrontTemperature();
    initBackTemperature();
  }
  
  void drawHeat() {
    initDrawAction(heatmap); 
    heatmap.shape(heatArea, 0, localY);
    endDrawAction(heatmap);
  }
  
  void drawOnObstaclesMap() {
    initDrawAction(obstacles);
    obstacles.shape(collider, 0, localY, dimensions.x, dimensions.y);
    endDrawAction(obstacles);
  }
  
  void display() {
    pushMatrix();
    translate(pos.x, pos.y);   
    rotate(rotation);
    if(moving){
      moveAnimation();
    } else {
      shape(shepherd1, 0, localY, dimensions.x, dimensions.y);
    }
    popMatrix();
    moving = false;
  }
  
  void move() {
    moving = true;
    if(getMoveStatus() == Status.VALID) {
      localY -= speed;
    }
    pos = getGlobalPos();
    localY = 0;
  }
  
  Status getMoveStatus() {
    int offset = 2;
    PVector[] vertices = new PVector[4];
    for(int i = 0; i < 4; i++) {
      vertices[i] = collider.getVertex(i).copy().sub(new PVector(dimensions.x/2, dimensions.y/2 + offset)).rotate(rotation);
    }
    
    Status status = Status.VALID;
    
    for (PVector vertex : vertices) {
      PVector vertexPos = getGlobalPos().add(vertex); 
      
      Status currentStatus = getPixelStatus(vertexPos);
      if(currentStatus == Status.CHARACTER){
        return currentStatus;
      } else if (currentStatus == Status.OBSTACLE){
        status = currentStatus;
      }
    }
    return status;
  }
  
  void moveAnimation(){
    if(millis() % 2000 <= 1000){
      shape(shepherd1, 0, localY, dimensions.x, dimensions.y);
    } else {
      shape(shepherd2, 0, localY, dimensions.x, dimensions.y);
    }
  }
  
  void initFrontTemperature(){
    PShape diameter;
    for(int i = 0; i < 3; i++){
      diameter = createShape(RECT, -(20+i*15), -(35+i*15), 60+i*30, 55+i*15);
      diameter.setStroke(false);
      diameter.setFill(color(#000000, coldIntensity));
      heatArea.addChild(diameter);
    }
  }
  
  void initBackTemperature(){
    PShape diameter;
    diameter = createShape(RECT, -20, 20, 60, 40);
      diameter.setStroke(false);
      diameter.setFill(color(#000000, 3*coldIntensity));
      heatArea.addChild(diameter);
    for(int i = 1; i < 3; i++){
      diameter = createShape(RECT, -(20+i*15), 20, 60+i*30, 40+i*15);
      diameter.setStroke(false);
      diameter.setFill(color(#ffffff, heatIntensity));
      heatArea.addChild(diameter);
    }
  } 
}
