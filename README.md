# SoccerElo
Databases project for CPE 365. Using data from clubelo.com, create more 
advanced/useful queries related to club/nation/league elo.

All date queries should be of the form YYYY-MM-DD.

List of Queries (Michael)
5) For Date X, return the top 32 teams
7) Return team X's lowest ever ranking
8) Return 20 most dominant teams over period of time (find avg elo and sort)

List of Queries (Brent)
Done.

List of Queries (Vihari)
9) Given Month and Year, return top 20 teams based off ELO gained over month
	Already have a GetChangeOverTime query, just use it to group by team, order by elo change and limit to 20
10) Given Year, return top 20 teams based off ELO gained over year (year starts
    in July 1st)
	Similar to above, could use GetChangeOverTime, group, order, and limit
11) Find top 20 upsets in history (top 20 largest ELO gains over one game)
	Not worth it, unless you want the query to take a few hours.
12) For Team X, return net difference in ELO for given year
	E Z P Z (see 9 and 10)


NOTE: ROOM FOR EXPANSION
we can potentially use current elo to create new matches and predict game
outcomes based off factors such as home field advantage. Can also add a little
randomness to this so we can almost simulate games and not completely know
the outcome
