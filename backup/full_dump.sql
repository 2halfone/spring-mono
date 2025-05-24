--
-- PostgreSQL database cluster dump
--

SET default_transaction_read_only = off;

SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

--
-- Roles
--

CREATE ROLE springuser;
ALTER ROLE springuser WITH SUPERUSER INHERIT CREATEROLE CREATEDB LOGIN REPLICATION BYPASSRLS PASSWORD 'SCRAM-SHA-256$4096:XBhXRMNYFkZhdTRopdgwPA==$bBIj7XNlnEVHdci2FnO6f1hAhNLuhiMsmpS84yNJujY=:3c15avk5YsOhqXchuCjyM7/7Ri6cqASnvvEr5FMTel8=';

--
-- User Configurations
--








--
-- Databases
--

--
-- Database "template1" dump
--

\connect template1

--
-- PostgreSQL database dump
--

-- Dumped from database version 15.13 (Debian 15.13-1.pgdg120+1)
-- Dumped by pg_dump version 15.13 (Debian 15.13-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- PostgreSQL database dump complete
--

--
-- Database "mydb" dump
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 15.13 (Debian 15.13-1.pgdg120+1)
-- Dumped by pg_dump version 15.13 (Debian 15.13-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: mydb; Type: DATABASE; Schema: -; Owner: springuser
--

CREATE DATABASE mydb WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


ALTER DATABASE mydb OWNER TO springuser;

\connect mydb

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: login_log; Type: TABLE; Schema: public; Owner: springuser
--

CREATE TABLE public.login_log (
    id bigint NOT NULL,
    ip_address character varying(255),
    "timestamp" timestamp(6) without time zone,
    user_id bigint
);


ALTER TABLE public.login_log OWNER TO springuser;

--
-- Name: login_log_id_seq; Type: SEQUENCE; Schema: public; Owner: springuser
--

CREATE SEQUENCE public.login_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.login_log_id_seq OWNER TO springuser;

--
-- Name: login_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: springuser
--

ALTER SEQUENCE public.login_log_id_seq OWNED BY public.login_log.id;


--
-- Name: movie; Type: TABLE; Schema: public; Owner: springuser
--

CREATE TABLE public.movie (
    id bigint NOT NULL,
    title character varying(255),
    watched boolean NOT NULL
);


ALTER TABLE public.movie OWNER TO springuser;

--
-- Name: movie_id_seq; Type: SEQUENCE; Schema: public; Owner: springuser
--

CREATE SEQUENCE public.movie_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.movie_id_seq OWNER TO springuser;

--
-- Name: movie_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: springuser
--

ALTER SEQUENCE public.movie_id_seq OWNED BY public.movie.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: springuser
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    password character varying(255) NOT NULL,
    username character varying(255) NOT NULL
);


ALTER TABLE public.users OWNER TO springuser;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: springuser
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO springuser;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: springuser
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: login_log id; Type: DEFAULT; Schema: public; Owner: springuser
--

ALTER TABLE ONLY public.login_log ALTER COLUMN id SET DEFAULT nextval('public.login_log_id_seq'::regclass);


--
-- Name: movie id; Type: DEFAULT; Schema: public; Owner: springuser
--

ALTER TABLE ONLY public.movie ALTER COLUMN id SET DEFAULT nextval('public.movie_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: springuser
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: login_log; Type: TABLE DATA; Schema: public; Owner: springuser
--

COPY public.login_log (id, ip_address, "timestamp", user_id) FROM stdin;
1	172.18.0.1	2025-05-23 21:54:58.534116	3
\.


--
-- Data for Name: movie; Type: TABLE DATA; Schema: public; Owner: springuser
--

COPY public.movie (id, title, watched) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: springuser
--

COPY public.users (id, password, username) FROM stdin;
1	$2a$10$lZWovQVDW0Dlmr5AZ2aOj.b3855.H66XYxygpGiBIi8MYRuFf6kOG	alice
2	$2a$10$pjuWAWm1Bpl4/npApjHz5OJTrLQUTvpNd/VL7cWzskVt8/mx9Cjy6	frank
3	$2a$10$kZDG9IgS7U1XI6shFM7Cc.u7sv4PcnLoRdpZX9PdioNf8G0izp4yi	grace
\.


--
-- Name: login_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: springuser
--

SELECT pg_catalog.setval('public.login_log_id_seq', 1, true);


--
-- Name: movie_id_seq; Type: SEQUENCE SET; Schema: public; Owner: springuser
--

SELECT pg_catalog.setval('public.movie_id_seq', 1, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: springuser
--

SELECT pg_catalog.setval('public.users_id_seq', 3, true);


--
-- Name: login_log login_log_pkey; Type: CONSTRAINT; Schema: public; Owner: springuser
--

ALTER TABLE ONLY public.login_log
    ADD CONSTRAINT login_log_pkey PRIMARY KEY (id);


--
-- Name: movie movie_pkey; Type: CONSTRAINT; Schema: public; Owner: springuser
--

ALTER TABLE ONLY public.movie
    ADD CONSTRAINT movie_pkey PRIMARY KEY (id);


--
-- Name: users uk_r43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: springuser
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_r43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: springuser
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: login_log fkq8smpj42ldwgtjru0fm0q3en8; Type: FK CONSTRAINT; Schema: public; Owner: springuser
--

ALTER TABLE ONLY public.login_log
    ADD CONSTRAINT fkq8smpj42ldwgtjru0fm0q3en8 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

--
-- Database "postgres" dump
--

\connect postgres

--
-- PostgreSQL database dump
--

-- Dumped from database version 15.13 (Debian 15.13-1.pgdg120+1)
-- Dumped by pg_dump version 15.13 (Debian 15.13-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- PostgreSQL database dump complete
--

--
-- PostgreSQL database cluster dump complete
--

