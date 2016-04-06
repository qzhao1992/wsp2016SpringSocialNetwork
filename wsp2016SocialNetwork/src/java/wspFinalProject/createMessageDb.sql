/**
 * Author:  justin.hampton
 * Created: Mar 27, 2016
 */
create table Message(
    id INT NOT NULL AUTO_INCREMENT,
    type INT,
    text VARCHAR(200),
    date TIMESTAMP,
    primary key(id)
);

insert into Message (id, type, text) values(null, 1, "Hello this is message one");
insert into Message (id, type, text) values(null, 1, "this is second message");
