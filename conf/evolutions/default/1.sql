# Users schema

# --- !Ups

CREATE TABLE "User" (
    "user_id" int(10) NOT NULL AUTO_INCREMENT,
    "name" varchar(255) NOT NULL,
    PRIMARY KEY ("user_id")
);

# --- !Downs

DROP TABLE "User";