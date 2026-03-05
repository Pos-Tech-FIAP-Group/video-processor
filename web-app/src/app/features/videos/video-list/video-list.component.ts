import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { VideoService } from '../video.service';
import { VideoResponse, PageResponse } from '../../../shared/models/video.model';
import { NgIf, NgFor, DatePipe } from '@angular/common';

@Component({
  selector: 'app-video-list',
  standalone: true,
  imports: [RouterLink, NgIf, NgFor, DatePipe],
  template: `
    <h1>Meus vídeos</h1>
    <p *ngIf="!userUuid" class="error">Não foi possível identificar o usuário. Faça login novamente.</p>
    <div *ngIf="userUuid">
      <div *ngIf="loading">Carregando...</div>
      <div *ngIf="error" class="error">{{ error }}</div>
      <table *ngIf="!loading && !error && page" class="table">
        <thead>
          <tr>
            <th>Arquivo</th>
            <th>Status</th>
            <th>Data</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let v of page?.items">
            <td>{{ v.originalFilename }}</td>
            <td><span [class]="'status status-' + v.status.toLowerCase()">{{ v.status }}</span></td>
            <td>{{ v.createdAt | date:'short' }}</td>
            <td><a [routerLink]="['/videos', v.id]">Ver</a></td>
          </tr>
        </tbody>
      </table>
      <p *ngIf="!loading && (page?.items?.length ?? 0) === 0">Nenhum vídeo. <a routerLink="/videos/upload">Enviar vídeo</a></p>
      <p *ngIf="page && (page.items?.length ?? 0) > 0"><a routerLink="/videos/upload">Enviar outro vídeo</a></p>
    </div>
  `,
  styles: [`
    .table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
    th, td { padding: 0.75rem 1rem; text-align: left; border-bottom: 1px solid #eee; }
    th { background: #f5f5f5; font-weight: 600; }
    .status { padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.875rem; }
    .status-pendente { background: #fff3e0; color: #e65100; }
    .status-processando { background: #e3f2fd; color: #1565c0; }
    .status-concluido { background: #e8f5e9; color: #2e7d32; }
    .status-erro { background: #ffebee; color: #c62828; }
    .error { color: #c62828; }
  `],
})
export class VideoListComponent implements OnInit {
  page: PageResponse<VideoResponse> | null = null;
  loading = false;
  error = '';
  userUuid: string | null = null;

  constructor(
    private auth: AuthService,
    private videoService: VideoService,
  ) {}

  ngOnInit(): void {
    this.userUuid = this.auth.getUserUuid();
    if (this.userUuid) this.load();
  }

  load(): void {
    if (!this.userUuid) return;
    this.loading = true;
    this.error = '';
    this.videoService.list(this.userUuid).subscribe({
      next: (p) => { this.page = p; this.loading = false; },
      error: (e) => { this.error = e.message || 'Erro ao carregar vídeos.'; this.loading = false; },
    });
  }
}
