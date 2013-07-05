-- Table: funcionalidad

-- DROP TABLE funcionalidad;

CREATE TABLE funcionalidad
(
  id text NOT NULL,
  activo boolean,
  CONSTRAINT "id_Pk" PRIMARY KEY (id)
)
WITHOUT OIDS;
ALTER TABLE funcionalidad OWNER TO postgres;

