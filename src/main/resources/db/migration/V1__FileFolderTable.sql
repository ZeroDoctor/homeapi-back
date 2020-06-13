CREATE TABLE IF NOT EXISTS file(
    path_id LTREE NOT NULL,
    fname VARCHAR(280) NOT NULL,
    ftype VARCHAR(64),
    ffull_name VARCHAR(296),
    file_size BIGINT,
    last_modified TIMESTAMP NOT NULL,
    fpath VARCHAR(4096) NOT NULL,
    fdirectory SMALLINT NOT NULL,
    parent VARCHAR(4096) NOT NULL
);

CREATE UNIQUE INDEX uniq_file ON file(path_id, fdirectory);