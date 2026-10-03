create table room (
    id          uuid primary key,
    name        varchar(255),
    type        varchar(20) not null,
    created_at  timestamptz not null
);

create table room_member (
    room_id     uuid not null references room(id),
    user_id     uuid not null,
    role        varchar(20) not null,
    joined_at   timestamptz not null,
    primary key (room_id, user_id)
);
create index idx_room_member_user on room_member(user_id);

create table message (
    id          uuid primary key,
    room_id     uuid not null references room(id),
    sender_id   uuid not null,
    content     varchar(4000) not null,
    created_at  timestamptz not null
);
create index idx_message_room_created on message(room_id, created_at desc);

create table message_status (
    message_id  uuid not null references message(id),
    user_id     uuid not null,
    status      varchar(20) not null,
    updated_at  timestamptz not null,
    version     bigint not null default 0,
    primary key (message_id, user_id)
);

-- Les tables outbox_event (étape suivante : pattern outbox) et
-- push_subscription (module messaging-notifications) arriveront dans une
-- prochaine migration, au fur et à mesure qu'on construit les services.
