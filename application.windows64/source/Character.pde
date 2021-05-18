abstract class Character extends GameObject {  
  int localY;
  Target target;
  
  abstract void drawHeat();
  abstract void drawOnObstaclesMap();
  abstract void display();
  abstract void move();
  
  PVector getGlobalPos() {
    return pos.copy().add(new PVector(0, localY).rotate(rotation));
  }
  
  void resetPosition(){
    pos = getGlobalPos();
    localY = 0;
  }
  
  PVector getCurrentDir() {   
    return new PVector(0, -1).rotate(rotation).normalize();
  }
  
  void turnToTarget(){
    resetPosition();
    if(target != null){
      float angleToTarget = target.getCurrentAngle(getGlobalPos(), getCurrentDir());
      //float angleToTarget = target.angleFromEye;
      //System.out.println(degrees(angleToTarget));
      if(angleToTarget <= 0.1*HALF_PI && angleToTarget >= -0.1*HALF_PI) {
        rotation += angleToTarget;
        angleToTarget = 0;
        target = null;
      } else if(angleToTarget > 0){
        rotation += 0.1*HALF_PI;
      } else if(angleToTarget < 0){
        rotation -= 0.1*HALF_PI;
      }
    }
  }
  
  boolean isValidMove(PVector[] toCheck) { 
    
    for (PVector vertex : toCheck) {
      PVector vertexPos = getGlobalPos().add(vertex); 

      if(getPixelStatus(vertexPos) != Status.VALID){
        return false;
      }
    }
    return true;
  }
}
