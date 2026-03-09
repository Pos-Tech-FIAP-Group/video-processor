import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { Router } from '@angular/router';
import { NgIf } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    NgIf,
    MatToolbarModule,
    MatButtonModule,
  ],
  template: `
    <mat-toolbar color="primary" *ngIf="auth.isLoggedIn()">
      <button mat-button routerLink="/videos" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: false }">
        Vídeos
      </button>
      <button mat-button routerLink="/videos/upload" routerLinkActive="active">
        Upload
      </button>
      <span class="spacer"></span>
      <button mat-button (click)="logout()">Sair</button>
    </mat-toolbar>
    <main class="mat-app-background">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .spacer { flex: 1 1 auto; }
    main {
      padding: 1.5rem;
      max-width: 1200px;
      margin: 0 auto;
      min-height: calc(100vh - 64px);
    }
    .active { font-weight: bold; }
  `],
})
export class AppComponent {
  constructor(
    public auth: AuthService,
    private router: Router,
  ) {}

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
