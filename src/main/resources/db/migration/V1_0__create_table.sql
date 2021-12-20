CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE user_role AS ENUM ('ROLE_USER', 'ROLE_ADMIN');

CREATE TABLE IF NOT EXISTS public.users
(
    id         uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    first_name text,
    last_name  text,
    email      text                           NOT NULL UNIQUE,
    password   text                           NOT NULL,
    created_at timestamp        DEFAULT NOW() NOT NULL,
    updated_at timestamp,
    deleted_at timestamp,
    role       user_role                      NOT NULL
);

CREATE TABLE IF NOT EXISTS public.question
(
    id          uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    rating      bigint                                NOT NULL,
    title       text                                  NOT NULL,
    description text                                  NOT NULL,
    user_id     uuid
        CONSTRAINT user_id_fkey REFERENCES users (id) NOT NULL,
    created_at  timestamp        DEFAULT NOW()        NOT NULL,
    updated_at  timestamp,
    deleted_at  timestamp
);

CREATE TABLE IF NOT EXISTS public.answer
(
    id         uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    text       text                                   NOT NULL,
    rating     bigint                                 NOT NULL,
    user_id    uuid
        CONSTRAINT user_id_fkey REFERENCES users (id) NOT NULL,
    created_at timestamp        DEFAULT NOW()         NOT NULL,
    updated_at timestamp,
    deleted_at timestamp
);

CREATE TABLE IF NOT EXISTS public.question_details
(
    id          uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    question_id uuid
        CONSTRAINT question_id_fkey REFERENCES question (id) NOT NULL UNIQUE,
    answer_id   uuid
        CONSTRAINT answer_id_fkey REFERENCES answer (id)     NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS public.tag
(
    id   uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    name text NOT NULL
);

CREATE TABLE IF NOT EXISTS public.tag_details
(
    id          uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    question_id uuid
        CONSTRAINT question_id_fkey REFERENCES question (id) NOT NULL UNIQUE,
    tag_id      uuid
        CONSTRAINT tag_id_fkey REFERENCES tag (id)           NOT NULL UNIQUE
);
