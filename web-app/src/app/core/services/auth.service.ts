import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest } from '../../shared/models/auth.model';

const TOKEN_KEY = 'video_processor_token';
const USER_UUID_KEY = 'video_processor_user_uuid';
const USERNAME_KEY = 'video_processor_username';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/api/auth`;

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap((res) => {
        sessionStorage.setItem(TOKEN_KEY, res.token);
        sessionStorage.setItem(USER_UUID_KEY, res.userUuid ?? '');
        sessionStorage.setItem(USERNAME_KEY, res.username);
      }),
    );
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_UUID_KEY);
    sessionStorage.removeItem(USERNAME_KEY);
  }

  getToken(): string | null {
    return sessionStorage.getItem(TOKEN_KEY);
  }

  getUserUuid(): string | null {
    return sessionStorage.getItem(USER_UUID_KEY);
  }

  getUsername(): string | null {
    return sessionStorage.getItem(USERNAME_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
