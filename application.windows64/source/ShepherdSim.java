import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.sound.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ShepherdSim extends PApplet {



static PGraphics obstacles;
static PGraphics heatmap;

static PShape sheep;
static PShape shepherd1;
static PShape shepherd2;

static Shepherd shepherd;
static Dog dog;
static Flock flock;
static Sheepfold sheepfold;
static float setupTime;

SoundFile sheepSound;

float minutes, lastTime, currentTime;
boolean gameRunning;
float pauseStart, pauseTime;
int numberOfSheeps;

public void setup() {
  
  sheepSound = new SoundFile(this, "data/farm.wav");
  sheepSound.loop();
  frameRate(25);
  shapeMode(CENTER);
  background(0xff33cc33);
  minutes = 3;
  gameRunning = true;
  numberOfSheeps = 30;
  lastTime = 0;
  pauseTime = 0;
  setupTime = millis();
  
  sheep = loadShape("sheep.svg");
  shepherd1 = loadShape("shepherd1.svg");
  shepherd2 = loadShape("shepherd2.svg");

  obstacles = createGraphics(700, 700);
  obstacles.beginDraw();
  obstacles.shapeMode(CENTER);
  obstacles.background(0xffffffff);
  obstacles.loadPixels();
  obstacles.endDraw();
  
  heatmap = createGraphics(700, 700);
  heatmap.beginDraw();
  heatmap.shapeMode(CENTER);
  heatmap.background(0xff808080);
  heatmap.loadPixels();
  heatmap.endDraw();
  
  spawnGameObjects(numberOfSheeps);
}

public void draw() {
  //if game is not pausing
  if(gameRunning) {
    
    //if time is not over
    if(lastTime >= 0 && isInTime()) {  
    background(0xff33cc33);
    
    obstacles.beginDraw();
    obstacles.background(0xffffffff);
    obstacles.endDraw();
    
    heatmap.beginDraw();
    heatmap.background(0xff808080);
    heatmap.endDraw();
    
    drawMaps();
    displayGameObjects();
    writeTimeToDisplay();
    
    //image(obstacles, 0, 0);
    //image(heatmap, 0, 0);
    
    //if time is over
    } else if(lastTime >= 0) {
        lastTime = -1;
        fill(0xff000000, 150);
        rect(0, 0, width, height);
        writeResult();
        drawRestartButton();
    }
  }
}

public void drawRestartButton(){
  fill(0xffffffff);
  rect(width/2-60, height/2+50, 120, 50);
  textSize(32); 
  fill(0xff000000);
  textAlign(CENTER, CENTER);
  text("Restart", width/2, height/2+70);
}

public boolean overRect(int x, int y, int w, int h)  {
  if (mouseX >= x && mouseX <= x+w && 
      mouseY >= y && mouseY <= y+h) {
    return true;
  } else {
    return false;
  }
}

public boolean isInTime() {
  currentTime = minutes*60*1000 - millis() + setupTime + pauseTime;
  if(currentTime > 0) {
    lastTime = currentTime;
    return true;
  }
  return false;
}

public void writeResult() {
  textSize(32); 
  fill(0xffff0000);
  textAlign(CENTER, CENTER);
  text(numberOfSheeps-flock.getFlockSize() + " sheeps of " + numberOfSheeps, width/2, height/2);
}

public void writeTimeToDisplay() {
  int sec = PApplet.parseInt((currentTime/1000)%60);
  int min = PApplet.parseInt((currentTime/(1000*60))%60);
  if(currentTime >= 0) {
    textSize(32); 
    fill(0xffff0000);
    textAlign(RIGHT);
    text(min + " : " + sec, width, 33);
  }
}

public void spawnGameObjects(int numberSheeps) {
  sheepfold = new Sheepfold();
  sheepfold.drawOnObstaclesMap();

  do {
    shepherd = new Shepherd(random(1) * width, random(1) * height);
  } while(!shepherd.isValidSpawnPosition());
  shepherd.drawOnObstaclesMap();
  
  do {
    dog = new Dog(random(1) * width, random(1) * height);
  } while(!dog.isValidSpawnPosition());
  dog.drawOnObstaclesMap();
  
  flock = new Flock(numberSheeps);
}

public void drawMaps() {
  sheepfold.drawHeat();
  sheepfold.drawOnObstaclesMap();
  
  flock.drawHeat();
  flock.drawOnObstaclesMap();
  
  shepherd.drawHeat();
  shepherd.drawOnObstaclesMap();
  
  dog.drawHeat();
  dog.drawOnObstaclesMap();
}

public void displayGameObjects() {
  dog.display();  
  flock.display();
  flock.grazing();
  shepherd.display();
  sheepfold.display();
}

public void keyPressed() {
  switch(key){
    case 'w':
      shepherd.rotation = 0;
      shepherd.move();
      break;
    case 'a':
      shepherd.rotation = -HALF_PI;
      shepherd.move();
      break;
    case 's':
      shepherd.rotation = PI;
      shepherd.move();
      break;
    case 'd':
      shepherd.rotation = HALF_PI;
      shepherd.move();
      break;
    case 'p':
      if(gameRunning) {
        gameRunning = false;
        pauseStart = millis();      
      } else {
        gameRunning = true;
        pauseTime += millis() - pauseStart;      
      }  
      break;
  }   
}

public void mousePressed() {
  if(PVector.dist(dog.pos, new PVector(mouseX, mouseY)) > 20) {
    dog.target = new Target(mouseX, mouseY);
  }
  if(lastTime == -1) {
    if(overRect(width/2-60, height/2+50, 120, 50)){
      setup();
    }
  }
}

public static int readPixelFromVector(PGraphics graphic, PVector v) {
  return graphic.pixels[PApplet.parseInt(v.y)*graphic.width+PApplet.parseInt(v.x)];
}

public static boolean hasColor(PGraphics graphic, PVector pos, int c) {
  if(graphic.pixels[PApplet.parseInt(pos.y)*graphic.width+PApplet.parseInt(pos.x)] == c){
    return true;
  }
  return false;
}

public static boolean isInsideWindow(PGraphics graphic, PVector v) {
  return v.x <= graphic.width-1 && v.x >= 0 && v.y <= graphic.height-1 && v.y >= 0;
}
abstract class Character extends GameObject {  
  int localY;
  Target target;
  
  public abstract void drawHeat();
  public abstract void drawOnObstaclesMap();
  public abstract void display();
  public abstract void move();
  
  public PVector getGlobalPos() {
    return pos.copy().add(new PVector(0, localY).rotate(rotation));
  }
  
  public void resetPosition(){
    pos = getGlobalPos();
    localY = 0;
  }
  
  public PVector getCurrentDir() {   
    return new PVector(0, -1).rotate(rotation).normalize();
  }
  
  public void turnToTarget(){
    resetPosition();
    if(target != null){
      float angleToTarget = target.getCurrentAngle(getGlobalPos(), getCurrentDir());
      //float angleToTarget = target.angleFromEye;
      //System.out.println(degrees(angleToTarget));
      if(angleToTarget <= 0.1f*HALF_PI && angleToTarget >= -0.1f*HALF_PI) {
        rotation += angleToTarget;
        angleToTarget = 0;
        target = null;
      } else if(angleToTarget > 0){
        rotation += 0.1f*HALF_PI;
      } else if(angleToTarget < 0){
        rotation -= 0.1f*HALF_PI;
      }
    }
  }
  
  public boolean isValidMove(PVector[] toCheck) { 
    
    for (PVector vertex : toCheck) {
      PVector vertexPos = getGlobalPos().add(vertex); 

      if(getPixelStatus(vertexPos) != Status.VALID){
        return false;
      }
    }
    return true;
  }
}
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
    collider.fill(0xffff0000);
    collider.noStroke();
    collider.endShape();
    
    heatArea = createShape(GROUP);
    initTemperature();
  }
  
  public void display() {
    pushMatrix();
    translate(pos.x, pos.y);   
    rotate(rotation);
    shape(dog, 0, localY+20);
    popMatrix();
    move();
  }

  public void drawOnObstaclesMap() {
    initDrawAction(obstacles);
    obstacles.shape(collider, 0, localY);
    endDrawAction(obstacles);
  }

  public void drawHeat() {
    initDrawAction(heatmap);
    heatmap.shape(heatArea, 0, localY+20);
    endDrawAction(heatmap);
  }
  
  public void initTemperature(){
    PShape diameter;
    for(int i = 0; i < 5; i++){
      diameter = createShape(ELLIPSE, 0, localY, 300-i*35, 300-i*35);
      diameter.setStroke(false);
      diameter.setFill(color(0xff000000, 51));
      heatArea.addChild(diameter);
    }
  }
  
  public void move() {
    resetPosition();
    if(target != null && PVector.dist(pos, target.pos) > 5) {
      turnToTarget();
      if(isValidMove(getMovementCheckpoints())) {
        localY -= speed;
      }     
    }
  }
  
  public void turnToTarget(){
    resetPosition();
    if(target != null){
      float angleToTarget = target.getCurrentAngle(getGlobalPos(), getCurrentDir());
      if(angleToTarget <= 0.15f*HALF_PI && angleToTarget >= -0.15f*HALF_PI) {
        rotation += target.angle;
        angleToTarget = 0;
      } else if(angleToTarget > 0){
        rotation += 0.15f*HALF_PI;
      } else if(angleToTarget < 0){
        rotation -= 0.15f*HALF_PI;
      }
    }
  }
  
  public PVector[] getMovementCheckpoints() {
    int offset = 2;
    PVector[] vertices = new PVector[2];
    for(int i = 0; i < 2; i++) {
      vertices[i] = collider.getVertex(i).copy().sub(new PVector(dog.width/2, 66/2 + offset)).rotate(rotation);
    }
    return vertices;
  }
}
class Flock {
  ArrayList<Sheep> flock;
  
