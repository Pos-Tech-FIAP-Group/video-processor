import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { VideoService } from '../video.service';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-video-upload',
  standalone: true,
  imports: [NgIf, RouterLink],
  template: `
    <h1>Upload de vídeo</h1>
    <p *ngIf="!userUuid" class="error">Não foi possível identificar o usuário. Faça login novamente.</p>
    <div *ngIf="userUuid">
      <form (submit)="onSubmit($event)">
        <input type="file" #fileInput accept="video/*" (change)="onFileSelected($event)" />
        <p class="error" *ngIf="errorMessage">{{ errorMessage }}</p>
        <button type="submit" [disabled]="!file || uploading">Enviar</button>
      </form>
      <p *ngIf="success">Vídeo enviado. <a routerLink="/videos">Ver lista</a> ou <a [routerLink]="['/videos', createdId]">ver detalhe</a>.</p>
    </div>
  `,
  styles: [`
    input[type="file"] { display: block; margin: 1rem 0; }
    button { padding: 0.5rem 1rem; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
    button:disabled { opacity: 0.6; cursor: not-allowed; }
    .error { color: #c62828; }
  `],
})
export class VideoUploadComponent {
  file: File | null = null;
  uploading = false;
  errorMessage = '';
  success = false;
  createdId = '';
  userUuid: string | null = null;

  constructor(
    private auth: AuthService,
    private videoService: VideoService,
    private router: Router,
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
    this.success = false;
    this.videoService.upload(this.file).subscribe({
      next: (res) => {
        this.success = true;
        this.createdId = res.id;
        this.uploading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erro ao enviar vídeo.';
        this.uploading = false;
      },
    });
  }
}
