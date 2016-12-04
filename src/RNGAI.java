
import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;
import java.io.IOException;
import java.util.*;
import java.awt.geom.*;

public class RNGAI extends CKPlayer {
	
	public static boolean timeCutoff=false;
	private final int POSITIVE_INFINITY = Integer.MAX_VALUE;
	private final int NEGATIVE_INFINITY = Integer.MIN_VALUE;

	public RNGAI(byte player, BoardModel state)
	{
		super(player, state);
		teamName = "RNGAI";
	}

	@Override
	public Point getMove(BoardModel state)
	{
	
		return null;
	}

	@Override
	public Point getMove(BoardModel state, int deadline) 
	{
		Point result = null;
		long start = System.currentTimeMillis();
		byte opposite = oppositePlayer(this.player);
		int width = state.getWidth();
		int height = state.getHeight();
		Point mid = new Point(state.getWidth()/2, state.getHeight()/2);
		if (state.spacesLeft >= width * height - 2)
		{
			result = getOpeningMoves(state);
			if (result != null)
				return result;
		}
		result = searchMove(state, deadline, start);
		
		//Uncomment below for testing
		long elapsedMillys = System.currentTimeMillis() - start;
		float seconds = elapsedMillys / 1000f;
		System.out.println("Time: " + seconds);

		return result;

	}
	
	public byte oppositePlayer(byte currentPlayer)
	{
		return (byte) ((currentPlayer == 1) ? 2:1);
	}
	
	public Point getOpeningMoves(BoardModel state)
	{
		ArrayList<Point> remainingMoves = new ArrayList<Point>();
		int width = state.getWidth();
		int height = state.getHeight();
		
//		PotentialPQ orderedTurnTwoMoves = new PotentialPQ();
		Point middle = new Point(width/2, height/2);
//		Point2D.Double mid = new Point2D.Double((width-1)/2.0, (height-1)/2.0);	// used to calculate distances
		Point last = null;
		while (state.spacesLeft < width * height && last == null)		// wait until lastMove gets returned
			last = state.getLastMove();
//		System.out.println(last);
		
		if (state.spacesLeft == width * height)
		{
			remainingMoves.add(new Point(width/2, height/2));
		}
		else if (state.spacesLeft == width * height - 1)	// we went second
		{
//			for (Point move : validDiasearchnalCell(state, last.x, last.y))
//			{
//				double d = -distance(move, mid);
//				orderedTurnTwoMoves.add( new Potential( new Point2D.Double(d, 0), move) );
//			}
//
//			while (!orderedTurnTwoMoves.isEmpty())
//				remainingMoves.add( orderedTurnTwoMoves.poll().getPoint() );
			remainingMoves = validDiasearchnalCell(state, last.x, last.y);
		}
		else if (state.spacesLeft == width * height - 2)
		{
			ArrayList<Point> validDiasearchnals = validDiasearchnalCell(state, middle.x, middle.y);
			Point dir = new Point(last.x - middle.x, last.y - middle.y);
			Point mirror = new Point(middle.x - dir.x, middle.y - dir.y);
			
			// If opponent played adjacent move, get intersection of the diasearchnal cells 
			// of mid, and adjacent cells of the opponent's last played move
			if (dir.x == 0 || dir.y == 0)
			{
				for (Point move : validAdjacentCell(state, last.x, last.y))
					if (validDiasearchnals.contains(move))
						remainingMoves.add(move);
			}
			// If opponent played diasearchnal move, return another a non-mirror diasearchnal move
			else
				for (Point move : validDiasearchnals)
					if (move.x != mirror.x || move.y != mirror.y)
						remainingMoves.add(move);
		}
		
//		for (Point p : remainingMoves)
//			System.out.println("Opening moves: " + p);
//		System.out.println();
		
		return remainingMoves.size() == 0 ? null : remainingMoves.get(0);
	}
	
