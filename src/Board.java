import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener{//the board is hte main Controller, it is accessible by everyone and organizes both changes to data in Update and drawing of components
	public final int DRAWLIMIT = 50;
	int UNIT = 150; //#pixels (use this as a grid for points on JPanel canvas
	private Timer time;//timer for looping and executing actionPerofmred method
	private Point mouse; //mouse location variable
	private Update updater; //main data updater (model)
	private Draw draw; //main data visualizer (View)
	
	private Position selected; //the position object that the mouse is currently hovering over
	private Chip clicked; //the Chip object that the mouse is dragging
	private Data data; //data object for storing all game data for the state of the game
	private Rectangle button; //Rectangle bounds stored for the button
	
	private boolean gameStartFresh; //boolean variable to store if the user is continuing a game or staarting fresh
	private boolean blueFly; //boolean for storing if the blue player is in fly mode
	private boolean redFly; //boolean for storing if the red player is in fly mode
	private JCheckBox aibox;
	private AI ai; 
	
	public Board() throws AWTException{ //all game initialization occurs here
		init();
	}
	
	void init() {
		setFocusable(true);
		setPreferredSize(new Dimension(6*UNIT, 6*UNIT));
		
		aibox = new JCheckBox("Enable AI");
		this.setLayout(null);
		aibox.setSelected(false);
		aibox.setLocation(50, 50);
		aibox.setSize(90, 20);
		this.add(aibox);
		
		data = new Data();
		data.setState(State.SETTING);
	
		time = new Timer(20, this); //set up main loop
		
		this.addMouseListener( new Mouse(this) ); //add custom mouse listener to JPanel for tracking mouse movements and clicks
		this.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				System.out.println("WHYYYYY");
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				System.out.println("WHYYYYY");
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				System.out.println("WHYYYYY");
			}
		});
		//instantiate many values
		updater = new Update(this);
		draw = new Draw(this);
		data.setRed(new ArrayList<Chip>());
		data.setBlue(new ArrayList<Chip>());
		
		gameStartFresh = false; //variable to store if resuming a game or starting fresh
		redFly = false;
		blueFly = false;
		setPlayerTurn("");
		
		//setup Chip potential positions
		getPos()[0] = new Position(UNIT, UNIT, this); getPos()[1] = new Position(3*UNIT,UNIT, this); getPos()[2] = new Position(5*UNIT, UNIT, this);
		getPos()[3] = new Position(2*UNIT, 2*UNIT, this); getPos()[4] = new Position(3*UNIT, 2*UNIT, this); getPos()[5] = new Position(4*UNIT, 2*UNIT, this);
		getPos()[6] = new Position(UNIT, 3*UNIT, this); getPos()[7] = new Position(2*UNIT, 3*UNIT, this); getPos()[8] = new Position(4*UNIT, 3*UNIT, this); getPos()[9] = new Position(5*UNIT, 3*UNIT, this);
		getPos()[10] = new Position(2*UNIT, 4*UNIT, this); getPos()[11] = new Position(3*UNIT, 4*UNIT, this); getPos()[12] = new Position(4*UNIT, 4*UNIT, this);
		getPos()[13] = new Position(UNIT, 5*UNIT, this); getPos()[14] = new Position(3*UNIT, 5*UNIT, this); getPos()[15] = new Position(5*UNIT, 5*UNIT, this);
		
		//add connections between appropriate potential positions
		getPos()[0].addConnection(getPos()[1],getPos()[6]); getPos()[1].addConnection(getPos()[0],getPos()[2],getPos()[4]); getPos()[2].addConnection(getPos()[1],getPos()[9]); 
		getPos()[3].addConnection(getPos()[4],getPos()[7]); getPos()[4].addConnection(getPos()[3],getPos()[5], getPos()[1]); getPos()[5].addConnection(getPos()[4],getPos()[8]);
		getPos()[6].addConnection(getPos()[0],getPos()[13],getPos()[7]); getPos()[7].addConnection(getPos()[3],getPos()[10],getPos()[6]);getPos()[8].addConnection(getPos()[5],getPos()[9],getPos()[12]); getPos()[9].addConnection(getPos()[2],getPos()[8],getPos()[15]); 
		getPos()[10].addConnection(getPos()[7],getPos()[11]); getPos()[11].addConnection(getPos()[10],getPos()[12], getPos()[14]); getPos()[12].addConnection(getPos()[11],getPos()[8]);
		getPos()[13].addConnection(getPos()[14],getPos()[6]); getPos()[14].addConnection(getPos()[11],getPos()[13], getPos()[15]); getPos()[15].addConnection(getPos()[9],getPos()[14]);
		
		//add 6 chips for each side
		Position defaultBlue = new Position(UNIT/2, 3*UNIT, this);
		Position defaultRed = new Position(6*UNIT - UNIT/2, 3*UNIT, this);
		
		for(int i=0; i!= 6; i++){
			data.getBlue().add( new Chip("blue", defaultBlue, this) );
			data.getRed().add( new Chip("red", defaultRed, this) );
		}
		button = new Rectangle(2*UNIT, UNIT/2, 2*UNIT, UNIT/4); //define start button area
		
		
		
		time.start();//begin looping
	}


	public void paintComponent(Graphics g){//paint method for drawing
		getDraw().draw((Graphics2D) g);
	}

	@Override
	public void actionPerformed(ActionEvent e) {//main game loop constantly being called
		updater.update();//update then
		repaint();//draw
	}

	private void displayError(String msg) {//displays an error message with given string
		JOptionPane.showMessageDialog(this, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
	}

	//this method verifies the inital placement of Chips and returns true if valid
	public boolean verify() {
		boolean errors = false;//flag if errors are found
		int countRed = 0, countBlue=0; //counter for red and blue chips
		for(int i=0; i!= getPos().length; i++){

			if(getPos()[i].getChip()==null || ! getPos()[i].getChip().isPlaced())//disregard unplaced chips
				continue;
			if(getPos()[i].getChip().getType().equals("red"))
				countRed++;//count reds
			else
				countBlue++;//count blues
		}
		if(countRed == 0 && countBlue==0){
			gameStartFresh = true;
			return true; //if no chips were placed this is valid start
		}
		if(countRed < 3 || countBlue < 3){ //game ending condition, cannot start here
			displayError("This game would have already ended");
			errors=true;
		}
		if(countRed>6 || countBlue > 6) //too many chips may be placed by user
		{
			displayError("User has placed too many chips");
			errors = true;
		}
		//store all chips in Alist
		ArrayList<Chip> allChips = new ArrayList<Chip>(data.getRed());
		allChips.addAll(data.getBlue());
		
		//this part checks for chips in the same location. if chip[i].position() == chip[j].position() we say that the layout is invalid
		boolean errorDup = false;
		for(int i=0; i!=allChips.size(); i++){
			for(int j=0; j!= allChips.size(); j++){
				if(allChips.get(i).isPlaced() && allChips.get(j).isPlaced() && i!=j){
					Position pos1 = allChips.get(i).getPos();
					Position pos2 = allChips.get(j).getPos();
					if(pos1.getBounds2D().equals(pos2.getBounds2D())){
						errorDup = true;
						errors=true;
					}
				}
			}
		}
		if(errorDup)
			displayError("There are multiple chips in the same position");
		return(!errors); //return true if tests passed, false otehrwise
	}
	
	//checks and updates the state of the game each round
	public void gameCheck(){
		
		if(data.state() == State.MOVING || data.state() == State.FLYING){ //if in movement phase
			
		//calculate how many chips are on each board
		int NB = data.getBlue().size();
		int NR = data.getRed().size();
		
		//activy fly movement stage for 
//		if(NB==3)
//			blueFly = true;
//		if(NR==3)
//			redFly = true;
//		System.out.println();
//		if(blueFly||redFly)
//			System.out.println("Should be flying");//data.setState(State.FLYING);
		
		//game win condition checks
		if(NB<3)
			setPlayerTurn("RED WINS!");
		else if(NR<3)
			setPlayerTurn("BLUE WINS!");
		
		}
		
		//if we try to start the game at the beginning with no game board setup, then allow it and set the game state to placing stage
		if(data.state()== State.PLACING){
		ArrayList<Chip> allChips = new ArrayList<Chip>(data.getRed());
		allChips.addAll(data.getBlue());
		int count=0;
		for (Chip i : allChips)
			if(! i.isPlaced())
				count++;
		if(count == 0)
			data.setState(State.MOVING);
		}
		
		if(getConsTurns() >= DRAWLIMIT) //if the number fo consecutive turns exceeds the limit, a draw is reached
			setPlayerTurn("A Draw Has Been Reached!");
	}
	
	//called after every turn, checks for mills and updates to remove state if the last piece placed
	public void turnCheck() {
		try{
		Chip chip = (Chip) selected; //if a chip is selected create a reference for it
		Position pos = chip.getPos();
		if(millcheck(pos, chip.getType(),true)) //check if the action created a mill
			setPlayerTurn("Remove" + " " + getPlayerTurn());
		
		}catch(Exception f){}
		
		if(data.state()!= State.PLACING)
			gameCheck(); //check game logic if we're not in the placing stage
	}
	
	//method that returns true if the position supplied is part of a mill
	public boolean millcheck(Position pos, String type, boolean resetCons){
		int countV=0; //counts how many vertical chips are aligned with this one
		int countH=0; //same but with horizontal
		for(Position i : pos.getConnections()){
			if(pos.getChip()==null || !pos.getChip().getType().equals(type)) //if there is no chip on the position or they aren't the same type then ignore them
				continue;
			if(i.getChip()!= null && i.getChip().getType().equals(type) && i.relPos(pos).equals("vertical")) //if the position is aligned vertically and has the right type then count how many vertical chips are aligned
				countV+=countV(i, type, pos);
			else if (i.getChip()!= null && i.getChip().getType().equals(type) && i.relPos(pos).equals("horizontal")) //same with horizontal
				countH+=countH(i, type, pos);
		}
		//add one to each for logic simplicity
		countV++;
		countH++;

		if(countV == 3 || countH == 3) {//if  there's a count of 3 horizontally or vertically we are in a mill
			if(resetCons)
				resetConsTurns();
			return true;
		}
		return false;
	}
	
	//recursively counts the number of chips aligned horizontally with a given chip pos (ignores the chip it recursively came from)
	private int countH(Position pos, String type, Position from){
		for(Position i : pos.getConnections()){
			if(i.getChip()!= null && i.getChip().getType().equals(type) && i.relPos(pos).equals("horizontal")&& i!= from)
				return countH(i,type,pos) + 1;
		}
		return 1;
	}
	
	//recursively counts the number of chips aligned vertically with a given chip pos (ignores the chip it recursively came from)
	private int countV(Position pos, String type, Position from){
		for(Position i : pos.getConnections()){
			if(i.getChip()!= null && i.getChip().getType().equals(type) && i.relPos(pos).equals("vertical") && i!= from)
				return countV(i,type,pos) + 1;
			
		}
		return 1;
	}
	
	//removes all chips that are not plced on the game board (if resuming)
	public void removeUnplaced() {
		//add every chip to be removed to a temp arraylist and then call removeAll to both blue and red chip array
		ArrayList<Chip> rem = new ArrayList<Chip>();
		for(Chip i : data.getBlue())
			if(! i.isPlaced())
				rem.add(i);
			for(Chip i : data.getRed())
				if(! i.isPlaced())
					rem.add(i);
		if(rem.size()>=12){ //if no chips were placed
			data.setState(State.PLACING);
			return;
		}
		data.getBlue().removeAll(rem);
		data.getRed().removeAll(rem);
	}
	
	//returns true if hte given point is within teh rectangle created by x1,x2,y1,y2
	public static boolean isWithin(Point mouse, int x1,int x2,int y1,int y2){
		if(mouse.getX()>=x1 && mouse.getX()<=x2 && mouse.getY()>=y1 && mouse.getY()<=y2)
			return true;
		return false;
	}
	
	//returns number of placed chips based on what type is sent in. if type is "red" return the num ber of red chips placed on teh board
	public int getNumberOfPlaced(String type) {
		ArrayList<Chip> array;
		if(type.equals("red"))
			array = data.getRed();
		else
			array = data.getBlue();
		int count=0;
		for(Chip i : array)
			if(i.isPlaced())
				count++;
		return count;
	}
	
	//save method that saves all game data and current states out
	public void save(){
		String filename = (String) JOptionPane.showInputDialog(null, "File Name: ", "Save Game", JOptionPane.INFORMATION_MESSAGE, null, null, "save1"); //get teh file name from user
		if(filename != null)
		{
			filename = filename.trim() + ".smm"; //add our custom extension
		try
	      {
			//standard file outputstreams
	         FileOutputStream fileOut =
	         new FileOutputStream(filename);
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(data); //serialization
	         out.close();
	         fileOut.close();
	         System.out.println("Serialized data is saved in " + filename);
	      }catch(IOException e)
	      {
	          e.printStackTrace();
	      }
		}
	}
	
	public void load(){
		String filename = (String) JOptionPane.showInputDialog(null, "File Name: ", "Load Game", JOptionPane.INFORMATION_MESSAGE, null, null, "save1"); //get teh filename form user
		filename +=".smm"; //custom extension
		if(filename != null)
		{
		try
	      {
	         FileInputStream fileIn = new FileInputStream(filename);
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         data = (Data) in.readObject(); //read in through serialization the Data object that was saved out
	         data.refreshBoards(this); //refresh the objects that were read in with this current game board
	         in.close();
	         fileIn.close();
	      }catch(IOException e)
	      {
	         e.printStackTrace();
	         return;
	      }catch(ClassNotFoundException e)
	      {
	         e.printStackTrace();
	         return;
	      }
		}
	}
	
	public Chip getFirstUnplaced(String type){
		ArrayList<Chip> a;
		if(type.equals("blue"))
			a=data.getBlue();
		else
			a=data.getRed();
		for(Chip i : a)
			if(!i.isPlaced())
				return i;
		return null;
	}
	
	public void createAI(String colour) {
		this.ai=new AI(colour,this);
	}
	
	//GETTERS AND SETTERS

	public Point getMouse() {
		return mouse;
	}

	public void setMouse(Point mouse) {
		this.mouse = mouse;
	}

	public Draw getDraw() {
		return draw;
	}

	public Position[] getPos() {
		return data.getPos();
	}
	
	public void setPos(Position[] pos) {
		data.setPos(pos);
	}

	public Position getSelected() {
		return selected;
	}

	public void setSelected(Position selected) {
		this.selected = selected;
	}

	public Chip getClicked() {
		return clicked;
	}

	public void setClicked(Chip clicked) {
		if(data.state()==State.MOVING || data.state()==State.FLYING){
			draw.highlights(clicked);
		}
		this.clicked = clicked;
	}

	public boolean isGameStartFresh() {
		return gameStartFresh;
	}

	public Data getData() {
		return data;
	}
	
	public void setData(Data data) {
		this.data = data;
	}

	public boolean isBlueFly() {
		return blueFly;
	}

	public void setBlueFly(boolean blueFly) {
		this.blueFly = blueFly;
	}

	public boolean isRedFly() {
		return redFly;
	}

	public void setRedFly(boolean redFly) {
		this.redFly = redFly;
	}
	
	public String getPlayerTurn() {
		return data.getPlayerTurn();
	}
	
	public void setPlayerTurn(String playerTurn) {
		data.setPlayerTurn(playerTurn);
	}
	
	public Rectangle getButton() {
		return button;
	}
	
	public void disableAIBox(){
		aibox.setVisible(false);
	}
	public boolean isAI(){
		return aibox.isSelected();
	}

	public AI getAI() {
		return ai;
	}
	
	public int getConsTurns(){
		return data.getConsTurns();
	}
	public void incConsTurns(){
		int inc = (isAI()&&data.state()==State.MOVING?2:1);
		data.setConsTurns(data.getConsTurns()+inc);
	}
	public void resetConsTurns(){
		data.setConsTurns(0);
	}
}
