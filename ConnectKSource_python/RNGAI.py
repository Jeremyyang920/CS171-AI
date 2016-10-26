#Author: Toluwanimi Salako
from collections import defaultdict
import random, sys, copy
import board_model as boardmodel

team_name = "RNGAI" 
DIRECTIONS=[[0,1],[1,1],[1,0],[1,-1],[0,-1],[-1,-1],[-1,0],[-1,1]]
dirs=[[1,1,-1,-1],[-1,0,1,0],[0,1,0,-1],[1,-1,-1,1]]

class StudentAI():

	minEvalBoard = -1
	maxEvalBoard = 100000

	def __init__(self, player, state):
		self.last_move = state.get_last_move()
		self.model = state
		self.player = player

	def make_move(self, model,deadline):
		'''Write AI Here. Return a tuple (col, row)'''
		width = self.model.get_width()
		height = self.model.get_height()
		# spaces = defaultdict(int)

		# for i in range(width):
		#     for j in range(height):
		#         spaces[(i,j)] = self.model.get_space(i, j)

		# moves = [k for k in spaces.keys() if spaces[k] == 0]
		# return moves[random.randint(0, len(moves) - 1)]

		opt = 2; depth = 2 # set AI option and depth
		x,y = self.bestMove(self.model, self.player, depth = depth, opt = opt)
		# x,y = self.bestMove(self.model, self.player)
		print(x,y)
		# print(str(self.model))
		print(self.getRemaining(self.model))
		return (x,y)

##    def EvaluateBoard(self, board, player):
##        score=0
##        for x1,y1,x2,y2 in dirs:
##            
			
	def EvaluateBoard(self, state, player):
		""" Simple heuristic to evaluate board configurations
			Heuristic is (num of 4-in-a-rows)*99999 + (num of 3-in-a-rows)*100 + 
			(num of 2-in-a-rows)*10 - (num of opponent 4-in-a-rows)*99999 - (num of opponent
			3-in-a-rows)*100 - (num of opponent 2-in-a-rows)*10
		"""
	   
		print(state)
		my_fours = self.checkForStreak(state.pieces, player, 4)
		my_threes = self.checkForStreak(state.pieces, player, 3)
		my_twos = self.checkForStreak(state.pieces, player, 2)
		opp_fours = self.checkForStreak(state.pieces, self.play(player), 4)
		#opp_threes = self.checkForStreak(state, o_player, 3)
		#opp_twos = self.checkForStreak(state, o_player, 2)
		if opp_fours > 0:
			return -100000
		else:
			return my_fours*100000 + my_threes*100 + my_twos
			
	def checkForStreak(self, state, player, streak):
		count = 0
		for i in range(9):
			for j in range(7):
				if state[i][j] == player:
					count += self.verticalStreak(i, j, state, streak)
					
					count += self.horizontalStreak(i, j, state, streak)
					
					#count += self.diagonalCheck(i, j, state, streak)
		return count
			
	def verticalStreak(self, row, col, state, streak):
		consecutiveCount = 0
		for i in range(row, 9):
			if state[i][col] == state[row][col]:
				consecutiveCount += 1
			else:
				break
	
		if consecutiveCount >= streak:
			return 1
		else:
			return 0
	
	def horizontalStreak(self, row, col, state, streak):
		consecutiveCount = 0
		for j in range(col, 7):
			if state[row][j] == state[row][col]:
				consecutiveCount += 1
			else:
				break

		if consecutiveCount >= streak:
			return 1
		else:
			return 0
	
	def diagonalCheck(self, row, col, state, streak):

		total = 0
		consecutiveCount = 0
		j = col
		for i in range(row, 9):
			if j > 6:
				break
			elif state[i][j] == state[row][col]:
				consecutiveCount += 1
			else:
				break
			j += 1 
			
		if consecutiveCount >= streak:
			total += 1

		consecutiveCount = 0
		j = col
		for i in range(row, -1, -1):
			if j > 6:
				break
			elif state[i][j] == state[row][col]:
				consecutiveCount += 1
			else:
				break
			j += 1 

		if consecutiveCount >= streak:
			total += 1

		return total


	def getRemaining(self, board):
		spaces = defaultdict(int)
		for i in range(board.get_width()):
			for j in range(board.get_height()):
				spaces[(i,j)] = board.get_space(i, j)
		# return spaces
		print(spaces)
		return [(r,c) for r in range(board.get_width()) for c in range(board.get_height()) if board.get_space(r,c) == 0]

	def play(self, player):
		if (player == 1):
			return 2
		else:
			return 1

	def alphaBeta(self, board, player, depth, alpha, beta, maximizing):
		valid = self.getRemaining(board)
	    
		width = self.model.get_width()
		height = self.model.get_height()
		if depth == 0 or not board.has_moves_left():
			return self.EvaluateBoard(board, self.player)
		if maximizing:
			v = StudentAI.minEvalBoard
			for r in range(height):
				for c in range(width):
					if (r,c) in valid:
						
						newBoard = board.clone()
						newState = newBoard.place_piece((r,c), self.player)
						
						v = self.alphaBeta(newState, self.play(self.player), depth-1, alpha, beta, False)
						alpha = max(alpha, v)
						if beta <= alpha:
							break
			return v
		else:
			v = StudentAI.maxEvalBoard
			for r in range(height):
				for c in range(width):
					if (r,c) in valid:
						newBoard = board.clone()
						newState = newBoard.place_piece((r,c), self.player)
						v = self.alphaBeta(newState, self.play(self.player), depth-1, alpha, beta, True)
						beta = min(beta, v)
						if beta <= alpha:
							break
			return v

	def bestMove(self, board, player, depth = 0, opt = 0):
		maxPoints = 0
		mx = -1; my = -1
		valid = self.getRemaining(board)
		width = self.model.get_width()
		height = self.model.get_height()
		for r in range(height):
			for c in range(width):
				if (r,c) in valid:
					newState = board.clone().place_piece((r,c), self.player)
					if opt == 0:
						points = self.EvaluateBoard(newState, self.player)
					elif opt == 1:
						points = self.minMax(newState, self.player, depth, True)
					elif opt == 2:
						points = self.alphaBeta(board, self.player, depth, StudentAI.minEvalBoard, StudentAI.maxEvalBoard, True)
					if points > maxPoints:
						maxPoints = points
						mx = r; my = c
		return (mx, my)
					
