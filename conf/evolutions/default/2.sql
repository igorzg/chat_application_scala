# Messages schema

# --- !Ups

CREATE TABLE "Message" (
    "session_id" varchar(255) NOT NULL,
    "user" varchar(255) NOT NULL,
    "message" varchar(255) NOT NULL
);

# --- !Downs

DROP TABLE "Message";