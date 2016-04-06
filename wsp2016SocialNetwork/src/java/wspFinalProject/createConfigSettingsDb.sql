
/**
 * Author:  justin.hampton
 * Created: Apr 6, 2016
 */


-- drop table config_settings;

create table config_settings (
    ID INT NOT NULL AUTO_INCREMENT,
    USER_ID INT,
    ORDER_BY VARCHAR(20),
    PRIMARY KEY (ID)
);

insert into config_settings values (0, 1, "date_desc");