'''===================================
DO NOT MODIFY ANYTHING BELOW THIS LINE
==================================='''

is_first_player = False
deadline = 0

def make_ai_shell_from_input():
	'''
	Reads board state from input and returns the move chosen by StudentAI
	DO NOT MODIFY THIS
	'''
	global is_first_player
	ai_shell = None
	begin =  "makeMoveWithState:"
	end = "end"

	go = True
	while (go):
			mass_input = input().split(" ")
			if (mass_input[0] == end):
					sys.exit()
			elif (mass_input[0] == begin):
					#first I want the gravity, then number of cols, then number of rows, then the col of the last move, then the row of the last move then the values for all the spaces.
					# 0 for no gravity, 1 for gravity
					#then rows
					#then cols
					#then lastMove col
					#then lastMove row.
					#then deadline.
					#add the K variable after deadline.
					#then the values for the spaces.
					#cout<<"beginning"<<endl;
					gravity = int(mass_input[1])
					col_count = int(mass_input[2])
					row_count = int(mass_input[3])
					last_move_col = int(mass_input[4])
					last_move_row = int(mass_input[5])

					#add the deadline here:
					deadline = -1
					deadline = int(mass_input[6])
					k = int(mass_input[7])
					#now the values for each space.


					counter = 8
					#allocate 2D array.
					model = boardmodel.BoardModel(col_count, row_count, k, gravity)
					count_own_moves = 0

					for col in range(col_count):
							for row in range(row_count):
									model.pieces[col][row] = int(mass_input[counter])
									if (model.pieces[col][row] == 1):
											count_own_moves += model.pieces[col][row]
									counter+=1

					if (count_own_moves % 2 == 0):
							is_first_player = True

					model.last_move = (last_move_col, last_move_row)
					ai_shell = StudentAI(1 if is_first_player else 2, model)

					return ai_shell
			else:
					print("unrecognized command", mass_input)
			#otherwise loop back to the top and wait for proper _input.
	return ai_shell

def return_move(move):
	'''
	Prints the move made by the AI so the wrapping shell can input it
	DO NOT MODIFY THIS
	'''
	made_move = "ReturningTheMoveMade";
	#outputs made_move then a space then the row then a space then the column then a line break.
	print(made_move, move[0], move[1])

def check_if_first_player():
	global is_first_player
	return is_first_player

if __name__ == '__main__':
	'''
	DO NOT MODIFY THIS
	'''
	print ("Make sure this program is ran by the Java shell. It is incomplete on its own. :")
	go = True
	while (go): #do this forever until the make_ai_shell_from_input function ends the process or it is killed by the java wrapper.
		ai_shell = make_ai_shell_from_input()
		moveMade = ai_shell.make_move(deadline)
		return_move(moveMade)
		del ai_shell
		sys.stdout.flush()
