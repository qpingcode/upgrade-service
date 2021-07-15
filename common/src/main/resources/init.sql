DROP TABLE IF EXISTS FILE_PROGRESS;

CREATE TABLE FILE_PROGRESS (
  id INT IDENTITY PRIMARY KEY,
  node_id BIGINT,
  to_node_id BIGINT,
  source_url varchar(500),
  target_url varchar(500),
  total_size BIGINT,
  read_position BIGINT,
  err_msg  varchar(500),
  status INT,     -- 1 中间 2 结束 3 错误
  begin_date DATE,
  end_date DATE
);