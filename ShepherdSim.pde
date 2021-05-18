import processing.sound.*;

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

void setup() {
  size(700, 700);
  sheepSound = new SoundFile(this, "data/farm.wav");
  sheepSound.loop();
  frameRate(25);
  shapeMode(CENTER);
  background(#33cc33);
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
  obstacles.background(#ffffff);
  obstacles.loadPixels();
  obstacles.endDraw();
  
  heatmap = createGraphics(700, 700);
  heatmap.beginDraw();
  heatmap.shapeMode(CENTER);
  heatmap.background(#808080);
  heatmap.loadPixels();
  heatmap.endDraw();
  
  spawnGameObjects(numberOfSheeps);
}

void draw() {
  //if game is not pausing
  if(gameRunning) {
    
    //if time is not over
    if(lastTime >= 0 && isInTime()) {  
    background(#33cc33);
    
    obstacles.beginDraw();
    obstacles.background(#ffffff);
    obstacles.endDraw();
    
    heatmap.beginDraw();
    heatmap.background(#808080);
    heatmap.endDraw();
    
    drawMaps();
    displayGameObjects();
    writeTimeToDisplay();
    
    //image(obstacles, 0, 0);
    //image(heatmap, 0, 0);
    
    //if time is over
    } else if(lastTime >= 0) {
        lastTime = -1;
        fill(#000000, 150);
        rect(0, 0, width, height);
        writeResult();
        drawRestartButton();
    }
  }
}

void drawRestartButton(){
  fill(#ffffff);
  rect(width/2-60, height/2+50, 120, 50);
  textSize(32); 
  fill(#000000);
  textAlign(CENTER, CENTER);
  text("Restart", width/2, height/2+70);
}

boolean overRect(int x, int y, int w, int h)  {
  if (mouseX >= x && mouseX <= x+w && 
      mouseY >= y && mouseY <= y+h) {
    return true;
  } else {
    return false;
  }
}

boolean isInTime() {
  currentTime = minutes*60*1000 - millis() + setupTime + pauseTime;
  if(currentTime > 0) {
    lastTime = currentTime;
    return true;
  }
  return false;
}

void writeResult() {
  textSize(32); 
  fill(#ff0000);
  textAlign(CENTER, CENTER);
  text(numberOfSheeps-flock.getFlockSize() + " sheeps of " + numberOfSheeps, width/2, height/2);
}

void writeTimeToDisplay() {
  int sec = int((currentTime/1000)%60);
  int min = int((currentTime/(1000*60))%60);
  if(currentTime >= 0) {
    textSize(32); 
    fill(#ff0000);
    textAlign(RIGHT);
    text(min + " : " + sec, width, 33);
  }
}

void spawnGameObjects(int numberSheeps) {
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

void drawMaps() {
  sheepfold.drawHeat();
  sheepfold.drawOnObstaclesMap();
  
  flock.drawHeat();
  flock.drawOnObstaclesMap();
  
  shepherd.drawHeat();
  shepherd.drawOnObstaclesMap();
  
  dog.drawHeat();
  dog.drawOnObstaclesMap();
}

void displayGameObjects() {
  dog.display();  
  flock.display();
  flock.grazing();
  shepherd.display();
  sheepfold.display();
}

void keyPressed() {
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

void mousePressed() {
  if(PVector.dist(dog.pos, new PVector(mouseX, mouseY)) > 20) {
    dog.target = new Target(mouseX, mouseY);
  }
  if(lastTime == -1) {
    if(overRect(width/2-60, height/2+50, 120, 50)){
      setup();
    }
  }
}

static color readPixelFromVector(PGraphics graphic, PVector v) {
  return graphic.pixels[int(v.y)*graphic.width+int(v.x)];
}

static boolean hasColor(PGraphics graphic, PVector pos, color c) {
  if(graphic.pixels[int(pos.y)*graphic.width+int(pos.x)] == c){
    return true;
  }
  return false;
}

static boolean isInsideWindow(PGraphics graphic, PVector v) {
  return v.x <= graphic.width-1 && v.x >= 0 && v.y <= graphic.height-1 && v.y >= 0;
}
