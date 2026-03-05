import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { VideoService } from '../video.service';
import { VideoResponse } from '../../../shared/models/video.model';
import { NgIf, DatePipe } from '@angular/common';

@Component({
  selector: 'app-video-detail',
  standalone: true,
  imports: [NgIf, DatePipe, RouterLink],
  template: `
    <div *ngIf="loading">Carregando...</div>
    <div *ngIf="error" class="error">{{ error }}</div>
    <div *ngIf="video && !loading">
      <h1>{{ video.originalFilename }}</h1>
      <dl class="detail-list">
        <dt>Status</dt>
        <dd><span [class]="'status status-' + video.status.toLowerCase()">{{ video.status }}</span></dd>
        <dt>Criado em</dt>
        <dd>{{ video.createdAt | date:'medium' }}</dd>
        <dt>Atualizado em</dt>
        <dd>{{ video.updatedAt | date:'medium' }}</dd>
        <dt *ngIf="video.frameCount != null">Frames</dt>
        <dd *ngIf="video.frameCount != null">{{ video.frameCount }}</dd>
        <dt *ngIf="video.errorMessage">Erro</dt>
        <dd *ngIf="video.errorMessage" class="error">{{ video.errorMessage }}</dd>
        <dt *ngIf="video.processedAt">Processado em</dt>
        <dd *ngIf="video.processedAt">{{ video.processedAt | date:'medium' }}</dd>
      </dl>
      <div class="actions">
        <button type="button" (click)="load()">Atualizar</button>
        @if (video.status === 'CONCLUIDO' && video.zipPath) {
          <button type="button" (click)="downloadZip()" [disabled]="downloadingZip">
            {{ downloadingZip ? 'Baixando...' : 'Download ZIP' }}
          </button>
        }
        <a routerLink="/videos">Voltar à lista</a>
      </div>
    </div>
  `,
  styles: [`
    .detail-list { display: grid; grid-template-columns: auto 1fr; gap: 0.5rem 1.5rem; max-width: 480px; }
    dt { font-weight: 600; }
    .status { padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.875rem; }
    .status-pendente { background: #fff3e0; color: #e65100; }
    .status-processando { background: #e3f2fd; color: #1565c0; }
    .status-concluido { background: #e8f5e9; color: #2e7d32; }
    .status-erro { background: #ffebee; color: #c62828; }
    .error { color: #c62828; }
    .actions { display: flex; align-items: center; gap: 0.5rem 1rem; flex-wrap: wrap; margin-top: 1rem; }
    .actions button { padding: 0.5rem 1rem; cursor: pointer; }
    .actions button:disabled { opacity: 0.7; cursor: not-allowed; }
  `],
})
export class VideoDetailComponent implements OnInit {
  video: VideoResponse | null = null;
  loading = false;
  error = '';
  downloadingZip = false;

  constructor(
    private route: ActivatedRoute,
    private videoService: VideoService,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.load(id);
  }

  load(id?: string): void {
    const videoId = id ?? this.route.snapshot.paramMap.get('id');
    if (!videoId) return;
    this.loading = true;
    this.error = '';
    this.videoService.getById(videoId).subscribe({
      next: (v) => { this.video = v; this.loading = false; },
      error: (e) => { this.error = e.message || 'Erro ao carregar vídeo.'; this.loading = false; },
    });
  }

  downloadZip(): void {
    const videoId = this.video?.id ?? this.route.snapshot.paramMap.get('id');
    if (!videoId || !this.video?.originalFilename) return;
    this.downloadingZip = true;
    this.videoService.downloadZip(videoId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const baseName = this.video!.originalFilename.replace(/\.[^.]+$/, '') || 'video';
        a.download = `${baseName}_frames.zip`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.downloadingZip = false;
      },
      error: () => {
        this.error = 'Erro ao baixar o ZIP.';
        this.downloadingZip = false;
      },
    });
  }
}
