import { HttpInterceptorFn } from '@angular/common/http';

import { TOKEN_KEY } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = sessionStorage.getItem(TOKEN_KEY);
  if (!token || req.url.includes('/api/v1/auth/login')) {
    return next(req);
  }
  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
