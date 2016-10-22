#Author: Toluwanimi Salako
from collections import defaultdict
import random, sys, copy
import board_model as boardmodel

team_name = "RNGAI" 

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

        opt = 2; depth = 3 # set AI option and depth
        x,y = self.bestMove(self.model, self.player, depth = depth, opt = opt)
        print(x,y)
        return (x,y)

    def EvaluateBoard(board, player):
        return[random.randint(0,10)]


    def getRemaining(self):
        spaces = defaultdict(int)
        for i in range(self.model.get_width()):
            for j in range(self.model.get_height()):
                spaces[(i,j)] = self.model.get_space(i, j)
        return spaces


    def play(self, player):
        if (player == 1):
            return 2
        else:
            return 1

    def minMax(self, board, player, depth, maximizing):
        valid = self.getRemaining()
        width = self.model.get_width()
        height = self.model.get_height()
        if depth == 0 or not board.has_moves_left():
           return EvaluateBoard(board, self.player)
        if maximizing:
            bestVal = StudentAI.minEvalBoard
            for r in range(height):
               for c in range(width):
                    if((r,c) in valid):
                       newState = copy.deepcopy(board).place_piece((r,c), self.player)
                       v = self.minMax(newState, self.play(self.player), depth-1, False)
                       bestValue = max(v,bestValue)
        else:
            bestValue = StudentAI.maxEvalBoard
            for r in range(height):
                for c in range(width):
                    if (r,c) in valid:
                        newState = copy.deepcopy(board).place_piece((r,c), self.player)
                        v = self.minMax(newState,self.play(self.player), depth-1, True)
                        bestValue = min(v,bestValue)
        return bestValue

    def alphaBeta(self, board, player, depth, alpha, beta, maximizing):
        valid = self.getRemaining()
        width = self.model.get_width()
        height = self.model.get_height()
        if depth == 0 or not board.has_moves_left():
            return EvaluateBoard(board, self.player)
        if maximizing:
            v = StudentAI.minEvalBoard
            for r in range(height):
                for c in range(width):
                    if (r,c) in valid:
                        print('row:',r,'col:',c)
                        print('model:\n', str(self.model))
                        # newState = copy.deepcopy(board).place_piece((r,c), self.player)
                        board.place_piece((0,6),self.player)
                        newState=board.clone()
                        #newState = board.clone().place_piece((r,c), self.player)
                        print('model:\n', str(self.model))
                        print('new model:\n', str(newState))
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
                        newState = copy.deepcopy(board).place_piece((r,c), self.player)
                        v = self.alphaBeta(newState,self.play(self.player), depth-1, alpha, beta, True)
                        beta = min(beta, v)
                        if beta <= alpha:
                            break
            return v

    def bestMove(self, board, player, depth = 0, opt = 0):
        maxPoints = 0
        mx = -1; my = -1
        valid = self.getRemaining()
        width = self.model.get_width()
        height = self.model.get_height()
        for r in range(height):
            for c in range(width):
                if (r,c) in valid:
                    newState = copy.deepcopy(board).place_piece((r,c), self.player)
                    if opt == 0:
                        points = self.EvaluateBoard(newState, self.player)
                    elif opt == 1:
                        points = self.minMax(newState, self.player, depth, True)
                    elif opt == 2:
                        points = self.alphaBeta(newState, self.player, depth, StudentAI.minEvalBoard, StudentAI.maxEvalBoard, True)
                    if points > maxPoints:
                        maxPoints = points
                        mx = x; my = y
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
