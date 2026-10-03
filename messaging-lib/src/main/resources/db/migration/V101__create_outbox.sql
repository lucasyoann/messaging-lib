create table outbox_event (
                              id           uuid primary key,
                              event_type   varchar(100) not null,
                              payload      text not null,
                              created_at   timestamptz not null,
                              published_at timestamptz,
                              attempts      int not null default 0
);
create index idx_outbox_unpublished on outbox_event(published_at) where published_at is null;