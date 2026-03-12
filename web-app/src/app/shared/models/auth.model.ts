export interface AuthResponse {
  token: string;
  type: string;
  expiresIn: number;
  username: string;
  userId: string;
  userUuid: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

/** Request para POST /api/auth/register. Backend: username 3–50 chars, email válido, senha mín. 6. */
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

/** Resposta do POST /api/auth/register (201). */
export interface RegisterResponse {
  id: string;
  userUuid: string;
  username: string;
  email: string;
  enabled: boolean;
}

/** Corpo de erro da API (ex.: 400 username/email já existe, validação). */
export interface ApiErrorResponse {
  status: number;
  message: string;
}
