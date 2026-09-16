create table if not exists submission
(
    id            varchar     not null,
    email         varchar     not null,
    thumbnail_key varchar,
    created_at    timestamp   not null,
    constraint submission_pk primary key (id)
);
