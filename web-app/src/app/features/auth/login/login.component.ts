import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { NgIf } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIf,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  template: `
    <div class="login-wrapper">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Video Processor</mat-card-title>
          <mat-card-subtitle>Entre com seu usuário</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Usuário</mat-label>
              <input matInput formControlName="username" autocomplete="username" />
              <mat-error *ngIf="form.get('username')?.invalid && form.get('username')?.touched">
                Usuário é obrigatório.
              </mat-error>
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Senha</mat-label>
              <input matInput type="password" formControlName="password" autocomplete="current-password" />
              <mat-error *ngIf="form.get('password')?.invalid && form.get('password')?.touched">
                Senha é obrigatória.
              </mat-error>
            </mat-form-field>
            <p class="error" *ngIf="errorMessage">{{ errorMessage }}</p>
            <button mat-flat-button color="primary" type="submit" class="full-width" [disabled]="form.invalid || loading">
              Entrar
            </button>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-wrapper { max-width: 360px; margin: 2rem auto; padding: 0 1rem; }
    .full-width { width: 100%; display: block; }
    .error { color: #c62828; margin: 0.5rem 0 0; font-size: 0.875rem; }
    button { margin-top: 0.5rem; }
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
