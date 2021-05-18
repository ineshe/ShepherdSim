class Dog extends Character {
  PShape dog, heatArea;
  float speed;

  Dog(float xpos, float ypos) { 
    dog = loadShape("dog.svg");
    pos = new PVector(xpos, ypos);
    rotation = random(TWO_PI);
    dimensions = new PVector(dog.width, 66);
    speed = 3;
    
    collider = createShape();    
    collider.beginShape();   
    collider.vertex(0, 0);
    collider.vertex(dog.width, 0);
    collider.vertex(dog.width, 66);
    collider.vertex(0, 66);
    collider.fill(#ff0000);
    collider.noStroke();
    collider.endShape();
    
    heatArea = createShape(GROUP);
    initTemperature();
  }
  
  void display() {
    pushMatrix();
    translate(pos.x, pos.y);   
    rotate(rotation);
    shape(dog, 0, localY+20);
    popMatrix();
    move();
  }

  void drawOnObstaclesMap() {
    initDrawAction(obstacles);
    obstacles.shape(collider, 0, localY);
    endDrawAction(obstacles);
  }

  void drawHeat() {
    initDrawAction(heatmap);
    heatmap.shape(heatArea, 0, localY+20);
    endDrawAction(heatmap);
  }
  
  void initTemperature(){
    PShape diameter;
    for(int i = 0; i < 5; i++){
      diameter = createShape(ELLIPSE, 0, localY, 300-i*35, 300-i*35);
      diameter.setStroke(false);
      diameter.setFill(color(#000000, 51));
      heatArea.addChild(diameter);
    }
  }
  
  void move() {
    resetPosition();
    if(target != null && PVector.dist(pos, target.pos) > 5) {
      turnToTarget();
      if(isValidMove(getMovementCheckpoints())) {
        localY -= speed;
      }     
    }
  }
  
  void turnToTarget(){
    resetPosition();
    if(target != null){
      float angleToTarget = target.getCurrentAngle(getGlobalPos(), getCurrentDir());
      if(angleToTarget <= 0.15*HALF_PI && angleToTarget >= -0.15*HALF_PI) {
        rotation += target.angle;
        angleToTarget = 0;
      } else if(angleToTarget > 0){
        rotation += 0.15*HALF_PI;
      } else if(angleToTarget < 0){
        rotation -= 0.15*HALF_PI;
      }
    }
  }
  
  PVector[] getMovementCheckpoints() {
    int offset = 2;
    PVector[] vertices = new PVector[2];
    for(int i = 0; i < 2; i++) {
      vertices[i] = collider.getVertex(i).copy().sub(new PVector(dog.width/2, 66/2 + offset)).rotate(rotation);
    }
    return vertices;
  }
}