  Flock(int number){
    flock = new ArrayList<Sheep>(number);
    for (int i = 0; i<number; i++){
      Sheep sheep;
      do {
        sheep = new Sheep(random(1) * width, random(1) * height);
      } while (!sheep.isValidSpawnPosition());
      flock.add(sheep);
      flock.get(flock.size()-1).drawOnObstaclesMap();
    }
  }
  
  public int getFlockSize(){
    return flock.size();
  }
  
  public void drawHeat(){
    for(Sheep sheep : flock){
      sheep.drawHeat();
    }
  }
  
  public void grazing(){
    for(Sheep sheep : flock){
      sheep.grazing();
    }
  }
  
  public void drawOnObstaclesMap(){
    for(Sheep sheep : flock){
      sheep.drawOnObstaclesMap();
    }
  }
  
  public void display(){  
    for(int i = 0; i < flock.size(); i++){
      flock.get(i).display();
    }
    removeSheepsInSheepfold();
    move();
  }
  
  public void move(){
    for(Sheep sheep : flock){
      sheep.move();
    }
  }
  
  public void removeSheepsInSheepfold() {
    for (int i = 0; i < flock.size(); i++) {
      Sheep sheep = flock.get(i);
      if(sheep.moveStatus == Status.SHEEPFOLD){
        flock.remove(i);
      } 
    }
  }
}
abstract class GameObject {
  PShape collider;
  PVector pos, dimensions;
  float rotation;
  
