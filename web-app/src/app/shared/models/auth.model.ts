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
