"""The Game of Hog."""

from dice import four_sided, six_sided, make_test_dice
from ucb import main, trace, log_current_line, interact

GOAL_SCORE = 100  # The goal of Hog is to score 100 points.


######################
# Phase 1: Simulator #
######################


def roll_dice(num_rolls, dice=six_sided):
    """Simulate rolling the DICE exactly NUM_ROLLS times. Return the sum of
    the outcomes unless any of the outcomes is 1. In that case, return 0.
    """
    # These assert statements ensure that num_rolls is a positive integer.
    assert type(num_rolls) == int, 'num_rolls must be an integer.'
    assert num_rolls > 0, 'Must roll at least once.'
    # BEGIN Question 1
    total = 0
    for counter in range(num_rolls):
        temp = dice()

        # Returnsa total score of 0 if a 1 is rolled 
        if temp == 1:
            for t in range(counter + 1, num_rolls):
                dice()
            return 0
        else:
            total += temp
    return total
    # END Question 1

def is_prime(n):
    for i in range(2,n):
        if n % i == 0:
            return False
    return True

def next_prime(n):
    count, done = n+1, False
    while not done:
        if is_prime(count):
            return count
        count += 1


def take_turn(num_rolls, opponent_score, dice=six_sided):
    """Simulate a turn rolling NUM_ROLLS dice, which may be 0 (Free bacon).

    num_rolls:       The number of dice rolls that will be made.
    opponent_score:  The total score of the opponent.
    dice:            A function of no args that returns an integer outcome.
    """
    assert type(num_rolls) == int, 'num_rolls must be an integer.'
    assert num_rolls >= 0, 'Cannot roll a negative number of dice.'
    assert num_rolls <= 10, 'Cannot roll more than 10 dice.'
    assert opponent_score < 100, 'The game should be over.'
    # BEGIN Question 2
    total = 0

    # Free bacon rule 
    if num_rolls == 0:
        first = opponent_score // 10
        second = opponent_score % 10
        total = max(first, second) + 1
    
    else:
        total = roll_dice(num_rolls, dice)
    
    # Hogtimus Prime
    if total > 1 and is_prime(total):
        total = next_prime(total)

    return total 
    # END Question 2


def select_dice(score, opponent_score):
    """Select six-sided dice unless the sum of SCORE and OPPONENT_SCORE is a
    multiple of 7, in which case select four-sided dice (Hog wild).
    """
    # BEGIN Question 3
    if (score + opponent_score) % 7 == 0:
        return four_sided
    else:
        return six_sided
    # END Question 3


def is_swap(score0, score1):
    """Returns whether the last two digits of SCORE0 and SCORE1 are reversed
    versions of each other, such as 19 and 91.
    """
    # BEGIN Question 4
    temp0 = score0 % 100
    temp1 = score1 % 100
    first0 = temp0//10
    first1 = temp1//10
    last0 = temp0 % 10
    last1 = temp1 % 10
    if first0 == last1 and last0 == first1:
        return True
    else:
        return False
    # END Question 4


def other(who):
    """Return the other player, for a player WHO numbered 0 or 1.

    >>> other(0)
    1
    >>> other(1)
    0
    """
    return 1 - who


def play(strategy0, strategy1, score0=0, score1=0, goal=GOAL_SCORE):
    """Simulate a game and return the final scores of both players, with
    Player 0's score first, and Player 1's score second.

    A strategy is a function that takes two total scores as arguments
    (the current player's score, and the opponent's score), and returns a
    number of dice that the current player will roll this turn.

    strategy0:  The strategy function for Player 0, who plays first
    strategy1:  The strategy function for Player 1, who plays second
    score0   :  The starting score for Player 0
    score1   :  The starting score for Player 1
    """
    
    who = 0
    # BEGIN Question 5
    while score0 < goal and score1 < goal:

        # Plays if it's the irst player's turn
        if who == 0:
            rolls = strategy0(score0, score1)
            temp_score = take_turn(rolls, score1, dice = select_dice(score0, score1))
            if temp_score == 0:
                score1 += rolls
            score0 += temp_score

        # Plays if it's the second player's turn
        if who == 1:
            rolls = strategy1(score1, score0)
            temp_score = take_turn(rolls, score0, dice = select_dice(score0, score1))
            if temp_score == 0:
                score0 += rolls
            score1 += temp_score
            
        if is_swap(score0, score1):
            score0, score1 = score1, score0

        who = other(who)

    # END Question 5
    return score0, score1 

#######################
# Phase 2: Strategies #
#######################


def always_roll(n):
    """Return a strategy that always rolls N dice.

    A strategy is a function that takes two total scores as arguments
    (the current player's score, and the opponent's score), and returns a
    number of dice that the current player will roll this turn.

    >>> strategy = always_roll(5)
    >>> strategy(0, 0)
    5
    >>> strategy(99, 99)
    5
    """
    def strategy(score, opponent_score):
        return n

    return strategy


# Experiments

