import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { VideoService } from '../video.service';
import { VideoResponse, PageResponse } from '../../../shared/models/video.model';
import { NgIf, DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-video-list',
  standalone: true,
  imports: [
    RouterLink,
    NgIf,
    DatePipe,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>Meus vídeos</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <p *ngIf="!userUuid" class="error">Não foi possível identificar o usuário. Faça login novamente.</p>
        <div *ngIf="userUuid">
          <mat-spinner *ngIf="loading" diameter="32" class="spinner"></mat-spinner>
          <p *ngIf="error" class="error">{{ error }}</p>
          <table mat-table [dataSource]="dataSource" *ngIf="!loading && !error && (page?.items?.length ?? 0) > 0" class="mat-elevation-z0">
            <ng-container matColumnDef="filename">
              <th mat-header-cell *matHeaderCellDef>Arquivo</th>
              <td mat-cell *matCellDef="let v">{{ v.originalFilename }}</td>
            </ng-container>
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let v">
                <span [class]="'status status-' + v.status.toLowerCase()">{{ v.status }}</span>
              </td>
            </ng-container>
            <ng-container matColumnDef="date">
              <th mat-header-cell *matHeaderCellDef>Data</th>
              <td mat-cell *matCellDef="let v">{{ v.createdAt | date:'short' }}</td>
            </ng-container>
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let v" class="actions-cell">
                <button mat-icon-button [routerLink]="['/videos', v.id]" matTooltip="Detalhes" aria-label="Detalhes">
                  <mat-icon>visibility</mat-icon>
                </button>
                <button mat-icon-button (click)="downloadZip(v.id, v.originalFilename, v.zipPath)" [disabled]="v.status !== 'CONCLUIDO' || !v.zipPath || downloadingId === v.id" [matTooltip]="(v.status === 'CONCLUIDO' && v.zipPath) ? 'Download ZIP' : 'Disponível quando concluído'" aria-label="Download ZIP">
                  <mat-icon>download</mat-icon>
                </button>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>
          <p *ngIf="!loading && !error && (page?.items?.length ?? 0) === 0" class="empty">
            Nenhum vídeo.
            <button mat-flat-button color="primary" routerLink="/videos/upload">Enviar vídeo</button>
          </p>
          <p *ngIf="!loading && page && page.items.length > 0" class="actions">
            <button mat-stroked-button routerLink="/videos/upload">Enviar outro vídeo</button>
          </p>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .spinner { margin: 1rem auto; }
    .error { color: #c62828; }
    .empty, .actions { margin-top: 1rem; }
    .empty button, .actions button { margin-left: 0.5rem; }
    .status { padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.875rem; }
    .status-pendente { background: #fff3e0; color: #e65100; }
    .status-processando { background: #e3f2fd; color: #1565c0; }
    .status-concluido { background: #e8f5e9; color: #2e7d32; }
    .status-erro { background: #ffebee; color: #c62828; }
    table { width: 100%; }
    .actions-cell { white-space: nowrap; }
  `],
})
export class VideoListComponent implements OnInit {
  page: PageResponse<VideoResponse> | null = null;
  dataSource = new MatTableDataSource<VideoResponse>([]);
  displayedColumns: string[] = ['filename', 'status', 'date', 'actions'];
  loading = false;
  error = '';
  userUuid: string | null = null;
  downloadingId: string | null = null;

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
      next: (p) => {
        this.page = p;
        this.dataSource.data = p?.items ?? [];
        this.loading = false;
      },
      error: (e) => {
        this.error = e.message || 'Erro ao carregar vídeos.';
        this.loading = false;
      },
    });
  }

  downloadZip(videoId: string, originalFilename: string, zipPath: string | null): void {
    if (zipPath && (zipPath.startsWith('http://') || zipPath.startsWith('https://'))) {
      window.open(zipPath, '_blank');
      return;
    }
    if (this.downloadingId) return;
    this.downloadingId = videoId;
    this.videoService.downloadZip(videoId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const baseName = originalFilename.replace(/\.[^.]+$/, '') || 'video';
        a.download = `${baseName}_frames.zip`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.downloadingId = null;
      },
      error: () => {
        this.downloadingId = null;
      },
    });
  }
}
