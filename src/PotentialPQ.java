import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class PotentialPQ 
{
	
	public PriorityQueue<Potential> values;
	public PotentialPQ()
	{
			values = new PriorityQueue<Potential>(10, new Comparator<Potential>() {
		    public int compare(Potential p1, Potential p2) {
		    	Point2D values1 = p1.getValues(), values2 = p2.getValues();
		    	double a = values1.getX(), b = values1.getY(), c = values2.getX(), d = values2.getY();
		        if(a < c)
		        	return 1;
		        else if(a > c)
		        	return -1;
		        return b == d ? (b < c ? 1 : -1) : 0;
		    } 
		});
		
	}
	
	public void add(Potential p1)
	{
		values.add(p1);
	}
	
	public void clear()
	{
		values.clear();
	}
	
	public Potential poll()
	{
		return values.poll();
	}
	
	public Potential peek()
	{
		return values.peek();
	}
	
	public boolean isEmpty()
	{
		return values.isEmpty();
	}
	
	public int size()
	{
		return values.size();
	}
	
	public boolean hasPoint(Point p)
	{
		for (Potential potential : values)
			if (potential.getPoint() == p)
				return true;
		return false;
	}
}


