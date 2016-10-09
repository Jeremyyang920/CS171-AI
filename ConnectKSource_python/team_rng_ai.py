#Author: Toluwanimi Salako

from collections import defaultdict
import random

team_name = "RNG"

class TeamRNGAI():
        def __init__(self, player, state):
                self.last_move = state.get_last_move()
                self.model = state
        def make_move(self, deadline):
                '''Write AI Here. Return a tuple (col, row)'''
                width = self.model.get_width()
                height = self.model.get_height()
                spaces = defaultdict(int)
                for i in range(width):
                        for j in range(height):
                                spaces[(i,j)] = self.model.pieces[i][j]

                moves = [k for k in spaces.keys() if spaces[k] == 0]
                self.update()
                return moves[random.randint(0, len(moves) - 1)]
        def update(self):
                self.model=self.model.clone()