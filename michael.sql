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

