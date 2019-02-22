
import java.awt.MouseInfo;
import java.awt.Point;

public class Update{//update class for all updates and management of game logic
	Board board;
	

	public Update(Board board) {
		this.board=board; //reference parent Board
	}

	public void update(){ //main update method
		updateMouse();
		updatePositions();
		updateChips();
		//System.out.println(board.getMouse());
		updateAI();
	}
	
	private void updateAI() {
		if(board.isAI() && board.getData().state()!=State.SETTING){
			if(board.getPlayerTurn().equals(board.getAI().getColour()))
				board.getAI().move(board.getData());
			else if(board.getPlayerTurn().contains("Remove") && !board.getPlayerTurn().contains(board.getAI().getColour()))
				board.getAI().remove(board.getData());
		}
		
	}

	private void updateChips() {//updates all chips by checking if a mouse intersects with one, if so it sets it as board.selected
		//if there is a chip selected to be dragged...
		if(board.getClicked()!=null)
			board.getClicked().setLocation(board.getMouse());//make chip follow mouse
		for(int i=0; i!= board.getData().getBlue().size(); i++) //for each blue chip
			if(board.getData().getBlue().get(i).contains(board.getMouse())){
				board.getData().getBlue().get(i).mouseWithin(); //notify listener if mouse is on it
				return;
			}
		//same with red
		for(int i=0; i!= board.getData().getRed().size(); i++)
			if(board.getData().getRed().get(i).contains(board.getMouse())){
				board.getData().getRed().get(i).mouseWithin();
				return;
			}
	}

	private void updatePositions() {
		for(int i=0; i!= board.getPos().length; i++)//for each position object call mousewithin if the mouse is within
			if(board.getPos()[i].contains(board.getMouse())){
				board.getPos()[i].mouseWithin();
				return;
			}
			board.setSelected(null);
	}

	private void updateMouse() {//update global mouse location
		Point mSc = MouseInfo.getPointerInfo().getLocation();
		Point bL = board.getLocationOnScreen();
		board.setMouse(new Point((int)(mSc.getX()-bL.getX()),(int)(mSc.getY()-bL.getY())));
	}
	
}