def make_averaged(fn, num_samples=1000):
    """Return a function that returns the average_value of FN when called.

    To implement this function, you will have to use *args syntax, a new Python
    feature introduced in this project.  See the project description.

    >>> dice = make_test_dice(3, 1, 5, 6)
    >>> averaged_dice = make_averaged(dice, 1000)
    >>> averaged_dice()
    3.75
    >>> make_averaged(roll_dice, 1000)(2, dice)
    5.5

    In this last example, two different turn scenarios are averaged.
    - In the first, the player rolls a 3 then a 1, receiving a score of 0.
    - In the other, the player rolls a 5 and 6, scoring 11.
    Thus, the average value is 5.5.
    Note that the last example uses roll_dice so the hogtimus prime rule does
    not apply.
    """
    # BEGIN Question 6
    def avg(*args):
        counter, total = 0, 0
        while counter < num_samples:
            total += fn(*args)
            counter += 1
        return total/counter
    return avg
    # END Question 6


def max_scoring_num_rolls(dice=six_sided, num_samples=1000):
    """Return the number of dice (1 to 10) that gives the highest average turn
    score by calling roll_dice with the provided DICE over NUM_SAMPLES times.
    Assume that dice always return positive outcomes.

    >>> dice = make_test_dice(3)
    >>> max_scoring_num_rolls(dice)
    10
    """
    # BEGIN Question 7
    highest, index = 0, 1
    for i in range(1, 11):
        temp = make_averaged(roll_dice, num_samples)(i, dice)
        if temp > highest:
            highest, index = temp, i
    return index
    # END Question 7


def winner(strategy0, strategy1):
    """Return 0 if strategy0 wins against strategy1, and 1 otherwise."""
    score0, score1 = play(strategy0, strategy1)
    if score0 > score1:
        return 0
    else:
        return 1


def average_win_rate(strategy, baseline=always_roll(5)):
    """Return the average win rate of STRATEGY against BASELINE. Averages the
    winrate when starting the game as player 0 and as player 1.
    """
    win_rate_as_player_0 = 1 - make_averaged(winner)(strategy, baseline)
    win_rate_as_player_1 = make_averaged(winner)(baseline, strategy)

    return (win_rate_as_player_0 + win_rate_as_player_1) / 2


def run_experiments():
    """Run a series of strategy experiments and report results."""
    if True:  # Change to False when done finding max_scoring_num_rolls
        six_sided_max = max_scoring_num_rolls(six_sided)
        print('Max scoring num rolls for six-sided dice:', six_sided_max)
        four_sided_max = max_scoring_num_rolls(four_sided)
        print('Max scoring num rolls for four-sided dice:', four_sided_max)

    if True:  # Change to True to test always_roll(8)
        print('always_roll(8) win rate:', average_win_rate(always_roll(8)))

    if True:  # Change to True to test bacon_strategy
        print('bacon_strategy win rate:', average_win_rate(bacon_strategy))

    if True:  # Change to True to test swap_strategy
        print('swap_strategy win rate:', average_win_rate(swap_strategy))

    if True:
        print('final_strategy win rate:', average_win_rate(final_strategy))
    "*** You may add additional experiments as you wish ***"


# Strategies

def bacon_strategy(score, opponent_score, margin=8, num_rolls=5):
    """This strategy rolls 0 dice if that gives at least MARGIN points,
    and rolls NUM_ROLLS otherwise.
    """
    # BEGIN Question 8
    zero_rolls = take_turn(0, opponent_score)
    if zero_rolls >= margin:
        return 0
    return num_rolls  
    # END Question 8


def swap_strategy(score, opponent_score, num_rolls=5):
    """This strategy rolls 0 dice when it results in a beneficial swap and
    rolls NUM_ROLLS otherwise.
    """
    # BEGIN Question 9
    score += take_turn(0, opponent_score)
    if opponent_score > score and is_swap(score, opponent_score):
        return 0
    return num_rolls  # Replace this statement
    # END Question 9


def final_strategy(score, opponent_score):
    """ Use the highest possible score from a corresponding number of rolls 
    for each type of dice. Then run through various strategies to obtain maximum 
    points.  """
    # BEGIN Question 10
    dice = select_dice(score, opponent_score)
    # Take different amounts of risk based upon who is winning and the type of dice
    # Numbers achieved through experimentation
    if opponent_score > score:
        if dice == six_sided:
            num_rolls = 3
            margin = 7
        else: 
            num_rolls = 3
            margin = 5
    if score >= opponent_score:
        if dice == six_sided:
            num_rolls = 4
            margin = 6
        else: 
            num_rolls = 1
            margin = 4

    # Points earned if roll 0
    zero = take_turn(0, opponent_score)
    score += zero
    # Roll 0 to avoid Piggy Back and allowing opponent to win or advantageous swap or roll 0 will score more than margin or to force oppoenent to use 4-sided dice
    if 100 - opponent_score <= num_rolls or (opponent_score + margin >= score and is_swap(score, opponent_score) and opponent_score - score >= margin + 3) or zero >= margin or (score + opponent_score) % 7 == 0:
        return 0
    # Take more risk with extra roll when losing but by less than margin
    if opponent_score - score < margin and opponent_score - score >= 0:
        return num_rolls + 1
    # Roll num_rolls since there are no other beneficial options
    else:
        return num_rolls



    # END Question 10


##########################
# Command Line Interface #
##########################


# Note: Functions in this section do not need to be changed. They use features
#       of Python not yet covered in the course.


@main
def run(*args):
    """Read in the command-line argument and calls corresponding functions.

    This function uses Python syntax/techniques not yet covered in this course.
    """
    import argparse
    parser = argparse.ArgumentParser(description="Play Hog")
    parser.add_argument('--run_experiments', '-r', action='store_true',
                        help='Runs strategy experiments')

    args = parser.parse_args()

    if args.run_experiments:
        run_experiments()
