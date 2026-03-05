export type VideoStatus = 'PENDENTE' | 'PROCESSANDO' | 'CONCLUIDO' | 'ERRO';

export interface VideoResponse {
  id: string;
  userId: string;
  originalFilename: string;
  contentType: string;
  videoPath: string;
  zipPath: string | null;
  status: VideoStatus;
  frameCount: number | null;
  errorMessage: string | null;
  createdAt: string;
  updatedAt: string;
  processedAt: string | null;
}

export interface CreateVideoResponse {
  id: string;
  status: VideoStatus;
  createdAt: string;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}
