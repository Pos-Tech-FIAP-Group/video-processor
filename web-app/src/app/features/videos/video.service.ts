import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { VideoResponse, CreateVideoResponse, PageResponse } from '../../shared/models/video.model';

@Injectable({ providedIn: 'root' })
export class VideoService {
  private readonly apiUrl = `${environment.apiUrl}/api/videos`;

  constructor(private http: HttpClient) {}

  list(userId: string, page = 0, size = 10): Observable<PageResponse<VideoResponse>> {
    const params = new HttpParams().set('userId', userId).set('page', String(page)).set('size', String(size));
    return this.http.get<PageResponse<VideoResponse>>(this.apiUrl, { params });
  }

  getById(id: string): Observable<VideoResponse> {
    return this.http.get<VideoResponse>(`${this.apiUrl}/${id}`);
  }

  downloadZip(id: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/zip`, { responseType: 'blob' });
  }

  upload(file: File, frameIntervalSeconds?: number): Observable<CreateVideoResponse> {
    const formData = new FormData();
    formData.append('file', file);
    if (frameIntervalSeconds != null && frameIntervalSeconds > 0) {
      formData.append('frameIntervalSeconds', String(frameIntervalSeconds));
    }
    return this.http.post<CreateVideoResponse>(this.apiUrl, formData, {
      observe: 'response',
      responseType: 'json',
    }).pipe(
      map((res) => res.body ?? { id: '', status: 'PROCESSANDO' as const, createdAt: new Date().toISOString() }),
    );
  }
}
