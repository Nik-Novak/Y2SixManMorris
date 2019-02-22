import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import nik.customlib.streams.FastByteArrayOutputStream;


public class Data implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4754480537057532917L;
	private State state; //current state of the game
	private ArrayList<Chip> red, blue; //all red and blue chips
	private String playerTurn; //string for storing the player's turn or current action
	private Position[] pos; //all possible positions a chip can be placed in
	private int consTurns; //stores the number of consecutive turns
	
	//initialize data to null
	public Data(){
		state = null;
		red = null; blue=null;
		setPos(new Position[16]);
		setConsTurns(0);
	}
	
	public Data deepCopy(){
		 Data ret = null;
	        try {
	            FastByteArrayOutputStream os = 
	                    new FastByteArrayOutputStream();
	            ObjectOutputStream out = new ObjectOutputStream(os);
	            out.writeObject(this);
	            out.flush();
	            out.close();
	            ObjectInputStream in = 
	                new ObjectInputStream(os.getInputStream());
	            ret = (Data) in.readObject();
	        }catch(IOException | ClassNotFoundException e){
	        	e.printStackTrace();
	        }
	        return ret;
	}

	public void refreshBoards(Board board) {
		for(Position i : pos)
			i.board = board;
		for(Position i : red)
			i.board=board;
		for(Position i : blue)
			i.board=board;
		
	}
	
	public State state() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public ArrayList<Chip> getBlue() {
		return blue;
	}

	public void setBlue(ArrayList<Chip> blue) {
		this.blue = blue;
	}

	public ArrayList<Chip> getRed() {
		return red;
	}

	public void setRed(ArrayList<Chip> red) {
		this.red = red;
	}
	
	public String getPlayerTurn() {
		return playerTurn;
	}
	public void setPlayerTurn(String playerTurn) {
		this.playerTurn = playerTurn;
	}

	public Position[] getPos() {
		return pos;
	}

	public void setPos(Position[] pos) {
		this.pos = pos;
	}

	public int getConsTurns() {
		return consTurns;
	}

	public void setConsTurns(int consTurns) {
		this.consTurns = consTurns;
	}

	public List<Chip> getChips(String colour) {
		if(colour.equals("blue"))
			return blue;
		else if(colour.equals("red"))
			return red;
		return null;
	}

}