	public ArrayList<Point> getRemainingMoves(BoardModel state)
	{
		ArrayList<Point> remainingMoves = new ArrayList<Point>();
		ArrayList<Point> immediateMoves = new ArrayList<Point>();
		int width = state.getWidth();
		int height = state.getHeight();
		int k = state.getkLength();
		byte opposite = oppositePlayer(this.player);
		
		if (state.gravity)
			// Adds the center column first since we order by increasing distance from the middle
			// Then adds the piece to the left and to the right until the boundary of the row.
		{
			int mid = width/2;
			if(state.pieces[mid][height-1]==0)
				remainingMoves.add(new Point(mid, height-1));
			for (int i = 1; i <= mid; i++)
			{
				if (mid - i >= 0 && state.getSpace(mid - i, height-1) == 0)
					remainingMoves.add(new Point(mid - i, height-1));
				if (mid + 1 < width && state.getSpace(mid + i, height-1) == 0)
					remainingMoves.add(new Point(mid + i, height-1));
			}
			return remainingMoves;
		}
		else
		{
			PotentialPQ orderedAdjacent = new PotentialPQ();
			PotentialPQ orderedPotentialWins = new PotentialPQ(), orderedOppPotentialWins = new PotentialPQ();
			Point2D.Double mid = new Point2D.Double(width/2, height/2);
			
			ArrayList<Point> myJunctions = new ArrayList<Point>(), oppJunctions = new ArrayList<Point>();
			PotentialPQ orderedJunctions = new PotentialPQ(), orderedOppJunctions = new PotentialPQ();
			
			ArrayList<Point> myStreakLocations = streakLocations(state, this.player, this.player, k);
			if (myStreakLocations.size() > 0)
				return myStreakLocations;
			
			ArrayList<Point> oppStreakLocations = streakLocations(state, opposite, this.player, k);
			if (oppStreakLocations.size() > 0)
				return oppStreakLocations;
			
			for (Point junction : getJunctions(state, this.player))
				orderedJunctions.add( new Potential( new Point2D.Double( -distance(junction, mid), 0), junction));
			while (!orderedJunctions.isEmpty())
				myJunctions.add( orderedJunctions.poll().getPoint() );
			
			for (Point junction : getJunctions(state, opposite))
				orderedOppJunctions.add( new Potential( new Point2D.Double( -distance(junction, mid), 0), junction));
			while (!orderedJunctions.isEmpty())
				oppJunctions.add( orderedOppJunctions.poll().getPoint() );

			for (Point myPotentialWin : streakLocations(state, this.player, this.player, k-1))
				orderedPotentialWins.add( new Potential( 
						new Point2D.Double( -distance(myPotentialWin, mid), 0), myPotentialWin) );
			
			for (Point oppPotentialWin : streakLocations(state, opposite, this.player, k-1))
			{
				BoardModel child = state.clone();
				child.placePiece(oppPotentialWin, opposite);
				double count = adjacentCount(child, oppPotentialWin.x, oppPotentialWin.y, opposite);
				double d = -distance(oppPotentialWin, mid);
				Point2D.Double criterion = new Point2D.Double(count, d);
				orderedOppPotentialWins.add( new Potential( criterion, oppPotentialWin) );
			}
			
			while (!orderedPotentialWins.isEmpty())
			{
				Point temp = orderedPotentialWins.poll().getPoint();
				if (myJunctions.contains(temp))
				{
					immediateMoves.add(temp);
					return immediateMoves;
				}
				remainingMoves.add(temp);
			}
			
			while (!orderedOppPotentialWins.isEmpty())
			{
				Point temp = orderedOppPotentialWins.poll().getPoint();
				if (oppJunctions.contains(temp))
				{
					immediateMoves.add(temp);
					return immediateMoves;
				}
				remainingMoves.add(temp);
			}
			
			remainingMoves.addAll(myJunctions);
			remainingMoves.addAll(oppJunctions);

			for (int r = 0; r < width; r++)
				for (int c = 0; c < height; c++)
					if (state.getSpace(r, c) == 0)
					{
						Point move = new Point(r, c);
						double d = -distance(move, mid);					
						if (validEmptyCell(state, r, c))
							orderedAdjacent.add( new Potential( new Point2D.Double(d, 0), move) );
					}
			
			if (remainingMoves.size() == 0)
				while (!orderedAdjacent.isEmpty())
					remainingMoves.add( orderedAdjacent.poll().getPoint() );
		}
					
		return remainingMoves;
	}
	
	public double distance(Point point1, Point2D.Double point2)
	{
		return Math.sqrt(Math.pow(point2.x - point1.x, 2) + Math.pow(point2.y - point1.y, 2));
	}
	
