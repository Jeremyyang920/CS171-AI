import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;
import java.util.*;

//Testing Commit To Branch
public class RNGAI extends CKPlayer {

	public RNGAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "RNGAI";
	}

	@Override
	public Point getMove(BoardModel state)
	{
		Point best=bestMove(6,state,this.player);
		return best;
	}

	@Override
	public Point getMove(BoardModel state, int deadline) 
	{
		
		return getMove(state);
	}
	
	
	public byte oppositePlater(byte currentPlayer)
	{
		if(currentPlayer==1)
			return 2;
		else 
			return 1;
	}
	
	public ArrayList<Point> getRemainingMoves(BoardModel state)
	{
		ArrayList<Point> remainingMoves = new ArrayList<Point>();
		int width=state.getWidth();
		int height =state.getHeight();
		if(state.gravityEnabled())
		{
			for(int c=0; c<width;c++)
			{
				if(state.getSpace(c, height-1)==0)
					remainingMoves.add(new Point(c,height-1));
			}
		}
		else
		{
			for(int r=0;r<width;r++)
			{
				for(int c=0;c<height;c++)
				{
					if(state.getSpace(r, c)==0)
					{
						remainingMoves.add(new Point(r,c));
					}
				}
			}
		}
		return remainingMoves;
	}
	
	public int EvaluateBoard(BoardModel state, byte player)
	{
		Random rand = new Random();
		return rand.nextInt(101);
	}
	
	public Point bestMove(int depth,BoardModel state, byte player)
	{
		
		byte opposite = (byte) ((player == 1) ? 2:1);
		HashMap<Point, Integer> legalMoves=new HashMap<Point, Integer>();
		int width=state.getWidth();
		int height =state.getHeight();
		ArrayList<Point> valid = getRemainingMoves(state);
		for(int r=0;r<width;r++)
		{
			for(int c=0;c<height;c++)
			{
				if(valid.contains(new Point(r,c)))
				{
					BoardModel newState = state.clone();
					newState=newState.placePiece(new Point(r,c), player);
					legalMoves.put(new Point(r,c), AlphaBeta(state,player,depth,Integer.MIN_VALUE,Integer.MAX_VALUE,true));
				}
			}
		}
		int bestAlpha=-1000000;
		Point bestMove= null;
		for(Point move:legalMoves.keySet())
		{
			if(legalMoves.get(move)>bestAlpha)
			{
				bestAlpha=legalMoves.get(move);
				bestMove=move;
			}
		}
		return bestMove;		
	}
	
	public int minMax(int depth,BoardModel state,byte player)
	{
		ArrayList<BoardModel> legalMoves = new ArrayList<BoardModel>();
		int width=state.getWidth();
		int height =state.getHeight();
		ArrayList<Point> valid = getRemainingMoves(state);

		for(int r=0;r<width;r++)
		{
			for(int c=0;c<height;c++)
			{
				if(valid.contains(new Point(r,c)))
				{
					BoardModel newState = state.clone();
					newState=newState.placePiece(new Point(r,c), player);
					legalMoves.add(newState);
				}
			}
		}
		if(state.hasMovesLeft())
		{
			if(depth==0 || legalMoves.size()==0 || state.winner()!=-1)
			{
				return EvaluateBoard2(state,player);
			}
		}
		byte opposite = (byte) ((player == 1) ? 2:1);
		int alpha=-1000000;
		for (BoardModel newState:legalMoves)
			alpha=Math.max(alpha, -minMax(depth-1,newState,opposite));
		
		return alpha;
	}
	
	public int AlphaBeta(BoardModel state,byte player,int depth,int alpha,int beta,boolean maximizing)
	{
		ArrayList<Point> valid = getRemainingMoves(state);
		int width=state.getWidth();
		int height =state.getHeight();
		if(depth==0 || valid.size()==0)
		{
			return EvaluateBoard2(state,player);
		}
		if(maximizing)
		{
			int bestValue=Integer.MIN_VALUE;
			for(int r=0;r<width;r++)
			{
				for(int c=0;c<height;c++)
				{
					if(valid.contains(new Point(r,c)))
					{
						BoardModel newState=state.clone();
						newState=newState.placePiece(new Point(r,c), player);
						bestValue=Math.max(bestValue, AlphaBeta(newState,player,depth-1,alpha,beta,false));
						alpha=Math.max(alpha,bestValue);
						if(beta<=alpha)
							break;
					}
				}
			}
			return bestValue;
		}
			else
			{
				int bestValue=Integer.MAX_VALUE;
				for(int r=0;r<width;r++)
				{
					for(int c=0;c<height;c++)
					{
						if(valid.contains(new Point(r,c)))
						{
							BoardModel newState=state.clone();
							newState=newState.placePiece(new Point(r,c), player);
							bestValue=Math.max(bestValue, AlphaBeta(newState,player,depth-1,alpha,beta,true));
							beta=Math.min(beta,bestValue);
							if(beta<=alpha)
								break;
						}
					}
				}
				return bestValue;
			}
	}
	
	public int EvaluateBoard2(BoardModel state,byte player)
	{
		byte opposite = (byte) ((player == 1) ? 2:1);
		int myFour=checkStreak(state,player,5);
		int myThree=checkStreak(state,player,4);
		int myTwo=checkStreak(state,player,3);
		int opFour=checkStreak(state,opposite,5);
		int opThree=checkStreak(state,opposite,4);
		int opTwo=checkStreak(state,opposite,3);

		if(opFour >0)
			return -10000;
		else
			return (myFour*10000+myThree*100+myTwo*10)-(opFour*10000+opThree*100+opTwo*10);
	}
	
	public int checkStreak(BoardModel state, byte player, int streak)
	{
		int count=0;
		int width=state.getWidth();
		int height =state.getHeight();
		for(int r=0;r<width;r++)
		{
			for(int c=0;c<height;c++)
			{
				if(state.getSpace(r, c)==player)
				{
					count+=vertical(r,c,state,streak);
					count+= horizontal(r,c,state,streak);
				}
			}
		}
		return count;
	}
	public int vertical(int row, int col, BoardModel state,int streak)
	{
		int count=0;
		for(int i=0;i<state.getWidth();i++)
		{
			if(state.getSpace(i, col)==state.getSpace(row, col))
			{
				count+=1;
			}
			else
				break;
		}
		if (count>=streak)
				return 1;
		else
			return 0;
	}
	public int horizontal(int row, int col, BoardModel state,int streak)
	{
		int count=0;
		for(int i=0;i<state.getHeight();i++)
		{
			if(state.getSpace(row, i)==state.getSpace(row, col))
			{
				count+=1;
			}
			else
				break;
		}
		if (count>=streak)
				return 1;
		else
			return 0;
	}
}