  GameObject(){
    pos = new PVector(0, 0);
    rotation = 0;
  }
  
  public boolean isValidSpawnPosition() {
    for (int i = 0; i < collider.getVertexCount(); i++) {
      PVector vertex = collider.getVertex(i).copy().sub(new PVector(dimensions.x/2, dimensions.y/2)).rotate(rotation);
      PVector vertexPos = pos.copy().add(vertex);
      if (getPixelStatus(vertexPos) != Status.VALID) {
        return false;
      }
    }
    return true;
  }
  
  public Status getPixelStatus(PVector pos){
    if (!isInsideWindow(obstacles, pos) || hasColor(obstacles, pos, 0xff000000)) {
      return Status.OBSTACLE;
    } else if(hasColor(obstacles, pos, 0xffff0000)){
      return Status.CHARACTER;
    } else if(hasColor(obstacles, pos, 0xff00ffff)){
      return Status.SHEEPFOLD;
    }
    return Status.VALID;
  }
  
  public void initDrawAction(PGraphics graphic){
    graphic.beginDraw(); 
    graphic.pushMatrix();
    graphic.translate(pos.x, pos.y);
    graphic.rotate(rotation);   
  }
  
  public void endDrawAction(PGraphics graphic){
    graphic.popMatrix();
    graphic.endDraw();
    graphic.updatePixels();
  }
}
class Sheep extends Character {

  PShape heatArea;
  PShape[] diameters;
  int heatIntensity;
  float speed;
  int pausedSince, pauseTime, lastSearch, searchInterval;
  Status moveStatus;

  Sheep(float xpos, float ypos) {
    
    pos = new PVector(xpos, ypos);
    dimensions = new PVector(PApplet.parseInt(sheep.width), PApplet.parseInt(sheep.height));
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
    collider.fill(0xffff0000);
    collider.noStroke();
    endShape(); 
    
    diameters = new PShape[3];
    heatArea = createShape(GROUP);
    initTemperature();
    grazing();
  }
  
