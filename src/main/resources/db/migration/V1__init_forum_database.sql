create table users (
  id serial primary key not null,
  nick text not null unique,
  email text not null unique,
  password text not null
);

insert into users (nick, email, password) values (
  'anonymous',
  'anonymous@mail.com',
  crypt('anonymous', gen_salt('bf'))
);

create table session_token (
  id serial primary key not null,
   token text not null unique,
   user_id int not null,
   last_update_time timestamp not null,
   foreign key (user_id) references users (id)
);

create table topic (
  id serial primary key not null,
  owner_id int not null,
  title text not null,
  description text,
  foreign key (owner_id) references users (id)
);

create table message (
  id serial primary key  not null,
  owner_id int  not null,
  topic_id int not null,
  forward_id int,
  body text  not null,
  message_date timestamp not null,
 foreign key (owner_id) references users (id),
 foreign key (topic_id) references topic (id)
);