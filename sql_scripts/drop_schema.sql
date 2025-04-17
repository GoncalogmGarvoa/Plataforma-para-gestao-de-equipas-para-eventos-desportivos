import foreign schema dbp;

drop table if exists dbp.competition_equipment;
drop table if exists dbp.report;
drop table if exists dbp.session_referees;
drop table if exists dbp.position;
drop table if exists dbp.session;
drop table if exists dbp.participant;
drop table if exists dbp.match_day;
drop table if exists dbp.role;
drop table if exists dbp.call_list;

drop table if exists dbp.competition;
drop table if exists dbp.category_dir;
drop table if exists dbp.category;
drop table if exists dbp.arbitration_council;
drop table if exists dbp.referee;
drop table if exists dbp.admin;
drop table if exists dbp.equipment;
drop table if exists dbp.user;

select *
from dbp.competition_equipment;
select *
from dbp.report;
select *
from dbp.session_referees;
select *
from dbp.position;
select *
from  dbp.session;
select *
from  dbp.participant;
select *
from dbp.match_day;
select *
from dbp.role;
select *
from  dbp.call_list;
select *
from dbp.competition;
select *
from dbp.category_dir;
select *
from dbp.category;
select *
from dbp.arbitration_council;
select *
from  dbp.referee;
select *
from  dbp.admin;
select *
from dbp.equipment;
select *
from dbp.user;
