package corvidina.fire_bearer;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;


//The Display is the region in the window where drawing occurs.
public class Display extends JComponent implements
  KeyListener,  //need for keyboard input
  MouseListener,  //need for mouse input
  MouseMotionListener
{
  //main method for testing
  public static void main(String[] args)
  {
    Display display = new Display(60,new Application(null));
    display.run();

  }

  private BufferedImage characterImageWest;  //image to draw
  private BufferedImage characterImageEast;
  private BufferedImage platformImage;
  private BufferedImage backgroundImage;
  private BufferedImage killImage;
  private int characterImageX;  //position of left edge of image
  private int characterImageY;  //position of top edge of image
  private int platformImageX;
  private int platformImageY;
  private int frameRate;
  private int mouseY;
  private int mouseX;
  private VelocityHandler velocityHandler;
  private static final int NONE = 0;
  private static final int TOP = 1;
  private static final int LEFT = 2;
  private static final int DOWN = 3;
  private static final int RIGHT = 4;
  private int numJumpsOffOfGround;
  private final int characterStartX;
  private final int characterStartY;
  private final int numJumpsAllowed;
  private final double speedMultiplier;
  private final double jumpMultiplier;
  private final double gravityMultiplier;
  private boolean paintEast;

  public Display(int frameRate, Application application) {
    characterImageX = 200;
    characterStartX=characterImageX;
    characterImageY = 200;
    characterStartY=characterImageY;
    platformImageX = 0;
    platformImageY = 0;
    //System.out.println(application.getNumAirJumps());
    numJumpsAllowed=application.getNumAirJumps();
    speedMultiplier=application.getSpeedMultiplier();
    gravityMultiplier=application.getGravityMultiplier();
    jumpMultiplier=application.getJumpStrengthMultiplier();


    this.frameRate = frameRate;

    velocityHandler = new VelocityHandler(characterImageX, characterImageY, false,gravityMultiplier,speedMultiplier);
    //load image

    try {
      if(application.getCharacterImage()==null)
        characterImageWest = ImageIO.read(getClass().getResource("characterImage.png"));
      else
        characterImageWest = ImageIO.read(new File(application.getCharacterImage()));
    } catch (Exception e) {
      System.out.println(e.getMessage() + " 68");
    }

    AffineTransform affineTransform = AffineTransform.getScaleInstance(-1,1);
    affineTransform.translate(-characterImageWest.getWidth(),0);
    AffineTransformOp op = new AffineTransformOp(affineTransform,AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    characterImageEast=op.filter(characterImageWest,null);



    try {
      if(application.getPlatformImage()==null){
        platformImage =ImageIO.read(getClass().getResource("platforms.png"));
      } else
        platformImage = ImageIO.read(new File(application.getPlatformImage()));
    } catch (Exception e) {
      System.out.println(e.getMessage() + " 80");
    }

    try {
      if(application.getBackgroundImage()==null)
        backgroundImage = new BufferedImage(platformImage.getWidth(),platformImage.getHeight(),BufferedImage.TYPE_INT_ARGB);
      else
        backgroundImage = ImageIO.read(new File(application.getBackgroundImage()));
    } catch (Exception e) {
      System.out.println(e.getMessage() + " 73");
    }

    try {
      if(application.getKillAreaImage()==null) {
        killImage = new BufferedImage(backgroundImage.getWidth(), backgroundImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
      } else
        killImage = ImageIO.read(new File(application.getKillAreaImage()));
    } catch (Exception e){
      System.out.println(e.getMessage()+ " 97");
    }
    
    JFrame frame = new JFrame();  //create window
    frame.setTitle("Metroidvania");  //set title of window
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  //closing window will exit program
    setPreferredSize(new Dimension(600, 400));  //set size of drawing region
    
    //need for keyboard input
    setFocusable(true);  //indicates that Display can process key presses
    addKeyListener(this);  //will notify Display when a key is pressed
    
    //need for mouse input
    addMouseListener(this);  //will notify Display when the mouse is pressed
    addMouseMotionListener(this);

    frame.getContentPane().add(this);  //add drawing region to window
    frame.pack();  //adjust window size to fit drawing region
    frame.setVisible(true);  //show window
  }
  
  //called automatically when Java needs to draw the Display
  public void paintComponent(Graphics g)
  {
    int width = getWidth();  //get width of drawing region
    int height = getHeight();  //get height of drawing region
      //set pen color to white
    g.setColor(Color.BLACK);
    g.fillRect(-4,-4,width+4,height+4);
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, width, height);  //fill with white rectangle
    g.setColor(Color.BLACK);
    g.drawImage(backgroundImage,(getWidth()-characterImageX-this.getWidth())+this.getWidth()/2,getHeight()-characterImageY-getHeight()+this.getHeight()/2,null);
    g.drawImage(killImage, (getWidth()-characterImageX-getWidth())+getWidth()/2,getHeight()-characterImageY-getHeight()+getHeight()/2,null);
    g.drawImage(platformImage, (getWidth()-characterImageX-this.getWidth())+this.getWidth()/2,getHeight()-characterImageY-this.getHeight()+this.getHeight()/2,null);


    if(paintEast)
      g.drawImage(characterImageEast,getWidth()/2,getHeight()/2,null);
    else
      g.drawImage(characterImageWest, getWidth()/2,getHeight()/2,null);  //draw image.gif at (imageX, imageY)


  }
  private boolean aKey;
  private boolean sKey;
  private boolean dKey;
  private boolean wKey;;
  //need for keyboard input
  public void keyPressed(KeyEvent e)
  {
    int key = e.getKeyCode();  //indicates which key was pressed
    //System.out.println("key pressed:  " + key);  //shows you key code values for other keys
    if (key == 87)  //tests if "up" arrow was pressed
    {
      wKey=true;
    }
    if (key == 65) {
      aKey=true;
    }
    if (key == 83) {
      sKey=true;
    }
    if (key == 68) {
      dKey=true;
    }
  }

  public void moveItem(){
      int x = (int)velocityHandler.getX();
      int y = (int)velocityHandler.getY();

      int[] coords;
      List<Integer> list = isColliding(x,y);
      if(list.contains(TOP)){
        velocityHandler.setVelocityY(0);
      }
      if(list.contains(LEFT)||list.contains(RIGHT)){
        velocityHandler.setVelocityX(0);
      }


      if(!list.contains(DOWN)){
        velocityHandler.setOnGround(false);
      } else {
        velocityHandler.setOnGround(true);
      }
      if(list.contains(TOP)){
        if(list.contains(LEFT)){
          while(list.contains(LEFT)){
            x++;
            list=isColliding();
          }
        } else if (list.contains(RIGHT)){
          while(list.contains(RIGHT)){
            x--;
            list=isColliding();
          }
        }
          velocityHandler.setVelocityY(0);
          while (list.contains(TOP)) {
            y++;
            list = isColliding(x, y);
        }
      } else {

        if (!list.isEmpty() && list.contains(DOWN)) {
          if (velocityHandler.getVelocityY() < 0) {
            velocityHandler.setOnGround(false);
          } else if (list.size() == 1 && list.get(0) == DOWN) {
            y = characterImageY;
            velocityHandler.setY(y);
            velocityHandler.setOnGround(true);
            velocityHandler.setVelocityY(0);
          }
        } else if (!list.isEmpty()) {
          coords = moveOutOfObstructions(list.get(0), x, y);
          x = coords[0];
          y = coords[1];
          list = isColliding(x, y);
          if (list.contains(LEFT)) {
            while (list.contains(LEFT)) {
              x++;
              list = isColliding(x, y);
            }
          } else if (list.contains(RIGHT)) {
            while (list.contains(RIGHT)) {
              x--;
              list = isColliding(x, y);
            }
          }
        }
          list = isColliding(x, y);
          while (list.contains(DOWN)) {
            y--;
            list = isColliding(x, y);
          }
      }

      characterImageY=y;
      characterImageX=x;
      velocityHandler.setY(y);
      velocityHandler.setX(x);
  }

  public boolean kill(){
    if(new Color(killImage.getRGB(characterImageX, characterImageY),true).getAlpha()!=0){
      characterImageX=characterStartX;
      characterImageY=characterStartY;
      velocityHandler.setX(characterImageX);
      velocityHandler.setY(characterImageY);
      System.out.println("YOU WON!");
      //System.out.println(new Color(killImage.getRGB(characterImageX,characterImageY)).getAlpha());
      return true;
    }
    return false;
  }

  public int[] moveOutOfObstructions(int direction,int x,int y){
    int[] coords = new int[2];
    int offsetX = 0;
    int offsetY = 0;
    switch (direction){
      case 1: {
        offsetX= characterImageWest.getWidth()/2;
        while(isLocOfCollision(TOP,x,y)){
          y++;
        }
        break;
      }
      case 2: {
        offsetY= characterImageWest.getWidth()/2;
        while(isLocOfCollision(LEFT,x,y)){
          x++;
        }
        break;
      }
      case 3: {
        offsetX= characterImageWest.getWidth()/2;
        offsetY= characterImageWest.getHeight();
        while(isLocOfCollision(DOWN,x,y)){
          y--;
        }
        break;
      }
      case 4: {
        offsetX= characterImageWest.getWidth();
        offsetY= characterImageWest.getHeight()/2;
        while(isLocOfCollision(RIGHT,x,y)){
          x--;
        }
        break;
      }
    }
    coords[0]=x;
    coords[1]=y;
    return coords;
  }

  public List<Integer> isColliding(){
      int x = characterImageX;
      int y = characterImageY;
      ArrayList<Integer> arr = new ArrayList();
    if(isLocOfCollision(x+ characterImageWest.getWidth()/2,y))
      arr.add(TOP);
    else if (isLocOfCollision(x,y+ characterImageWest.getHeight()/2))
      arr.add(LEFT);
    else if (isLocOfCollision(x+ characterImageWest.getWidth()/2,y+ characterImageWest.getHeight()))
      arr.add(DOWN);
    else if (isLocOfCollision(x+ characterImageWest.getWidth(),y+ characterImageWest.getHeight()/2))
      arr.add(RIGHT);
    return arr;
  }

  public List<Integer> isColliding(int x, int y){
        ArrayList<Integer> arr = new ArrayList();
        if(isLocOfCollision(x+ characterImageWest.getWidth()/2,y))
            arr.add(TOP);
        else if (isLocOfCollision(x,y+ characterImageWest.getHeight()/2))
            arr.add(LEFT);
        else if (isLocOfCollision(x+ characterImageWest.getWidth()/2,y+ characterImageWest.getHeight()))
            arr.add(DOWN);
        else if (isLocOfCollision(x+ characterImageWest.getWidth(),y+ characterImageWest.getHeight()/2))
            arr.add(RIGHT);
        return arr;
  }

  public boolean isLocOfCollision(int direction,int x, int y){
    switch(direction){
      case 0: {
        return false;
      }
      case 1: {
        x+= characterImageWest.getWidth()/2;
        break;
      }
      case 2: {
        y+= characterImageWest.getHeight()/2;
        break;
      }
      case 3: {
        x+= characterImageWest.getWidth()/2;
        y+= characterImageWest.getHeight();
        break;
      }
      case 4: {
        x+= characterImageWest.getWidth();
        y+= characterImageWest.getHeight()/2;
        break;
      }
      default: {
        throw new RuntimeException("Non valid direction");
      }
    }/*
    if(x<0||y<0){
      //System.out.println("OUT OF BOUNDS ZERO");
      return true;
    }
    else if (x>=super.getWidth()||y>=super.getHeight()){
      //System.out.println("OUT OF BOUNDS TOO BIG");
      return true;
    }
    else */
    if(x<0||x>=platformImage.getWidth()){
      return true;
    }
    if(y<0||y>=platformImage.getHeight()){
      return true;
    }

    if (new Color(platformImage.getRGB(x,y)).getAlpha()==255){
      
      //System.out.println("COLLIDING WITH PLATFORM");
    }
    return false;
  }

  public boolean isLocOfCollision(int x,int y){
    /*
    if (x<0||y<0||x>=super.getWidth()||y>=super.getHeight()) {
      return true;
    }
     */
    if(x<0||x>=platformImage.getWidth()){
      return true;
    }
    if(y<0||y>=platformImage.getHeight()){
      return true;
    }

    Color c = new Color(platformImage.getRGB(x,y),true);
    //System.out.println(c.getAlpha());

    if (c.getAlpha()!=0) {
      return true;
    }
    return false;
  }

  public void keyReleased(KeyEvent e) {
    int key = e.getKeyCode();
    if (key == 87)  //tests if wkey was pressed
    {
      wKey=false;
      if(!velocityHandler.isOnGround() && numJumpsAllowed>=numJumpsOffOfGround) {
        numJumpsOffOfGround++;
        toJump = true;
      }

    }
    if (key == 65) {
      aKey = false;
    }
    if (key == 83) {
      sKey=false;
    }
    if (key == 68) {
      dKey=false;
    }
  }
  public void keyTyped(KeyEvent e) { }

  //need for mouse input
  public void mousePressed(MouseEvent e)
  {
    /*
    imageX = e.getX();  //get x-coordinate of mouse (and move image to it)
    imageY = e.getY();  //get y-coordinate of mouse (and move image to it)
    System.out.println("mouse clicked:  " + imageX + ", " + imageY);
    repaint();  //indicates Display must be redrawn (Java will call paintComponent)
     */
  }
  public void mouseReleased(MouseEvent e) { }
  public void mouseClicked(MouseEvent e) { }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }
  private boolean toJump;
  //need for automation (graphical changes not prompted by the keyboard or mouse)
  public void run()
  {
    while (true)
    {
      if(isColliding(characterImageX,characterImageY+1).contains(DOWN)) {
        velocityHandler.setOnGround(true);
      }
      if(velocityHandler.isOnGround()){
        numJumpsOffOfGround=0;
        toJump=false;
      }

      if(wKey){
        //System.out.println(velocityHandler.isOnGround());
        if(velocityHandler.isOnGround()) {
          velocityHandler.setVelocityY(-5.0*jumpMultiplier);
          velocityHandler.setOnGround(false);
        } else if (toJump&&numJumpsOffOfGround<=numJumpsAllowed){
          //System.out.println(numJumpsAllowed);
          //System.out.println(numJumpsOffOfGround);
          toJump=false;
          velocityHandler.setVelocityY(-5.0*jumpMultiplier);
        }
      }
      /*
      if(sKey){
        imageY+=3;
      }
      */
      if(aKey&&dKey) {
        velocityHandler.setVelocityX(0);
      } else {
          if (aKey) {
            paintEast=false;
            if (velocityHandler.getVelocityX() >= 0) {
              velocityHandler.setVelocityX(-2);
            } else if (velocityHandler.getVelocityX() > -5) {
              velocityHandler.setVelocityX(velocityHandler.getVelocityX() -0.06);
            }
          }
          if (dKey) {
            paintEast=true;
            if (velocityHandler.getVelocityX() <= 0) {
              velocityHandler.setVelocityX(2);
            } else if (velocityHandler.getVelocityX() < 5) {
              velocityHandler.setVelocityX(velocityHandler.getVelocityX() +0.06);
            }
          }
          if (!aKey&&!dKey){
            if(velocityHandler.getVelocityX()>0){
              velocityHandler.setVelocityX(velocityHandler.getVelocityX()-0.3>=0?velocityHandler.getVelocityX()-0.3:0);
            } else {
              velocityHandler.setVelocityX(velocityHandler.getVelocityX()+0.3<=0?velocityHandler.getVelocityX()+0.3:0);
            }
          }
      }
      velocityHandler.tick();
      moveItem();
      rotateImage();
      kill();
      repaint();  //indicates Display must be redrawn (Java will call paintComponent)
      try{Thread.sleep(frameRateToMillis(this.frameRate));}catch(Exception e){}  //give Java 100ms to run paintComponent
    }
  }

  private static int frameRateToMillis(int frameRate){
    return (int)(1.0/frameRate*1000);
  }

  @Override
  public void mouseDragged(MouseEvent e) {

  }

  @Override
  public void mouseMoved(MouseEvent e) {
    mouseX =e.getX();
    mouseY =e.getY();
  }

  public void rotateImage(){
    /*
    AffineTransform t = new AffineTransform();
    double angle = Math.atan2(mouseY-characterImageY,mouseX-characterImageX);
    t.rotate(angle,characterImage.getWidth()/2.0,characterImage.getHeight()/2.0);
    AffineTransformOp op = new AffineTransformOp(t,AffineTransformOp.TYPE_BILINEAR);
    int newWidth = (int) Math.floor(characterImage.getWidth() * Math.abs(Math.cos(angle)) + characterImage.getHeight() * Math.abs(Math.sin(angle)));
    int newHeight = (int) Math.floor(characterImage.getHeight() * Math.abs(Math.cos(angle)) + characterImage.getWidth() * Math.abs(Math.sin(angle)));
    characterImageToDraw = new BufferedImage(newWidth,newHeight,BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = characterImageToDraw.createGraphics();
    g.translate(newWidth-characterImage.getWidth(),newHeight-characterImage.getHeight());
    g.drawImage(characterImage,op,0,0);
    g.dispose();
     */
  }
}
class VelocityHandler {
  private double velocityY;
  private double y;
  private double velocityX;
  private double x;
  private boolean onGround;
  private double gravityMultipler;
  private double speedMultiplier;

  public VelocityHandler(int x, int y, boolean onGround,double gravityMultipler,double speedMultiplier) {
    this.y=y;
    this.x=x;
    this.onGround=onGround;
    this.gravityMultipler=gravityMultipler;
    this.speedMultiplier=speedMultiplier;
  }
  public void setX(double x){
    this.x=x;
  }

  public double getY() {
    return y;
  }

  public double getX(){ return x;}


  public void tick() {
    if(!onGround) {
      y += velocityY;
      velocityY += 0.2*gravityMultipler*speedMultiplier;
    } else {
      velocityY=0;
    }
    x+=velocityX;
  }
  public void setVelocityX(double velocityX){
    this.velocityX=velocityX*speedMultiplier;
  }
  public double getVelocityX(){
    return velocityX;
  }

  public void setVelocityY(double velocityY) {
    this.velocityY = velocityY*speedMultiplier;
  }
  public boolean isOnGround(){
    return onGround;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getVelocityY(){
    return velocityY;
  }

  public void setOnGround(boolean onGround) {
    this.onGround = onGround;
    if(onGround)
      velocityY =0;
  }
}
