CREATE SCHEMA IF NOT EXISTS dbp;


create table dbp.users (
                           id serial primary key,
                           phone_number varchar(13) not null ,
                           address varchar(255) not null,
                           name varchar(100) not null,
                           email varchar(100) unique not null,
                           password_validation varchar(255) not null,
                           birth_date date not null,
                           iban varchar(25) not null,
                           status VARCHAR(10)CHECK (status IN ('active', 'inactive'))DEFAULT 'active'

);

create table dbp.tokens(
                        id serial primary key,
                        token_validation VARCHAR(256),
                        user_id int ,
                        created_at bigint not null,
                        last_used_at bigint not null,
                        FOREIGN KEY (user_id) REFERENCES dbp.users(id)
);

create table dbp.role (
                          id serial primary key,
                          name varchar(100) not null

);

CREATE TABLE dbp.user_token_role (
                                     id SERIAL PRIMARY KEY,
                                     user_id INT NOT NULL,
                                     token_id int NOT NULL,
                                     role_id INT NOT NULL,
                                     FOREIGN KEY (user_id) REFERENCES dbp.users(id),
                                     FOREIGN KEY (token_id) REFERENCES dbp.tokens(id),
                                     FOREIGN KEY (role_id) REFERENCES dbp.role(id)
);

create table dbp.users_roles (
                                user_id int,
                                role_id int,
                                primary key (user_id, role_id),
                                foreign key (user_id) references dbp.users(id),
                                foreign key (role_id) references dbp.role(id)
);

create table dbp.category (
                              id serial primary key,
                              name varchar(100) not null
);

create table dbp.category_dir (
                                  user_id int,
                                  id serial,
                                  start_date date not null,
                                  end_date date,
                                  category_id int,
                                  primary key (id, user_id, category_id),
                                  foreign key (user_id) references dbp.users(id),
                                  foreign key (category_id) references dbp.category(id)
);

create table dbp.competition (
                                 competition_number serial primary key,
                                 name varchar(100) not null,
                                 address varchar(255) not null,
                                 email varchar(100) not null,
                                 phone_number varchar(13) not null,
                                 location varchar(100) not null,
                                 association varchar(100) not null
);

create table dbp.call_list (
                               id serial,
                               deadline date not null,
                               call_type varchar(100) default 'callList' check (call_type in ('callList','sealedCallList', 'confirmation', 'finalJury')),
                               user_id int,
                               competition_id int not null,
                               primary key (id),
                               foreign key (user_id) references dbp.users(id),
                               foreign key (competition_id) references dbp.competition(competition_number)
);

create table dbp.function (
                              id serial primary key,
                              name varchar(100) unique not null
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
                                 competition_id_match_day int,
                                 user_id int,
                                 function_id int,
                                 confirmation_status varchar(10) default 'waiting' check (confirmation_status in ('waiting', 'accepted', 'declined')),
                                 primary key (call_list_id, match_day_id, user_id, function_id,competition_id_match_day),
                                 foreign key (function_id) references dbp.function(id),
                                 foreign key (call_list_id) references dbp.call_list(id),
                                 foreign key (match_day_id, competition_id_match_day) references dbp.match_day(id, competition_id),
                                 foreign key (user_id) references dbp.users(id)
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
                                      user_id int,
                                      match_day_id_session int,
                                      competition_id_match_day int,
                                      primary key (position_id, session_id, user_id, match_day_id_session, competition_id_match_day),
                                      foreign key (session_id, match_day_id_session, competition_id_match_day)
                                          references dbp.session(id, match_day_id, competition_id_match_day),
                                      foreign key (user_id) references dbp.users(id),
                                      foreign key (position_id) references dbp.position(id)
);




create table dbp.report (
                            id varchar(100) not null,
                            report_type varchar(100),
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