ALTER TABLE IF EXISTS public.subs
    ADD COLUMN nsfw bool NOT NULL DEFAULT false;
