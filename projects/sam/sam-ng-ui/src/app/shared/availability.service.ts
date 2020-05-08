import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AvailabilityService {

  constructor(
    private http: HttpClient
  ) { }

  getStatus() {
    return this.http.get('/api/status');
  }
}
