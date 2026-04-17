import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { environment } from '../../../environments/environment';
import { IncidentDetail, IncidentEvent } from '../../models/incident.models';

@Component({
  selector: 'app-incident-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './incident-detail.component.html',
  styleUrl: './incident-detail.component.scss',
})
export class IncidentDetailComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  incident: IncidentDetail | null = null;
  events: IncidentEvent[] = [];
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error = 'Identificador no válido.';
      this.loading = false;
      return;
    }
    forkJoin({
      incident: this.http.get<IncidentDetail>(`${environment.apiBaseUrl}/api/v1/incidents/${id}`),
      events: this.http.get<IncidentEvent[]>(`${environment.apiBaseUrl}/api/v1/incidents/${id}/events`),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ incident, events }) => {
          this.incident = incident;
          this.events = [...events].sort(
            (a, b) => new Date(a.occurredAt).getTime() - new Date(b.occurredAt).getTime(),
          );
          this.loading = false;
        },
        error: () => {
          this.error = 'No se pudo cargar el incidente.';
          this.loading = false;
        },
      });
  }

  jsonPreview(value: unknown): string {
    try {
      return JSON.stringify(value, null, 2);
    } catch {
      return String(value);
    }
  }
}
