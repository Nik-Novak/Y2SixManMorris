import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class AI {
	private String colour;
	Board board;
	
	/**
	 * Local move datatype for packaging chip index, position index and score of a particular move
	 * @author nnova
	 *
	 */
	private class Move implements Comparable<Move>{
		int positionIndex; //position index to move to
		int score; //score of the board with that move applied
		int chipIndex; //chip index to move
		Move(int posIndex, int score, int chipIndex){ this.chipIndex = chipIndex; positionIndex=posIndex; this.score=score;} //instantiate
		@Override
		public int compareTo(Move o) { //for sorting highest to lwoest
			return o.score-score;
		}
	}
	
	//instantiate AI class with 
	public AI(String colour, Board board){
		this.colour=colour;
		this.board = board;
	}
	
	
	//helper method for placing a chip
	private boolean place(Chip c, int pos){
		board.setSelected(c);//note the chip of interest to board
		board.setClicked(c);
		boolean success = c.snapTo(board.getPos()[pos].getLoc()); //attempt to snap
		if(success){ //check implications of turn 
			board.turnCheck();
			c.setPlaced(true);
			board.gameCheck();
		}
		board.setSelected(null);//unselect the chip
		board.setClicked(null);
		return success;
	}
	
	/**
	 * Triggers the AI to select a chip to remove
	 * @param data
	 */
	public void remove(Data data){
			List<Move> s = computeBestPlace(data); //get a list of all possible moves with attached scores
			s.sort(null); //sort by highest score first
			for(Move i : s){ //go through all moves, best to worst
				if(remove(i.positionIndex)) //attempt to remove the chip
					break;
			}
			board.gameCheck(); //update game states
	}
	
	//remove helper method
	private boolean remove(int i){
		if(board.getPlayerTurn().split(" ")[0].equals("Remove")) //check if it is a remove state
			if(board.getSelected()!=null){
				try{
					Chip chip = (Chip)board.getSelected();
					if(board.getPlayerTurn().split(" ")[1].equals( chip.getType() ) && chip.isPlaced()){ //make sure its the right turn and the chip is placed
						
						if(!board.millcheck(chip.getPos(), chip.getType(),false) || board.getNumberOfPlaced(chip.getType()) == 3){ //take out non-mills first
							chip.remove();
							return true; //success
						}
								
					}
				}catch(Exception f){}
			}
		return false; //failed
	}

	/**
	 * Triggers the AI to make the next move, either placing a chip or moving
	 * @param data
	 */
	public void move(Data data) {
		State state = data.state();
		if(state==State.PLACING){ //placing chips
			List<Move> s = computeBestPlace(data);
			s.sort(null);
			for(Move i : s){
				if(place(board.getFirstUnplaced(colour), i.positionIndex)) //try to place chips from best to worst score
					break;
			}
		}
		else if(state == State.MOVING || state == State.FLYING){ //moving chips
			List<Move> s = computeBestMove(data); //all moves from highest to lowest stored here
			s.sort(null);
			
			List<Move> highest = new ArrayList<Move>(); //highest scoring moves are stored here
			int highscore = s.get(0).score;
			for (Move i : s)
				if (i.score == highscore)
					highest.add(i);
			
			for(Move i : s){
				//System.out.println("MOVE ATTEMPT");
				Chip chip = board.getData().getChips(colour).get(i.chipIndex); //chip to consider
				
				//this entire first if statement ensures that the AI randomly selects from the set of best moves, not just the first occurring
				if(highest.size()>0 && highest.contains(i)){ //if there are still tied highest elements
					int posind = (int) (Math.random()*highest.size()); //choose a random of the highest
					if(snapTo(data.getChips(colour).get(highest.get(posind).chipIndex ), data.getChips(colour).get(highest.get(posind).chipIndex ).getConnections().get(highest.get(posind).positionIndex))) //attempt to move
						break;
					highest.remove(posind);//if unsuccessful, remove that index and try other highest chips
				}
				else if(snapTo(chip, chip.getConnections().get(i.positionIndex))){ //otherwise if the highest scoring are exhausted, pick the next lower move
					break;
				}
			}
		}
	}

	//method for moving a chip to a location
	private boolean snapTo(Chip chip, Position pos) {
		if(pos.getChip()!=null) //if the position to go to is occupied exit
			return false;
		board.setSelected(chip);
		board.setClicked(chip);
		chip.setLocation(new Point((int) pos.getCenterX(), (int) pos.getCenterY())); //set the new location
		chip.getPos().assign(null); //assign its current position's chip to be empty
		pos.assign(chip);//set the chip of the pos to this object //assign the new position this chip
		chip.assignPos(pos); //assign the chip this position
		
		//switch turns
		if(board.getPlayerTurn().equals("red"))
			board.setPlayerTurn("blue");
		else if(board.getPlayerTurn().equals("blue"))
			board.setPlayerTurn("red");
		
		//update game status
		board.turnCheck();
		board.gameCheck();
//		board.setSelected(null);
		board.setClicked(null);
		return true;
	}

	//helper method for finding the best place to place a chip
	private List<Move> computeBestPlace(Data data) {
		Data copy = data.deepCopy();
		List<Move> s = new ArrayList<Move>();
		for(int i=0; i!=copy.getPos().length; i++){
			if(copy.getPos()[i].getChip()!=null)//if occupied don't consider the spot
				continue;
			copy.getPos()[i].setChip(new Chip(colour, copy.getPos()[i], null)); //assign temporary chip
			int score = score(copy,data, 0); //record board score with new chip
			s.add(new Move(i,score,-1)); //record all scores
			copy.getPos()[i].setChip(null);//remove the temporary chip
		} 
		return s;
	}

	//helper method for finding best moves
	private List<Move> computeBestMove(Data data) {
		Data copy = data.deepCopy();
		List<Move> s = new ArrayList<Move>();
		for(int i=0; i!= copy.getChips(colour).size(); i++){ //for every AI chip
			int nconn = copy.getChips(colour).get(i).getConnections().size();
			for(int q = 0; q!= nconn; q++){//for every one of its connected positions
				Data newcopy = copy.deepCopy(); //copy data so as not to modify
				List<Position> conn = newcopy.getChips(colour).get(i).getConnections(); //get all connected pos
				if(conn.get(q).getChip()!=null)
					continue;//if the spot is occupied dont consider
				muv(newcopy.getChips(colour).get(i), conn.get(q)); //temporary move without game implications
				int score = score(newcopy, data, 1);//assign and record score
				s.add(new Move(q, score, i));
			}
		}
		return s;
	}
	
	
		
	//temp helper method for moving a chip without any game implications
	private void muv(Chip chip, Position pos) {
		chip.setLocation(new Point((int) pos.getCenterX(), (int) pos.getCenterY()));
		chip.getPos().assign(null);
		pos.assign(chip);//set the chip of the pos to this object
		chip.assignPos(pos);//set the position of this chip to the position snapped
	}

	//scores a given board orientation
	private int score(Data copy, Data orig, int mode) {
		int score = 0;
		
		//Horizontal rows to score
		score+=score(0,1,2, copy, orig, mode);
		score+=score(3,4,5, copy,orig, mode);
		score+=score(10,11,12, copy,orig, mode);
		score+=score(13,14,15, copy,orig, mode);
		
		//Vertical rows to consider
		score+=score(0, 6, 13, copy,orig, mode);
		score+=score(3,7,10, copy,orig, mode);
		score+=score(5,8,12, copy,orig, mode);
		score+=score(2,9,15, copy,orig, mode);
		return score;
	}
	
	//mode 0 = place scoring, mode 1 = move scoring
	private int score(int n1, int n2, int n3, Data copy, Data orig, int mode){ //helper method for scoring a row
		int numAlly =0, numEnem=0; 
		int score = 0;
		Position p1 = copy.getPos()[n1];
		Position p2 = copy.getPos()[n2];
		Position p3 = copy.getPos()[n3];
		
		//increment our counts of ally or enemy chips if spot is occupied
		if(p1.getChip()!=null){
			if(p1.getChip().getType().equals(colour))
				numAlly++;
			else
				numEnem++;
		}
		//same as above
		if(p2.getChip()!=null){if(p2.getChip().getType().equals(colour)){ numAlly++;} else numEnem++;}
		//same as p1
		if(p3.getChip()!=null){if(p3.getChip().getType().equals(colour)){ numAlly++;}else numEnem++;}
		
		if(mode == 0){ //PLACING SCORES
			if(numAlly == 1 && numEnem == 0)
				score = 1;
			else if(numAlly == 2 && numEnem == 0)
				score = 3;
			else if(numAlly == 3 && numEnem == 0)
				score = 90000;
			else if(numAlly == 1 && numEnem == 1)
				score = 0;
			else if(numAlly == 2 && numEnem == 1)
				score = 0;
			else if(numAlly == 1 && numEnem == 2)
				score = 0;
			else if(numAlly == 0 && numEnem == 1)
				score = -1;
			else if(numAlly == 0 && numEnem == 2)
				score = -5;
			else if(numAlly == 0 && numEnem == 3)
				score = 0;
		}
		else { //MOVING SCORES
			if(numAlly == 1 && numEnem == 0)
				score = 1;
			else if(numAlly == 2 && numEnem == 0)
				score = 3;
			else if(numAlly == 3 && numEnem == 0){
				boolean mill = false; //if a friendly mill exists, don't assign it much score so the AI attempts to continually create the mill adn remove enemy chips
				for(int i=0; i!=copy.getPos().length; i++)
					if(board.millcheck(orig.getPos()[i], colour,false)){
						mill=true;
						System.out.println(i);
					}
				
				if(mill)
					score = 0;
				else
					score = 9000; //no friendly mill exists, make one
				System.out.println(mill);
			}
			else if(numAlly == 1 && numEnem == 1)
				score = 0;
			else if(numAlly == 2 && numEnem == 1)
				score = 0;
			else if(numAlly == 1 && numEnem == 2)
				score = 0;
			else if(numAlly == 0 && numEnem == 1)
				score = -1;
			else if(numAlly == 0 && numEnem == 2)
				score = -5;
			else if(numAlly == 0 && numEnem == 3)
				score = 0;
		}
		
		return score;
	}
	public String getColour(){ return colour; }
}
