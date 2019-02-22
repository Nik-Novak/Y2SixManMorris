import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Draw { //class for drawing all components (like view)
	private Board board; //the attached game board that this module acts on
	private Graphics2D drawer; //a g lobal drawer that allows drawing of basic elements (rectangles, strings, etc.)
	private String buttonMsg = "START GAME"; //The string message to be displayed on our button
	private Chip clicked; //The chip that is currently selected by the mouse

	public Draw(Board board) {
		this.board = board;
	}
	public void draw(Graphics2D drawer){//method for organizing priority of drawing
		this.drawer = drawer;
		drawBoard();
		drawHighlights();
		drawPos();
		drawChips();
		
		drawButtons();
	}
	
	private void drawPos() {
		for(int i=0; i!=board.getPos().length; i++){//draw every position item (place where a chip can go)
			drawer.fill(board.getPos()[i]);
			ArrayList<Position> connections = board.getPos()[i].getConnections();
			for(int j=0; j!= connections.size();j++) //for each position item draw all its connected nodes
				drawer.drawLine((int) board.getPos()[i].getCenterX(),(int) board.getPos()[i].getCenterY(), (int) connections.get(j).getCenterX(), (int) connections.get(j).getCenterY());
		}
	}
	
	//draws highlights which show the possible movements of the chip
	private void drawHighlights() {
		if(clicked == null || board.getData().state() == State.SETTING ) //if theres no selected chip or we're setting up teh board don't draw
			return;
		
		//if flying dont draw highlights
		if(clicked.getType().equals(board.getPlayerTurn()) && board.getData().state()==State.FLYING){
			if(board.getPlayerTurn().equals("red") && board.isRedFly())
				return;
			else if(board.getPlayerTurn().equals("blue") && board.isBlueFly())
				return;
		}
		
		//select teh color to draw based on player turn
		ArrayList<Position> hls = clicked.getPos().getConnections();
		if(board.getPlayerTurn().equals("red"))
			drawer.setColor(Color.RED);
		else
			drawer.setColor(Color.BLUE);
		
		for(Position i : hls){
			//vertical connections
			if(i.getLoc().x == clicked.getPos().getLoc().x) //check if vertically aligned
				drawer.fillRect((int) (i.getCenterX() - i.radius),(int) i.getCenterY(), 2*i.radius, (int) (clicked.getPos().getCenterY() - i.getCenterY())); //draw the highlight
			
			//horizontal connections
			if(i.getLoc().y == clicked.getPos().getLoc().y) //check if horizontally aligned
				drawer.fillRect((int) i.getCenterX(),(int) (i.getCenterY() - i.radius), (int) (clicked.getPos().getCenterX() - i.getCenterX()), 2*i.radius);
		}
		drawer.setColor(Color.BLACK);
	}
	private void drawButtons() {//draw any buttons
		if(board.getData().state() == State.SETTING){
			drawer.draw(board.getButton());
			drawer.drawString(buttonMsg, 410, 100);
		}
	}
	private void drawChips() { //draw all chips
		drawer.setColor(Color.BLUE);//blue chips
		for(int i=0; i!= board.getData().getBlue().size(); i++)
			drawer.fill(board.getData().getBlue().get(i));
		drawer.setColor(Color.RED);//red chips
		for(int i=0; i!= board.getData().getRed().size(); i++)
			drawer.fill(board.getData().getRed().get(i));
		drawer.setColor(Color.BLACK);
	}
	private void drawBoard() {//draw teh main game board
		drawer.setColor(Color.WHITE);
		drawer.fillRect(0, 0, board.getPreferredSize().width, board.getPreferredSize().height);//draw white background
		drawer.setColor(new Color(255, 242, 206));
		drawer.fillRect(board.UNIT,board.UNIT,4*board.UNIT, 4*board.UNIT);//draw beige inner square
		drawer.setColor(Color.BLACK);
		Font defont = drawer.getFont();
		drawer.setFont(new Font(Font.DIALOG, Font.BOLD, 30));
		String msg = "";
		if(board.getData().state() != State.PLACING)
			msg = "Move: ";
		else
			msg = "Place: ";
		drawer.drawString("Player "  + msg + board.getPlayerTurn(),board.getButton().x, (int) board.getButton().y - 30); //draw whose turn it is
		drawer.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
		drawer.drawString("Consecutive Turns: " + board.getConsTurns() + "/" + board.DRAWLIMIT + " for a draw.",board.getButton().x+50, (int) board.getButton().y - 10);
		drawer.setFont(defont);
		drawer.drawString("Click and drag pieces onto the board and hit start when you're done to resume a previously placed game or save/load a game from File drop down menu.",5, board.getPreferredSize().height-50);
		drawer.drawString("Otherwise, to start fresh, just hit start game.",5, board.getPreferredSize().height-35);
	}
	
	public void highlights(Chip clicked) {
		this.clicked=clicked;
	}
	
}
