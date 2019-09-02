create schema if not exists wb_dispatch;

CREATE TABLE wb_dispatch.commit_log
(
    id            character varying(64) NOT NULL,
    creation_time character varying(40) NOT NULL,
    CONSTRAINT pk_commit_log PRIMARY KEY (id)
);
