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
  
  int getFlockSize(){
    return flock.size();
  }
  
  void drawHeat(){
    for(Sheep sheep : flock){
      sheep.drawHeat();
    }
  }
  
  void grazing(){
    for(Sheep sheep : flock){
      sheep.grazing();
    }
  }
  
  void drawOnObstaclesMap(){
    for(Sheep sheep : flock){
      sheep.drawOnObstaclesMap();
    }
  }
  
  void display(){  
    for(int i = 0; i < flock.size(); i++){
      flock.get(i).display();
    }
    removeSheepsInSheepfold();
    move();
  }
  
  void move(){
    for(Sheep sheep : flock){
      sheep.move();
    }
  }
  
  void removeSheepsInSheepfold() {
    for (int i = 0; i < flock.size(); i++) {
      Sheep sheep = flock.get(i);
      if(sheep.moveStatus == Status.SHEEPFOLD){
        flock.remove(i);
      } 
    }
  }
}