	public int heuristic(BoardModel state, boolean print)
	{
		int width = state.getWidth();
		int height = state.getHeight();
		int k = state.getkLength();
		byte opposite = oppositePlayer(this.player);

		int[] myStreaks = streaks(state, this.player, false);
		int[] oppStreaks = streaks(state, opposite, false);
		int myTotal = 0, oppTotal = 0;
		
		if (print)
		{
			System.out.print("My Streaks: ");
			for (int i : myStreaks)
				System.out.print(i + " ");
			System.out.println();
			System.out.print("Opp Streaks: ");
			for (int i : oppStreaks)
				System.out.print(i + " ");
		}

		if (oppStreaks[k] > 0)
			return Integer.MIN_VALUE;
		else if (myStreaks[k] > 0)
			return Integer.MAX_VALUE;
		
//		Point p1 = null, p2 = null, p3 = null, p4 = null;
		Point p2 = null, p4 = null;
		
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			{
				if (junctionFromMid(state, x, y, this.player, false))
					p2 = new Point(x, y);
				if (junctionFromMid(state, x, y, opposite, false))
					p4 = new Point(x, y);
			}
		
		if (print)
		{
			System.out.println();
			System.out.println("My Junction Empty: " + p2);
			System.out.println("Opp Junction Empty: " + p4);
			System.out.println();
		}

		if (p2 != null)
			myTotal += 100;

		if (p4 != null)
			oppTotal += 200;
		
		if (print)
			System.out.println("Current totals: " + myTotal + " , " + oppTotal + "\n");

		
		for (int index = 2; index < myStreaks.length; index++)
		{
			myTotal += myStreaks[index] * Math.pow(2, index-1);
			oppTotal += oppStreaks[index] * Math.pow(2, index-1);
			if (print)
				System.out.println(myTotal + "  " + oppTotal);
		}

		return myTotal - oppTotal;
	}
	
	public boolean validCol(int col)
	{
		return 0 <= col && col < this.startState.getWidth();
	}
	
	public boolean validRow(int row)
	{
		return 0 <= row && row < this.startState.getHeight();
	}
	
	public boolean validCell(int row, int col)
	{
		return validRow(row) && validCol(col);
	}
	
	public int[] streaks(BoardModel state, byte player, boolean print)
	{
		int k = state.getkLength();
		int width = state.getWidth();
		int height = state.getHeight();
		byte opposite = oppositePlayer(player);
		
		int[] result = new int[k+1];
		
		// search through each row and check horizontals for streaks
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
			{
				int count = 0;
				int tempx = x;
				boolean add = (state.getSpace(tempx, y) == player);
				while (validCol(tempx) && tempx < x+k && add)
				{
					if (state.getSpace(tempx, y) == player)
						count++;
					else if (state.getSpace(tempx, y) == opposite)
//					else if ((!validCol(x-1) || state.getSpace(x-1, y) == opposite) || state.getSpace(tempx, y) == opposite)
						add = false;
					tempx++;
				}
	
				if (add && count > 0)
					result[count]++;
			}
		
		// search through each column and check verticals
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			{
				int count = 0;
				int tempy = y;
				boolean add = (state.getSpace(x, tempy) == player);
				while (validRow(tempy) && tempy < y+k && add)
				{
					if (state.getSpace(x, tempy) == player)
						count++;
					else if (state.getSpace(x, tempy) == opposite)
//					else if ((!validRow(y-1) || state.getSpace(x, y-1) == opposite) || state.getSpace(x, tempy) == opposite)
						add = false;
					tempy++;
				}
	
				if (add && count > 0)
					result[count]++;
			}

		// Check bottom left to top right diasearchnal
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
			{
				int count = 0;
				int tempx = x, tempy = y;
				boolean add = (state.getSpace(tempx, tempy) == player);
				while (validCell(tempy, tempx) && tempx < x+k && tempy < y+k && add)
				{
					if (state.getSpace(tempx, tempy) == player)
						count++;
					else if (state.getSpace(tempx, tempy) == opposite)
//					else if ((!validCell(y-1, x-1) || state.getSpace(x-1, y-1) == opposite) || 
//							state.getSpace(tempx, tempy) == opposite)
						add = false;
					tempx++; tempy++;
				}
	
				if (add && count > 0)
					result[count]++;
			}

		// Check bottom right to top left diasearchnal
		// x is constrained to bottom right square
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
			{
				int count = 0;
				int tempx = x, tempy = y;
				boolean add = (state.getSpace(tempx, tempy) == player);
				while (validCell(tempy, tempx) && tempx > x-k && tempy < y+k && add)
				{
					if (state.getSpace(tempx, tempy) == player)
						count++;
					else if (state.getSpace(tempx, tempy) == opposite)
//					else if ((!validCell(y-1, x+1) || state.getSpace(x+1, y-1) == opposite) || 
//							state.getSpace(tempx, tempy) == opposite)
						add = false;
					tempx--; tempy++;
				}

				if (add && count > 0)
					result[count]++;
			}	
		
		return result;
	}
	
