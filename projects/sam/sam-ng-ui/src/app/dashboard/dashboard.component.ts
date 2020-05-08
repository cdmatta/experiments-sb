import { Component, OnInit } from '@angular/core';
import { interval } from "rxjs";

import { AvailabilityService } from '../shared/availability.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  private monitoredServices = [];

  filteredServices = [];
  searchString = '';

  constructor(
    private availabilityService: AvailabilityService
  ) { }

  ngOnInit() {
    this.fetchCurrentStatus();
    interval(30 * 1000)
        .subscribe(() => {
            this.fetchCurrentStatus();
        });
  }

  private fetchCurrentStatus() {
    this.availabilityService.getStatus().subscribe(d => this.setStatusFromData(d));
  }

  private setStatusFromData(data: any) {
    this.monitoredServices = data;
    this.filteredServices = this.monitoredServices;
  }

  searchFilterChanged(filterText: string) {
    this.searchString = filterText;
    if (!filterText) {
        this.filteredServices = this.monitoredServices;
    }
    filterText = filterText.toLowerCase();
    this.filteredServices = this.monitoredServices.filter(s => {
        let allowed = false;
        if (s.url) allowed = allowed || s.url.toLowerCase().indexOf(filterText) !== -1;
        if (s.mandatoryContent) allowed = allowed || s.mandatoryContent.toLowerCase().indexOf(filterText) !== -1;
        if (s.lastFailureDetail) allowed = allowed || s.lastFailureDetail.toLowerCase().indexOf(filterText) !== -1;
        return allowed;
    });
  }
}
