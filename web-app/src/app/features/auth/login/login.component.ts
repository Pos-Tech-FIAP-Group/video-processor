import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf],
  template: `
    <div class="login-card">
      <h1>Video Processor</h1>
      <p class="subtitle">Entre com seu usuário</p>
      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <div class="field">
          <label for="username">Usuário</label>
          <input id="username" type="text" formControlName="username" autocomplete="username" />
          <span class="error" *ngIf="form.get('username')?.invalid && form.get('username')?.touched">Usuário é obrigatório.</span>
        </div>
        <div class="field">
          <label for="password">Senha</label>
          <input id="password" type="password" formControlName="password" autocomplete="current-password" />
          <span class="error" *ngIf="form.get('password')?.invalid && form.get('password')?.touched">Senha é obrigatória.</span>
        </div>
        <p class="error" *ngIf="errorMessage">{{ errorMessage }}</p>
        <button type="submit" [disabled]="form.invalid || loading">Entrar</button>
      </form>
    </div>
  `,
  styles: [`
    .login-card {
      max-width: 360px;
      margin: 2rem auto;
      padding: 2rem;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }
    h1 { margin-top: 0; font-size: 1.5rem; }
    .subtitle { color: #666; margin-bottom: 1.5rem; }
    .field { margin-bottom: 1rem; }
    .field label { display: block; margin-bottom: 0.25rem; font-weight: 500; }
    .field input {
      width: 100%;
      padding: 0.5rem;
      border: 1px solid #ccc;
      border-radius: 4px;
    }
    .error { color: #c62828; font-size: 0.875rem; margin-top: 0.25rem; }
    button {
      width: 100%;
      padding: 0.75rem;
      margin-top: 0.5rem;
      background: #1976d2;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 1rem;
    }
    button:disabled { opacity: 0.6; cursor: not-allowed; }
  `],
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });
  errorMessage = '';
  loading = false;

  onSubmit(): void {
    this.errorMessage = '';
    this.loading = true;
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/videos']),
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.status === 401 ? 'Usuário ou senha inválidos.' : 'Erro ao entrar. Tente novamente.';
      },
      complete: () => { this.loading = false; },
    });
  }
}