	// Primarily used to check if a space is a winning location for a connect-k
	// but is generalized to search for streaks based on the input parameter.
	public boolean streakLocation(BoardModel state, int x, int y, byte player, byte currentPlayer, int streak)
	{
		int k = state.getkLength();
		byte opposite = oppositePlayer(player);
		int maxspace = player == currentPlayer ? k - streak : 0;
		
		if (state.pieces[x][y] != 0)
			return false;
		 
		BoardModel child = state.clone();
		Point move = new Point(x, y);
		child = child.placePiece(move, player);
		
		ArrayList<Point> forwardDirections = new ArrayList<Point>(){{
			add(new Point(1,0));
			add(new Point(0,1));
			add(new Point(1,1));
			add(new Point(1,-1));
		}};
		
		for (Point dir : forwardDirections)
		{
			int count = -1;		// current piece is double counted
			int tempx = x, tempy = y;
			int blocked = 0;
			int current_space = 0, spaces_travelled = 0;
			Point first = null, second = null, edpt1 = null, edpt2 = null;
			
			while(validCell(tempy, tempx)) // count all in forward direction
			{
				if (child.getSpace(tempx, tempy) == opposite || current_space > maxspace)
					break;
				else if (child.getSpace(tempx, tempy) == player)
				{
					count++;
					spaces_travelled = current_space;
					first = new Point(tempx, tempy);
				}
				else if (child.getSpace(tempx, tempy) == 0)
					current_space++;
				tempx += dir.x;
				tempy += dir.y;
			}
			edpt1 = first == null ? new Point(x + dir.x, y + dir.y) : new Point(first.x + dir.x, first.y + dir.y);
			if (!validCell(edpt1.y, edpt1.x) || child.getSpace(edpt1) == opposite)
				blocked++;
			
			if (count < streak)
				current_space = spaces_travelled;
			else
				edpt2 = new Point(x - dir.x, y - dir.y);
			
			tempx = x; tempy = y;
			while(validCell(tempy, tempx)) // count all in reverse direction
			{
				if (child.getSpace(tempx, tempy) == opposite || current_space > maxspace)
					break;
				else if (child.getSpace(tempx, tempy) == player)
				{
					count++;
					second = new Point(tempx, tempy);
				}
				else if (child.getSpace(tempx, tempy) == 0)
					current_space++;
				tempx -= dir.x;
				tempy -= dir.y;
			}
			if (edpt2 == null)
				edpt2 = second == null ? new Point(x - dir.x, y - dir.y) : 
					new Point(second.x - dir.x, second.y - dir.y);
			if (!validCell(edpt2.y, edpt2.x) || child.getSpace(edpt2) == opposite)
				blocked++;
			
			// Given that count >= streak, if we're trying to find potential streak locations,
			// return true if the streak is not capped. If the current player is searching for
			// their own streak of k-1, return true, otherwise return true only if blocked == 0.
			if (count >= streak)
//				return streak == k-1 ? (player == currentPlayer ? blocked < 2 : blocked == 0) : true;
				if (streak == k-1)
					return player == currentPlayer ? (current_space == 0 ? blocked < 2 : true) : blocked == 0;
				else
					return true;
//			if (count >= streak)
//				return streak == k-1 ? (player == currentPlayer ? true : blocked == 0) : true;
		}
		
		return false;
	}
	
