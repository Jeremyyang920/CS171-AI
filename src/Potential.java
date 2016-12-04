import java.awt.*;
import java.awt.geom.*;

public class Potential
{
	private Point2D pointValues;
	private Point move;
	
	public Potential(Point2D values, Point point)
	{
		pointValues = values;
		move = point;
	}
	
	public Point getPoint()
	{
		return move;
	}
	public Point2D getValues()
	{
		return pointValues;
	}
	public void setPoint(Point newMove)
	{
		move = newMove;
	}
	
	public void setValue(Point2D newVal)
	{
		pointValues = newVal;
	}
}
