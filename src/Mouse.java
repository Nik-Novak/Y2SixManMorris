import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JOptionPane;

public class Mouse implements MouseListener{//mouse module and listener
	
	private Board board; //game board that this adapter is 
	private Position prev; //position

	public Mouse(Board board){
		this.board = board;
	}

	@Override
	public void mouseReleased(MouseEvent e) { //method fired when mosue button is released
		
		if(board.getClicked()!=null)
			if( ! ((Chip)board.getClicked()).snapTo(board.getMouse()) ){//if we attemtped to snap to and it was not successful then set the location to its old one
				board.getClicked().setLocation(prev.getLoc()); //bring the chip back to its last position
			}
			else{
				if(board.getData().state()!=State.SETTING )
					board.turnCheck(); //check the turn's implications
			}
		board.setClicked(null); //reset the clicked chip
		board.gameCheck();
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		
		//REMOVING A CHIP CODE
		if(board.getPlayerTurn().split(" ")[0].equals("Remove"))
			if(board.getSelected()!=null){
				try{
					Chip chip = (Chip)board.getSelected();
					if(board.getPlayerTurn().split(" ")[1].equals( chip.getType() ) && chip.isPlaced()){
						
						if(!board.millcheck(chip.getPos(), chip.getType(),false) || board.getNumberOfPlaced(chip.getType()) == 3)
							chip.remove();
								
					}
				}catch(Exception f){}
			}
		
		//SETTING THE GAMEBOARD SELECTED CHIP
		if(board.getSelected()!=null  && board.getData().state()==State.SETTING){ //if we're in the placing stage and we have a chip selected
			try{
				Chip chip = (Chip)board.getSelected();
					prev = chip.getPos(); //set previous position for unsusccessful placing
					board.setClicked(chip);//drag chip to follow mouse
					
			}catch(Exception f){}
		}
		
		//
		if(board.getSelected()!=null  &&  board.getData().state() == State.PLACING){ //if we're in the placing stage and we have a chip selected
			try{
				Chip chip = (Chip)board.getSelected();
				if(chip.getType().equals(board.getPlayerTurn()) && !chip.isPlaced()){
					prev = chip.getPos();
					board.setClicked(chip);//drag chip to follow mouse
				}
			}catch(Exception f){}
		}
		
		if(board.getSelected()!=null && board.getData().state()==State.MOVING || board.getData().state()==State.FLYING){
			try{
				Chip chip = (Chip)board.getSelected();
				if( chip.getPos() != null && chip.getType().equals(board.getPlayerTurn())){ //if the selected chip is on the board and its type matches the player's turn..
					prev = chip.getPos();
					board.setClicked((Chip) board.getSelected());
				}
				
				if( ((Chip)board.getSelected()).getPos() == null && ((Chip)board.getSelected()).getType().equals(board.getPlayerTurn()) && board.isGameStartFresh()){
					prev = chip.getPos();
					board.setClicked((Chip) board.getSelected());
				}
					
			}catch(Exception f){}
		}
		
		//START BUTTON
		Rectangle b = board.getButton();
		if(isWithin(board.getMouse(), b.x, b.x + b.width, b.y, b.y + b.height) && board.getData().state()==State.SETTING){//if the mouse is within the button and we're in the setting stage
			
			if(board.verify()){//verify placement, and if it passes..
				board.disableAIBox();
				if(board.isAI()){
					Object[] options = {"Red", "Blue"};
					int n = JOptionPane.showOptionDialog(null,
				            "Choose AI Colour: ",
				            "AI Colour",
				            JOptionPane.YES_NO_CANCEL_OPTION,
				            JOptionPane.DEFAULT_OPTION,
				            null,
				            options,
				            options[1]); 
					if(n==0)
						board.createAI("red");
					else
						board.createAI("blue");
				}
				board.getData().setState(State.MOVING); //update state to turn taking moving
				int turn = (int) (Math.random()*2); //randomly pick the turn
				if(turn==0)
					board.setPlayerTurn("red");
				else
					board.setPlayerTurn("blue");
				board.removeUnplaced();
			}
		}
		board.gameCheck();
	}

	//unused mouselistener methods
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	
	//method for determinign if the mouse is within a certain coordinate range
	public static boolean isWithin(Point mouse, int x1,int x2,int y1,int y2){
		if(mouse.getX()>=x1 && mouse.getX()<=x2 && mouse.getY()>=y1 && mouse.getY()<=y2)
			return true;
		return false;
	}
}
