DROP TABLE IF EXISTS FILE_PROGRESS;

CREATE TABLE FILE_PROGRESS (
  id INT IDENTITY PRIMARY KEY,
  message_id BIGINT,
  node_id BIGINT,
  to_node_id BIGINT,
  file_name varchar(2000),
  source_path varchar(2000),
  target_path varchar(2000),
  total_size BIGINT,
  read_position BIGINT,
  err_msg  varchar(2000),
  status INT,     -- 1 中间 2 结束 3 错误
  begin_date TIMESTAMP,
  end_date TIMESTAMP
);