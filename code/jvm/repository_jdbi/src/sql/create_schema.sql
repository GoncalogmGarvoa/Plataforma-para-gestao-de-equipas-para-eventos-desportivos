create table user (
                      id serial primary key,
                      name varchar(255) not null,
                      email varchar(255) unique not null,
                      password varchar(255) not null,
                      birth_date date,
                      iban varchar(21),
                      roles varchar(255)
);

create table admin (
                       user_id int primary key,
                       foreign key (user_id) references user(id)
);

create table category (
                          id int primary key,
                          name varchar(255)
);

create table referee (
                         user_id int primary key,
                         foreign key (user_id) references user(id)
);

create table arbitration_council (
                                     user_id int primary key,
                                     foreign key (user_id) references user(id)
);

create table category_dir (
                              referee_id int,
                              id int,
                              start_date date,
                              end_date date,
                              category_id int,
                              primary key (id, referee_id, category_id),
                              foreign key (referee_id) references referee(user_id),
                              foreign key (category_id) references category(id)
);

create table competition (
                             id int primary key,
                             competition_number int,
                             name varchar(100),
                             address varchar(255),
                             email varchar(100),
                             phone_number varchar(20),
                             location varchar(100),
                             association varchar(100)
);

create table call_list (
                           id serial primary key,
                           deadline date,
                           call_type varchar(100),
                           council_id int,
                           competition_id int,
                           foreign key (council_id) references arbitration_council(user_id),
                           foreign key (competition_id) references competition(id)
);

create table role (
                      id int primary key,
                      name varchar(100)
);

create table match_day (
                           id int,
                           match_date date,
                           competition_id int,
                           primary key (id, competition_id),
                           foreign key (competition_id) references competition(id)
);

create table participant (
                             call_list_id int,
                             match_day_id int,
                             referee_id int,
                             role_id int,
                             confirmation_status varchar(20) check (confirmation_status in ('waiting', 'accepted', 'declined')),
                             primary key (call_list_id, match_day_id, referee_id, role_id),
                             foreign key (role_id) references role(id),
                             foreign key (call_list_id) references call_list(id),
                             foreign key (match_day_id, competition_id) references match_day(id, competition_id),
                             foreign key (referee_id) references referee(user_id)
);

create table session (
                         id int,
                         start_time time not null,
                         end_time time,
                         match_day_id int,
                         competition_id_match_day int,
                         primary key (id, match_day_id, competition_id_match_day),
                         foreign key (match_day_id, competition_id_match_day) references match_day(id, competition_id)
);

create table position (
                          id serial primary key,
                          name varchar(100)
);

create table session_referees (
                                  session_id int,
                                  position_id int,
                                  referee_id int,
                                  match_day_id_session int,
                                  primary key (position_id, session_id, referee_id, match_day_id_session),
                                  foreign key (session_id, match_day_id_session) references session(id, match_day_id),
                                  foreign key (referee_id) references referee(user_id),
                                  foreign key (position_id) references position(id)
);

create table report (
                        id serial,
                        report_type varchar(50),
                        competition_id int,
                        primary key (id, competition_id),
                        foreign key (competition_id) references competition(id)
);

create table equipment (
                           id serial primary key,
                           name varchar(100)
);

create table competition_equipment (
                                       competition_id int,
                                       equipment_id int,
                                       primary key (equipment_id, competition_id),
                                       foreign key (competition_id) references competition(id),
                                       foreign key (equipment_id) references equipment(id)
);
