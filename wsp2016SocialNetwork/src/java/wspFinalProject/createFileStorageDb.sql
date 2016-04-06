/* 
 * jdbc resource name: jdbc/db1 referenced in Bean
*/

drop table filestorage;

create table FILESTORAGE (
    FILE_ID INT NOT NULL,
    FILE_NAME VARCHAR(255),
    FILE_TYPE VARCHAR(255),
    FILE_SIZE BIGINT,
    FILE_CONTENTS LONGBLOB,  /* binary data */
    PRIMARY KEY (FILE_ID)
);