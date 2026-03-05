import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { Router } from '@angular/router';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NgIf],
  template: `
    <nav class="nav" *ngIf="auth.isLoggedIn()">
      <a routerLink="/videos" routerLinkActive="active">Vídeos</a>
      <a routerLink="/videos/upload" routerLinkActive="active">Upload</a>
      <button type="button" (click)="logout()">Sair</button>
    </nav>
    <main>
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .nav {
      display: flex;
      gap: 1rem;
      padding: 1rem;
      background: #1976d2;
      color: white;
    }
    .nav a {
      color: white;
    }
    .nav a.active {
      font-weight: bold;
      text-decoration: underline;
    }
    .nav button {
      margin-left: auto;
      padding: 0.25rem 0.75rem;
      cursor: pointer;
      background: rgba(255,255,255,0.2);
      border: 1px solid rgba(255,255,255,0.5);
      color: white;
      border-radius: 4px;
    }
    main {
      padding: 1.5rem;
      max-width: 1200px;
      margin: 0 auto;
    }
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