	public ArrayList<Point> streakLocations(BoardModel state, byte player, byte currentPlayer, int streak)
	{
		ArrayList<Point> result = new ArrayList<Point>();
		for (int x = 0; x < state.getWidth(); x++)
			for (int y = 0; y < state.getHeight(); y++)
				if (streakLocation(state, x, y, player, currentPlayer, streak))
					result.add(new Point(x,y));
		return result;
	}
	
	public boolean junctionFromMid(BoardModel state, int x, int y, byte player, boolean print)
	{
		boolean result = false;
		byte opposite = oppositePlayer(player);
		
		if (state.pieces[x][y] != 0)
			return result;
		
		int width = state.getWidth();
		int height = state.getHeight();
		int k = state.getkLength();
		int connections = 0;
		int chain = Math.max(k-3, 1);		// chain increases as k increases
		int maxspace = k - chain - 1; 		// maxspace = 2 for k > 3
		int capped = 0;
		
		ArrayList<Point> forwardDirections = new ArrayList<Point>(){{
			add(new Point(1,0));
			add(new Point(0,1));
			add(new Point(1,1));
			add(new Point(1,-1));
		}};
		
		for (Point dir : forwardDirections)
		{
			int blocked = 0;
			int current_chain = 0;
			int current_space = 0, spaces_travelled = 0;	
				// counts number of empty spaces travelled to get to a piece of the same color
			Point first = null, second = null, edpt1 = null, edpt2 = null;
			boolean skip1 = false, skip2 = false;
			
			int tempx = x, tempy = y;
			while(validCell(tempy, tempx)) // count all in forward direction
			{
				if (state.getSpace(tempx, tempy) == opposite)
				{
					edpt1 = new Point(tempx, tempy); skip1 = true;
					break;
				}
				if (current_space > maxspace)
					break;
				else if (state.getSpace(tempx, tempy) == player)
				{
					current_chain++;
					spaces_travelled = current_space; // update spaces travelled when you get to your own piece
					first = new Point(tempx, tempy);
				}
				else if ((tempx != x || tempy != y) && state.getSpace(tempx, tempy) == 0)
					current_space++;
				tempx += dir.x;
				tempy += dir.y;
			}
			
			if (current_chain < chain)
				current_space = spaces_travelled;	// 'reset' current space
			
			tempx = x; tempy = y;
			while(validCell(tempy, tempx)) // count all in reverse direction
			{
				if (state.getSpace(tempx, tempy) == opposite)
				{
					edpt2 = new Point(tempx, tempy); skip2 = true;
					break;
				}
				if (current_space > maxspace)
					break;
				else if (state.getSpace(tempx, tempy) == player)
				{
					current_chain++;
					spaces_travelled = current_space;
					second = new Point(tempx, tempy);
				}
				else if ((tempx != x || tempy != y) && state.getSpace(tempx, tempy) == 0)
					current_space++;
				tempx -= dir.x;
				tempy -= dir.y;
			}
			current_space = spaces_travelled; 	// reset current space to max allowed
				
			if (print)
				System.out.println("First: " + first + " " + "Second: " + second + "Current space: " + current_space);
			
			if (first == null)
				first = new Point(x, y);
			if (second == null)
			{
				second = new Point(x, y);
				current_space = 0;
			}
			
			int jump = chain - current_chain + maxspace - current_space + 1;
			edpt1 = edpt1 == null ? new Point(first.x + jump * dir.x, first.y + jump * dir.y) : edpt1;
			edpt2 = edpt2 == null ? new Point(second.x - jump * dir.x, second.y - jump * dir.y) : edpt2;
			
			if (print)
				System.out.println("Initial endpoints: " + edpt1 + " " + edpt2 + "Jump: " + jump);
			
			if (!validCell(edpt1.y, edpt1.x))
				while (edpt1.x > width || edpt1.y > height)
				{
					edpt1.x -= dir.x; 
					edpt1.y -= dir.y;
				}
			else // if valid cell
				while (!skip1 && state.getSpace(edpt1) == opposite)
				{
					edpt1.x -= dir.x; 
					edpt1.y -= dir.y;
				}
			
			if (!validCell(edpt2.y, edpt2.x))
				while (edpt2.x > width || edpt2.y > height)
				{
					edpt2.x += dir.x; 
					edpt2.y += dir.y;
				}
			else // if valid cell
				while (!skip2 && state.getSpace(edpt2) == opposite)
				{
					edpt2.x += dir.x; 
					edpt2.y += dir.y;
				}
			
			// Not enough space to allow for a streak k when exploring potential streak
			if (current_chain >= 0 && 
					Math.max( Math.abs(edpt2.x - edpt1.x) - 1, Math.abs(edpt2.y - edpt1.y) - 1 ) < k)
				capped++; 
			
			if (print)
				System.out.println(edpt1 + " " + edpt2 + " " + capped);
			
			if (current_chain >= chain && capped == 0)	// if the streak is not capped
				connections++;
		}
		if (connections >= 2)
			return true;
		
		return result;
	}
	
