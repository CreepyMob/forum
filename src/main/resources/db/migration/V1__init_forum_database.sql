CREATE TABLE users (
  id SERIAL PRIMARY KEY NOT NULL,
  nick TEXT NOT NULL
);

INSERT INTO  users  (id, nick) VALUES (0, 'Anonymous');

CREATE TABLE session_token (
  id SERIAL PRIMARY KEY NOT NULL,
   token TEXT NOT NULL,
   user_id INT NOT NULL,
   FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE topic (
  id SERIAL PRIMARY KEY NOT NULL,
  owner_id INT NOT NULL,
  title TEXT NOT NULL,
  description TEXT,
  FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE message (
  id SERIAL PRIMARY KEY  NOT NULL,
  owner_id INT  NOT NULL,
  topic_id INT NOT NULL,
  forward_id INT,
  body TEXT  NOT NULL,
  message_date TIMESTAMP NOT NULL,
 FOREIGN KEY (owner_id) REFERENCES users (id),
 FOREIGN KEY (topic_id) REFERENCES topic (id)
);