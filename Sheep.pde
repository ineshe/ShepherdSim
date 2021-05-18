class Sheep extends Character {

  PShape heatArea;
  PShape[] diameters;
  int heatIntensity;
  float speed;
  int pausedSince, pauseTime, lastSearch, searchInterval;
  Status moveStatus;

  Sheep(float xpos, float ypos) {
    
    pos = new PVector(xpos, ypos);
    dimensions = new PVector(int(sheep.width), int(sheep.height));
    rotation = random(TWO_PI);
    localY = 0;
    pausedSince = -1;
    setSpeed();
    lastSearch = -1;
    heatIntensity = 2;

    collider = createShape();    
    collider.beginShape();   
    collider.vertex(dimensions.x/2, 0);
    collider.vertex(0, dimensions.y/3);
    collider.vertex(0, 2*dimensions.y/3);
    collider.vertex(dimensions.x/2, dimensions.y);
    collider.vertex(dimensions.x, 2*dimensions.y/3);
    collider.vertex(dimensions.x, dimensions.y/3);
    collider.fill(#ff0000);
    collider.noStroke();
    endShape(); 
    
    diameters = new PShape[3];
    heatArea = createShape(GROUP);
    initTemperature();
    grazing();
  }
  
  void display() {
    pushMatrix();
    translate(pos.x, pos.y);   
    rotate(rotation);    
    shape(sheep, 0, localY, dimensions.x, dimensions.y);
    popMatrix();     
  }
  
  void drawOnObstaclesMap() {
    initDrawAction(obstacles);
    obstacles.shape(collider, 0, localY, dimensions.x, dimensions.y);
    endDrawAction(obstacles);
  }

  void drawHeat() {
    initDrawAction(heatmap);
    heatmap.shape(heatArea, 0, localY);
    endDrawAction(heatmap);
  }
  
  void initTemperature() {
    for(int i = 0; i < 3; i++){
      diameters[i] = createShape(ELLIPSE, 0, localY, 50+i*30, 50+i*30);
      diameters[i].setStroke(false);
      diameters[i].setFill(color(#ffffff, heatIntensity));
      heatArea.addChild(diameters[i]);
    }
  }
  
  void drawHeatMask(){
    for(PShape diameter : diameters) {
      diameter.setFill(color(#000000, heatIntensity));
    }
  }
  
  void deleteHeatMask(){
    for(PShape diameter : diameters) {
      diameter.setFill(color(#ffffff, heatIntensity));
    }
  }
  
  boolean isAfraid(){
    return PVector.dist(getGlobalPos(), shepherd.getGlobalPos()) <= 300 || PVector.dist(getGlobalPos(), dog.getGlobalPos()) <= 300;
  }
  
  void setSpeed(){
    speed = isAfraid()? 2.5 : 1.5;
  }
  
  void setMoveStatus() {
    int offset = 2;
    PVector[] vertices = new PVector[3];
    vertices[0] = collider.getVertex(0).copy().sub(new PVector(dimensions.x/2, dimensions.y/2 + offset)).rotate(rotation);
    vertices[1] = vertices[0].copy().rotate(-QUARTER_PI);
    vertices[2] = vertices[0].copy().rotate(QUARTER_PI);
    Status status = Status.VALID;
    
    for (PVector vertex : vertices) {
      PVector vertexPos = getGlobalPos().add(vertex); 
      
      Status currentStatus = getPixelStatus(vertexPos);
      if(currentStatus == Status.CHARACTER){
        moveStatus = currentStatus;
        return;
      } else if (currentStatus == Status.OBSTACLE){
        status = currentStatus;
      } else if (currentStatus == Status.SHEEPFOLD){
        moveStatus =  currentStatus;
        return;
      }
    }
    moveStatus =  status;
  }
 
  void move() {
    setMoveStatus();
    if ((pausedSince == -1 || isAfraid())) {   
      if(target == null) { 
        targetTimer();      
      }
      if(moveStatus == Status.VALID){
        setSpeed();
        localY -= speed;
      }
      turnToTarget();
    }  
  }
  
  void setSearchInterval(){
    if(isAfraid()){
      if(moveStatus == Status.OBSTACLE || moveStatus == Status.CHARACTER){
        searchInterval = 1;
      } else {
        searchInterval = 3;
      }
    } else {
      searchInterval = 10;
    }
  }
  
  void targetTimer(){
    boolean isAfraid = isAfraid();
    setSearchInterval();
    if (lastSearch == -1) {
        lastSearch = millis();
    } else if (millis() - lastSearch > searchInterval * 1000) {
      if(moveStatus == Status.OBSTACLE){
        setRandomTarget();
      } else {
        if(isAfraid){
          if(random(1) < 0.1){
            setRandomTarget();
          } else {
            searchForTarget();
          }
        } else {
          setRandomTarget();
        } 
      }       
      lastSearch = millis();
    }
  }
  
  void setRandomTarget(){
    target = new Target(random(1) * width, random(1) * height);
  }
  
  void searchForTarget() {
    ArrayList<Target> targets = new ArrayList<Target>(); 

    for (float angle = -0.8*PI; angle <= PI; angle += 0.2*PI) {
      float averageBrightness = 0;
      
      for (int dist = 1; dist < 5; dist++) {
        PVector checkpoint = getGlobalPos().add(getCurrentDir().mult(20*(dist-1)+0.55*dimensions.y).rotate(angle));
        
        if(getPixelStatus(checkpoint) != Status.VALID) {
          break;
        }
        
        drawHeatMask();
        color  pixelColor  = readPixelFromVector(heatmap, checkpoint);
        float brightness = brightness(pixelColor);
        deleteHeatMask();
        
        if(dist == 1){
          averageBrightness += brightness;
        } else {
          averageBrightness = (averageBrightness + brightness)/2;
        }
                
        targets.add(new Target(checkpoint, angle, dist, averageBrightness));
      }
    }
    //for (Target point : targets) {
    //  drawPoint(g, point.pos, #ff3300);
    //}
    Target bestTarget = getBestTarget(targets);
    target = bestTarget;
  }

  Target getBestTarget(ArrayList<Target> targets) {
    ArrayList<Target> bestTargets = getHottestTargets(targets);      
    if (bestTargets.size() > 1) {
      bestTargets = getNearestTargets(bestTargets);
      if(bestTargets.size() > 1) {
        return bestTargets.get(int(random(bestTargets.size()))); //<>// //<>//
      }
    }
    if (bestTargets.isEmpty()){
      return null;
    }
    return bestTargets.get(0);
  }
  
  ArrayList<Target> getHottestTargets(ArrayList<Target> targets){
    ArrayList<Target> hottestTargets = new ArrayList<Target>();
    float maxHeat = -1;
    for (Target target : targets) {
      if (target.brightness > maxHeat) {
        hottestTargets = new ArrayList<Target>();
        maxHeat = target.brightness;
        hottestTargets.add(target);
      } else if (target.brightness == maxHeat) {
        hottestTargets.add(target);
      }       
    }
    return hottestTargets;
  }
  
  ArrayList<Target> getNearestTargets(ArrayList<Target> targets){
    ArrayList<Target> nearestTargets = new ArrayList<Target>();
    int minDist = -1;
    for (Target target : targets) {
      if(minDist == -1){
        minDist = target.dist;
        nearestTargets.add(target);
      } else if (target.dist < minDist) {
        nearestTargets = new ArrayList<Target>();
        minDist = target.dist;
        nearestTargets.add(target);
      } else if(target.dist == minDist){
        nearestTargets.add(target);
      }        
    }
    return nearestTargets;
  }

  void grazing() {
    if (pausedSince == -1) {
      if (random(1) < 0.01) {
        startPause(int(random(1) * 40));
      }
    } else {
      pause();
    }
  }
  
  void startPause(int seconds) {
    pausedSince = millis();
    pauseTime = seconds;
  }

  void pause() {
    if (millis() - pausedSince > pauseTime * 1000) {
      pausedSince = -1;
      pauseTime = -1;
    }
  }

  /* ONLY FOR DEBUGGING */

  void drawPoint(PGraphics g, PVector v, color col) {
    g.beginDraw();
    g.ellipseMode(CENTER);
    g.fill(col);
    g.ellipse(v.x, v.y, 5, 5);
    g.endDraw();
  }
}