	public ArrayList<Point> getJunctions(BoardModel state, byte player)
	{
		ArrayList<Point> result = new ArrayList<Point>();
		for (int x = 0; x < state.getWidth(); x++)
			for (int y = 0; y < state.getHeight(); y++)
				if (junctionFromMid(state, x, y, player, false)) 
					result.add(new Point(x,y));
		return result;
	}
	
	public ArrayList<Point> validAdjacentCell(BoardModel state, int x, int y)
	{
		ArrayList<Point> result = new ArrayList<Point>();
		
		ArrayList<Point> directions = new ArrayList<Point>(){{
			add(new Point(1,0));
			add(new Point(0,1));
			add(new Point(0,-1));
			add(new Point(-1,0));
		}};
		
		for (Point dir : directions)
		{
			int tempx = x + dir.x, tempy = y + dir.y;
			if (validCell(tempy, tempx) && state.getSpace(tempx, tempy) == 0)
				result.add( new Point(tempx, tempy) );
		}
		return result;
	}
	
	public ArrayList<Point> validDiasearchnalCell(BoardModel state, int x, int y)
	{
		ArrayList<Point> result = new ArrayList<Point>();
		
		ArrayList<Point> directions = new ArrayList<Point>(){{
			add(new Point(1,1));
			add(new Point(1,-1));
			add(new Point(-1,-1));
			add(new Point(-1,1));
		}};
		
		for (Point dir : directions)
		{
			int tempx = x + dir.x, tempy = y + dir.y;
			if (validCell(tempy, tempx) && state.getSpace(tempx, tempy) == 0)
				result.add( new Point(tempx, tempy) );
		}
		return result;
	}
	
	public boolean validEmptyCell(BoardModel state, int x, int y)
	{
		boolean result = false;
		
		ArrayList<Point> directions = new ArrayList<Point>(){{
			add(new Point(1,0));
			add(new Point(0,1));
			add(new Point(0,-1));
			add(new Point(-1,0));
			add(new Point(1,1));
			add(new Point(1,-1));
			add(new Point(-1,-1));
			add(new Point(-1,1));
		}};
		
		for (Point dir : directions)
		{
			int tempx = x + dir.x, tempy = y + dir.y;
			if (validCell(tempy, tempx) && state.getSpace(tempx, tempy) != 0)
				return true;
		}
		return result;
	}
	
	public int adjacentCount(BoardModel state, int x, int y, byte player)
	{
		int count = 0;
		
		ArrayList<Point> directions = new ArrayList<Point>(){{
			add(new Point(1,0));
			add(new Point(0,1));
			add(new Point(1,1));
			add(new Point(1,-1));
			add(new Point(0,-1));
			add(new Point(-1,-1));
			add(new Point(-1,0));
			add(new Point(-1,1));
		}};
		
		for (Point dir : directions)
		{
			int tempx = x + dir.x, tempy = y + dir.y;
			if (validCell(tempy, tempx) && state.getSpace(tempx, tempy) == player)
				count++;
		}
		return count;
	}
	

