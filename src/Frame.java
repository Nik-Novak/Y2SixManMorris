import java.awt.AWTException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/*
 * DESIGN DECISIONS: We decided to implement a drag and drop method of moving discs as it was more streamlined (Mouse Class). We also decided to implement a custom button for starting the game (Draw.drawButtons()). For displaying error arrangement messages we chose to use JOptionPane popups (Board.verify).
 */
public class Frame extends JFrame{
	public Frame() {
		try {
			setFocusable(true);
			//create game board
			setDefaultCloseOperation(3);
			setVisible(true);
			Board board = new Board();
			getContentPane().add(board);//add game board to frame
			//more init
			pack();
			setLocationRelativeTo(null);
			setAlwaysOnTop(false);
			
			//Menu Items like Save Game, Reset, etc
			JMenuBar menuBar = new JMenuBar();
			setJMenuBar(menuBar);
			
			JMenu mnFile = new JMenu("File");
			menuBar.add(mnFile);
			
			JMenuItem mntmReset = new JMenuItem("Reset");
			mnFile.add(mntmReset);
			mntmReset.addActionListener(e -> {board.init();}); //reset teh board using its init
			
			JMenuItem mntmSaveGame = new JMenuItem("Save Game");
			mnFile.add(mntmSaveGame);
			
			mntmSaveGame.addActionListener(e -> {board.save();}); //call the board save method when clicked
			
			JMenuItem mntmLoadGame = new JMenuItem("Load Game");
			mnFile.add(mntmLoadGame);
			
			mntmLoadGame.addActionListener(e -> {board.load();}); //call teh boards load method
			
			JMenuItem mntmQuit = new JMenuItem("Quit");
			mnFile.add(mntmQuit);
			mntmQuit.addActionListener(e->{System.exit(0);});
			setVisible(true);
		} catch (AWTException e1) {
			e1.printStackTrace();
		}
	}
	public static void main(String[] args) throws AWTException{
		//create an initialize new JFrame
		new Frame();
	}
	
}