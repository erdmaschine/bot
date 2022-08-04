create table if not exists post_history
(
    "guildId"   varchar(100) not null,
    "channelId" varchar(100) not null,
    "sub"       varchar(100) not null,
    "postId"    varchar(100) not null,
    primary key ("guildId", "channelId", "sub", "postId")
);
