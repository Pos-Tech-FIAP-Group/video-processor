CREATE TABLE videos (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,

  original_filename VARCHAR(255) NOT NULL,
  content_type VARCHAR(100) NOT NULL,

  video_path TEXT NOT NULL,
  zip_path TEXT NULL,

  status VARCHAR(30) NOT NULL,
  frame_count INT NULL,
  error_message TEXT NULL,

  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  processed_at TIMESTAMP NULL
);

CREATE INDEX idx_videos_user_created_at ON videos (user_id, created_at DESC);
CREATE INDEX idx_videos_status ON videos (status);