	//search for and will try and return the best move.
	// there is a small glitch on bigger sized boards if we are found to just have lost the game
	// it will return 0,0 as a sign of giving up. Sometimes if 0,0 has been played already though
	//we will end up returning a bad move. But we think this only happens after a 10x10 board since we set the best to 0,0 at the beginning 
	public Point ids(BoardModel state, int deadline, long start)
	{
		boolean search = true;
		Potential best = new Potential(new Point2D.Double(0, 0),new Point(0,0));
		Potential temp = new Potential(new Point2D.Double(0, 0),new Point(0,0));
		int depth = 1;
		int moveSize=state.getHeight()*state.getWidth();
		
		Potential[] potentialMoves = new Potential[moveSize];
		int size = 0;
		
		while (search)
		{
			temp = baseReturn(state, NEGATIVE_INFINITY, POSITIVE_INFINITY, depth, deadline, start);
			boolean backtrack=true;
			potentialMoves[size] = temp;
			size++;
			
			if (temp.getValues().getX() == 10000)

				return temp.getPoint();
			
			if (temp.getValues().getX() == 0)

				return temp.getPoint();
			
			if (temp.getValues().getX() == -123456)
			{
				--size;
				//Since we timed out, backtrack to find a value that is not a lose value.
				//If the current best move is a losing value, find the first one in potentialMoves that is not a losing value then return it.
				//Make sure we reduce the size when we backtrack otherwise we will go out of bounds.

				if (best.getValues().getX() == -10000)
				{
					
					while (size - 1 >= 0 && backtrack)
					{
						if (potentialMoves[size - 1].getValues().getX() != -10000)
						{
							best = potentialMoves[size - 1];
							backtrack = false;
						}
						--size;
					}
				}
				return best.getPoint();
			}
			
			//if we get a lose value, we want to return a move that doesnt make us lose.
			//backtrack and find the first move that is not a lose val.
			if (temp.getValues().getX() == -10000)
			{
	
				
				while (size - 1 >= 0 && backtrack)
				{
					if (potentialMoves[size - 1].getValues().getX() != -10000)
					{
						best = potentialMoves[size - 1];
						backtrack = false;
					}
					--size;
				}
				return best.getPoint();
			}
			
			
			best = temp;
			//increse the depth.
			depth+=1;
			
			long currentTime = System.currentTimeMillis() - start + 100;
			if (currentTime >= deadline)
				search = false;
		}


		//if even after the search our best move is still a losing situation,we just return the first move that doesnt have a losing value.
		if (best.getValues().getX() == -10000)
		{
			boolean backtrack=true;
			while (size - 1 >= 0 && backtrack)
			{
				if (potentialMoves[size - 1].getValues().getX() != -10000)
				{
					best = potentialMoves[size - 1];
					backtrack = false;
				}
				--size;
			}
		}
		

		return best.getPoint();
		
	}
	
	
	//this is the first call that is made in ids.
	//this is where it actually returns a Potential move where it contains the heuristic from the ABP and the point associated with it
	//we decided to include this for ease of readability since with min and max combined, the method become extremely long and debugging became more complicated.
	public Potential baseReturn(BoardModel state, int alpha, int beta, int limit, int deadline, long start)
	{
		int v = NEGATIVE_INFINITY;
		ArrayList<Point> myPoints = getRemainingMoves(state);

		Potential bestAct = new Potential(new Point2D.Double(0, 0),new Point(0,0));

		for (Point p:myPoints)
		{
			if (state.getSpace(p) == 0)
			{
				BoardModel temp = state.placePiece(p, player);
				byte isWinner = temp.winner();
				
				if (isWinner == player)
					return new Potential(new Point2D.Double(10000,0), p);
				else if (isWinner == oppositePlayer(player))
					v = Math.max(v, -10000);
				else if (isWinner == 0)
					v = Math.max(v, 0);
				else
					v = Math.max(v, minVal(temp, alpha, beta, limit - 1, deadline, start));
				
				if (v == 10000)
					return new Potential(new Point2D.Double(v,0), p);
				
				long currentTime = System.currentTimeMillis() - start + 100;
				if (currentTime >= deadline)
					return new Potential(new Point2D.Double(-123456,0), new Point(0, 0));
				
				if (v > alpha)
					bestAct = new Potential(new Point2D.Double(v,0), p);
				alpha = Math.max(alpha, v);
			}
		}
		
		return bestAct;
	}
	