  public void display() {
    pushMatrix();
    translate(pos.x, pos.y);   
    rotate(rotation);    
    shape(sheep, 0, localY, dimensions.x, dimensions.y);
    popMatrix();     
  }
  
  public void drawOnObstaclesMap() {
    initDrawAction(obstacles);
    obstacles.shape(collider, 0, localY, dimensions.x, dimensions.y);
    endDrawAction(obstacles);
  }

  public void drawHeat() {
    initDrawAction(heatmap);
    heatmap.shape(heatArea, 0, localY);
    endDrawAction(heatmap);
  }
  
  public void initTemperature() {
    for(int i = 0; i < 3; i++){
      diameters[i] = createShape(ELLIPSE, 0, localY, 50+i*30, 50+i*30);
      diameters[i].setStroke(false);
      diameters[i].setFill(color(0xffffffff, heatIntensity));
      heatArea.addChild(diameters[i]);
    }
  }
  
  public void drawHeatMask(){
    for(PShape diameter : diameters) {
      diameter.setFill(color(0xff000000, heatIntensity));
    }
  }
  
  public void deleteHeatMask(){
    for(PShape diameter : diameters) {
      diameter.setFill(color(0xffffffff, heatIntensity));
    }
  }
  
  public boolean isAfraid(){
    return PVector.dist(getGlobalPos(), shepherd.getGlobalPos()) <= 300 || PVector.dist(getGlobalPos(), dog.getGlobalPos()) <= 300;
  }
  
  public void setSpeed(){
    speed = isAfraid()? 2.5f : 1.5f;
  }
  
