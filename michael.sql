5)
create view EloWithinDates as
select E.name, E.elo
from ClubEloEntry E
where [date] >= startDate and [date] <= endDate;

create view LowestElo as
select min(E.elo) as lowElo
from EloWithinDates E
order by E.elo desc
limit 32;

select E.name, E.elo
from ClubEloEntry E
where E.elo >= (select L.elo from LowestElo L)
order by E.elo desc;

drop view EloWithinDates;
drop view LowestElo;

6)
create view AugustRatings as
select E.country, E.name, E.elo, E.endDate
from ClubEloEntry E
where year(E.endDate) = [year] and month(E.endDate) = 'August';

create view AugustTeams as
select A.country, A.name, max(A.endDate) as lastRatingOfYear
from AugustRatings A
group by A.country, A.name;

create view AugustElos as
select *
from AugustTeams T inner join AugustRatings R on T.country = R.country and
T.name = R.name and T.lastRatingOfYear = R.endDate;

create view MinElo as
select min(A.elo) minElo
from AugustElos A
order by A.elo desc
limit 32

select A.country, A.name
from AugustElos A
where A.elo >= (select M.minElo from MinElo M);

drop view AugustRatings;
drop view AugustTeams;
drop view AugustElos;
drop view MinElo;

7)
create view TeamHistory as
select C.country, C.name, C.elo
from ClubEloEntry C
where C.country = [country] and C.name = [name];

select C.country, C.name
from ClubEloEntry C
where C.elo = (select min(T.elo) from TeamHistory T);
