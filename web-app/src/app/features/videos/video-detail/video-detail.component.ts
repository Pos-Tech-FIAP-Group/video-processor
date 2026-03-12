import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { VideoService } from '../video.service';
import { VideoResponse } from '../../../shared/models/video.model';
import { NgIf, DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-video-detail',
  standalone: true,
  imports: [
    NgIf,
    DatePipe,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <mat-spinner *ngIf="loading" diameter="32" class="spinner"></mat-spinner>
    <p *ngIf="error" class="error">{{ error }}</p>
    <mat-card *ngIf="video && !loading">
      <mat-card-header>
        <mat-card-title>{{ video.originalFilename }}</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <dl class="detail-list">
          <dt>Status</dt>
          <dd><span [class]="'status status-' + (video.status || '').toLowerCase()">{{ video.status }}</span></dd>
          <dt>Criado em</dt>
          <dd>{{ video.createdAt ? (video.createdAt | date:'medium') : '—' }}</dd>
          <dt>Atualizado em</dt>
          <dd>{{ video.updatedAt ? (video.updatedAt | date:'medium') : '—' }}</dd>
          <ng-container *ngIf="video.frameCount != null">
            <dt>Frames</dt>
            <dd>{{ video.frameCount }}</dd>
          </ng-container>
          <ng-container *ngIf="video.errorMessage">
            <dt>Erro</dt>
            <dd class="error">{{ video.errorMessage }}</dd>
          </ng-container>
          <ng-container *ngIf="video.processedAt">
            <dt>Processado em</dt>
            <dd>{{ video.processedAt | date:'medium' }}</dd>
          </ng-container>
        </dl>
      </mat-card-content>
      <mat-card-actions>
        <button mat-stroked-button (click)="load()">Atualizar</button>
        <button *ngIf="video.status === 'CONCLUIDO' && video.zipPath" mat-flat-button color="primary" (click)="downloadZip()" [disabled]="downloadingZip">
          {{ downloadingZip ? 'Baixando...' : 'Download ZIP' }}
        </button>
        <button mat-button routerLink="/videos">Voltar à lista</button>
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    .spinner { margin: 1rem auto; }
    .error { color: #c62828; }
    .detail-list { display: grid; grid-template-columns: auto 1fr; gap: 0.5rem 1.5rem; max-width: 480px; }
    dt { font-weight: 600; }
    .status { padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.875rem; }
    .status-pendente { background: #fff3e0; color: #e65100; }
    .status-processando { background: #e3f2fd; color: #1565c0; }
    .status-concluido { background: #e8f5e9; color: #2e7d32; }
    .status-erro { background: #ffebee; color: #c62828; }
    mat-card-actions { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; }
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
    if (!videoId) {
      this.error = 'ID do vídeo não informado.';
      return;
    }
    this.loading = true;
    this.error = '';
    this.video = null;
    this.videoService.getById(videoId).subscribe({
      next: (v) => {
        this.video = v;
        this.loading = false;
      },
      error: (e) => {
        this.error = e.error?.message || e.message || 'Erro ao carregar vídeo.';
        this.loading = false;
      },
      complete: () => {
        this.loading = false;
      },
    });
  }

  downloadZip(): void {
    const videoId = this.video?.id ?? this.route.snapshot.paramMap.get('id');
    if (!videoId || !this.video?.originalFilename) return;
    const zipPath = this.video.zipPath;
    if (zipPath && (zipPath.startsWith('http://') || zipPath.startsWith('https://'))) {
      window.open(zipPath, '_blank');
      return;
    }
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