  public void setMoveStatus() {
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
 
  public void move() {
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
  
  public void setSearchInterval(){
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
  
  public void targetTimer(){
    boolean isAfraid = isAfraid();
    setSearchInterval();
    if (lastSearch == -1) {
        lastSearch = millis();
    } else if (millis() - lastSearch > searchInterval * 1000) {
      if(moveStatus == Status.OBSTACLE){
        setRandomTarget();
      } else {
        if(isAfraid){
          if(random(1) < 0.1f){
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
  
  public void setRandomTarget(){
    target = new Target(random(1) * width, random(1) * height);
  }
  
  public void searchForTarget() {
    ArrayList<Target> targets = new ArrayList<Target>(); 

    for (float angle = -0.8f*PI; angle <= PI; angle += 0.2f*PI) {
      float averageBrightness = 0;
      
      for (int dist = 1; dist < 5; dist++) {
        PVector checkpoint = getGlobalPos().add(getCurrentDir().mult(20*(dist-1)+0.55f*dimensions.y).rotate(angle));
        
        if(getPixelStatus(checkpoint) != Status.VALID) {
          break;
        }
        
        drawHeatMask();
        int  pixelColor  = readPixelFromVector(heatmap, checkpoint);
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

  public Target getBestTarget(ArrayList<Target> targets) {
    ArrayList<Target> bestTargets = getHottestTargets(targets);      
    if (bestTargets.size() > 1) {
      bestTargets = getNearestTargets(bestTargets);
      if(bestTargets.size() > 1) {
        return bestTargets.get(PApplet.parseInt(random(bestTargets.size()))); //<>//
      }
    }
    if (bestTargets.isEmpty()){
      return null;
    }
    return bestTargets.get(0);
  }
  
  public ArrayList<Target> getHottestTargets(ArrayList<Target> targets){
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
  
  public ArrayList<Target> getNearestTargets(ArrayList<Target> targets){
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

  public void grazing() {
    if (pausedSince == -1) {
      if (random(1) < 0.01f) {
        startPause(PApplet.parseInt(random(1) * 40));
      }
    } else {
      pause();
    }
  }
  
  public void startPause(int seconds) {
    pausedSince = millis();
    pauseTime = seconds;
  }

  public void pause() {
    if (millis() - pausedSince > pauseTime * 1000) {
      pausedSince = -1;
      pauseTime = -1;
    }
  }

  /* ONLY FOR DEBUGGING */

  public void drawPoint(PGraphics g, PVector v, int col) {
    g.beginDraw();
    g.ellipseMode(CENTER);
    g.fill(col);
    g.ellipse(v.x, v.y, 5, 5);
    g.endDraw();
  }
}
class Sheepfold extends GameObject {
  PShape sheepfold, heatArea, boundary;

  Sheepfold() { 
    sheepfold = loadShape("sheepfold.svg");
    
    collider  = createShape(RECT, 0, 0, 100, 10);
    collider.setStroke(false);
    collider.setFill(0xff00ffff);
     
    boundary = createShape(RECT, 0, 0, 10, sheepfold.height);
    boundary.setStroke(false);
    boundary.setFill(0xff000000);
    
    heatArea = createShape(GROUP);
    initTemperature();
  }
  
  public void display() { 
    shape(sheepfold, width/2, sheepfold.height/2); 
  }

  public void drawOnObstaclesMap() {
    initDrawAction(obstacles);
    obstacles.shape(collider, width/2-50, 0);
    obstacles.shape(boundary, width/2-sheepfold.width/2, 0);
    obstacles.shape(boundary, width/2+sheepfold.width/2-10, 0);
    endDrawAction(obstacles);
  }

  public void drawHeat() {
    initDrawAction(heatmap);
    heatmap.shape(heatArea, width/2-50, 0);
    endDrawAction(heatmap);
  }
  
  public void initTemperature(){
    PShape diameter;
    for(int i = 0; i < 6; i++){
      diameter = createShape(RECT, 0, 20*i, 100, 20);
      diameter.setStroke(false);
      diameter.setFill(color(0xffffffff, 255-i*3));
      heatArea.addChild(diameter);
    }
  }
}
class Shepherd extends Character {
  
  PShape heatArea;
  int heatIntensity, coldIntensity;
  float speed;
  int lastStep;
  boolean moving;

  Shepherd(float xpos, float ypos) {
    
    pos = new PVector(xpos, ypos);
    dimensions = new PVector(PApplet.parseInt(shepherd1.width), PApplet.parseInt(shepherd1.height));
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
    collider.fill(0xffff0000);
    collider.noStroke();
    collider.endShape();
    
    initFrontTemperature();
    initBackTemperature();
  }
  
  public void drawHeat() {
    initDrawAction(heatmap); 
    heatmap.shape(heatArea, 0, localY);
    endDrawAction(heatmap);
  }
  
  public void drawOnObstaclesMap() {
    initDrawAction(obstacles);
    obstacles.shape(collider, 0, localY, dimensions.x, dimensions.y);
    endDrawAction(obstacles);
  }
  
  public void display() {
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
  
  public void move() {
    moving = true;
    if(getMoveStatus() == Status.VALID) {
      localY -= speed;
    }
    pos = getGlobalPos();
    localY = 0;
  }
  
  public Status getMoveStatus() {
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
  
  public void moveAnimation(){
    if(millis() % 2000 <= 1000){
      shape(shepherd1, 0, localY, dimensions.x, dimensions.y);
    } else {
      shape(shepherd2, 0, localY, dimensions.x, dimensions.y);
    }
  }
  
  public void initFrontTemperature(){
    PShape diameter;
    for(int i = 0; i < 3; i++){
      diameter = createShape(RECT, -(20+i*15), -(35+i*15), 60+i*30, 55+i*15);
      diameter.setStroke(false);
      diameter.setFill(color(0xff000000, coldIntensity));
      heatArea.addChild(diameter);
    }
  }
  
  public void initBackTemperature(){
    PShape diameter;
    diameter = createShape(RECT, -20, 20, 60, 40);
      diameter.setStroke(false);
      diameter.setFill(color(0xff000000, 3*coldIntensity));
      heatArea.addChild(diameter);
    for(int i = 1; i < 3; i++){
      diameter = createShape(RECT, -(20+i*15), 20, 60+i*30, 40+i*15);
      diameter.setStroke(false);
      diameter.setFill(color(0xffffffff, heatIntensity));
      heatArea.addChild(diameter);
    }
  } 
}
public enum Status {
    VALID, OBSTACLE, CHARACTER, SHEEPFOLD
}
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
  
  public float getCurrentAngle(PVector globalPos, PVector currentDir){
    float angle = PVector.angleBetween(currentDir, pos.copy().sub(globalPos));
    PVector currDirNormal = currentDir.copy().rotate(HALF_PI);
    float dot = PVector.dot(currDirNormal, pos.copy().sub(globalPos));
    if(dot < 0){
      return -angle;
    }
    return angle;
  }
}
  public void settings() {  size(700, 700); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ShepherdSim" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
