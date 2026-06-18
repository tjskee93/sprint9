CREATE SCHEMA IF NOT EXISTS accounts;

create table accounts.account (
    login      varchar(128)  primary key,
    first_name varchar(128),
    last_name  varchar(128),
    birth_date date,
    balance    BIGINT NOT NULL CHECK (balance >= 0)
);

insert into account (login, first_name, last_name, birth_date, balance) values
    ('solovev', 'Илья', 'Соловьев', date '1993-12-21', 1000),
    ('solovev2',   'Илья',   'Соловьев2',  date '1993-12-22',  10000),
    ('solovev3',   'Илья',   'Соловьев3',  date '1993-12-23',  100000);