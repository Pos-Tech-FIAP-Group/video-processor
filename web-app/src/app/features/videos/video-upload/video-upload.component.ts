import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { VideoService } from '../video.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-video-upload',
  standalone: true,
  imports: [
    NgIf,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatTooltipModule,
  ],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>Upload de vídeo</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <p *ngIf="!userUuid" class="error">Não foi possível identificar o usuário. Faça login novamente.</p>
        <div *ngIf="userUuid">
          <form (submit)="onSubmit($event)" class="upload-form">
            <div class="top-row">
              <div class="file-part">
                <input type="file" #fileInput accept="video/*" (change)="onFileSelected($event)" class="file-input" />
                <button mat-stroked-button type="button" (click)="fileInput.click()">Escolher arquivo</button>
              </div>
              <div class="interval-part">
                <span class="interval-label">
                  Intervalo entre frames
                  <span class="info-superscript" matTooltip="Ex.: 1 = um frame por segundo; 2 = um a cada 2 segundos" tabindex="0" role="button" aria-label="Ajuda">
                    <mat-icon>info</mat-icon>
                  </span>
                </span>
                <div class="interval-input-line">
                  <input class="interval-input" type="number" min="0.1" step="0.5" [(ngModel)]="frameIntervalSeconds" name="frameInterval" />
                  <span class="unit-s">s</span>
                </div>
              </div>
            </div>
            <p *ngIf="file" class="file-name-label">Arquivo: {{ file.name }}</p>
            <div class="actions">
              <button mat-flat-button color="primary" type="submit" [disabled]="!file || uploading">
                {{ uploading ? 'Enviando...' : 'Enviar' }}
              </button>
              <mat-spinner *ngIf="uploading" diameter="24" class="inline-spinner"></mat-spinner>
            </div>
            <p class="error" *ngIf="errorMessage">{{ errorMessage }}</p>
          </form>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .file-input { display: none; }
    .upload-form { max-width: 520px; }
    .top-row { display: flex; align-items: flex-end; gap: 1rem; flex-wrap: wrap; margin-bottom: 0.5rem; }
    .file-part { display: flex; align-items: center; }
    .file-name-label { font-size: 0.875rem; color: rgba(0,0,0,0.6); margin: 0 0 1rem 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .interval-part { display: flex; flex-direction: column; gap: 0.25rem; }
    .interval-label { font-size: 0.875rem; color: rgba(0,0,0,0.6); }
    .interval-label .info-superscript { display: inline-flex; vertical-align: super; align-items: center; justify-content: center; width: 14px; height: 14px; margin-left: 0.125rem; cursor: default; }
    .interval-label .info-superscript .mat-icon { font-size: 11px; width: 11px; height: 11px; }
    .interval-input-line { display: flex; align-items: center; gap: 0.25rem; }
    .interval-input { width: 4ch; box-sizing: content-box; font-size: 0.875rem; padding: 0.25rem 0.25rem; border: 1px solid rgba(0,0,0,0.38); border-radius: 4px; text-align: right; }
    .interval-input:focus { outline: none; border-color: var(--mat-form-field-outline-color, #1976d2); }
    .unit-s { font-size: 0.875rem; color: rgba(0,0,0,0.6); margin-left: 0.125rem; }
    .actions { display: flex; align-items: center; gap: 0.5rem; }
    .error { color: #c62828; margin: 0.5rem 0 0; font-size: 0.875rem; }
    .inline-spinner { margin-left: 0.25rem; }
  `],
})
export class VideoUploadComponent {
  file: File | null = null;
  frameIntervalSeconds = 1;
  uploading = false;
  errorMessage = '';
  userUuid: string | null = null;

  constructor(
    private auth: AuthService,
    private videoService: VideoService,
    private router: Router,
    private snackBar: MatSnackBar,
  ) {
    this.userUuid = this.auth.getUserUuid();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.file = input.files?.[0] ?? null;
    this.errorMessage = '';
  }

  onSubmit(e: Event): void {
    e.preventDefault();
    if (!this.file || !this.userUuid) return;
    this.uploading = true;
    this.errorMessage = '';
    this.videoService.upload(this.file, this.frameIntervalSeconds).subscribe({
      next: () => {
        this.router.navigate(['/videos']).then(() => {
          this.snackBar.open('Vídeo enviado.', undefined, {
            duration: 4000,
            panelClass: ['snackbar-success'],
          });
        });
      },
      error: (err) => {
        this.errorMessage = err.error?.message || err.message || 'Erro ao enviar vídeo.';
      },
      complete: () => {
        this.uploading = false;
      },
    });
  }
}
