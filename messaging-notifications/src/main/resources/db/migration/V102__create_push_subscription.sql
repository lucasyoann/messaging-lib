create table push_subscription (
                                   id          uuid primary key,
                                   user_id     uuid not null,
                                   endpoint    varchar(1000) not null unique,
                                   p256dh_key  text not null,
                                   auth_key    text not null,
                                   created_at  timestamptz not null
);
create index idx_push_subscription_user on push_subscription(user_id);