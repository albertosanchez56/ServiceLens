import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';

import { environment } from '../../../environments/environment';
import { IncidentSummary, PageResponse } from '../../models/incident.models';

@Component({
  selector: 'app-incident-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './incident-list.component.html',
  styleUrl: './incident-list.component.scss',
})
export class IncidentListComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);

  items: IncidentSummary[] = [];
  loading = true;
  error: string | null = null;
  totalElements = 0;

  ngOnInit(): void {
    this.http
      .get<PageResponse<IncidentSummary>>(`${environment.apiBaseUrl}/api/v1/incidents?page=0&size=50`)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (p) => {
          this.items = p.items;
          this.totalElements = p.totalElements;
          this.loading = false;
        },
        error: () => {
          this.error = 'No se pudieron cargar los incidentes.';
          this.loading = false;
        },
      });
  }
}
