5)
create view EloWithinDates as
select *
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
select C.elo, C.endDate
from ClubEloEntry C
where C.country = [country] and C.name = [name];

create view TeamsWorst as
select T.endDate, T.elo
from TeamHistory T
where T.elo = (select min(T.elo) from TeamHistory T);

select 1 + count(*) as numBetterTeams
from ClubEloEntry C
where C.startDate <= (select T.endDate from TeamsWorst T)
    and C.endDate >= (select T.endDate from TeamsWorst T)
    and C.elo >= (select T.elo from TeamsWorst T);

drop view TeamHistory;
drop view TeamsWorst;

8)
create view TeamsOverEra as
select C.country, C.name, C.elo
from ClubEloEntry C
where C.startDate >= [startEra] and C.endDate <= [endEra];

create view AvgOverEra
select T.country, T.name, avg(T.elo) as avgElo
from TeamsOverEra T
group by T.country, T.name;

create view Min
select min(A.avgElo) as minAvg
from AvgOverEra A
order by A.avgElo desc
limit 20;

select A.country, A.name
from AvgOverEra A
where A.avgElo >= (select M.minAvg from Min M);

drop view TeamsOverEra;
drop view AvgOverEra;
drop view Min;
