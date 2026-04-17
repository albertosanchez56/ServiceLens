import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../environments/environment';
import { LoginResponse } from '../models/incident.models';

export const TOKEN_KEY = 'sl_access_token';
export const ROLES_KEY = 'sl_roles';
export const USER_KEY = 'sl_username';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiBaseUrl}/api/v1/auth/login`, { username, password })
      .pipe(
        tap((res) => {
          sessionStorage.setItem(TOKEN_KEY, res.accessToken);
          sessionStorage.setItem(ROLES_KEY, JSON.stringify(res.roles));
          sessionStorage.setItem(USER_KEY, username);
        }),
      );
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(ROLES_KEY);
    sessionStorage.removeItem(USER_KEY);
  }

  isLoggedIn(): boolean {
    return !!sessionStorage.getItem(TOKEN_KEY);
  }

  getUsername(): string | null {
    return sessionStorage.getItem(USER_KEY);
  }

  getRoles(): string[] {
    const raw = sessionStorage.getItem(ROLES_KEY);
    if (!raw) {
      return [];
    }
    try {
      return JSON.parse(raw) as string[];
    } catch {
      return [];
    }
  }

  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }
}
