import { Routes } from '@angular/router';

import { authGuard } from './core/auth.guard';
import { IncidentDetailComponent } from './pages/incidents/incident-detail.component';
import { IncidentListComponent } from './pages/incidents/incident-list.component';
import { LoginComponent } from './pages/login/login.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', pathMatch: 'full', redirectTo: 'incidents' },
  { path: 'incidents', component: IncidentListComponent, canActivate: [authGuard] },
  { path: 'incidents/:id', component: IncidentDetailComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'incidents' },
];
