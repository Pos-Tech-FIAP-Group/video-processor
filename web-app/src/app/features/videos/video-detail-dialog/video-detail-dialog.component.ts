import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { VideoService } from '../video.service';
import { VideoResponse } from '../../../shared/models/video.model';
import { NgIf, DatePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';

export interface VideoDetailDialogData {
  videoId: string;
}

@Component({
  selector: 'app-video-detail-dialog',
  standalone: true,
  imports: [
    NgIf,
    DatePipe,
    MatDialogModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
  ],
  template: `
    <div class="dialog-header">
      <h2 mat-dialog-title>Detalhe do vídeo</h2>
      <button mat-icon-button (click)="close()" aria-label="Fechar">
        <mat-icon>close</mat-icon>
      </button>
    </div>
    <mat-dialog-content>
      <mat-spinner *ngIf="loading" diameter="32" class="spinner"></mat-spinner>
      <p *ngIf="error" class="error">{{ error }}</p>
      <div *ngIf="video && !loading" class="detail-content">
        <p class="filename">{{ video.originalFilename }}</p>
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
      </div>
    </mat-dialog-content>
    <mat-dialog-actions *ngIf="video && !loading">
      <button mat-stroked-button (click)="load()">Atualizar</button>
      <button *ngIf="video.status === 'CONCLUIDO' && video.zipPath" mat-flat-button color="primary" (click)="downloadZip()" [disabled]="downloadingZip">
        {{ downloadingZip ? 'Baixando...' : 'Download ZIP' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dialog-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-right: 8px;
    }
    .dialog-header h2 {
      margin: 0;
    }
    .spinner { margin: 1rem auto; }
    .error { color: #c62828; }
    .filename { font-weight: 600; margin: 0 0 1rem 0; }
    .detail-list { display: grid; grid-template-columns: auto 1fr; gap: 0.5rem 1.5rem; max-width: 480px; margin: 0; }
    dt { font-weight: 600; }
    .status { padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.875rem; }
    .status-pendente { background: #fff3e0; color: #e65100; }
    .status-processando { background: #e3f2fd; color: #1565c0; }
    .status-concluido { background: #e8f5e9; color: #2e7d32; }
    .status-erro { background: #ffebee; color: #c62828; }
    mat-dialog-actions { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; }
  `],
})
export class VideoDetailDialogComponent implements OnInit {
  video: VideoResponse | null = null;
  loading = false;
  error = '';
  downloadingZip = false;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: VideoDetailDialogData,
    private dialogRef: MatDialogRef<VideoDetailDialogComponent>,
    private videoService: VideoService,
  ) {}

  ngOnInit(): void {
    if (this.data?.videoId) this.load();
  }

  close(): void {
    this.dialogRef.close();
  }

  load(): void {
    const videoId = this.data?.videoId;
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
    const videoId = this.video?.id ?? this.data?.videoId;
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
