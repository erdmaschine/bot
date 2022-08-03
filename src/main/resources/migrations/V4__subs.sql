create table if not exists subs
(
    "guildId"   varchar(100) not null,
    "channelId" varchar(100) not null,
    "sub"       varchar(100) not null,
    "listing"   varchar(100) not null,
    primary key ("guildId", "channelId", "sub")
);
