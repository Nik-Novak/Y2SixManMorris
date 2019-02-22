import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Chip extends Position {
	private String type; //type of chip either "red" or "blue"
	private Position cpos; //position that this chip is in
	private boolean placed; //a boolean value that keeps the state of whether the chip has been placed on the game board or not
	
	public Chip(String type, Position pos, Board board) {
		//this is basically a Position with larger circle and a type stored
		super((int)pos.getCenterX(), (int)pos.getCenterY(), board);
		super.radius = 15;
		this.type = type;
		cpos = pos;
		setPlaced(false);
	}

	//fired if mouse is within area enclosed
	@Override
	public void mouseWithin(){
		board.setSelected(this);
	}
	
	public boolean snapTo(Point mouse) {//snaps Chip to position if it is within 30 pixels of its center in any direction
		Position[] array = null; //array of positions that this piece can legally access
		boolean access = false; //access variable that gives access to flying mode based on certain conditions (listed below)
		if(board.getPlayerTurn().equals("blue") && board.isBlueFly()) //if blue should be allowed to fly and its their turn
			access = true;
		else if(board.getPlayerTurn().equals("red") && board.isRedFly())//if red should be allowed to fly and its their turn
			access = true;
		if(board.getData().state()==State.PLACING) //if we're placing, allow flying basically to be able to place anywhere
			access = true;
		if(board.getData().state()==State.SETTING || access) //if we have access or we're in teh setting stage, allow the array to be all positions
			array = board.getPos();
		else if (board.getData().state()==State.MOVING || board.getData().state()==State.FLYING)//otherwise only allow the chip to access the adjacent positions
			array = cpos.getConnections().toArray(new Position[getConnections().size()]);
		
		
		for(int i=0; i!= array.length; i++){//check every position
			Position pos = array[i];
			if(Board.isWithin(mouse, (int) pos.getX()-30, (int) pos.getX()+30, (int) pos.getY()-30, (int) pos.getY()+30) 
					&& pos.getChip() == null){ //if within range
				if(board.getData().state()!=State.SETTING)
					board.incConsTurns();
				setLocation(new Point((int) pos.getCenterX(), (int) pos.getCenterY()));
				cpos.assign(null);
				pos.assign(this);//set the chip of the pos to this object
				assignPos(pos);//set the position of this chip to the position snapped
				
				//if we're in the moving stage and we successfully snap into place, then switch turns
					if(board.getPlayerTurn().equals( "red"))
						board.setPlayerTurn("blue");
					else if(board.getPlayerTurn().equals("blue"))
						board.setPlayerTurn("red");
				placed = true;
				return true;//true if succesfully snapped
			}
		}
		return false;//false otherwise
	}
	
	//removes a chip from its attached gameboard
	public void remove() {
		if(type.equals("blue"))
			board.getData().getBlue().remove(this);
		else if (type.equals("red"))
			board.getData().getRed().remove(this);
		cpos.assign(null); //assign the position to be null
		String s = board.getPlayerTurn();
		board.setPlayerTurn(s.substring(s.indexOf(' ')+1, s.length())); //reset teh player turn state from removeable
		board.setSelected(null); //reset teh selected chip
	}
	
	@Override
	public ArrayList<Position> getConnections(){
		return (getPos()==null ? null : getPos().getConnections());
	}
	
	//GETTERS & SETTERS
	public void assignPos(Position pos){
		this.cpos=pos;
	}
	public Position getPos(){
		return cpos;
	}

	
	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}

	public boolean isPlaced() {
		return placed;
	}

	public void setPlaced(boolean placed) {
		this.placed = placed;
	}
}
