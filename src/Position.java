import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;

//represents places chips can go, and Chips also extend off tehse as they share similar properties
public class Position extends Ellipse2D.Double {
	//location values
	private int x;
	private int y;
	protected int radius = 8; //radius of Shape superclass
	private ArrayList<Position> connected; //all connections that this position has
	protected transient Board board; //board parent, children may use this
	private Chip chip; //chip this position holds

	public Position(int x, int y, Board board){ //init
		this.x=x;
		this.y=y;
		this.board = board;
		connected = new ArrayList<>();
	}
	
	public void addConnection(Position... positions){ //add an arbitrary number of positions to the list of connections
		for(Position s : positions)
			connected.add(s);
	}
	
	public String relPos(Position pos) { //returns the relative position of this chip to the passed in pos, "vertical" if vertically aligned, etc.
		if(getX() == pos.getX())
			return "vertical";
		if(getY() == pos.getY())
			return "horizontal";
		return null;
	}
	
	public void mouseWithin() { //fired when mouse is within component
		board.setSelected(this);
	}
	
	//GETTERS AND SETTERS
	@Override
	public Rectangle2D getBounds2D() {
		return new Rectangle(x,y,16,16);
	}

	@Override
	public double getHeight() {
		return 2*radius;
	}

	@Override
	public double getWidth() {
		// TODO Auto-generated method stub
		return 2*radius;
	}

	@Override
	public double getX() {
		// TODO Auto-generated method stub
		return x-radius;
	}

	@Override
	public double getY() {
		// TODO Auto-generated method stub
		return y-radius;
	}
	
	public Point getLoc(){
		return new Point(x, y);
	}
	
	public void setLocation(Point loc) {
		this.x=loc.x;
		this.y=loc.y;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFrame(double x, double y, double w, double h) {
		this.x=(int) x; this.y=(int) y;
	}

	protected void assign(Position position) {
		this.chip = (Chip)position;
	}

	public Chip getChip() {
		return chip;
	}

	public void setChip(Chip chip) {
		this.chip = chip;
	}
	public ArrayList<Position> getConnections(){
		return connected;
	}
}
