import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { NgIf } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { ApiErrorResponse } from '../../../shared/models/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIf,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  template: `
    <div class="register-wrapper">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Video Processor</mat-card-title>
          <mat-card-subtitle>Crie sua conta</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Usuário</mat-label>
              <input matInput formControlName="username" autocomplete="username" />
              <mat-error *ngIf="form.get('username')?.invalid && form.get('username')?.touched">
                Usuário é obrigatório (mín. 3 caracteres).
              </mat-error>
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>E-mail</mat-label>
              <input matInput type="email" formControlName="email" autocomplete="email" />
              <mat-error *ngIf="form.get('email')?.invalid && form.get('email')?.touched">
                E-mail inválido.
              </mat-error>
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Senha</mat-label>
              <input matInput type="password" formControlName="password" autocomplete="new-password" />
              <mat-error *ngIf="form.get('password')?.invalid && form.get('password')?.touched">
                Senha é obrigatória (mín. 6 caracteres).
              </mat-error>
            </mat-form-field>
            <p class="error" *ngIf="errorMessage">{{ errorMessage }}</p>
            <button mat-flat-button color="primary" type="submit" class="full-width" [disabled]="form.invalid || loading">
              Cadastrar
            </button>
            <p class="link-row">
              <a routerLink="/login">Já tem conta? Entrar</a>
            </p>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .register-wrapper { max-width: 360px; margin: 2rem auto; padding: 0 1rem; }
    .full-width { width: 100%; display: block; }
    .error { color: #c62828; margin: 0.5rem 0 0; font-size: 0.875rem; }
    button { margin-top: 0.5rem; }
    .link-row { margin-top: 1rem; text-align: center; font-size: 0.875rem; }
    .link-row a { color: var(--mat-primary-color, #1976d2); text-decoration: none; }
    .link-row a:hover { text-decoration: underline; }
  `],
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });
  errorMessage = '';
  loading = false;

  onSubmit(): void {
    this.errorMessage = '';
    this.loading = true;
    this.auth.register(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/login'], { queryParams: { registered: 'true' } }),
      error: (err) => {
        this.loading = false;
        const body = err.error as ApiErrorResponse | undefined;
        this.errorMessage = body?.message ?? 'Erro ao cadastrar. Tente novamente.';
      },
      complete: () => { this.loading = false; },
    });
  }
}
