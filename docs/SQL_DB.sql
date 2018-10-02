CREATE TABLE USERS (
    USERNAME VARCHAR(30) not null primary key, 
    NAME VARCHAR(30) not null ,
    LASTNAME VARCHAR(30) ,
    EMAIL VARCHAR(30) not null unique, 
    IMAGINE VARCHAR(20),
    PASSWORD VARCHAR(60) not null 
);

CREATE TABLE USERSANONIMOUS (
    COOKIE varchar(30),
    PRIMARY KEY (COOKIE)

);

CREATE TABLE CLIST (
    IDCATEGORY INTEGER not null primary key,
    "NAME" VARCHAR(30),
    DESCRIPTION VARCHAR(30),
    IMAGINE VARCHAR(10)

);

CREATE TABLE LIST (
    IDLIST INTEGER not null primary key,
    USERCREATOR VARCHAR (30) unique,  
    IDCAT INTEGER,
    NAME VARCHAR(30),
    DESCRIPTION VARCHAR(30),
    IMAGINE VARCHAR(10),
    FOREIGN KEY (USERCREATOR) REFERENCES USERS(USERNAME),
    FOREIGN KEY (IDCAT) REFERENCES CLIST(IDCATEGORY)
);

CREATE TABLE CITEM (
    IDCATEGORY INTEGER not null primary key,
    NOTE VARCHAR(30),
    DESCRIPTION VARCHAR(30),
    IMAGINE VARCHAR(10)
);


CREATE TABLE ITEM (
    CAT INTEGER not null,
    IDITEM INTEGER not null primary key,
    FOREIGN KEY (CAT) REFERENCES CITEM(IDCATEGORY),
    CALORIE INTEGER,
    "NAME" VARCHAR(30),
    LOGO VARCHAR(30),
    NOTE VARCHAR(10)
);


CREATE TABLE COMPOSE(
    CCITEM INTEGER ,
    CCLIST INTEGER,
    FOREIGN KEY (CCITEM) REFERENCES ITEM(IDITEM),
    FOREIGN KEY (CCLIST) REFERENCES LIST(IDLIST),
    primary key (CCITEM, CCLIST)  
);

CREATE TABLE ACCESS(
    AAUSER VARCHAR (30),
    AALIST INTEGER,
    FOREIGN KEY (AAUSER) REFERENCES USERS(USERNAME),
    FOREIGN KEY (AALIST) REFERENCES LIST(IDLIST),
    primary key (AAUSER, AALIST)    
);


