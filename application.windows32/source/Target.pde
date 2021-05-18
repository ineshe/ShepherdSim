class Target {

  PVector pos;
  float angle;
  int dist;
  float brightness;
  
  Target(float xpos, float ypos){
    this.pos = new PVector(xpos, ypos);
  }
  
  Target(PVector pos, float angle, int dist, float brightness){
    this.pos = pos;
    this.angle = angle;
    this.dist = dist;
    this.brightness = brightness;
  }
  
  float getCurrentAngle(PVector globalPos, PVector currentDir){
    float angle = PVector.angleBetween(currentDir, pos.copy().sub(globalPos));
    PVector currDirNormal = currentDir.copy().rotate(HALF_PI);
    float dot = PVector.dot(currDirNormal, pos.copy().sub(globalPos));
    if(dot < 0){
      return -angle;
    }
    return angle;
  }
}
