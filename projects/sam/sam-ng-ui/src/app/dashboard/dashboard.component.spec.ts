import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { DashboardComponent } from './dashboard.component';
import { AvailabilityService } from '../shared/availability.service';
import { SortByPipe } from '../shared/pipes';
import { FormsModule } from '@angular/forms';


describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let availabilityService: AvailabilityService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DashboardComponent, SortByPipe ],
      providers: [
        { provide: AvailabilityService, useClass: MockAvailabilityService }
      ],
      imports: [ FormsModule ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

class MockAvailabilityService {
  getStatus() {
    return of([
      {"url":"https://a.com/","mandatoryContent":null,"recentFailureCount":0,"downtimeStart":null,"lastFailureDetail":"","downSince":""},
      {"url":"https://b.com/","mandatoryContent":null,"recentFailureCount":1,"downtimeStart":"2020-05-08T23:15:45.009512","lastFailureDetail":"Failure in http connection/reading page contents","downSince":"2020-05-08 23:15:45"},
      {"url":"https://c.com/","mandatoryContent":"world","recentFailureCount":0,"downtimeStart":null,"lastFailureDetail":"","downSince":""},
      {"url":"https://d.com/","mandatoryContent":"one","recentFailureCount":2,"downtimeStart":"2020-05-08T23:15:45.009512","lastFailureDetail":"Page does not contain required text","downSince":"2020-05-08 23:15:45"}
    ]);
  }
}