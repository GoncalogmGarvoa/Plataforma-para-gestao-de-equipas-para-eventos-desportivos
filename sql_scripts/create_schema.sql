CREATE SCHEMA IF NOT EXISTS dbp;


create table dbp.users (
                           id serial primary key,
                           phone_number Int not null,
                           address varchar(255) not null,
                           name varchar(255) not null,
                           email varchar(255) unique not null,
                           password varchar(255) not null,
                           birth_date date not null,
                           iban varchar(25) not null,

                           roles varchar(255)
);

create table dbp.admin (
                           user_id int primary key,
                           foreign key (user_id) references dbp.users(id)
);

create table dbp.category (
                              id serial primary key,
                              name varchar(255) not null
);

create table dbp.referee (
                             user_id int primary key,
                             foreign key (user_id) references dbp.users(id)
);

create table dbp.arbitration_council (
                                         user_id int primary key,
                                         foreign key (user_id) references dbp.users(id)
);

create table dbp.category_dir (
                                  referee_id int,
                                  id serial,
                                  start_date date not null,
                                  end_date date,
                                  category_id int,
                                  primary key (id, referee_id, category_id),
                                  foreign key (referee_id) references dbp.referee(user_id),
                                  foreign key (category_id) references dbp.category(id)
);

create table dbp.competition (
                                 competition_number serial primary key,
                                 name varchar(100) not null,
                                 address varchar(255) not null,
                                 email varchar(100) not null,
                                 phone_number varchar(20) not null,
                                 location varchar(100) not null,
                                 association varchar(100) not null
);

create table dbp.call_list (
                               id serial,
                               deadline date not null,
                               call_type varchar(100),
                               council_id int,
                               competition_id int not null,
                               primary key (id, council_id),
                               foreign key (council_id) references dbp.arbitration_council(user_id),
                               foreign key (competition_id) references dbp.competition(competition_number)
);

create table dbp.role (
                          id serial primary key,
                          name varchar(100) not null
);

create table dbp.match_day (
                               id serial,
                               match_date date not null,
                               competition_id int,
                               primary key (id, competition_id),
                               foreign key (competition_id) references dbp.competition(competition_number)
);

create table dbp.participant (
                                 call_list_id int,
                                 match_day_id int,
                                 council_id int,
                                 competition_id_match_day int,
                                 referee_id int,
                                 role_id int,
                                 confirmation_status varchar(20) check (confirmation_status in ('waiting', 'accepted', 'declined')),
                                 primary key (call_list_id, match_day_id, referee_id, role_id),
                                 foreign key (role_id) references dbp.role(id),
                                 foreign key (call_list_id, council_id) references dbp.call_list(id, council_id),
                                 foreign key (match_day_id, competition_id_match_day) references dbp.match_day(id, competition_id),
                                 foreign key (referee_id) references dbp.referee(user_id)
);

create table dbp.session (
                             id serial ,
                             start_time time not null,
                             end_time time,
                             match_day_id int,
                             competition_id_match_day int,
                             primary key (id, match_day_id, competition_id_match_day),
                             foreign key (match_day_id, competition_id_match_day) references dbp.match_day(id, competition_id)
);

create table dbp.position (
                              id serial primary key,
                              name varchar(100) not null
);



create table dbp.session_referees (
                                      session_id int,
                                      position_id int,
                                      referee_id int,
                                      match_day_id_session int,
                                      competition_id_match_day int,
                                      primary key (position_id, session_id, referee_id, match_day_id_session, competition_id_match_day),
                                      foreign key (session_id, match_day_id_session, competition_id_match_day)
                                          references dbp.session(id, match_day_id, competition_id_match_day),
                                      foreign key (referee_id) references dbp.referee(user_id),
                                      foreign key (position_id) references dbp.position(id)
);




create table dbp.report (
                            id serial,
                            report_type varchar(50),
                            competition_id int,
                            primary key (id, competition_id),
                            foreign key (competition_id) references dbp.competition(competition_number)
);

create table dbp.equipment (
                               id serial primary key,
                               name varchar(100) not null
);

create table dbp.competition_equipment (
                                           competition_id int,
                                           equipment_id int,
                                           primary key (equipment_id, competition_id),
                                           foreign key (competition_id) references dbp.competition(competition_number),
                                           foreign key (equipment_id) references dbp.equipment(id)
);