	//checks if a node is quiet. We defined quiet as neither player having a k-1 streak.
	public boolean quiet(BoardModel state,byte player)
	{
		return streakLocations(state,player,player,state.kLength-1).size()==0 &&
				streakLocations(state,oppositePlayer(player),oppositePlayer(player),state.kLength-1).size()==0;
	}
	
	
	//if a node was not quiet, we evaluated a 2 depth mini max to further check it. 
	public int quiescence(BoardModel state, int depth,boolean maximizing,long deadline,long start)
	{
		
		ArrayList<Point> moves = getRemainingMoves(state);
		if(depth==0||quiet(state,player) || moves.size()==0 || !state.hasMovesLeft() )
			return (int) heuristic(state,false);
		else
		{
			if(maximizing)
			{
				int v = Integer.MIN_VALUE;
				for(Point move : moves)
				{
					BoardModel copy = state.clone();
					copy = copy.placePiece(move, this.player);
					byte isWinner = copy.winner();
					long currentTime = System.currentTimeMillis() - start + 100;
					if (currentTime >= deadline)
						return -123456;
					if (isWinner == player)
						return 10000;
					else if (isWinner == oppositePlayer(player))
						v = Math.max(v, -10000);
					else if (isWinner == 0)
						v = Math.max(v, 0);
					else
					 v = Math.max(v, quiescence(copy,depth-1,false,deadline,start));
				}
				return v;
			}
			else
			{
				int v = Integer.MAX_VALUE;
				Collections.reverse(moves);

				for(Point move : moves)
				{
					BoardModel copy = state.clone();
					copy = copy.placePiece(move, oppositePlayer(player));
					byte isWinner = copy.winner();
					long currentTime = System.currentTimeMillis() - start + 100;
					if (currentTime >= deadline)
						return -123456;
					if (isWinner == oppositePlayer(player))
						return -10000;
					else if (isWinner == player)
						v = Math.min(v, 10000);
					else if (isWinner == 0)
						v = Math.min(v, 0);
					else
					 v = Math.min(v, quiescence(copy,depth-1,true,deadline,start));
				}
				return v;
			}
		}
		
	}
	
	//max player for ABP
	
	public int maxVal(BoardModel state, int alpha, int beta, int limit, int deadline, long start)
	{
		ArrayList<Point> myPoints = getRemainingMoves(state);

		if (limit == 0)
		{
			if(quiet(state,this.player))
				return (int) heuristic(state,false);
			return quiescence(state,2,true,deadline,start);
		}
		else
		{
			int v = NEGATIVE_INFINITY;
			
			for (Point p:myPoints)
			{
				if (state.getSpace(p) == 0)
				{
					BoardModel temp = state.placePiece(p, player);
					byte isWinner = temp.winner();
					
					long currentTime = System.currentTimeMillis() - start + 100;
					if (currentTime >= deadline)
						return -123456;
					if (isWinner == player)
						return 10000;
					else if (isWinner == oppositePlayer(player))
						v = Math.max(v, -10000);
					else if (isWinner == 0) //game is a draw
						v = Math.max(v, 0);
					else
						v = Math.max(v, minVal(temp, alpha, beta, limit - 1, deadline, start));
					
					if (v >= beta)
						return v;
					alpha = Math.max(alpha, v);
				}
			}
			
			return v;
		}
		
	}
	
	// min player for abp. We reverse the moves for min so its sorted least to highest. 
	public int minVal(BoardModel state, int alpha, int beta, int limit, int deadline, long start)
	{
		ArrayList<Point> myPoints = getRemainingMoves(state);

		if (limit == 0)
		{
			if(quiet(state,oppositePlayer(player)))
				return (int) heuristic(state,false);
			return quiescence(state,2,false,deadline,start);
		}
		else
		{
			int v = POSITIVE_INFINITY;
			Collections.reverse(myPoints);
			for (Point p:myPoints)
			{
				if (state.getSpace(p) == 0)
				{
					BoardModel temp = state.placePiece(p, oppositePlayer(player));
					byte isWinner = temp.winner();
					
					long currentTime = System.currentTimeMillis() - start + 100;
					if (currentTime >= deadline)
						return -123456;
					
					if (isWinner == oppositePlayer(player))
						return -10000;
					else if (isWinner == player)
						v = Math.min(v, 10000);
					else if (isWinner == 0) //game is a draw
						v = Math.min(v, 0);
					else
						v = Math.min(v, maxVal(temp, alpha, beta, limit - 1, deadline, start));
					
					if (v <= alpha)
						return v;
					beta = Math.min(beta, v);
				}
			}
			
			return v;
		}
	}
	
	public Point searchMove(BoardModel state, int deadline, long start)
	{
	
		return ids(state, deadline, start);
	}
}

