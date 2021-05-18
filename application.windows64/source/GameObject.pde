abstract class GameObject {
  PShape collider;
  PVector pos, dimensions;
  float rotation;
  
  GameObject(){
    pos = new PVector(0, 0);
    rotation = 0;
  }
  
  boolean isValidSpawnPosition() {
    for (int i = 0; i < collider.getVertexCount(); i++) {
      PVector vertex = collider.getVertex(i).copy().sub(new PVector(dimensions.x/2, dimensions.y/2)).rotate(rotation);
      PVector vertexPos = pos.copy().add(vertex);
      if (getPixelStatus(vertexPos) != Status.VALID) {
        return false;
      }
    }
    return true;
  }
  
  Status getPixelStatus(PVector pos){
    if (!isInsideWindow(obstacles, pos) || hasColor(obstacles, pos, #000000)) {
      return Status.OBSTACLE;
    } else if(hasColor(obstacles, pos, #ff0000)){
      return Status.CHARACTER;
    } else if(hasColor(obstacles, pos, #00ffff)){
      return Status.SHEEPFOLD;
    }
    return Status.VALID;
  }
  
  void initDrawAction(PGraphics graphic){
    graphic.beginDraw(); 
    graphic.pushMatrix();
    graphic.translate(pos.x, pos.y);
    graphic.rotate(rotation);   
  }
  
  void endDrawAction(PGraphics graphic){
    graphic.popMatrix();
    graphic.endDraw();
    graphic.updatePixels();
  }
}
